package com.akacaraka.herbal.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akacaraka.herbal.data.HerbalCategory
import com.akacaraka.herbal.data.HerbalItem
import com.akacaraka.herbal.data.HerbalSearchEngine
import com.akacaraka.herbal.data.SearchResult
import com.akacaraka.herbal.data.Visitor
import com.akacaraka.herbal.firebase.HerbalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class HerbalUiState(
    val page: AppPage = AppPage.SPLASH,
    val previousPage: AppPage = AppPage.HOME,
    val pageHistory: List<AppPage> = emptyList(),
    val session: UserSession = UserSession(),
    val authName: String = "",
    val authEmail: String = "",
    val authPassword: String = "",
    val authError: String = "",
    val favoriteIds: Set<String> = emptySet(),
    val allItems: List<HerbalItem> = emptyList(),
    val keyword: String = "",
    val category: HerbalCategory = HerbalCategory.TANAMAN,
    val searchResult: SearchResult = SearchResult(),
    val selectedItem: HerbalItem? = null,
    val adminEmail: String = "",
    val adminPassword: String = "",
    val isAdminLoggedIn: Boolean = false,
    val adminItems: List<HerbalItem> = emptyList(),
    val visitors: List<Visitor> = emptyList(),
    val selectedReportType: ReportType? = null,
    val editingItem: HerbalItem = HerbalItem(),
    val selectedImageUri: Uri? = null,
    val isUploadingImage: Boolean = false,
    val imageUploadError: String = "",
    val imageUploadSuccess: Boolean = false,
    val message: String = "",
    val isLoading: Boolean = false
)

private const val MaxPageHistory = 12
private val MainAppPages = setOf(AppPage.HOME, AppPage.EXPLORE, AppPage.FAVORITES, AppPage.PROFILE)
private val AuthPages = setOf(AppPage.LOGIN, AppPage.REGISTER)

private fun AppPage.canBeBackDestination(): Boolean = this in MainAppPages || this == AppPage.ADMIN

private fun AppPage.asSafeBackDestination(): AppPage = if (canBeBackDestination()) this else AppPage.HOME

private fun List<AppPage>.withBackDestination(page: AppPage): List<AppPage> {
    if (!page.canBeBackDestination()) return this
    return (this + page).takeLast(MaxPageHistory)
}

private fun List<AppPage>.popBackDestination(currentPage: AppPage): Pair<AppPage?, List<AppPage>> {
    var remaining = this
    while (remaining.isNotEmpty()) {
        val candidate = remaining.last()
        remaining = remaining.dropLast(1)
        if (candidate != currentPage && candidate.canBeBackDestination()) return candidate to remaining
    }
    return null to emptyList()
}

class HerbalViewModel : ViewModel() {
    private val repository = HerbalRepository()
    private val searchEngine = HerbalSearchEngine()
    private val _state = MutableStateFlow(HerbalUiState())
    val state: StateFlow<HerbalUiState> = _state.asStateFlow()

    private data class LoadedHerbalData(
        val items: List<HerbalItem>,
        val searchResult: SearchResult,
        val isAdminLoggedIn: Boolean,
        val visitors: List<Visitor> = emptyList()
    )

    private data class ResolvedLogin(
        val session: UserSession,
        val isAdmin: Boolean,
        val data: LoadedHerbalData
    )

    init {
        restoreSessionAndData()
    }

    fun navigateTo(page: AppPage) {
        _state.update { state ->
            if (state.page == page) {
                state
            } else {
                state.copy(
                    previousPage = state.page.asSafeBackDestination(),
                    pageHistory = if (page == AppPage.ONBOARDING) emptyList() else state.pageHistory.withBackDestination(state.page),
                    page = page,
                    selectedItem = if (page == AppPage.DETAIL) state.selectedItem else null,
                    authError = if (page in AuthPages) "" else state.authError
                )
            }
        }
    }

    fun canNavigateBack(snapshot: HerbalUiState = _state.value): Boolean = when (snapshot.page) {
        AppPage.LOGIN, AppPage.REGISTER, AppPage.DETAIL, AppPage.ADMIN -> true
        AppPage.HOME -> snapshot.pageHistory.any { it.canBeBackDestination() && it != AppPage.HOME }
        AppPage.EXPLORE, AppPage.FAVORITES, AppPage.PROFILE -> true
        else -> false
    }

