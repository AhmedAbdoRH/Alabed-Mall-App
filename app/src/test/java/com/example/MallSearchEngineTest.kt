package com.example

import com.example.data.local.SeedCatalogData
import com.example.data.map.HypermarketMapCatalog
import com.example.data.model.MallPlaceCategory
import com.example.data.model.ShoppingTaskEntity
import com.example.data.search.ArabicSearchNormalizer
import com.example.data.search.MallProductCatalog
import com.example.data.search.MallSearchEngine
import com.example.data.search.MallTaskPlaceSuggester
import com.example.data.search.SearchPathTab
import com.example.sharing.MallShareHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MallSearchEngineTest {

    @Test
    fun arabic_normalizer_unifies_alef_taa_alefmaksura_diacritics_tatweel_digits_and_al() {
        val raw = "أَإِآلْعَـابِدُ ةى ١٢٣"
        val normalized = ArabicSearchNormalizer.normalize(raw, stripDefiniteArticle = false)
        assertEquals("ااالعابد هي 123", normalized)

        val stripped = ArabicSearchNormalizer.normalize("المطعم الهندي ٢٠١٩", stripDefiniteArticle = true)
        assertEquals("مطعم هندي 2019", stripped)

        val hamzaAlStripped = ArabicSearchNormalizer.normalize("الألبان الألعاب", stripDefiniteArticle = true)
        assertEquals("البان العاب", hamzaAlStripped)

        assertTrue(
            ArabicSearchNormalizer.matchesQuery(
                query = "الإسكندرية",
                "طريق الاسكندريه الزراعي"
            )
        )
    }

    @Test
    fun hashtag_meat_matches_hypermarket() {
        val results = MallSearchEngine.filterPlaces(
            places = SeedCatalogData.stores,
            offers = SeedCatalogData.offers,
            rawQuery = "#لحوم_ماي_ميت",
            searchTab = SearchPathTab.PRODUCTS,
            selectedCategory = null,
            selectedFloor = -1
        )
        assertTrue(results.any { it.id == "hypermarket" })
    }

    @Test
    fun dialect_and_synonym_queries_match_expected_places() {
        val restaurantResults = MallSearchEngine.filterPlaces(
            places = SeedCatalogData.stores,
            offers = SeedCatalogData.offers,
            rawQuery = "ريستورانت",
            searchTab = SearchPathTab.ALL,
            selectedCategory = null,
            selectedFloor = -1
        )
        assertTrue(restaurantResults.any { it.id == "kfc_toukh" })
        assertTrue(restaurantResults.any { it.id == "pizza_primo" })

        val coffeeResults = MallSearchEngine.filterPlaces(
            places = SeedCatalogData.stores,
            offers = SeedCatalogData.offers,
            rawQuery = "قهوة",
            searchTab = SearchPathTab.ALL,
            selectedCategory = null,
            selectedFloor = -1
        )
        assertTrue(coffeeResults.any { it.id == "elabed_cafe" })
        assertTrue(coffeeResults.any { it.id == "hadouta_masreya" })

        val pharmacyHealthResults = MallSearchEngine.filterPlaces(
            places = SeedCatalogData.stores,
            offers = SeedCatalogData.offers,
            rawQuery = "صيدلية",
            searchTab = SearchPathTab.ALL,
            selectedCategory = null,
            selectedFloor = -1
        )
        assertTrue(pharmacyHealthResults.any { it.category == MallPlaceCategory.SERVICES })

        val medicineDialectResults = MallSearchEngine.filterPlaces(
            places = SeedCatalogData.stores,
            offers = SeedCatalogData.offers,
            rawQuery = "دوا",
            searchTab = SearchPathTab.ALL,
            selectedCategory = null,
            selectedFloor = -1
        )
        assertTrue(medicineDialectResults.any { it.category == MallPlaceCategory.SERVICES })

        val clothesResults = MallSearchEngine.filterPlaces(
            places = SeedCatalogData.stores,
            offers = SeedCatalogData.offers,
            rawQuery = "هدوم",
            searchTab = SearchPathTab.ALL,
            selectedCategory = null,
            selectedFloor = -1
        )
        assertTrue(clothesResults.any { it.id == "kids_fashion" })

        val phoneResults = MallSearchEngine.filterPlaces(
            places = SeedCatalogData.stores,
            offers = SeedCatalogData.offers,
            rawQuery = "جوال",
            searchTab = SearchPathTab.ALL,
            selectedCategory = null,
            selectedFloor = -1
        )
        assertTrue(phoneResults.any { it.id == "samsung" })
        assertTrue(phoneResults.any { it.id == "tecno_store" })
    }

    @Test
    fun brand_tab_excludes_service_facilities() {
        val results = MallSearchEngine.filterPlaces(
            places = SeedCatalogData.stores,
            offers = SeedCatalogData.offers,
            rawQuery = "",
            searchTab = SearchPathTab.BRANDS,
            selectedCategory = null,
            selectedFloor = -1
        )
        assertTrue(results.none { it.isServiceFacility })
        assertTrue(results.any { it.id == "samsung" })
    }

    @Test
    fun suggestions_return_matching_stores_with_reasons() {
        val suggestions = MallSearchEngine.buildSuggestions(
            places = SeedCatalogData.stores,
            offers = SeedCatalogData.offers,
            rawQuery = "سامسونج",
            searchTab = SearchPathTab.ALL
        )
        assertTrue(suggestions.isNotEmpty())
        assertTrue(suggestions.first().store.id == "samsung")
    }

    @Test
    fun task_suggester_suggests_hypermarket_for_meat_or_groceries() {
        val suggestions = MallTaskPlaceSuggester.suggestPlacesForTask(
            query = "لحمة وخضار ولبن",
            places = SeedCatalogData.stores
        )
        assertTrue(suggestions.isNotEmpty())
        assertTrue(suggestions.first().store.id == "hypermarket")
    }

    @Test
    fun task_suggester_suggests_electronics_for_charger() {
        val suggestions = MallTaskPlaceSuggester.suggestPlacesForTask(
            query = "شاحن سامسونج",
            places = SeedCatalogData.stores
        )
        assertTrue(suggestions.isNotEmpty())
        assertTrue(suggestions.first().store.id == "samsung")
    }

    @Test
    fun product_catalog_filters_meat_and_samsung_products() {
        val meatResults = MallProductCatalog.filterProducts(
            rawQuery = "لحوم",
            selectedCategory = null,
            selectedFloor = -1
        )
        assertTrue(meatResults.any { it.id == "prod_meat" && it.storeId == "hypermarket" })

        val samsungResults = MallProductCatalog.filterProducts(
            rawQuery = "سامسونج",
            selectedCategory = MallPlaceCategory.ELECTRONICS,
            selectedFloor = 0
        )
        assertTrue(samsungResults.any { it.id == "prod_tvs" })
        assertTrue(samsungResults.any { it.id == "prod_samsung_phones" })
    }

    @Test
    fun product_catalog_filters_by_floor_correctly() {
        val firstFloorOnly = MallProductCatalog.filterProducts(
            rawQuery = "",
            selectedCategory = null,
            selectedFloor = 1
        )
        assertTrue(firstFloorOnly.isNotEmpty())
        assertTrue(firstFloorOnly.all { it.floorLabelAr == "الأول" })
    }

    @Test
    fun hypermarket_map_sorts_shopping_tasks_by_route_order() {
        assertEquals(10, HypermarketMapCatalog.sections.size)

        val cashierTask = ShoppingTaskEntity(id = 1, itemName = "دفع الحساب عند الكاشير")
        val detergentTask = ShoppingTaskEntity(id = 2, itemName = "مسحوق غسيل وكلور")
        val meatTask = ShoppingTaskEntity(id = 3, itemName = "لحم مفروم وبانيه")
        val dairyTask = ShoppingTaskEntity(id = 4, itemName = "لبن وجبنة بيضاء")
        val produceTask = ShoppingTaskEntity(id = 5, itemName = "طماطم وخيار")

        val sorted = HypermarketMapCatalog.sortTasksByMapOrder(
            listOf(cashierTask, detergentTask, meatTask, dairyTask, produceTask)
        )

        assertEquals(
            listOf(5L, 4L, 3L, 2L, 1L),
            sorted.map { it.id }
        )

        val formatted = MallShareHelper.buildFormattedShoppingListText(sorted)
        assertTrue(formatted.contains("قائمة المهام والمشتريات"))
        assertTrue(formatted.contains("طماطم وخيار"))
        assertTrue(formatted.contains("خضار وفواكه"))
        assertFalse(formatted.contains("http://"))
        assertFalse(formatted.contains("https://"))
        assertFalse(formatted.contains("QR", ignoreCase = true))
    }
}
