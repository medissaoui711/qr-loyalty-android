package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "campaigns")
data class Campaign(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val merchantName: String,
    val merchantNameAr: String,
    val couponTitle: String,
    val couponTitleAr: String,
    val couponDescription: String,
    val couponDescriptionAr: String,
    val discountCode: String,
    val latitude: Float,
    val longitude: Float,
    val geofenceRadius: Float,
    val colorHex: String,
    val pointsPerRedeem: Int = 1
)

@Entity(tableName = "user_coupons")
data class UserCoupon(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val campaignId: Int,
    val couponCode: String,
    val status: String // "added_to_wallet", "redeemed"
)

@Entity(tableName = "loyalty_cards")
data class LoyaltyCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val campaignId: Int,
    val stampsCount: Int,
    val maxStamps: Int,
    val points: Int
)

@Entity(tableName = "geofence_logs")
data class GeofenceLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val campaignId: Int,
    val merchantName: String,
    val message: String,
    val messageAr: String,
    val timestamp: Long = System.currentTimeMillis()
)
