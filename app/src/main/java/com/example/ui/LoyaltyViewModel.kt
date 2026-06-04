package com.example.ui

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LoyaltyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LoyaltyRepository
    val context: Context = application.applicationContext

    // DB flows
    val campaigns: StateFlow<List<Campaign>>
    val userCoupons: StateFlow<List<UserCoupon>>
    val loyaltyCards: StateFlow<List<LoyaltyCard>>
    val geofenceLogs: StateFlow<List<GeofenceLog>>

    // Simulated interactive location states (User can drag avatar)
    var userLatitude by mutableStateOf(250f) // Center of simulated map canvas (0-500)
    var userLongitude by mutableStateOf(250f)

    // Scanning & Claiming overlays
    var scannedCampaign by mutableStateOf<Campaign?>(null)
    var showScanSuccessDialog by mutableStateOf(false)

    // Current nearby campaign that triggered geofencing
    var nearCampaign by mutableStateOf<Campaign?>(null)

    // Track campaign IDs that have already triggered a push alert to prevent spam
    private val notifiedCampaignIds = mutableSetOf<Int>()

    // Notification channel constants
    private val CHANNEL_ID = "geofence_channel"
    private val NOTIFICATION_ID_BASE = 1000

    init {
        val database = AppDatabase.getDatabase(context)
        repository = LoyaltyRepository(database)

        campaigns = repository.campaigns.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        userCoupons = repository.userCoupons.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        loyaltyCards = repository.loyaltyCards.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        geofenceLogs = repository.geofenceLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Prepopulate defaults and trigger notification setup
        createNotificationChannel()
        viewModelScope.launch {
            repository.populateDefaultsIfNeeded()
        }
    }

    // Interactive scanner: Simulate QR Scan
    fun simulateQRScan(qrContent: String) {
        viewModelScope.launch {
            // Find campaign with matching code
            val matchingCampaign = campaigns.value.find { it.discountCode == qrContent }
            if (matchingCampaign != null) {
                val claimed = repository.scanAndClaim(matchingCampaign.id)
                if (claimed) {
                    scannedCampaign = matchingCampaign
                    showScanSuccessDialog = true
                } else {
                    // Already claimed message or normal behavior
                    scannedCampaign = matchingCampaign
                    showScanSuccessDialog = true
                }
            }
        }
    }

    // Add scanned coupon to simulated wallet
    fun addCouponToWallet(campaignId: Int) {
        viewModelScope.launch {
            repository.addToWallet(campaignId)
            showScanSuccessDialog = false
            scannedCampaign = null
        }
    }

    // Redeem coupon from Wallet -> Updates status and increments Loyalty card stamp!
    fun redeemWalletCoupon(campaignId: Int) {
        viewModelScope.launch {
            repository.redeemCoupon(campaignId)
        }
    }

    // Updates user position on the simulated map and evaluates geofencing triggers
    fun updateUserPosition(x: Float, y: Float) {
        userLatitude = x
        userLongitude = y
        checkGeofencing(x, y)
    }

    // Checks proximity to campaigns. If user enters radius, alert is fired.
    private fun checkGeofencing(x: Float, y: Float) {
        val activeCampaigns = campaigns.value
        var currentlyNear: Campaign? = null

        for (camp in activeCampaigns) {
            val dist = Math.hypot((x - camp.latitude).toDouble(), (y - camp.longitude).toDouble()).toFloat()
            if (dist <= camp.geofenceRadius) {
                currentlyNear = camp
                break
            }
        }

        nearCampaign = currentlyNear

        if (currentlyNear != null) {
            // Check if user coupon is added to wallet AND not redeemed
            val userCoupon = userCoupons.value.find { it.campaignId == currentlyNear.id }
            val hasActiveCouponInWallet = userCoupon != null && userCoupon.status == "added_to_wallet"

            if (hasActiveCouponInWallet && !notifiedCampaignIds.contains(currentlyNear.id)) {
                // Trigger real local notification
                sendProximityNotification(currentlyNear)
                notifiedCampaignIds.add(currentlyNear.id)

                // Log the trigger in database
                viewModelScope.launch {
                    repository.logGeofenceTrigger(
                        campaignId = currentlyNear.id,
                        merchantName = currentlyNear.merchantName,
                        msg = "Alert: You are near ${currentlyNear.merchantName}! Utilize your ${currentlyNear.couponTitle} coupon from your Wallet.",
                        msgAr = "تنبيه: أنت بالقرب من ${currentlyNear.merchantNameAr}! لا تنسَ استخدام كوبون المرفق بمحفظتك: ${currentlyNear.couponTitleAr} ☕"
                    )
                }
            }
        } else {
            // If user walks outside all geofences, reset notification triggers to allow alerts next time they enter
            if (notifiedCampaignIds.isNotEmpty()) {
                // Check if user is truly far away from all previously notified
                var farFromAll = true
                for (campId in notifiedCampaignIds) {
                    val camp = activeCampaigns.find { it.id == campId }
                    if (camp != null) {
                        val dist = Math.hypot((x - camp.latitude).toDouble(), (y - camp.longitude).toDouble()).toFloat()
                        if (dist <= camp.geofenceRadius + 20f) { // hysteresis
                            farFromAll = false
                        }
                    }
                }
                if (farFromAll) {
                    notifiedCampaignIds.clear()
                }
            }
        }
    }

    // Create campaigns (Merchant mode)
    fun createMerchantCampaign(
        name: String,
        nameAr: String,
        title: String,
        titleAr: String,
        desc: String,
        descAr: String,
        code: String,
        radius: Float = 120f,
        color: String = "#795548"
    ) {
        viewModelScope.launch {
            // Random position in safe map canvas
            val randomX = (100..400).random().toFloat()
            val randomY = (100..400).random().toFloat()

            val newCamp = Campaign(
                merchantName = name,
                merchantNameAr = nameAr,
                couponTitle = title,
                couponTitleAr = titleAr,
                couponDescription = desc,
                couponDescriptionAr = descAr,
                discountCode = code,
                latitude = randomX,
                longitude = randomY,
                geofenceRadius = radius,
                colorHex = color
            )
            repository.insertCampaign(newCamp)
        }
    }

    // Clear all simulation logs
    fun resetSimulation() {
        viewModelScope.launch {
            repository.clearAllData()
            notifiedCampaignIds.clear()
            userLatitude = 250f
            userLongitude = 250f
            nearCampaign = null
        }
    }

    // Trigger local device system notification
    private fun sendProximityNotification(campaign: Campaign) {
        val titleText = "📍 قسيمة ولاء قريبة! Inside Geofence"
        val bodyText = "أنت قريب من ${campaign.merchantNameAr}! افتح محفظتك لاستخدام القسيمة ☕"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat) // default system drawable for safe compatibility
            .setContentTitle(titleText)
            .setContentText(campaign.couponTitleAr)
            .setStyle(NotificationCompat.BigTextStyle().bigText("${campaign.couponTitleAr}\n${campaign.couponDescriptionAr}\n📍 ${campaign.merchantNameAr}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify(NOTIFICATION_ID_BASE + campaign.id, builder.build())
        } catch (e: Exception) {
            android.util.Log.e("LoyaltyViewModel", "System notification failed to deliver", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Proximity Wallet Alerts"
            val descriptionText = "Triggers local notifications when user simulated location enters café geofences"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
