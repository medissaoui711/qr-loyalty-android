package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. CAMPAIGN ENTITY
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
    val pointsPerRedeem: Int = 1,
    val latitude: Float, // Map X coordinates (0-500 canvas area)
    val longitude: Float, // Map Y coordinates (0-500 canvas area)
    val geofenceRadius: Float = 120f, // Geofence radius in pixels
    val colorHex: String = "#8D6E63" // Brown coffee tone
)

// 2. USER COUPON ENTITY
@Entity(
    tableName = "user_coupons",
    foreignKeys = [
        ForeignKey(
            entity = Campaign::class,
            parentColumns = ["id"],
            childColumns = ["campaignId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["campaignId"])]
)
data class UserCoupon(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val campaignId: Int,
    val couponCode: String,
    val status: String, // "scanned" | "added_to_wallet" | "redeemed"
    val scannedAt: Long = System.currentTimeMillis(),
    val redeemedAt: Long? = null
)

// 3. LOYALTY CARD ENTITY
@Entity(
    tableName = "loyalty_cards",
    foreignKeys = [
        ForeignKey(
            entity = Campaign::class,
            parentColumns = ["id"],
            childColumns = ["campaignId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["campaignId"])]
)
data class LoyaltyCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val campaignId: Int,
    val stampsCount: Int = 0,
    val maxStamps: Int = 5,
    val points: Int = 0,
    val lastUpdatedAt: Long = System.currentTimeMillis()
)

// 4. GEOFENCE LOG ENTITY
@Entity(tableName = "geofence_logs")
data class GeofenceLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val campaignId: Int,
    val merchantName: String,
    val message: String,
    val messageAr: String,
    val timestamp: Long = System.currentTimeMillis()
)

// DAOs
@Dao
interface CampaignDao {
    @Query("SELECT * FROM campaigns")
    fun getAllCampaigns(): Flow<List<Campaign>>

    @Query("SELECT * FROM campaigns WHERE id = :id LIMIT 1")
    suspend fun getCampaignById(id: Int): Campaign?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: Campaign): Long

    @Query("DELETE FROM campaigns WHERE id = :id")
    suspend fun deleteCampaignById(id: Int)
}

@Dao
interface UserCouponDao {
    @Query("SELECT * FROM user_coupons")
    fun getAllUserCoupons(): Flow<List<UserCoupon>>

    @Query("SELECT * FROM user_coupons WHERE campaignId = :campaignId LIMIT 1")
    suspend fun getCouponByCampaignId(campaignId: Int): UserCoupon?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserCoupon(coupon: UserCoupon): Long

    @Update
    suspend fun updateUserCoupon(coupon: UserCoupon)

    @Query("DELETE FROM user_coupons")
    suspend fun clearAllCoupons()
}

@Dao
interface LoyaltyCardDao {
    @Query("SELECT * FROM loyalty_cards")
    fun getAllLoyaltyCards(): Flow<List<LoyaltyCard>>

    @Query("SELECT * FROM loyalty_cards WHERE campaignId = :campaignId LIMIT 1")
    suspend fun getLoyaltyCardByCampaignId(campaignId: Int): LoyaltyCard?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoyaltyCard(card: LoyaltyCard): Long

    @Update
    suspend fun updateLoyaltyCard(card: LoyaltyCard)

    @Query("DELETE FROM loyalty_cards")
    suspend fun clearAllLoyaltyCards()
}

@Dao
interface GeofenceLogDao {
    @Query("SELECT * FROM geofence_logs ORDER BY timestamp DESC")
    fun getAllGeofenceLogs(): Flow<List<GeofenceLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: GeofenceLog)

    @Query("DELETE FROM geofence_logs")
    suspend fun clearLogs()
}

// 5. DATABASE HOLDER
@Database(
    entities = [Campaign::class, UserCoupon::class, LoyaltyCard::class, GeofenceLog::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun campaignDao(): CampaignDao
    abstract fun userCouponDao(): UserCouponDao
    abstract fun loyaltyCardDao(): LoyaltyCardDao
    abstract fun geofenceLogDao(): GeofenceLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "loyalty_wallet_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
