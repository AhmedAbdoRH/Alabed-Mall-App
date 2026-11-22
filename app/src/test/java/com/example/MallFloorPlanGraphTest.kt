package com.example

import com.example.data.local.SeedCatalogData
import com.example.data.map.MallFloorPlanGraph
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MallFloorPlanGraphTest {

    @Test
    fun routeSameFloor_fromGate1ToHypermarket_findsPathWithoutFloorChange() {
        val hypermarket = SeedCatalogData.stores.first { it.id == "hypermarket" }
        val plan = MallFloorPlanGraph.computeRoute(
            startX = 335f,
            startY = 635f,
            startFloor = 0,
            destination = hypermarket
        )

        assertFalse(plan.requiresFloorChange)
        assertTrue(plan.nodes.isNotEmpty())
        assertTrue(plan.totalDistanceMeters > 0)
        assertTrue(plan.steps.isNotEmpty())
    }

    @Test
    fun routeSameFloor_fromGate1ToRealLandmarks_succeeds() {
        val landmarkIds = listOf("kfc_toukh", "tecno_store", "pingo_toys", "tutu_kids")
        landmarkIds.forEach { id ->
            val place = SeedCatalogData.stores.first { it.id == id }
            val plan = MallFloorPlanGraph.computeRoute(
                startX = 335f,
                startY = 635f,
                startFloor = 0,
                destination = place
            )
            assertFalse(plan.requiresFloorChange)
            assertEquals(place.id, plan.nodes.last().linkedPlaceId)
        }
    }

    @Test
    fun routeCrossFloor_fromGate1ToFirstFloorDestinations_requiresVerticalTransition() {
        val firstFloorIds = listOf("kids_rides", "furniture_home", "perfumes_oud")
        firstFloorIds.forEach { id ->
            val target = SeedCatalogData.stores.first { it.id == id }
            val plan = MallFloorPlanGraph.computeRoute(
                startX = 335f,
                startY = 635f,
                startFloor = 0,
                destination = target
            )

            assertTrue(plan.requiresFloorChange)
            assertNotNull(plan.transitionNode)
            assertEquals(0, plan.nodes.first().floor)
            assertEquals(1, plan.nodes.last().floor)
            assertTrue(plan.steps.any { it.isTransitionStep && it.targetFloor == 1 })
        }
    }

    @Test
    fun geoToMallCoords_detectsInsideAndOutsideMallBounds() {
        val (insideCoords, isInside) = MallFloorPlanGraph.geoToMallCoords(
            MallFloorPlanGraph.MALL_CENTER_LAT,
            MallFloorPlanGraph.MALL_CENTER_LNG
        )
        assertTrue(isInside)
        assertEquals(485f, insideCoords.first, 5f)
        assertEquals(555f, insideCoords.second, 5f)

        val (_, isOutside) = MallFloorPlanGraph.geoToMallCoords(30.0444, 31.2357)
        assertFalse(isOutside)
    }
}
