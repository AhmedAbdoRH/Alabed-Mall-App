package com.example

import com.example.data.local.SeedCatalogData
import com.example.data.search.MallSearchEngine
import com.example.data.search.MallTaskPlaceSuggester
import com.example.data.search.SearchPathTab
import org.junit.Assert.assertTrue
import org.junit.Test

class MallSearchEngineTest {

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
}
