package com.example.data.search

/**
 * Local Arabic text normalizer and dialect/synonym matcher for El Abed Mall search.
 */
object ArabicSearchNormalizer {

    private val DIACRITICS_REGEX = Regex("[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]")
    private val PUNCTUATION_REGEX = Regex("[#_\\-/،,.؛:!؟()\\[\\]\"']+")
    private val WHITESPACE_REGEX = Regex("\\s+")

    private val rawSynonymGroups: List<List<String>> = listOf(
        listOf(
            "مطعم", "مطاعم", "ريستورانت", "رستورانت", "restaurant",
            "اكل", "وجبة", "وجبات", "غدا", "عشا", "فطار", "دجاج", "فراخ", "كنتاكي", "بيتزا"
        ),
        listOf(
            "كافيه", "كافي", "قهوة", "مقهى", "شاي", "مشروبات", "كابتشينو", "لاتيه", "cafe", "coffee"
        ),
        listOf(
            "صيدلية", "صيدليات", "دوا", "دواء", "أدوية", "علاج", "طبي", "إسعاف", "صحة", "صحية", "خدمات صحية"
        ),
        listOf(
            "ملابس", "هدوم", "لبس", "أزياء", "تيشيرت", "بنطلون", "جاكيت", "أحذية", "كوتشي", "جزمة", "شنط"
        ),
        listOf(
            "موبايل", "موبايلات", "جوال", "تليفون", "تلفون", "هاتف", "هواتف", "فون", "شاحن", "سماعة", "جراب"
        ),
        listOf(
            "إلكترونيات", "أجهزة", "كهربائية", "شاشة", "شاشات", "تلفزيون", "ثلاجة", "تلاجة", "غسالة", "بوتاجاز"
        ),
        listOf(
            "هايبر", "ماركت", "سوبر ماركت", "سوبرماركت", "بقالة", "مقاضي", "خضار", "فاكهة", "فواكه",
            "لحم", "لحمة", "لحوم", "لبن", "حليب", "ألبان", "عيش", "خبز"
        ),
        listOf(
            "بنك", "بنوك", "صراف", "atm", "فلوس", "كاش", "نقدي", "سحب", "إيداع", "فيزا"
        ),
        listOf(
            "أثاث", "موبيليا", "عفش", "مفروشات", "ركنة", "انتريه", "سفرة", "سرير", "سجاد", "ستائر"
        ),
        listOf(
            "ألعاب", "لعب", "لعبة", "ملاهي", "ترفيه", "أطفال", "عيال", "فسحة"
        ),
        listOf(
            "عطر", "عطور", "برفان", "برفيوم", "عود", "مسك", "هدية", "هدايا"
        ),
        listOf(
            "بوابة", "مدخل", "موقف", "باركينج", "جراج", "سيارة", "عربية",
            "مصعد", "اسانسير", "سلم", "حمام", "تواليت", "دورات مياه"
        )
    )

    private val normalizedSynonymGroups: List<Set<String>> = rawSynonymGroups.map { group ->
        group.flatMap { term ->
            val base = normalize(term, stripDefiniteArticle = false)
            val stripped = normalize(term, stripDefiniteArticle = true)
            listOf(base, stripped)
        }.filter { it.isNotBlank() }.toSet()
    }