    fun navigateBack() {
        _state.update { state ->
            when (state.page) {
                AppPage.LOGIN, AppPage.REGISTER -> state.copy(
                    page = AppPage.ONBOARDING,
                    previousPage = AppPage.HOME,
                    pageHistory = emptyList(),
                    authError = ""
                )

                AppPage.DETAIL -> state.backToLastDestination(state.previousPage.asSafeBackDestination(), clearSelectedItem = true)
                AppPage.REPORT_DETAIL -> state.copy(page = AppPage.REPORT)
                AppPage.ADMIN, AppPage.REPORT -> state.backToLastDestination(AppPage.HOME, clearSelectedItem = true)
                AppPage.HOME -> state.backToLastDestination(AppPage.HOME, clearSelectedItem = true)
                AppPage.EXPLORE, AppPage.FAVORITES, AppPage.PROFILE -> state.backToLastDestination(AppPage.HOME, clearSelectedItem = true)
                else -> state
            }
        }
    }

    fun completeOnboarding() {
        _state.update { it.copy(page = AppPage.LOGIN, previousPage = AppPage.ONBOARDING, pageHistory = emptyList()) }
    }

    fun continueAsGuest() {
        repository.logoutAdmin()
        repository.rememberGuestSession()
        viewModelScope.launch { repository.trackVisitor() }
        _state.update {
            it.copy(
                page = AppPage.HOME,
                previousPage = AppPage.HOME,
                pageHistory = emptyList(),
                session = UserSession(UserSessionType.GUEST),
                isAdminLoggedIn = false,
                authError = "",
                message = "Kamu sedang menggunakan mode tamu."
            )
        }
    }

    fun updateAuthName(name: String) {
        _state.update { it.copy(authName = name) }
    }

    fun updateAuthEmail(email: String) {
        _state.update { it.copy(authEmail = email) }
    }

    fun updateAuthPassword(password: String) {
        _state.update { it.copy(authPassword = password) }
    }

