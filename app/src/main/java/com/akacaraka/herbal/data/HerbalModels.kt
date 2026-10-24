package com.akacaraka.herbal.data

enum class HerbalCategory(val label: String) {
    TANAMAN("Tanaman Herbal"),
    MINUMAN("Minuman Herbal")
}

data class HerbalItem(
    val id: String = "",
    val category: HerbalCategory = HerbalCategory.TANAMAN,
    val localName: String = "",
    val scientificName: String = "",
    val description: String = "",
    val benefits: String = "",
    val dosage: String = "",
    val symptoms: List<String> = emptyList(),
    val imageUrl: String = ""
) {
    fun searchableText(): String = buildString {
        append(localName).append(' ')
        append(scientificName).append(' ')
        append(description).append(' ')
        append(benefits).append(' ')
        append(dosage).append(' ')
        append(symptoms.joinToString(" "))
    }.lowercase()
}

data class HerbalNode(
    val item: HerbalItem? = null,
    val children: List<HerbalNode> = emptyList()
)

data class SearchResult(
    val items: List<HerbalItem> = emptyList(),
    val elapsedMs: Long = 0L,
    val visitedNodes: Int = 0
)

data class Visitor(
    val id: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
