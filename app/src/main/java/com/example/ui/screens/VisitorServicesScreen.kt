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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.StoreEntity
import com.example.data.model.VisitorTicketEntity
import com.example.ui.components.CategoryBadgeIcon
import com.example.ui.components.FloorPill
import com.example.ui.theme.ElAbedRoyalBlue
import com.example.ui.theme.ElAbedRoyalBlueContainer
import com.example.ui.theme.ElAbedSurfaceVariant
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitorServicesScreen(
    places: List<StoreEntity>,
    tickets: List<VisitorTicketEntity>,
    onBack: () -> Unit,
    onOpenIndoorMap: (String) -> Unit,
    onSubmitTicket: (type: String, name: String, phone: String, message: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val essentialServices = remember(places) {
        places.filter {
            it.isServiceFacility || it.id in setOf("customer_service", "atm_bank", "pharmacy_care")
        }
    }

    var ticketType by remember { mutableStateOf("استفسار") }
    var visitorName by remember { mutableStateOf("") }
    var visitorPhone by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("خدمة الزوار والاستعلامات", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("visitor_services_back")) {
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Direct Contact Card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = ElAbedRoyalBlueContainer.copy(alpha = 0.35f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "مركز خدمة عملاء مول العابد",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ElAbedRoyalBlue,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "متاح يومياً ١٠:٠٠ ص - ١٢:٠٠ ص • 0132460105",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Button(
                                onClick = {
                                    context.startActivity(
                                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:0132460105"))
                                    )
                                },
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("اتصال", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "الخدمات والمرافق الأساسية داخل المول",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(essentialServices, key = { it.id }) { svc ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ElAbedSurfaceVariant.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryBadgeIcon(category = svc.category)
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = svc.nameAr,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FloorPill(floor = svc.floor, modifier = Modifier.wrapContentWidth())
                                    Text(
                                        text = svc.zoneAr,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            OutlinedButton(
                                onClick = { onOpenIndoorMap(svc.id) },
                                modifier = Modifier
                                    .height(48.dp)
                                    .wrapContentWidth()
                            ) {
                                Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("الموقع", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "إرسال استفسار أو ملاحظة أو بلاغ لإدارة المول",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("استفسار", "ملاحظة", "بلاغ").forEach { type ->
                                    FilterChip(
                                        selected = ticketType == type,
                                        onClick = { ticketType = type },
                                        label = { Text(type, style = MaterialTheme.typography.labelLarge) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ElAbedRoyalBlueContainer,
                                            selectedLabelColor = ElAbedRoyalBlue
                                        )
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = visitorName,
                                onValueChange = { visitorName = it },
                                label = { Text("الاسم (اختياري)", style = MaterialTheme.typography.bodyMedium) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = visitorPhone,
                                onValueChange = { visitorPhone = it },
                                label = { Text("رقم الهاتف للتواصل (اختياري)", style = MaterialTheme.typography.bodyMedium) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = message,
                                onValueChange = { message = it },
                                label = { Text("تفاصيل الرسالة *", style = MaterialTheme.typography.bodyMedium) },
                                minLines = 3,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("visitor_message_input")
                            )

                            Button(
                                onClick = {
                                    if (message.trim().length < 5) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("يرجى كتابة تفاصيل الرسالة أولاً (5 أحرف على الأقل)")
                                        }
                                    } else {
                                        onSubmitTicket(ticketType, visitorName, visitorPhone, message)
                                        visitorName = ""
                                        visitorPhone = ""
                                        message = ""
                                        scope.launch {
                                            snackbarHostState.showSnackbar("تم استلام رسالتك بنجاح — شكراً لتواصلك مع مول العابد")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("visitor_submit_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("إرسال لإدارة المول", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (tickets.isNotEmpty()) {
                    item {
                        Text(
                            text = "سجل رسائلك المرسلة (${tickets.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(tickets, key = { it.id }) { ticket ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = ElAbedSurfaceVariant.copy(alpha = 0.5f),
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${ticket.ticketType} • ${ticket.visitorName.ifBlank { "زائر" }}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ElAbedRoyalBlue
                                    )
                                    Text(
                                        text = ticket.statusLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(text = ticket.message, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}
