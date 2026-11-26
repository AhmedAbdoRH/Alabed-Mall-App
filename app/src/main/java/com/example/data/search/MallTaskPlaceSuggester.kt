package com.example.data.search

import com.example.data.model.MallPlaceCategory
import com.example.data.model.StoreEntity

data class TaskPlaceSuggestion(
    val store: StoreEntity,
    val reasonAr: String
)

/**
 * Suggests real El Abed Mall stores/brands/facilities matching what the visitor
 * is typing in the Tasks & Purchases screen.
 */
object MallTaskPlaceSuggester {

    private val keywordCategoryHints: List<Pair<List<String>, MallPlaceCategory>> = listOf(
        listOf(
            "لحم", "لحوم", "ماي ميت", "فراخ", "سمك", "لبن", "جبن", "ألبان",
            "عيش", "خبز", "مخبوزات", "زيت", "سكر", "أرز", "مكرونة", "خضار",
            "فاكهة", "بقالة", "سوبر", "هايبر", "منظف", "شامبو", "صابون", "مياه", "عصير"
        ) to MallPlaceCategory.HYPERMARKET,
        listOf(
            "موبايل", "هاتف", "تليفون", "تلفون", "سامسونج", "تكنو", "شاحن",
            "سماعة", "شاشة", "تلفزيون", "صيانة", "كفر", "لابتوب", "خط", "شريحة", "انترنت", "فودافون"
        ) to MallPlaceCategory.ELECTRONICS,
        listOf(
            "أكل", "غدا", "عشا", "فطار", "وجبة", "كنتاكي", "بيتزا", "دجاج",
            "كافيه", "قهوة", "شاي", "لاتيه", "حلا", "حلويات", "مطعم"
        ) to MallPlaceCategory.DINING,
        listOf(
            "أثاث", "ركنة", "انتريه", "كنبة", "سرير", "دولاب", "سفرة", "سجاد", "مفروشات", "ستائر"
        ) to MallPlaceCategory.FURNITURE,
        listOf(
            "ملابس", "هدوم", "حذاء", "أحذية", "كوتشي", "طقم", "تيشيرت", "بنطلون",
            "عطر", "برفان", "برفيوم", "عود", "هدية", "هدايا"
        ) to MallPlaceCategory.FASHION,
        listOf(
            "لعب", "لعبة", "ألعاب", "ملاهي", "طفل", "أطفال", "بينجو", "توتو"
        ) to MallPlaceCategory.ENTERTAINMENT,
        listOf(
            "بنك", "صراف", "فلوس", "سحب", "إيداع", "فيزا", "تحويل", "مفقودات",
            "خدمة عملاء", "استعلام", "صيدلية", "دوا", "دواء", "علاج"
        ) to MallPlaceCategory.SERVICES
    )

    fun suggestPlacesForTask(
        query: String,
        places: List<StoreEntity>,
        limit: Int = 8
    ): List<TaskPlaceSuggestion> {
        if (places.isEmpty()) return emptyList()
        val trimmed = query.trim()

        if (trimmed.isBlank()) {
            // Default curated top places across mall categories so user sees instant options
            val priorityIds = listOf(
                "hypermarket",
                "samsung",
                "tecno_store",
                "kfc_toukh",
                "pizza_primo",
                "elabed_cafe",
                "furniture_home",
                "perfumes_oud",
                "kids_fashion",
                "pingo_toys",
                "vodafone",
                "banque_misr"
            )
            val prioritized = priorityIds.mapNotNull { id -> places.firstOrNull { it.id == id } }
            val extras = places.filter { p -> p.id !in priorityIds && !p.isServiceFacility }
            return (prioritized + extras)
                .take(limit)
                .map { store ->
                    TaskPlaceSuggestion(
                        store = store,
                        reasonAr = "${store.category.labelAr} • ${store.floorLabelAr}"
                    )
                }
        }

        val normalizedQuery = ArabicSearchNormalizer.normalize(trimmed, stripDefiniteArticle = true)
        val tokens = normalizedQuery.split(" ").filter { it.isNotBlank() }
        val expandedTerms = tokens.flatMap { ArabicSearchNormalizer.expandTermWithSynonyms(it) }.toSet()

        val hintedCategories = keywordCategoryHints
            .filter { (keywords, _) ->
                keywords.any { kw ->
                    ArabicSearchNormalizer.containsNormalized(trimmed, kw) ||
                        ArabicSearchNormalizer.normalize(kw, stripDefiniteArticle = true) in expandedTerms
                }
            }
            .map { it.second }
            .toSet()

        val scored = places.mapNotNull { place ->
            var score = 0
            var reason = "${place.category.labelAr} • ${place.floorLabelAr}"

            if (ArabicSearchNormalizer.containsNormalized(place.nameAr, trimmed) ||
                ArabicSearchNormalizer.containsNormalized(place.nameEn, trimmed)
            ) {
                score += 100
                reason = "مطابق للاسم • ${place.floorLabelAr}"
            }

            if (ArabicSearchNormalizer.containsNormalized(place.descriptionAr, trimmed)) {
                score += 60
                reason = "متوفر في ${place.nameAr}"
            }

            tokens.forEach { token ->
                if (token.length >= 2) {
                    val variants = ArabicSearchNormalizer.expandTermWithSynonyms(token)
                    if (variants.any { ArabicSearchNormalizer.containsNormalized(place.nameAr, it) } ||
                        variants.any { ArabicSearchNormalizer.containsNormalized(place.nameEn, it) }
                    ) {
                        score += 45
                    }
                    if (variants.any { ArabicSearchNormalizer.containsNormalized(place.descriptionAr, it) }) {
                        score += 30
                    }
                    if (variants.any { ArabicSearchNormalizer.containsNormalized(place.category.labelAr, it) }) {
                        score += 25
                    }
                }
            }

            if (place.category in hintedCategories) {
                score += 50
                reason = "مقترح لـ ${place.category.labelAr} • ${place.floorLabelAr}"
            }

            // Extra boost for Vodafone when user types خط / شريحة / انترنت
            if (place.id == "vodafone" &&
                listOf("فودافون", "خط", "شريحة", "انترنت", "كاش", "رصيد").any {
                    ArabicSearchNormalizer.containsNormalized(trimmed, it)
                }
            ) {
                score += 75
                reason = "خدمات اتصالات وإنترنت • ${place.floorLabelAr}"
            }

            // Extra boost for perfumes when user types عطر / برفان / هدية
            if (place.id == "perfumes_oud" &&
                listOf("عطر", "عطور", "برفان", "عود", "هدية", "هدايا").any {
                    ArabicSearchNormalizer.containsNormalized(trimmed, it)
                }
            ) {
                score += 75
                reason = "عطور وهدايا • ${place.floorLabelAr}"
            }

            if (score > 0) {
                Triple(place, score, reason)
            } else {
                null
            }
        }.sortedWith(
            compareByDescending<Triple<StoreEntity, Int, String>> { it.second }
                .thenByDescending { it.first.isFeatured }
                .thenBy { it.first.floor }
        )

        return if (scored.isNotEmpty()) {
            scored.take(limit).map { (store, _, reason) ->
                TaskPlaceSuggestion(store = store, reasonAr = reason)
            }
        } else {
            // Fallback to general featured places if no keyword matched
            places.filter { !it.isServiceFacility || it.id == "customer_service" }
                .sortedByDescending { it.isFeatured }
                .take(limit)
                .map { store ->
                    TaskPlaceSuggestion(
                        store = store,
                        reasonAr = "${store.category.labelAr} • ${store.floorLabelAr}"
                    )
                }
        }
    }
}
