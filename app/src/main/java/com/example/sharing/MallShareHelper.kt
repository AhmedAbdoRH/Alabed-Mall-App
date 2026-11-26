package com.example.sharing

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.map.HypermarketMapCatalog
import com.example.data.map.MallFloorPlanGraph
import com.example.data.model.OfferEntity
import com.example.data.model.ShoppingTaskEntity
import com.example.data.model.StoreEntity
import java.io.File
import java.io.FileOutputStream

enum class ShareTarget {
    WHATSAPP,
    FACEBOOK,
    GENERAL
}

data class MallShareContent(
    val id: String,
    val badge: String,
    val title: String,
    val subtitle: String,
    val details: String,
    val couponCode: String? = null,
    val floorOrLocation: String,
    val shareLink: String = MallFloorPlanGraph.MALL_GOOGLE_MAPS_URL
) {
    fun buildShareText(): String = buildString {
        appendLine("✨ $title — مول العابد (طوخ)")
        if (subtitle.isNotBlank()) appendLine(subtitle)
        if (details.isNotBlank()) appendLine(details)
        if (!couponCode.isNullOrBlank()) appendLine("🎟️ كود الخصم: $couponCode")
        appendLine("📍 الموقع: $floorOrLocation")
        append("🗺️ رابط مول العابد على الخرائط: $shareLink")
    }

    companion object {
        fun forStore(store: StoreEntity): MallShareContent {
            return MallShareContent(
                id = "store_${store.id}",
                badge = store.category.labelAr,
                title = store.nameAr,
                subtitle = store.nameEn,
                details = store.descriptionAr,
                floorOrLocation = "${store.floorLabelAr} • ${store.zoneAr}"
            )
        }

        fun forOffer(offer: OfferEntity, store: StoreEntity? = null): MallShareContent {
            val loc = if (store != null) {
                "${store.floorLabelAr} • ${store.zoneAr}"
            } else {
                offer.validUntilAr
            }
            return MallShareContent(
                id = "offer_${offer.id}",
                badge = offer.discountBadge,
                title = offer.titleAr,
                subtitle = offer.storeNameAr,
                details = "${offer.descriptionAr} (${offer.validUntilAr})",
                couponCode = offer.couponCode,
                floorOrLocation = loc
            )
        }
    }
}

object MallShareHelper {

