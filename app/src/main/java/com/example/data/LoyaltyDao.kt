package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LoyaltyDao {

    @Query("SELECT * FROM campaigns ORDER BY id DESC")
    fun getCampaigns(): Flow<List<Campaign>>

    @Query("SELECT * FROM campaigns WHERE id = :id LIMIT 1")
    suspend fun getCampaignById(id: Int): Campaign?

    @Query("SELECT * FROM campaigns WHERE discountCode = :code LIMIT 1")
    suspend fun getCampaignByCode(code: String): Campaign?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: Campaign): Long

    @Query("SELECT * FROM user_coupons")
    fun getUserCoupons(): Flow<List<UserCoupon>>

    @Query("SELECT * FROM user_coupons WHERE campaignId = :campaignId LIMIT 1")
    suspend fun getUserCouponByCampaign(campaignId: Int): UserCoupon?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserCoupon(coupon: UserCoupon): Long

    @Query("UPDATE user_coupons SET status = :status WHERE campaignId = :campaignId")
    suspend fun updateUserCouponStatus(campaignId: Int, status: String)

    @Query("SELECT * FROM loyalty_cards")
    fun getLoyaltyCards(): Flow<List<LoyaltyCard>>

    @Query("SELECT * FROM loyalty_cards WHERE campaignId = :campaignId LIMIT 1")
    suspend fun getLoyaltyCardByCampaign(campaignId: Int): LoyaltyCard?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoyaltyCard(card: LoyaltyCard): Long

    @Query("UPDATE loyalty_cards SET stampsCount = :stamps, points = :points WHERE campaignId = :campaignId")
    suspend fun updateLoyaltyCard(campaignId: Int, stamps: Int, points: Int)

    @Query("SELECT * FROM geofence_logs ORDER BY timestamp DESC")
    fun getGeofenceLogs(): Flow<List<GeofenceLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeofenceLog(log: GeofenceLog): Long

    @Query("DELETE FROM campaigns")
    suspend fun clearCampaigns()

    @Query("DELETE FROM user_coupons")
    suspend fun clearUserCoupons()

    @Query("DELETE FROM loyalty_cards")
    suspend fun clearLoyaltyCards()

    @Query("DELETE FROM geofence_logs")
    suspend fun clearGeofenceLogs()
}
