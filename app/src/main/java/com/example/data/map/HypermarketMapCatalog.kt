package com.example.data.map

import androidx.compose.ui.graphics.Color
import com.example.data.model.ShoppingTaskEntity
import com.example.data.search.ArabicSearchNormalizer

data class HypermarketSection(
    val id: String,
    val nameAr: String,
    val aisleLabelAr: String,
    val routeOrder: Int,
    val normX: Float,
    val normY: Float,
    val normW: Float,
    val normH: Float,
    val accentColor: Color,
    val softBgColor: Color,
    val sampleItemsPlaceholderAr: String,
    val keywords: List<String>
)

/**
 * Placeholder indoor section layout and map-order sorting for El Abed Hypermarket.
 * Sections follow a serpentine walking route (1 -> 10) from entrance to cashier.
 */
object HypermarketMapCatalog {

    val sections: List<HypermarketSection> = listOf(
        HypermarketSection(
            id = "sec_produce",
            nameAr = "خضار وفواكه",
            aisleLabelAr = "ممر ١ • المدخل الطازج",
            routeOrder = 1,
            normX = 0.68f,
            normY = 0.10f,
            normW = 0.26f,
            normH = 0.21f,
            accentColor = Color(0xFF15803D),
            softBgColor = Color(0xFFDCFCE7),
            sampleItemsPlaceholderAr = "طماطم، بطاطس، خيار، موز، تفاح",
            keywords = listOf("خضار", "خضروات", "فاكهة", "فواكه", "طماطم", "بطاطس", "بصل", "خيار", "موز", "تفاح", "برتقال", "ليمون", "سلطة")
        ),
        HypermarketSection(
            id = "sec_bakery",
            nameAr = "مخبوزات",
            aisleLabelAr = "ممر ٢ • ركن الفرن",
            routeOrder = 2,
            normX = 0.37f,
            normY = 0.10f,
            normW = 0.26f,
            normH = 0.21f,
            accentColor = Color(0xFFB45309),
            softBgColor = Color(0xFFFEF3C7),
            sampleItemsPlaceholderAr = "عيش بلدي، فينو، توست، كرواسون",
            keywords = listOf("مخبوزات", "عيش", "خبز", "فينو", "توست", "باتيه", "كرواسون", "كيك", "تورتة", "فطير", "معجنات")
        ),
        HypermarketSection(
            id = "sec_dairy",
            nameAr = "ألبان",
            aisleLabelAr = "ممر ٣ • المبردات",
            routeOrder = 3,
            normX = 0.06f,
            normY = 0.10f,
            normW = 0.26f,
            normH = 0.21f,
            accentColor = Color(0xFF1D4ED8),
            softBgColor = Color(0xFFDBEAFE),
            sampleItemsPlaceholderAr = "لبن، جبنة بيضاء، زبادي، زبدة، بيض",
            keywords = listOf("ألبان", "لبن", "حليب", "جبن", "جبنة", "زبادي", "زبدة", "سمن", "قشطة", "بيض", "رايب")
        ),
        HypermarketSection(
            id = "sec_meat",
            nameAr = "لحوم",
            aisleLabelAr = "ممر ٤ • ماي ميت",
            routeOrder = 4,
            normX = 0.06f,
            normY = 0.37f,
            normW = 0.26f,
            normH = 0.21f,
            accentColor = Color(0xFFB91C1C),
            softBgColor = Color(0xFFFEE2E2),
            sampleItemsPlaceholderAr = "لحم مفروم، بوفتيك، فراخ، كبدة، سمك",
            keywords = listOf("لحم", "لحمة", "لحوم", "ماي ميت", "مفروم", "فراخ", "دجاج", "بانيه", "كبدة", "سجق", "كفتة", "سمك", "جزارة")
        ),
        HypermarketSection(
            id = "sec_frozen",
            nameAr = "مجمدات",
            aisleLabelAr = "ممر ٥ • الفريزرات",
            routeOrder = 5,
            normX = 0.37f,
            normY = 0.37f,
            normW = 0.26f,
            normH = 0.21f,
            accentColor = Color(0xFF0369A1),
            softBgColor = Color(0xFFE0F2FE),
            sampleItemsPlaceholderAr = "خضار مجمد، ناجتس، برجر، آيس كريم",
            keywords = listOf("مجمد", "مجمدات", "فريزر", "برجر", "ناجتس", "سوسيس", "بطاطس مجمدة", "بسلة", "ملوخية", "آيس كريم", "ايس كريم")
        ),
        HypermarketSection(
            id = "sec_groceries",
            nameAr = "بقالة",
            aisleLabelAr = "ممر ٦ • السلع والحبوب",
            routeOrder = 6,
            normX = 0.68f,
            normY = 0.37f,
            normW = 0.26f,
            normH = 0.21f,
            accentColor = Color(0xFF0F766E),
            softBgColor = Color(0xFFCCFBF1),
            sampleItemsPlaceholderAr = "أرز، مكرونة، زيت، سكر، دقيق، صلصة",
            keywords = listOf("بقالة", "أرز", "رز", "مكرونة", "زيت", "سكر", "ملح", "دقيق", "تونة", "فول", "صلصة", "بهارات", "مقاضي", "مواد غذائية")
        ),
        HypermarketSection(
            id = "sec_beverages",
            nameAr = "مشروبات",
            aisleLabelAr = "ممر ٧ • العصائر والمياه",
            routeOrder = 7,
            normX = 0.68f,
            normY = 0.64f,
            normW = 0.26f,
            normH = 0.21f,
            accentColor = Color(0xFF7C3AED),
            softBgColor = Color(0xFFEDE9FE),
            sampleItemsPlaceholderAr = "مياه، عصير، شاي، قهوة، نسكافيه",
            keywords = listOf("مشروب", "مشروبات", "مياه", "ميه", "عصير", "شاي", "قهوة", "نسكافيه", "كاكاو", "صودا", "بيبسي")
        ),
        HypermarketSection(
            id = "sec_detergents",
            nameAr = "منظفات",
            aisleLabelAr = "ممر ٨ • العناية بالمنزل",
            routeOrder = 8,
            normX = 0.46f,
            normY = 0.64f,
            normW = 0.19f,
            normH = 0.21f,
            accentColor = Color(0xFF0284C7),
            softBgColor = Color(0xFFE0F2FE),
            sampleItemsPlaceholderAr = "مسحوق غسيل، كلور، سائل أطباق، مناديل",
            keywords = listOf("منظف", "منظفات", "مسحوق", "غسيل", "كلور", "بريل", "أطباق", "معطر", "مطهر", "ديتول", "مناديل", "أكياس")
        ),
        HypermarketSection(
            id = "sec_personal_care",
            nameAr = "عناية شخصية",
            aisleLabelAr = "ممر ٩ • التجميل والعناية",
            routeOrder = 9,
            normX = 0.25f,
            normY = 0.64f,
            normW = 0.19f,
            normH = 0.21f,
            accentColor = Color(0xFFBE185D),
            softBgColor = Color(0xFFFCE7F3),
            sampleItemsPlaceholderAr = "شامبو، صابون، معجون أسنان، مزيل عرق",
            keywords = listOf("عناية", "شخصية", "شامبو", "بلسم", "صابون", "معجون", "فرشاة", "شاور", "كريم", "لوشن", "حفاضات", "فوط")
        ),
        HypermarketSection(
            id = "sec_cashier",
            nameAr = "الكاشير",
            aisleLabelAr = "بوابة الدفع والمحاسبة",
            routeOrder = 10,
            normX = 0.06f,
            normY = 0.64f,
            normW = 0.17f,
            normH = 0.21f,
            accentColor = Color(0xFF334155),
            softBgColor = Color(0xFFE2E8F0),
            sampleItemsPlaceholderAr = "دفع كاش أو فيزا، استلام الفاتورة",
            keywords = listOf("كاشير", "حساب", "دفع", "فاتورة", "فيزا", "خزنة", "خروج")
        )
    )