    fun shareContent(
        context: Context,
        target: ShareTarget,
        content: MallShareContent
    ) {
        val imageUri = generateShareCardUri(context, content)
        val shareText = content.buildShareText()

        val baseIntent = Intent(Intent.ACTION_SEND).apply {
            type = if (imageUri != null) "image/png" else "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "${content.title} - مول العابد")
            putExtra(Intent.EXTRA_TEXT, shareText)
            if (imageUri != null) {
                putExtra(Intent.EXTRA_STREAM, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }

        val packages = when (target) {
            ShareTarget.WHATSAPP -> listOf("com.whatsapp", "com.whatsapp.w4b")
            ShareTarget.FACEBOOK -> listOf("com.facebook.katana", "com.facebook.lite")
            ShareTarget.GENERAL -> emptyList()
        }

        if (packages.isNotEmpty()) {
            val installedPkg = packages.firstOrNull { isPackageInstalled(context, it) }
            if (installedPkg != null) {
                val targeted = Intent(baseIntent).apply { setPackage(installedPkg) }
                try {
                    context.startActivity(targeted)
                    return
                } catch (_: Exception) {
                    // Fallback to system chooser below
                }
            } else {
                val appLabel = if (target == ShareTarget.WHATSAPP) "واتساب" else "فيسبوك"
                Toast.makeText(
                    context,
                    "تطبيق $appLabel غير مثبت على الجهاز، تم فتح قائمة المشاركة العامة.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val chooser = Intent.createChooser(baseIntent, "مشاركة عبر...")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(chooser)
        } catch (_: Exception) {
            Toast.makeText(context, "لا يوجد تطبيق مشاركة متاح حالياً.", Toast.LENGTH_SHORT).show()
        }
    }

    fun buildFormattedShoppingListText(tasks: List<ShoppingTaskEntity>): String {
        val sorted = HypermarketMapCatalog.sortTasksByMapOrder(tasks)
        val pending = sorted.filter { !it.isChecked }
        val completed = sorted.filter { it.isChecked }

        return buildString {
            appendLine("🛒 قائمة المهام والمشتريات — مول العابد")
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            if (pending.isEmpty() && completed.isEmpty()) {
                append("القائمة فارغة حالياً.")
                return@buildString
            }

            if (pending.isNotEmpty()) {
                appendLine("📌 مطلوب شراؤه (${pending.size}):")
                pending.forEachIndexed { idx, task ->
                    val section = HypermarketMapCatalog.sectionForTask(task)
                    val sectionHint = section?.let { " [${it.routeOrder}. ${it.nameAr}]" } ?: ""
                    val storeHint = task.storeNameAr?.takeIf { it.isNotBlank() }?.let { " — $it" } ?: ""
                    appendLine("${idx + 1}. ⬜ ${task.itemName}$sectionHint$storeHint")
                }
            }

            if (completed.isNotEmpty()) {
                if (pending.isNotEmpty()) appendLine()
                appendLine("✅ تم إنجازه (${completed.size}):")
                completed.forEach { task ->
                    appendLine("• ✔️ ${task.itemName}")
                }
            }
        }.trimEnd()
    }

    fun shareFormattedTextToWhatsApp(
        context: Context,
        subject: String,
        formattedText: String
    ) {
        if (formattedText.isBlank()) {
            Toast.makeText(context, "القائمة فارغة للمشاركة.", Toast.LENGTH_SHORT).show()
            return
        }
        val baseIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, formattedText)
        }

        val packages = listOf("com.whatsapp", "com.whatsapp.w4b")
        val installedPkg = packages.firstOrNull { isPackageInstalled(context, it) }
        if (installedPkg != null) {
            val targeted = Intent(baseIntent).apply { setPackage(installedPkg) }
            try {
                context.startActivity(targeted)
                return
            } catch (_: Exception) {
                // Fallback to system chooser
            }
        } else {
            Toast.makeText(
                context,
                "تطبيق واتساب غير مثبت على الجهاز، تم فتح قائمة المشاركة العامة.",
                Toast.LENGTH_SHORT
            ).show()
        }

        val chooser = Intent.createChooser(baseIntent, "مشاركة القائمة عبر...")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(chooser)
        } catch (_: Exception) {
            Toast.makeText(context, "لا يوجد تطبيق مشاركة متاح حالياً.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun generateShareCardUri(context: Context, content: MallShareContent): Uri? {
        return try {
            val dir = File(context.cacheDir, "shared_cards").apply { mkdirs() }
            val safeId = content.id.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val file = File(dir, "share_${safeId}.png")

            val width = 1080
            val height = 1080
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Gradient background
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    intArrayOf(
                        Color.parseColor("#18317A"),
                        Color.parseColor("#28459B"),
                        Color.parseColor("#1D3782")
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Subtle decorative circles
            val decorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(28, 255, 255, 255)
            }
            canvas.drawCircle(160f, 140f, 220f, decorPaint)
            canvas.drawCircle(960f, 940f, 260f, decorPaint)

            // White inner card
            val cardRect = RectF(64f, 64f, width - 64f, height - 64f)
            val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
            }
            canvas.drawRoundRect(cardRect, 48f, 48f, cardPaint)

            // Top brand bar inside card
            val headerRect = RectF(64f, 64f, width - 64f, 260f)
            val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    64f, 64f, width - 64f, 260f,
                    Color.parseColor("#28459B"),
                    Color.parseColor("#EB8C28"),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.save()
            val cardClipPath = android.graphics.Path().apply {
                addRoundRect(cardRect, 48f, 48f, android.graphics.Path.Direction.CW)
            }
            canvas.clipPath(cardClipPath)
            canvas.drawRect(headerRect, headerPaint)
            canvas.restore()

            // Mall Logo on white pill at top-right (RTL visual start)
            val logoBgRect = RectF(width - 244f, 96f, width - 104f, 236f)
            canvas.drawRoundRect(logoBgRect, 28f, 28f, cardPaint)
            val logoBmp = BitmapFactory.decodeResource(context.resources, R.drawable.logo_elabed_mall)
            if (logoBmp != null) {
                val logoDst = RectF(logoBgRect.left + 14f, logoBgRect.top + 14f, logoBgRect.right - 14f, logoBgRect.bottom - 14f)
                canvas.drawBitmap(logoBmp, null, logoDst, Paint(Paint.FILTER_BITMAP_FLAG))
            }

            // Mall title inside top header
            val headerTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 44f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            drawRtlText(canvas, "العابد مول • طوخ", headerTitlePaint, 108f, 118f, (width - 380).toInt())

            val headerSubPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#EEF3FF")
                textSize = 28f
            }
            drawRtlText(canvas, "كيلو ٣٧ طريق الإسكندرية الزراعي", headerSubPaint, 108f, 182f, (width - 380).toInt())

            // Badge pill
            val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFE5CC")
            }
            val badgeTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#8A4300")
                textSize = 34f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val badgeWidth = (badgeTextPaint.measureText(content.badge) + 72f).coerceAtMost(width - 220f)
            val badgeRect = RectF(width - 110f - badgeWidth, 305f, width - 110f, 375f)
            canvas.drawRoundRect(badgeRect, 35f, 35f, badgePaint)
            drawRtlText(canvas, content.badge, badgeTextPaint, badgeRect.left + 20f, 319f, (badgeWidth - 40f).toInt())

            // Subtitle (Store name)
            val subPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#28459B")
                textSize = 36f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            drawRtlText(canvas, content.subtitle, subPaint, 110f, 405f, width - 220)

            // Main Title
            val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#141926")
                textSize = 52f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val titleBottom = drawRtlText(canvas, content.title, titlePaint, 110f, 468f, width - 220)

            // Description
            val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#475166")
                textSize = 33f
            }
            val descStartY = (titleBottom + 24f).coerceAtMost(640f)
            drawRtlText(canvas, content.details.take(150), bodyPaint, 110f, descStartY, width - 220)

            // Coupon Box if present
            if (!content.couponCode.isNullOrBlank()) {
                val couponRect = RectF(110f, 755f, width - 110f, 845f)
                val couponBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#DCE4FF")
                }
                canvas.drawRoundRect(couponRect, 24f, 24f, couponBg)
                val couponTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#0A1B54")
                    textSize = 38f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                drawRtlText(
                    canvas,
                    "كود الخصم: ${content.couponCode}",
                    couponTextPaint,
                    140f,
                    777f,
                    width - 280
                )
            }

            // Footer strip
            val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#129E4B")
                textSize = 30f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            drawRtlText(canvas, "📍 ${content.floorOrLocation}", footerPaint, 110f, 885f, width - 220)

            val linkPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#525B70")
                textSize = 26f
            }
            drawRtlText(canvas, "افتح الموقع على خرائط Google عبر الرابط المرفق", linkPaint, 110f, 940f, width - 220)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
            bitmap.recycle()

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (_: Exception) {
            null
        }
    }

    private fun drawRtlText(
        canvas: Canvas,
        text: String,
        paint: TextPaint,
        left: Float,
        top: Float,
        maxWidth: Int
    ): Float {
        if (text.isBlank() || maxWidth <= 0) return top
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, maxWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .setLineSpacing(6f, 1f)
            .setMaxLines(3)
            .build()
        canvas.save()
        canvas.translate(left, top)
        layout.draw(canvas)
        canvas.restore()
        return top + layout.height
    }
}