    fun loginUser() {
        val email = _state.value.authEmail.trim()
        val password = _state.value.authPassword
        if (email.isBlank() || password.length < 6) {
            _state.update { it.copy(authError = "Masukkan email dan password minimal 6 karakter.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, authError = "") }
            runCatching {
                val login = repository.loginAccount(email, password)
                val data = loadHerbalData(_state.value, forceRefresh = true)
                ResolvedLogin(login.session, login.isAdmin, data)
            }
                .onSuccess { login ->
                    _state.update {
                        it.withLoadedData(login.data).copy(
                            page = AppPage.HOME,
                            previousPage = AppPage.HOME,
                            pageHistory = if (login.isAdmin) listOf(AppPage.HOME) else emptyList(),
                            session = login.session,
                            isAdminLoggedIn = login.isAdmin,
                            isLoading = false,
                            authPassword = "",
                            authError = "",
                            adminEmail = "",
                            adminPassword = "",
                            message = if (login.isAdmin) "" else "Login berhasil."
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, authError = error.message ?: "Login gagal.") }
                }
        }
    }

    fun registerUser() {
        val email = _state.value.authEmail.trim()
        val password = _state.value.authPassword
        if (_state.value.authName.isBlank() || email.isBlank() || password.length < 6) {
            _state.update { it.copy(authError = "Lengkapi nama, email, dan password minimal 6 karakter.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, authError = "") }
            runCatching { repository.registerUser(email, password) }
                .onSuccess { session ->
                    _state.update {
                        it.copy(
                            page = AppPage.HOME,
                            previousPage = AppPage.HOME,
                            pageHistory = emptyList(),
                            session = session,
                            isAdminLoggedIn = false,
                            isLoading = false,
                            authPassword = ""
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, authError = error.message ?: "Pendaftaran gagal.") }
                }
        }
    }

    fun logoutUser() {
        repository.logoutUser()
        _state.update {
            it.copy(
                page = AppPage.ONBOARDING,
                previousPage = AppPage.HOME,
                pageHistory = emptyList(),
                session = UserSession(),
                authPassword = "",
                authError = "",
                favoriteIds = emptySet(),
                isAdminLoggedIn = false
            )
        }
    }

    fun toggleFavorite(itemId: String) {
        _state.update {
            val favorites = it.favoriteIds.toMutableSet()
            if (!favorites.add(itemId)) favorites.remove(itemId)
            it.copy(favoriteIds = favorites)
        }
    }

    fun openDetail(item: HerbalItem) {
        _state.update {
            it.copy(
                selectedItem = item,
                previousPage = it.page.asSafeBackDestination(),
                pageHistory = it.pageHistory.withBackDestination(it.page),
                page = AppPage.DETAIL
            )
        }
    }

    fun closeDetail() {
        _state.update { it.backToLastDestination(it.previousPage.asSafeBackDestination(), clearSelectedItem = true) }
    }

    fun updateKeyword(keyword: String) {
        _state.update { it.copy(keyword = keyword) }
    }

    fun updateCategory(category: HerbalCategory) {
        _state.update { it.copy(category = category) }
        executeSearch()
    }

    fun executeSearch() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val keyword = _state.value.keyword
            val category = _state.value.category
            
            val result = withContext(Dispatchers.Default) {
                val tree = repository.loadTree()
                searchEngine.search(tree, keyword, category)
            }
            
            _state.update { it.copy(searchResult = result, isLoading = false, message = "${result.items.size} herbal ditemukan") }
        }
    }

    fun selectItem(item: HerbalItem?) {
        if (item == null) closeDetail() else openDetail(item)
    }

    fun updateAdminEmail(email: String) {
        _state.update { it.copy(adminEmail = email) }
    }

    fun updateAdminPassword(password: String) {
        _state.update { it.copy(adminPassword = password) }
    }

    fun loginAdmin() {
        val email = _state.value.adminEmail.trim()
        val password = _state.value.adminPassword
        if (email.isBlank() || password.length < 6) {
            _state.update { it.copy(message = "Masukkan email dan password admin minimal 6 karakter.") }
            return
        }
        viewModelScope.launch {
            runCatching {
                _state.update { it.copy(isLoading = true, message = "") }
                repository.loginAdmin(email, password)
                val data = loadHerbalData(_state.value, forceRefresh = true)
                ResolvedLogin(repository.currentUserSession(), true, data)
            }.onSuccess { login ->
                _state.update {
                    it.withLoadedData(login.data).copy(
                        page = AppPage.HOME,
                        previousPage = AppPage.HOME,
                        pageHistory = listOf(AppPage.HOME),
                        session = login.session,
                        isAdminLoggedIn = true,
                        adminPassword = "",
                        message = "",
                        isLoading = false
                    )
                }
            }.onFailure { error ->
                _state.update { it.copy(message = error.message ?: "Login gagal", isLoading = false) }
            }
        }
    }

    fun logoutAdmin() {
        repository.logoutAdmin()
        _state.update {
            it.copy(
                page = AppPage.LOGIN,
                previousPage = AppPage.ONBOARDING,
                pageHistory = emptyList(),
                session = UserSession(),
                isAdminLoggedIn = false,
                authPassword = "",
                adminPassword = "",
                message = ""
            )
        }
    }

    fun editItem(item: HerbalItem) {
        _state.update {
            it.copy(
                editingItem = item,
                selectedImageUri = null,
                imageUploadError = "",
                imageUploadSuccess = false,
                message = it.message.clearAdminLoginMessage()
            )
        }
    }

    fun updateEditingItem(item: HerbalItem) {
        _state.update { it.copy(editingItem = item, message = it.message.clearAdminLoginMessage()) }
    }

    fun openReport(type: ReportType) {
        _state.update { it.copy(selectedReportType = type, page = AppPage.REPORT_DETAIL) }
    }

    fun selectImage(uri: Uri?) {
        if (uri == null) return
        _state.update {
            it.copy(
                selectedImageUri = uri,
                imageUploadError = "",
                imageUploadSuccess = false,
                message = it.message.clearAdminLoginMessage()
            )
        }
    }

    fun clearSelectedImage() {
        _state.update {
            it.copy(
                selectedImageUri = null,
                imageUploadError = "",
                imageUploadSuccess = false
            )
        }
    }

    fun uploadSelectedImage() {
        val uri = _state.value.selectedImageUri
        if (uri == null) {
            _state.update { it.copy(imageUploadError = "Pilih foto herbal terlebih dahulu.") }
            return
        }

        viewModelScope.launch {
            val currentItem = _state.value.editingItem
            val itemId = currentItem.id.ifBlank { UUID.randomUUID().toString() }
            _state.update {
                it.copy(
                    editingItem = currentItem.copy(id = itemId),
                    isUploadingImage = true,
                    imageUploadError = "",
                    imageUploadSuccess = false,
                    message = "Mengunggah foto..."
                )
            }
            runCatching {
                repository.uploadImage(itemId, uri)
            }.onSuccess { downloadUrl ->
                _state.update {
                    it.copy(
                        editingItem = it.editingItem.copy(imageUrl = downloadUrl),
                        selectedImageUri = null,
                        isUploadingImage = false,
                        imageUploadSuccess = true,
                        message = "Foto berhasil diunggah."
                    )
                }
            }.onFailure { error ->
                val uploadError = error.message ?: "Foto gagal diunggah. Silakan coba lagi."
                _state.update {
                    it.copy(
                        isUploadingImage = false,
                        imageUploadError = uploadError,
                        imageUploadSuccess = false,
                        message = uploadError
                    )
                }
            }
        }
    }

    fun saveEditingItem() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            var itemToSave = _state.value.editingItem
            val selectedUri = _state.value.selectedImageUri
            if (selectedUri != null) {
                val itemId = itemToSave.id.ifBlank { UUID.randomUUID().toString() }
                _state.update {
                    it.copy(
                        isUploadingImage = true,
                        imageUploadError = "",
                        imageUploadSuccess = false,
                        message = "Mengunggah foto..."
                    )
                }
                val uploadResult = runCatching { repository.uploadImage(itemId, selectedUri) }
                if (uploadResult.isFailure) {
                    val uploadError = uploadResult.exceptionOrNull()?.message ?: "Foto gagal diunggah. Silakan coba lagi."
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isUploadingImage = false,
                            imageUploadError = uploadError,
                            imageUploadSuccess = false,
                            message = uploadError
                        )
                    }
                    return@launch
                }
                itemToSave = itemToSave.copy(id = itemId, imageUrl = uploadResult.getOrThrow())
                _state.update {
                    it.copy(
                        editingItem = itemToSave,
                        selectedImageUri = null,
                        isUploadingImage = false,
                        imageUploadSuccess = true,
                        message = "Foto terunggah. Menyimpan data herbal..."
                    )
                }
            }
            runCatching {
                repository.saveItem(itemToSave)
                refreshData(forceRefresh = true)
                _state.update {
                    it.copy(
                        editingItem = HerbalItem(),
                        selectedImageUri = null,
                        imageUploadError = "",
                        imageUploadSuccess = false,
                        message = "Data herbal tersimpan"
                    )
                }
            }.onFailure { error ->
                _state.update { it.copy(message = error.message ?: "Gagal menyimpan data") }
            }
            _state.update { it.copy(isLoading = false, isUploadingImage = false) }
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            runCatching {
                repository.deleteItem(id)
                refreshData(forceRefresh = true)
                _state.update { it.copy(message = "Data herbal dihapus") }
            }.onFailure { error ->
                _state.update { it.copy(message = error.message ?: "Gagal menghapus data") }
            }
        }
    }

    private fun restoreSessionAndData() {
        viewModelScope.launch {
            _state.update { it.copy(page = AppPage.SPLASH, isLoading = true, authError = "", message = "") }
            val accountSession = repository.currentUserSession()
            val restoredSession = when {
                accountSession.type == UserSessionType.ACCOUNT -> accountSession
                repository.hasGuestSession() -> UserSession(UserSessionType.GUEST)
                else -> UserSession()
            }
            val data = loadHerbalData(_state.value)
            _state.update {
                val targetPage = when {
                    restoredSession.type == UserSessionType.SIGNED_OUT -> AppPage.ONBOARDING
                    else -> AppPage.HOME
                }
                it.withLoadedData(data).copy(
                    page = targetPage,
                    previousPage = AppPage.HOME,
                    pageHistory = if (data.isAdminLoggedIn) listOf(AppPage.HOME) else emptyList(),
                    session = restoredSession,
                    isLoading = false
                )
            }
        }
    }

    private suspend fun refreshData(forceRefresh: Boolean = false) {
        val data = loadHerbalData(_state.value, forceRefresh)
        _state.update { it.withLoadedData(data) }
    }

    private suspend fun loadHerbalData(snapshot: HerbalUiState, forceRefresh: Boolean = false): LoadedHerbalData = withContext(Dispatchers.IO) {
        val items = repository.loadItems(forceRefresh)
        val visitors = if (repository.isAdminLoggedIn()) repository.loadVisitors() else emptyList()
        val tree = repository.loadTree(forceRefresh)
        
        val searchResult = withContext(Dispatchers.Default) {
            searchEngine.search(
                root = tree,
                keyword = snapshot.keyword,
                category = snapshot.category
            )
        }
        
        LoadedHerbalData(
            items = items,
            isAdminLoggedIn = repository.isAdminLoggedIn(),
            visitors = visitors,
            searchResult = searchResult
        )
    }

    private fun HerbalUiState.withLoadedData(data: LoadedHerbalData): HerbalUiState = copy(
        allItems = data.items,
        adminItems = data.items,
        visitors = data.visitors,
        isAdminLoggedIn = data.isAdminLoggedIn,
        searchResult = data.searchResult
    )

    private fun String.clearAdminLoginMessage(): String = if (startsWith("Login admin berhasil")) "" else this

    private fun HerbalUiState.backToLastDestination(fallback: AppPage, clearSelectedItem: Boolean): HerbalUiState {
        val (target, remainingHistory) = pageHistory.popBackDestination(page)
        val nextPage = target ?: fallback
        return copy(
            page = nextPage,
            previousPage = remainingHistory.lastOrNull()?.asSafeBackDestination() ?: AppPage.HOME,
            pageHistory = remainingHistory,
            selectedItem = if (clearSelectedItem) null else selectedItem,
            authError = if (nextPage in AuthPages) authError else ""
        )
    }
}