    fun normalize(text: String, stripDefiniteArticle: Boolean = false): String {
        if (text.isBlank()) return ""
        val withoutDiacritics = DIACRITICS_REGEX.replace(text, "")
        val preFolded = StringBuilder(withoutDiacritics.length)
        for (ch in withoutDiacritics) {
            when (ch) {
                '\u0640' -> Unit // Tatweel
                'ة' -> preFolded.append('ه')
                'ى', 'ئ' -> preFolded.append('ي')
                in '٠'..'٩' -> preFolded.append(('0'.code + (ch.code - '٠'.code)).toChar())
                in '۰'..'۹' -> preFolded.append(('0'.code + (ch.code - '۰'.code)).toChar())
                else -> preFolded.append(ch.lowercaseChar())
            }
        }
        val cleanedPreFold = PUNCTUATION_REGEX.replace(preFolded.toString(), " ")
            .replace(WHITESPACE_REGEX, " ")
            .trim()
        if (cleanedPreFold.isBlank()) return ""

        val afterArticle = if (stripDefiniteArticle) {
            cleanedPreFold.split(" ")
                .joinToString(" ") { token ->
                    if (token.startsWith("ال") && token.length >= 4) {
                        token.removePrefix("ال")
                    } else {
                        token
                    }
                }
                .trim()
        } else {
            cleanedPreFold
        }

        val finalSb = StringBuilder(afterArticle.length)
        for (ch in afterArticle) {
            when (ch) {
                'أ', 'إ', 'آ', 'ٱ' -> finalSb.append('ا')
                else -> finalSb.append(ch)
            }
        }
        return finalSb.toString().replace(WHITESPACE_REGEX, " ").trim()
    }

    fun expandTermWithSynonyms(rawTerm: String): Set<String> {
        val base = normalize(rawTerm, stripDefiniteArticle = false)
        val stripped = normalize(rawTerm, stripDefiniteArticle = true)
        if (base.isBlank() && stripped.isBlank()) return emptySet()

        val result = linkedSetOf<String>()
        if (base.isNotBlank()) result.add(base)
        if (stripped.isNotBlank()) result.add(stripped)

        normalizedSynonymGroups.forEach { group ->
            if (base in group || stripped in group) {
                result.addAll(group)
            }
        }
        return result
    }

    fun containsNormalized(haystack: String, needle: String): Boolean {
        if (needle.isBlank() || haystack.isBlank()) return false
        val normHaystack = normalize(haystack, stripDefiniteArticle = false)
        val strippedHaystack = normalize(haystack, stripDefiniteArticle = true)
        val normNeedle = normalize(needle, stripDefiniteArticle = false)
        val strippedNeedle = normalize(needle, stripDefiniteArticle = true)

        return (normNeedle.isNotBlank() && (normHaystack.contains(normNeedle) || strippedHaystack.contains(normNeedle))) ||
            (strippedNeedle.isNotBlank() && (normHaystack.contains(strippedNeedle) || strippedHaystack.contains(strippedNeedle)))
    }

    fun matchesQuery(query: String, vararg fields: String): Boolean {
        val normQuery = normalize(query, stripDefiniteArticle = false)
        if (normQuery.isBlank()) return true

        val combinedFieldBase = fields.joinToString(" ") { normalize(it, stripDefiniteArticle = false) }
        val combinedFieldStripped = fields.joinToString(" ") { normalize(it, stripDefiniteArticle = true) }

        // Direct full-phrase match
        val queryStripped = normalize(query, stripDefiniteArticle = true)
        if (combinedFieldBase.contains(normQuery) ||
            combinedFieldStripped.contains(normQuery) ||
            (queryStripped.isNotBlank() && (combinedFieldBase.contains(queryStripped) || combinedFieldStripped.contains(queryStripped)))
        ) {
            return true
        }

        // Token-by-token match with synonym expansion (all meaningful tokens must match via direct or synonym)
        val tokens = normQuery.split(" ").filter { it.length >= 2 }
        if (tokens.isEmpty()) {
            return expandTermWithSynonyms(normQuery).any { term ->
                combinedFieldBase.contains(term) || combinedFieldStripped.contains(term)
            }
        }

        return tokens.all { token ->
            val variants = expandTermWithSynonyms(token)
            variants.any { variant ->
                containsTokenOrSubstring(combinedFieldBase, variant) ||
                    containsTokenOrSubstring(combinedFieldStripped, variant)
            }
        }
    }

    private fun containsTokenOrSubstring(haystack: String, variant: String): Boolean {
        if (variant.length <= 3) {
            // Short 2-3 char synonyms (e.g. دوا, لبس, لبن, atm) match whole tokens or clear word starts
            val haystackTokens = haystack.split(" ")
            return haystackTokens.any { it == variant || it.startsWith(variant) }
        }
        return haystack.contains(variant)
    }
}
