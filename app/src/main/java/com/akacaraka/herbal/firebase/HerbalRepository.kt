package com.akacaraka.herbal.firebase

import android.content.Context
import android.net.Uri
import com.akacaraka.herbal.AppContextHolder
import com.akacaraka.herbal.data.HerbalCategory
import com.akacaraka.herbal.data.HerbalItem
import com.akacaraka.herbal.data.HerbalNode
import com.akacaraka.herbal.data.Visitor
import com.akacaraka.herbal.ui.UserSession
import com.akacaraka.herbal.ui.UserSessionType
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class HerbalRepository {
    private val gson = Gson()
    private var cachedItems: List<HerbalItem>? = null
    private var cachedTree: HerbalNode? = null

    data class AccountLoginResult(
        val session: UserSession,
        val isAdmin: Boolean
    )

    private val sessionPreferences by lazy {
        AppContextHolder.context.getSharedPreferences(SESSION_PREFERENCES, Context.MODE_PRIVATE)
    }

    private fun clearGuestSession() {
        sessionPreferences.edit().remove(KEY_GUEST_SESSION).apply()
    }

    private val hasFirebaseConfig: Boolean
        get() = FirebaseApp.getApps(AppContextHolder.context).isNotEmpty()

    suspend fun loadItems(forceRefresh: Boolean = false): List<HerbalItem> = withContext(Dispatchers.IO) {
        if (!forceRefresh && cachedItems != null) return@withContext cachedItems!!
        
        runCatching {
            if (!hasFirebaseConfig) return@runCatching emptyList<HerbalItem>()
            val items = FirebaseFirestore.getInstance().collection("herbal_items").get().await().documents.mapNotNull { document ->
                document.toHerbalItem()
            }
            cachedItems = items
            cachedTree = null // Reset tree if items change
            items
        }.getOrDefault(cachedItems ?: emptyList())
    }

    suspend fun loadTree(forceRefresh: Boolean = false): HerbalNode = withContext(Dispatchers.Default) {
        if (!forceRefresh && cachedTree != null) return@withContext cachedTree!!
        
        val items = loadItems(forceRefresh)
        val tanaman = HerbalNode(children = items.filter { it.category == HerbalCategory.TANAMAN }.map { HerbalNode(it) })
        val minuman = HerbalNode(children = items.filter { it.category == HerbalCategory.MINUMAN }.map { HerbalNode(it) })
        val tree = HerbalNode(children = listOf(tanaman, minuman))
        cachedTree = tree
        tree
    }

    suspend fun trackVisitor() {
        val current = loadVisitors().toMutableList()
        val id = "ID-${(1000..9999).random()}"
        current.add(0, Visitor(id = id, timestamp = System.currentTimeMillis()))
        saveVisitorsLocal(current)
    }

    fun loadVisitors(): List<Visitor> {
        val json = sessionPreferences.getString(KEY_LOCAL_VISITORS, "[]")
        return try {
            val type = object : TypeToken<List<Visitor>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveVisitorsLocal(visitors: List<Visitor>) {
        val json = gson.toJson(visitors)
        sessionPreferences.edit().putString(KEY_LOCAL_VISITORS, json).apply()
    }

    suspend fun loginAdmin(email: String, password: String) {
        if (!hasFirebaseConfig) error("Firebase belum dikonfigurasi. Tambahkan app/google-services.json untuk login admin.")
        val auth = FirebaseAuth.getInstance()
        val user = auth.signInWithEmailAndPassword(email, password).await().user
            ?: error("Login admin gagal.")
        if (!isAdminUser(user.uid)) {
            auth.signOut()
            sessionPreferences.edit().remove(KEY_ADMIN_AUTH).apply()
            error("Akun ini tidak memiliki akses admin.")
        }
        clearGuestSession()
        sessionPreferences.edit().putBoolean(KEY_ADMIN_AUTH, true).apply()
    }

    fun logoutAdmin() {
        runCatching {
            sessionPreferences.edit().remove(KEY_ADMIN_AUTH).apply()
            if (hasFirebaseConfig) FirebaseAuth.getInstance().signOut()
        }
    }

    fun isAdminLoggedIn(): Boolean = sessionPreferences.getBoolean(KEY_ADMIN_AUTH, false)

    suspend fun registerUser(email: String, password: String): UserSession {
        if (!hasFirebaseConfig) error("Firebase belum dikonfigurasi. Kamu tetap bisa melanjutkan sebagai tamu.")
        val user = FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).await().user
            ?: error("Akun tidak berhasil dibuat.")
        clearGuestSession()
        sessionPreferences.edit().remove(KEY_ADMIN_AUTH).apply()
        return UserSession(UserSessionType.ACCOUNT, user.email.orEmpty(), user.uid)
    }

    suspend fun loginUser(email: String, password: String): UserSession {
        return loginAccount(email, password).session
    }

    suspend fun loginAccount(email: String, password: String): AccountLoginResult {
        if (!hasFirebaseConfig) error("Firebase belum dikonfigurasi. Kamu tetap bisa melanjutkan sebagai tamu.")
        val user = FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await().user
            ?: error("Akun tidak ditemukan.")
        clearGuestSession()
        val isAdmin = isAdminUser(user.uid)
        sessionPreferences.edit().putBoolean(KEY_ADMIN_AUTH, isAdmin).apply()
        return AccountLoginResult(UserSession(UserSessionType.ACCOUNT, user.email.orEmpty(), user.uid), isAdmin)
    }

    fun rememberGuestSession() {
        sessionPreferences.edit().putBoolean(KEY_GUEST_SESSION, true).apply()
    }

    fun hasGuestSession(): Boolean = sessionPreferences.getBoolean(KEY_GUEST_SESSION, false)

    fun currentUserSession(): UserSession = runCatching {
        if (!hasFirebaseConfig) return@runCatching UserSession()
        val user = FirebaseAuth.getInstance().currentUser ?: return@runCatching UserSession()
        UserSession(UserSessionType.ACCOUNT, user.email.orEmpty(), user.uid)
    }.getOrDefault(UserSession())

    fun logoutUser() {
        runCatching {
            sessionPreferences.edit().remove(KEY_ADMIN_AUTH).apply()
            clearGuestSession()
            if (hasFirebaseConfig) FirebaseAuth.getInstance().signOut()
        }
    }

    private suspend fun isAdminUser(userId: String): Boolean {
        return FirebaseFirestore.getInstance().collection("admins").document(userId).get().await().exists()
    }

    suspend fun saveItem(item: HerbalItem) {
        if (!hasFirebaseConfig) error("Firebase belum dikonfigurasi. Tambahkan app/google-services.json untuk menyimpan data.")
        val collection = FirebaseFirestore.getInstance().collection("herbal_items")
        val id = item.id.ifBlank { collection.document().id }
        collection.document(id).set(item.copy(id = id).toFirestoreMap()).await()
        cachedItems = null // Invalidate cache
    }

    suspend fun deleteItem(id: String) {
        if (!hasFirebaseConfig) error("Firebase belum dikonfigurasi. Tambahkan app/google-services.json untuk menghapus data.")
        FirebaseFirestore.getInstance().collection("herbal_items").document(id).delete().await()
        cachedItems = null // Invalidate cache
    }

    suspend fun uploadImage(itemId: String, uri: Uri): String {
        return uploadImageToTencentCos(itemId, uri)
    }

    private suspend fun uploadImageToTencentCos(itemId: String, uri: Uri): String = withContext(Dispatchers.IO) {
        val contentResolver = AppContextHolder.context.contentResolver
        val contentType = contentResolver.getType(uri) ?: "image/jpeg"
        val objectKey = "herbal_images/${itemId}.${contentType.imageExtension()}"
        val uploadUrl = URL("https://$TENCENT_COS_BUCKET_DOMAIN/$objectKey")
        val connection = (uploadUrl.openConnection() as HttpURLConnection).apply {
            requestMethod = "PUT"
            doOutput = true
            connectTimeout = 20_000
            readTimeout = 30_000
            setRequestProperty("Content-Type", contentType)
        }
        try {
            contentResolver.openInputStream(uri)?.use { input ->
                connection.outputStream.use { output -> input.copyTo(output) }
            } ?: error("Foto tidak bisa dibaca dari galeri.")
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) error("Upload Tencent COS gagal ($responseCode).")
            uploadUrl.toString()
        } finally {
            connection.disconnect()
        }
    }
}

