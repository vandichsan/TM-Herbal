package com.akacaraka.herbal.data

class HerbalSearchEngine {
    fun search(root: HerbalNode, keyword: String, category: HerbalCategory): SearchResult {
        val normalizedKeyword = keyword.trim().lowercase()
        val startedAt = System.nanoTime()
        val matches = mutableListOf<HerbalItem>()
        var visitedNodes = 0

        fun dfs(node: HerbalNode) {
            visitedNodes += 1
            val item = node.item
            if (item != null && item.category == category) {
                val matchesKeyword = normalizedKeyword.isBlank() || item.searchableText().contains(normalizedKeyword)
                if (matchesKeyword) {
                    matches += item
                }
            }
            node.children.forEach(::dfs)
        }

        dfs(root)
        val elapsedMs = (System.nanoTime() - startedAt) / 1_000_000
        return SearchResult(matches, elapsedMs, visitedNodes)
    }
}