    private val sectionsById: Map<String, HypermarketSection> = sections.associateBy { it.id }

    fun sectionById(id: String?): HypermarketSection? = id?.let { sectionsById[it] }

    fun resolveSectionForInput(
        itemName: String,
        departmentTag: String = "",
        explicitSectionId: String? = null
    ): HypermarketSection? {
        sectionById(explicitSectionId)?.let { return it }

        val combined = "$itemName $departmentTag".trim()
        if (combined.isBlank()) return null

        // Direct match against section name first
        sections.firstOrNull { section ->
            ArabicSearchNormalizer.containsNormalized(combined, section.nameAr)
        }?.let { return it }

        // Keyword match using Arabic normalizer
        return sections.firstOrNull { section ->
            section.keywords.any { kw ->
                ArabicSearchNormalizer.containsNormalized(combined, kw)
            }
        }
    }

    fun sectionForTask(task: ShoppingTaskEntity): HypermarketSection? {
        return resolveSectionForInput(
            itemName = task.itemName,
            departmentTag = task.departmentTag,
            explicitSectionId = task.hypermarketSectionId
        )
    }

    fun routeOrderForTask(task: ShoppingTaskEntity): Int {
        return sectionForTask(task)?.routeOrder ?: 99
    }

    fun sortTasksByMapOrder(tasks: List<ShoppingTaskEntity>): List<ShoppingTaskEntity> {
        return tasks.sortedWith(
            compareBy<ShoppingTaskEntity> { it.isChecked }
                .thenBy { routeOrderForTask(it) }
                .thenBy { it.createdAtMillis }
        )
    }
}
