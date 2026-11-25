package com.example.data.search

import com.example.data.model.MallPlaceCategory
import com.example.data.model.OfferEntity
import com.example.data.model.StoreEntity

enum class SearchPathTab(val labelAr: String) {
    ALL("الكل"),
    BRANDS("البراندات والمحلات"),
    PRODUCTS("المنتجات والأقسام")
}

data class MallSearchHashtag(
    val tag: String,
    val queryKeywords: List<String>,
    val targetTab: SearchPathTab = SearchPathTab.ALL
)

data class SearchSuggestion(
    val store: StoreEntity,
    val matchReasonAr: String,
    val activeOfferCount: Int
)

object MallSearchEngine {

    val popularHashtags: List<MallSearchHashtag> = listOf(
        MallSearchHashtag(
            tag = "#هايبر_ماركت",
            queryKeywords = listOf("هايبر", "سوبر ماركت", "غذائية", "ألبان", "مخبوزات"),
            targetTab = SearchPathTab.PRODUCTS
        ),
        MallSearchHashtag(
            tag = "#لحوم_ماي_ميت",
            queryKeywords = listOf("ماي ميت", "لحوم"),
            targetTab = SearchPathTab.PRODUCTS
        ),
        MallSearchHashtag(
            tag = "#موبايلات_وإلكترونيات",
            queryKeywords = listOf("سامسونج", "تكنو", "هواتف", "إلكترونيات", "شاشات", "صيانة"),
            targetTab = SearchPathTab.PRODUCTS
        ),
        MallSearchHashtag(
            tag = "#مطاعم_وكافيهات",
            queryKeywords = listOf("كنتاكي", "بيتزا", "كافيه", "مطعم", "مشروبات"),
            targetTab = SearchPathTab.ALL
        ),
        MallSearchHashtag(
            tag = "#أثاث_ومفروشات",
            queryKeywords = listOf("أثاث", "ركنات", "انتريهات", "مفروشات", "سجاد"),
            targetTab = SearchPathTab.PRODUCTS
        ),
        MallSearchHashtag(
            tag = "#ألعاب_وملاهي",
            queryKeywords = listOf("ألعاب", "ملاهي", "بينجو", "توتو", "أطفال"),
            targetTab = SearchPathTab.ALL
        ),
        MallSearchHashtag(
            tag = "#عطور_وهدايا",
            queryKeywords = listOf("عطور", "هدايا", "عود"),
            targetTab = SearchPathTab.PRODUCTS
        ),
        MallSearchHashtag(
            tag = "#بنوك_ATM",
            queryKeywords = listOf("بنك", "صراف", "ATM"),
            targetTab = SearchPathTab.PRODUCTS
        )
    )

    fun resolveHashtag(query: String): MallSearchHashtag? {
        val trimmed = query.trim()
        if (!trimmed.startsWith("#")) return null
        return popularHashtags.firstOrNull { it.tag.equals(trimmed, ignoreCase = true) }
    }

    fun filterPlaces(
        places: List<StoreEntity>,
        offers: List<OfferEntity>,
        rawQuery: String,
        searchTab: SearchPathTab,
        selectedCategory: MallPlaceCategory?,
        selectedFloor: Int
    ): List<StoreEntity> {
        val offersByStore = offers.groupBy { it.storeId }
        val q = rawQuery.trim()
        val hashtag = resolveHashtag(q)
        val normalizedQ = q.removePrefix("#").replace("_", " ").trim()

        return places.filter { place ->
            val matchesFloor = selectedFloor == -1 || place.floor == selectedFloor
            val matchesCategory = selectedCategory == null || place.category == selectedCategory
            val matchesTabScope = when (searchTab) {
                SearchPathTab.ALL -> true
                SearchPathTab.BRANDS -> !place.isServiceFacility
                SearchPathTab.PRODUCTS -> true
            }

            if (!matchesFloor || !matchesCategory || !matchesTabScope) return@filter false
            if (normalizedQ.isBlank()) return@filter true

            val storeOffers = offersByStore[place.id].orEmpty()
            if (hashtag != null) {
                return@filter hashtag.queryKeywords.any { kw ->
                    matchesStoreText(place, storeOffers, kw, searchTab)
                }
            }

            matchesStoreText(place, storeOffers, normalizedQ, searchTab)
        }
    }

    fun buildSuggestions(
        places: List<StoreEntity>,
        offers: List<OfferEntity>,
        rawQuery: String,
        searchTab: SearchPathTab,
        limit: Int = 6
    ): List<SearchSuggestion> {
        val q = rawQuery.trim()
        if (q.isBlank()) return emptyList()

        val offersByStore = offers.groupBy { it.storeId }
        val filtered = filterPlaces(
            places = places,
            offers = offers,
            rawQuery = q,
            searchTab = searchTab,
            selectedCategory = null,
            selectedFloor = -1
        )

        val hashtag = resolveHashtag(q)
        val normalizedQ = q.removePrefix("#").replace("_", " ").trim()

        return filtered
            .sortedWith(
                compareByDescending<StoreEntity> {
                    it.nameAr.startsWith(normalizedQ, ignoreCase = true) ||
                        it.nameEn.startsWith(normalizedQ, ignoreCase = true)
                }.thenByDescending { it.isFeatured }
                    .thenBy { it.floor }
            )
            .take(limit)
            .map { store ->
                val storeOffers = offersByStore[store.id].orEmpty()
                val reason = when {
                    hashtag != null -> "${hashtag.tag} • ${store.floorLabelAr}"
                    searchTab == SearchPathTab.PRODUCTS && storeOffers.isNotEmpty() ->
                        "${storeOffers.first().discountBadge} • ${store.category.labelAr}"
                    else -> "${store.category.labelAr} • ${store.floorLabelAr}"
                }
                SearchSuggestion(
                    store = store,
                    matchReasonAr = reason,
                    activeOfferCount = storeOffers.size
                )
            }
    }

    private fun matchesStoreText(
        place: StoreEntity,
        storeOffers: List<OfferEntity>,
        query: String,
        searchTab: SearchPathTab
    ): Boolean {
        val brandMatch = place.nameAr.contains(query, ignoreCase = true) ||
            place.nameEn.contains(query, ignoreCase = true) ||
            place.zoneAr.contains(query, ignoreCase = true)

        val productMatch = place.descriptionAr.contains(query, ignoreCase = true) ||
            place.category.labelAr.contains(query, ignoreCase = true) ||
            storeOffers.any {
                it.titleAr.contains(query, ignoreCase = true) ||
                    it.descriptionAr.contains(query, ignoreCase = true) ||
                    it.discountBadge.contains(query, ignoreCase = true)
            }

        return when (searchTab) {
            SearchPathTab.ALL -> brandMatch || productMatch
            SearchPathTab.BRANDS -> brandMatch || place.category.labelAr.contains(query, ignoreCase = true)
            SearchPathTab.PRODUCTS -> productMatch || place.nameAr.contains(query, ignoreCase = true)
        }
    }
}