private const val SESSION_PREFERENCES = "herbal_session"
private const val KEY_GUEST_SESSION = "guest_session_active"
private const val KEY_ADMIN_AUTH = "admin_auth_active"
private const val KEY_LOCAL_VISITORS = "local_visitors_data"
private const val TENCENT_COS_BUCKET_DOMAIN = "herbal-devin-1455601581.cos.ap-jakarta.myqcloud.com"

private fun String.imageExtension(): String = when (substringBefore(';').lowercase()) {
    "image/png" -> "png"
    "image/webp" -> "webp"
    "image/gif" -> "gif"
    else -> "jpg"
}

private fun HerbalItem.toFirestoreMap(): Map<String, Any> = mapOf(
    "id" to id,
    "category" to category.name,
    "localName" to localName,
    "scientificName" to scientificName,
    "description" to description,
    "benefits" to benefits,
    "dosage" to dosage,
    "symptoms" to symptoms,
    "imageUrl" to imageUrl
)

private fun com.google.firebase.firestore.DocumentSnapshot.toHerbalItem(): HerbalItem? {
    val categoryName = getString("category") ?: return null
    val category = runCatching { HerbalCategory.valueOf(categoryName) }.getOrNull() ?: return null
    val rawSymptoms = get("symptoms") as? List<*>
    return HerbalItem(
        id = getString("id") ?: id,
        category = category,
        localName = getString("localName").orEmpty(),
        scientificName = getString("scientificName").orEmpty(),
        description = getString("description").orEmpty(),
        benefits = getString("benefits").orEmpty(),
        dosage = getString("dosage").orEmpty(),
        symptoms = rawSymptoms?.filterIsInstance<String>().orEmpty(),
        imageUrl = getString("imageUrl").orEmpty()
    )
}
