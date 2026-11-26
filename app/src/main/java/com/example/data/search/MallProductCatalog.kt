package com.example.data.search

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Attractions
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Blender
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Toys
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.model.MallPlaceCategory

data class MallProductSuggestion(
    val id: String,
    val nameAr: String,
    val brandNameAr: String,
    val storeId: String,
    val floorLabelAr: String,
    val category: MallPlaceCategory,
    val icon: ImageVector,
    val accentColor: Color,
    val softTintColor: Color,
    val keywords: List<String>
)

object MallProductCatalog {

    val suggestedProducts: List<MallProductSuggestion> = listOf(
        // الماركت والغذائية
        MallProductSuggestion(
            id = "prod_meat",
            nameAr = "لحوم طازجة ماي ميت",
            brandNameAr = "هايبر العابد",
            storeId = "hypermarket",
            floorLabelAr = "الأرضي",
            category = MallPlaceCategory.HYPERMARKET,
            icon = Icons.Filled.Restaurant,
            accentColor = Color(0xFFD9383A),
            softTintColor = Color(0xFFFFF1F1),
            keywords = listOf("لحم", "لحوم", "ماي ميت", "فراخ", "جزارة", "هايبر", "ماركت")
        ),
        MallProductSuggestion(
            id = "prod_dairy_groceries",
            nameAr = "بقالة وألبان يومية",
            brandNameAr = "هايبر العابد",
            storeId = "hypermarket",
            floorLabelAr = "الأرضي",
            category = MallPlaceCategory.HYPERMARKET,
            icon = Icons.Filled.LocalGroceryStore,
            accentColor = Color(0xFF129E4B),
            softTintColor = Color(0xFFEBF9F1),
            keywords = listOf("لبن", "ألبان", "جبنة", "زيت", "سكر", "أرز", "مكرونة", "بقالة", "خضار", "فاكهة")
        ),
        MallProductSuggestion(
            id = "prod_bakery",
            nameAr = "مخبوزات وفطائر طازجة",
            brandNameAr = "مخبز العابد",
            storeId = "hypermarket",
            floorLabelAr = "الأرضي",
            category = MallPlaceCategory.HYPERMARKET,
            icon = Icons.Filled.BakeryDining,
            accentColor = Color(0xFFD97706),
            softTintColor = Color(0xFFFFF6E7),
            keywords = listOf("عيش", "خبز", "مخبوزات", "فينو", "كيك", "حلويات", "تورتة")
        ),

        // أجهزة كهربائية وإلكترونيات
        MallProductSuggestion(
            id = "prod_tvs",
            nameAr = "شاشات وتلفزيونات سمارت",
            brandNameAr = "سامسونج",
            storeId = "samsung",
            floorLabelAr = "الأرضي",
            category = MallPlaceCategory.ELECTRONICS,
            icon = Icons.Filled.Tv,
            accentColor = Color(0xFF28459B),
            softTintColor = Color(0xFFEEF3FF),
            keywords = listOf("شاشة", "تلفزيون", "سمارت", "سامسونج", "أجهزة", "كهربائية")
        ),
        MallProductSuggestion(
            id = "prod_fridge",
            nameAr = "ثلاجات وغسالات وبوتاجازات",
            brandNameAr = "الشركة المتحدة",
            storeId = "united_electronics",
            floorLabelAr = "الأول",
            category = MallPlaceCategory.ELECTRONICS,
            icon = Icons.Filled.Kitchen,
            accentColor = Color(0xFF0284C7),
            softTintColor = Color(0xFFEAF6FD),
            keywords = listOf("ثلاجة", "غسالة", "تكييف", "بوتاجاز", "فريزر", "أجهزة منزلية")
        ),
        MallProductSuggestion(
            id = "prod_appliances",
            nameAr = "أجهزة مطبخ وخلاطات",
            brandNameAr = "الشركة المتحدة",
            storeId = "united_electronics",
            floorLabelAr = "الأول",
            category = MallPlaceCategory.ELECTRONICS,
            icon = Icons.Filled.Blender,
            accentColor = Color(0xFF0D9488),
            softTintColor = Color(0xFFE8F8F5),
            keywords = listOf("خلاط", "ميكروويف", "مكواة", "كاتل", "عجان", "مطبخ")
        ),
        MallProductSuggestion(
            id = "prod_samsung_phones",
            nameAr = "موبايلات وصيانة سامسونج",
            brandNameAr = "سامسونج",
            storeId = "samsung",
            floorLabelAr = "الأرضي",
            category = MallPlaceCategory.ELECTRONICS,
            icon = Icons.Filled.PhoneAndroid,
            accentColor = Color(0xFF1E40AF),
            softTintColor = Color(0xFFEBF1FF),
            keywords = listOf("موبايل", "تليفون", "هاتف", "شاحن", "سماعة", "صيانة", "سامسونج")
        ),
        MallProductSuggestion(
            id = "prod_tecno_phones",
            nameAr = "هواتف وإكسسوارات تكنو",
            brandNameAr = "توكيل تكنو",
            storeId = "tecno_store",
            floorLabelAr = "الأرضي",
            category = MallPlaceCategory.ELECTRONICS,
            icon = Icons.Filled.PhoneAndroid,
            accentColor = Color(0xFF0369A1),
            softTintColor = Color(0xFFE8F4FC),
            keywords = listOf("تكنو", "هاتف", "موبايل", "جراب", "اسكرينة", "باور بانك")
        ),
        MallProductSuggestion(
            id = "prod_vodafone_sim",
            nameAr = "خطوط وإنترنت وفودافون كاش",
            brandNameAr = "فودافون",
            storeId = "vodafone",
            floorLabelAr = "الأرضي",
            category = MallPlaceCategory.ELECTRONICS,
            icon = Icons.Filled.SimCard,
            accentColor = Color(0xFFDC2626),
            softTintColor = Color(0xFFFFEEEE),
            keywords = listOf("فودافون", "خط", "شريحة", "انترنت", "راوتر", "كاش", "رصيد")
        ),

        // ملابس وعطور وأثاث
        MallProductSuggestion(
            id = "prod_kids_clothes",
            nameAr = "ملابس أطفال وحديثي الولادة",
            brandNameAr = "أزياء الصغار",
            storeId = "kids_fashion",
            floorLabelAr = "الأول",
            category = MallPlaceCategory.FASHION,
            icon = Icons.Filled.ChildCare,
            accentColor = Color(0xFFDB2777),
            softTintColor = Color(0xFFFDF0F6),
            keywords = listOf("ملابس", "أطفال", "بيبي", "أطقم", "كوتشي", "بنطلون", "تيشيرت")
        ),
        MallProductSuggestion(
            id = "prod_casual_fashion",
            nameAr = "ملابس كاجوال وأحذية",
            brandNameAr = "أزياء العابد",
            storeId = "kids_fashion",
            floorLabelAr = "الأول",
            category = MallPlaceCategory.FASHION,
            icon = Icons.Filled.Checkroom,
            accentColor = Color(0xFF9333EA),
            softTintColor = Color(0xFFF6EEFE),
            keywords = listOf("ملابس", "هدوم", "جاكيت", "قميص", "عبايات", "أحذية", "شنط")
        ),
        MallProductSuggestion(
            id = "prod_perfumes",
            nameAr = "عطور شرقية وهدايا وعود",
            brandNameAr = "بيت العطور",
            storeId = "perfumes_oud",
            floorLabelAr = "الأول",
            category = MallPlaceCategory.FASHION,
            icon = Icons.Filled.Spa,
            accentColor = Color(0xFFB45309),
            softTintColor = Color(0xFFFFF5E8),
            keywords = listOf("عطر", "عطور", "برفان", "عود", "مسك", "بوكس", "هدية", "هدايا")
        ),
        MallProductSuggestion(
            id = "prod_furniture",
            nameAr = "ركنات وأثاث ومفروشات",
            brandNameAr = "الأثاث للبيت",
            storeId = "furniture_home",
            floorLabelAr = "الأول",
            category = MallPlaceCategory.FURNITURE,
            icon = Icons.Filled.Weekend,
            accentColor = Color(0xFF0F766E),
            softTintColor = Color(0xFFE7F7F5),
            keywords = listOf("أثاث", "ركنة", "انتريه", "سفرة", "سرير", "مفروشات", "سجاد", "ستائر")
        ),

        // مطاعم وكافيهات
        MallProductSuggestion(
            id = "prod_kfc",
            nameAr = "وجبات دجاج مقرمشة عائلية",
            brandNameAr = "كنتاكي طوخ",
            storeId = "kfc_toukh",
            floorLabelAr = "الأرضي",
            category = MallPlaceCategory.DINING,
            icon = Icons.Filled.Fastfood,
            accentColor = Color(0xFFC81E1E),
            softTintColor = Color(0xFFFFF0F0),
            keywords = listOf("كنتاكي", "فراخ", "بروست", "ساندوتش", "وجبة", "مطعم", "أكل")
        ),
        MallProductSuggestion(
            id = "prod_pizza",
            nameAr = "بيتزا إيطالي وفطائر ساخنة",
            brandNameAr = "بيتزا بريمو",
            storeId = "pizza_primo",
            floorLabelAr = "الأرضي",
            category = MallPlaceCategory.DINING,
            icon = Icons.Filled.LocalPizza,
            accentColor = Color(0xFFEA580C),
            softTintColor = Color(0xFFFFF3EB),
            keywords = listOf("بيتزا", "مكرونة", "لازانيا", "فطير", "مطعم", "عشا")
        ),
        MallProductSuggestion(
            id = "prod_coffee",
            nameAr = "قهوة ومشروبات وحلويات",
            brandNameAr = "العابد كافيه",
            storeId = "elabed_cafe",
            floorLabelAr = "الأرضي",
            category = MallPlaceCategory.DINING,
            icon = Icons.Filled.Coffee,
            accentColor = Color(0xFF7C3AED),
            softTintColor = Color(0xFFF4EEFF),
            keywords = listOf("قهوة", "شاي", "كافيه", "عصير", "وافل", "آيس كريم", "لاتيه", "حلا")
        ),

        // ترفيه وأطفال
        MallProductSuggestion(
            id = "prod_pingo_toys",
            nameAr = "لعب أطفال وهدايا وعجل",
            brandNameAr = "Pingo العابد",
            storeId = "pingo_toys",
            floorLabelAr = "الأول",
            category = MallPlaceCategory.ENTERTAINMENT,
            icon = Icons.Filled.Toys,
            accentColor = Color(0xFF16A34A),
            softTintColor = Color(0xFFECFAEF),
            keywords = listOf("لعب", "ألعاب", "عروسة", "عجلة", "سكوتر", "هدية", "طفل")
        ),
        MallProductSuggestion(
            id = "prod_tutu_rides",
            nameAr = "ملاهي وألعاب ترفيهية للأطفال",
            brandNameAr = "Tutu Kids",
            storeId = "tutu_kids",
            floorLabelAr = "الأول",
            category = MallPlaceCategory.ENTERTAINMENT,
            icon = Icons.Filled.Attractions,
            accentColor = Color(0xFF6D28D9),
            softTintColor = Color(0xFFF3EEFF),
            keywords = listOf("ملاهي", "ألعاب", "توتو", "فسحة", "ترامبولين", "مراجيح")
        ),

        // خدمات مالية وخدمة الزوار
        MallProductSuggestion(
            id = "prod_atm_cash",
            nameAr = "سحب وإيداع ATM فوري",
            brandNameAr = "ماكينات الصراف الآلي",
            storeId = "banque_misr",
            floorLabelAr = "الأرضي",
            category = MallPlaceCategory.SERVICES,
            icon = Icons.Filled.CreditCard,
            accentColor = Color(0xFF1D4ED8),
            softTintColor = Color(0xFFEEF4FF),
            keywords = listOf("سحب", "إيداع", "صراف", "فيزا", "بنك", "فلوس", "atm")
        ),
        MallProductSuggestion(
            id = "prod_banking",
            nameAr = "معاملات بنكية وتحويلات",
            brandNameAr = "فروع البنوك",
            storeId = "alex_bank",
            floorLabelAr = "الأرضي",
            category = MallPlaceCategory.SERVICES,
            icon = Icons.Filled.AccountBalance,
            accentColor = Color(0xFF0F766E),
            softTintColor = Color(0xFFE7F7F5),
            keywords = listOf("بنك", "بنك مصر", "الإسكندرية", "فيصل", "القاهرة", "تحويل", "حساب")
        ),
        MallProductSuggestion(
            id = "prod_customer_service",
            nameAr = "خدمة العملاء والمفقودات",
            brandNameAr = "استقبال العابد مول",
            storeId = "customer_service",
            floorLabelAr = "الأرضي",
            category = MallPlaceCategory.SERVICES,
            icon = Icons.Filled.SupportAgent,
            accentColor = Color(0xFF059669),
            softTintColor = Color(0xFFE9FBF3),
            keywords = listOf("استعلام", "مفقودات", "شكوى", "خدمة عملاء", "مساعدة", "كرسي متحرك")
        ),
        MallProductSuggestion(
            id = "prod_parking",
            nameAr = "موقف السيارات والبوابة الرئيسية",
            brandNameAr = "بوابة ١ والباركينج",
            storeId = "parking_west",
            floorLabelAr = "الأرضي",
            category = MallPlaceCategory.FACILITY,
            icon = Icons.Filled.DirectionsCar,
            accentColor = Color(0xFF334155),
            softTintColor = Color(0xFFF1F5F9),
            keywords = listOf("ركنة", "باركينج", "سيارة", "بوابة", "مدخل", "جراج")
        )
    )

    fun filterProducts(
        rawQuery: String,
        selectedCategory: MallPlaceCategory?,
        selectedFloor: Int
    ): List<MallProductSuggestion> {
        val q = rawQuery.trim().removePrefix("#").replace("_", " ").trim()
        return suggestedProducts.filter { item ->
            val matchesCat = selectedCategory == null || item.category == selectedCategory
            val matchesFloor = when (selectedFloor) {
                0 -> item.floorLabelAr == "الأرضي"
                1 -> item.floorLabelAr == "الأول"
                else -> true
            }
            val matchesQuery = q.isBlank() || ArabicSearchNormalizer.matchesQuery(
                q,
                item.nameAr,
                item.brandNameAr,
                item.category.labelAr,
                *item.keywords.toTypedArray()
            )

            matchesCat && matchesFloor && matchesQuery
        }
    }
}
