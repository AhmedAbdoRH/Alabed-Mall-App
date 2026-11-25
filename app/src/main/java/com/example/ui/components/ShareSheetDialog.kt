package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sharing.MallShareContent
import com.example.sharing.MallShareHelper
import com.example.sharing.ShareTarget
import com.example.ui.theme.ElAbedRoyalBlue
import com.example.ui.theme.ElAbedSurfaceVariant
import com.example.ui.theme.ElAbedVibrantGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSheetDialog(
    content: MallShareContent?,
    onDismiss: () -> Unit
) {
    if (content == null) return
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = null,
                    tint = ElAbedRoyalBlue
                )
                Text(
                    text = "مشاركة بصورة ورابط",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ElAbedSurfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = content.badge,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElAbedRoyalBlue
                    )
                    Text(
                        text = content.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = content.floorOrLocation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!content.couponCode.isNullOrBlank()) {
                        Text(
                            text = "كود الخصم: ${content.couponCode}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = ElAbedVibrantGreen
                        )
                    }
                }
            }

            Button(
                onClick = {
                    MallShareHelper.shareContent(context, ShareTarget.WHATSAPP, content)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1F9D55),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("share_whatsapp_button")
            ) {
                Icon(Icons.Filled.Chat, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "مشاركة على واتساب (WhatsApp)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    MallShareHelper.shareContent(context, ShareTarget.FACEBOOK, content)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1877F2),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("share_facebook_button")
            ) {
                Icon(Icons.Filled.Public, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "مشاركة على فيسبوك (Facebook)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = {
                    MallShareHelper.shareContent(context, ShareTarget.GENERAL, content)
                    onDismiss()
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("share_more_button")
            ) {
                Icon(Icons.Filled.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "المشاركة عبر تطبيقات أخرى",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
