package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MallPlaceCategory(
    val labelAr: String,
    val isServiceOnly: Boolean = false
) {
    HYPERMARKET("هايبر ماركت"),
    ELECTRONICS("إلكترونيات"),
    FASHION("ملابس وأزياء"),
    FURNITURE("أثاث ومفروشات"),
    DINING("مطاعم وكافيهات"),
    ENTERTAINMENT("ترفيه وأطفال"),
    SERVICES("خدمات مالية وصحية"),
    FACILITY("مرافق وبوابات", isServiceOnly = true);

    companion object {
        val interestCategories: List<MallPlaceCategory>
            get() = entries.filter { !it.isServiceOnly }
    }
}

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey val id: String,
    val nameAr: String,
    val nameEn: String,
    val categoryName: String,
    val floor: Int, // 0 = Ground Floor (الدور الأرضي), 1 = First Floor (الدور الأول)
    val zoneAr: String,
    val descriptionAr: String,
    val hoursAr: String,
    val phone: String,
    val mapX: Float,
    val mapY: Float,
    val width: Float = 90f,
    val height: Float = 60f,
    val isServiceFacility: Boolean = false,
    val isFeatured: Boolean = false,
    val isNewOpening: Boolean = false
) {
    val category: MallPlaceCategory
        get() = MallPlaceCategory.entries.firstOrNull { it.name == categoryName }
            ?: MallPlaceCategory.SERVICES

    val floorLabelAr: String
        get() = if (floor == 0) "الدور الأرضي" else "الدور الأول"
}

@Entity(tableName = "offers")
data class OfferEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val storeNameAr: String,
    val titleAr: String,
    val descriptionAr: String,
    val discountBadge: String,
    val couponCode: String,
    val validUntilAr: String,
    val termsAr: String,
    val isFeatured: Boolean = true
)

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val titleAr: String,
    val descriptionAr: String,
    val dateRangeAr: String,
    val timeAr: String,
    val locationPlaceId: String,
    val locationLabelAr: String,
    val floor: Int,
    val isNewOpening: Boolean = false,
    val badgeAr: String
)

enum class NotificationCategory(val labelAr: String) {
    OFFER("عروض وخصومات"),
    OPENING("افتتاحات جديدة"),
    EVENT("مهرجانات وفعاليات"),
    ALERT("تنبيهات المول")
}

@Entity(tableName = "notifications")
data class MallNotificationEntity(
    @PrimaryKey val id: String,
    val titleAr: String,
    val bodyAr: String,
    val categoryName: String,
    val timestampLabelAr: String,
    val linkedTargetId: String? = null,
    val linkedType: String = "MAP", // "MAP", "STORE", "OFFER", "EVENT"
    val isRead: Boolean = false
) {
    val category: NotificationCategory
        get() = NotificationCategory.entries.firstOrNull { it.name == categoryName }
            ?: NotificationCategory.ALERT
}

@Entity(tableName = "visitor_tickets")
data class VisitorTicketEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ticketType: String, // استفسار، ملاحظة، بلاغ
    val visitorName: String,
    val visitorPhone: String,
    val message: String,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val statusLabel: String = "تم الاستلام"
)

@Entity(tableName = "trip_stops")
data class TripStopEntity(
    @PrimaryKey val id: String, // e.g., "store_<storeId>" or "offer_<offerId>"
    val storeId: String,
    val offerId: String? = null,
    val titleAr: String,
    val subtitleAr: String,
    val badgeAr: String = "",
    val floor: Int,
    val mapX: Float,
    val mapY: Float,
    val isVisited: Boolean = false,
    val addedAtMillis: Long = System.currentTimeMillis()
) {
    val floorLabelAr: String
        get() = if (floor == 0) "الدور الأرضي" else "الدور الأول"
}

@Entity(tableName = "shopping_tasks")
data class ShoppingTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemName: String,
    val departmentTag: String = "عام",
    val storeId: String? = null,
    val storeNameAr: String? = null,
    val floor: Int? = null,
    val isChecked: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class PromoBannerConfig(
    val imagePathOrKey: String = DEFAULT_DRAWABLE_HYPERMARKET,
    val titleAr: String = "عروض الأسبوع في مول العابد",
    val subtitleAr: String = "خصومات حصرية على الهايبر ماركت والمطاعم والأزياء — تصفح العروض الآن",
    val badgeAr: String = "عرض ترويجي",
    val linkedTargetId: String = "hypermarket",
    val isVisible: Boolean = true
) {
    companion object {
        const val DEFAULT_DRAWABLE_HYPERMARKET = "drawable:img_hypermarket_banner"
        const val DEFAULT_DRAWABLE_EVENTS = "drawable:img_events_banner"
        const val DEFAULT_DRAWABLE_HERO = "drawable:img_mall_hero"
    }
}
