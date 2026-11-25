package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.data.map.MallFloorPlanGraph
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessScreen(
    onBack: () -> Unit,
    onOpenIndoorMap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mallLatLng = remember {
        LatLng(MallFloorPlanGraph.MALL_CENTER_LAT, MallFloorPlanGraph.MALL_CENTER_LNG)
    }
    val gate1LatLng = remember { LatLng(30.38892, 31.19175) }
    val hyperParkingLatLng = remember { LatLng(30.38958, 31.19145) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mallLatLng, 17f)
    }
    val hasConfiguredMapsKey = BuildConfig.MAPS_API_KEY.isNotBlank() &&
        BuildConfig.MAPS_API_KEY != "MY_MAPS_API_KEY"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("الوصول إلى مول العابد") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("access_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Embedded Google Map
                item {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            ) {
                                GoogleMap(
                                    modifier = Modifier.fillMaxSize(),
                                    cameraPositionState = cameraPositionState,
                                    uiSettings = MapUiSettings(
                                        zoomControlsEnabled = true,
                                        compassEnabled = true,
                                        mapToolbarEnabled = true
                                    )
                                ) {
                                    Marker(
                                        state = MarkerState(position = mallLatLng),
                                        title = "مول العابد - El Abed Mall",
                                        snippet = "كيلو ٣٧، طريق الإسكندرية الزراعي، طوخ"
                                    )
                                    Marker(
                                        state = MarkerState(position = gate1LatLng),
                                        title = "بوابة رقم ١ (المدخل الرئيسي)",
                                        snippet = "مدخل الساحة الوسطى"
                                    )
                                    Marker(
                                        state = MarkerState(position = hyperParkingLatLng),
                                        title = "بوابة ٢ وموقف السيارات الغربي",
                                        snippet = "دخول مباشر لهايبر ماركت العابد"
                                    )
                                }
                            }

                            if (!hasConfiguredMapsKey) {
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "تعذر تحميل خريطة Google المباشرة حالياً. يمكنك فتح الاتجاهات بدقة مباشرة عبر تطبيق Google Maps أدناه.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val gmmUri = Uri.parse(MallFloorPlanGraph.MALL_GOOGLE_MAPS_URL)
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmUri)
                                        context.startActivity(mapIntent)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("access_open_google_maps")
                                ) {
                                    Icon(Icons.Filled.Directions, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("فتح الاتجاهات في Google Maps")
                                }

                                OutlinedButton(
                                    onClick = {
                                        val navUri = Uri.parse(
                                            "google.navigation:q=${MallFloorPlanGraph.MALL_CENTER_LAT},${MallFloorPlanGraph.MALL_CENTER_LNG}"
                                        )
                                        val intent = Intent(Intent.ACTION_VIEW, navUri).apply {
                                            setPackage("com.google.android.apps.maps")
                                        }
                                        runCatching { context.startActivity(intent) }
                                            .onFailure {
                                                context.startActivity(
                                                    Intent(
                                                        Intent.ACTION_VIEW,
                                                        Uri.parse(MallFloorPlanGraph.MALL_GOOGLE_MAPS_URL)
                                                    )
                                                )
                                            }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Text("بدء الملاحة بالسيارة فوراً")
                                }
                            }
                        }
                    }
                }

                // Address & Working Hours
                item {
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text("العنوان والموقع", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "كيلو ٣٧، طريق الإسكندرية الزراعي، طوخ، محافظة القليوبية",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "Plus Code: 95QR+PW طوخ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Column {
                                    Text("مواعيد العمل الرسمية", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "المحلات والمطاعم: يومياً ١٠:٠٠ ص – ١٢:٠٠ ص\nهايبر ماركت العابد: ٩:٠٠ ص – ١:٠٠ ص",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                // Main Entrances & Parking
                item {
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "المداخل ونقاط الوصول الرئيسية",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            EntranceRow(
                                title = "بوابة رقم ١ (المدخل الرئيسي)",
                                description = "مواجهة لطريق الإسكندرية الزراعي وتؤدي للساحة الوسطى والمحلات",
                                onLocate = { onOpenIndoorMap("gate_1") }
                            )
                            EntranceRow(
                                title = "بوابة ٢ (مدخل الهايبر والموقف)",
                                description = "ملاصقة لموقف السيارات الغربي للوصول السريع لهايبر ماركت العابد",
                                onLocate = { onOpenIndoorMap("gate_2") }
                            )
                            EntranceRow(
                                title = "موقف السيارات الرئيسي",
                                description = "ساحة انتظار واسعة مجانية لسيارات الزوار بالجهة الغربية",
                                onLocate = { onOpenIndoorMap("parking_west") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EntranceRow(
    title: String,
    description: String,
    onLocate: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.LocalParking,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(onClick = onLocate) {
                Text("الخريطة", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
