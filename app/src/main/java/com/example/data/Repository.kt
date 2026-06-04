package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class LoyaltyRepository(private val db: AppDatabase) {

    val campaigns: Flow<List<Campaign>> = db.campaignDao().getAllCampaigns()
    val userCoupons: Flow<List<UserCoupon>> = db.userCouponDao().getAllUserCoupons()
    val loyaltyCards: Flow<List<LoyaltyCard>> = db.loyaltyCardDao().getAllLoyaltyCards()
    val geofenceLogs: Flow<List<GeofenceLog>> = db.geofenceLogDao().getAllGeofenceLogs()

    suspend fun getCampaignById(id: Int): Campaign? {
        return db.campaignDao().getCampaignById(id)
    }

    suspend fun insertCampaign(campaign: Campaign): Long {
        return db.campaignDao().insertCampaign(campaign)
    }

    suspend fun deleteCampaign(id: Int) {
        db.campaignDao().deleteCampaignById(id)
    }

    // Triggered when scanning QR
    suspend fun scanAndClaim(campaignId: Int): Boolean {
        val existing = db.userCouponDao().getCouponByCampaignId(campaignId)
        if (existing != null) {
            // Already claimed, do not overwrite unless redeemed
            if (existing.status == "redeemed") {
                // Let user claim a new one
                val newCoupon = UserCoupon(
                    campaignId = campaignId,
                    couponCode = "COUPON_${campaignId}_${System.currentTimeMillis() % 10000}",
                    status = "scanned"
                )
                db.userCouponDao().insertUserCoupon(newCoupon)
                return true
            }
            return false // Already active/in wallet
        }
        val campaign = db.campaignDao().getCampaignById(campaignId) ?: return false
        val newCoupon = UserCoupon(
            campaignId = campaignId,
            couponCode = campaign.discountCode,
            status = "scanned"
        )
        db.userCouponDao().insertUserCoupon(newCoupon)
        return true
    }

    // Triggered when clicking "Add to Wallet"
    suspend fun addToWallet(campaignId: Int) {
        val existing = db.userCouponDao().getCouponByCampaignId(campaignId)
        if (existing != null) {
            val updated = existing.copy(status = "added_to_wallet")
            db.userCouponDao().updateUserCoupon(updated)
        } else {
            // Safe fallback
            val campaign = db.campaignDao().getCampaignById(campaignId) ?: return
            val coupon = UserCoupon(
                campaignId = campaignId,
                couponCode = campaign.discountCode,
                status = "added_to_wallet"
            )
            db.userCouponDao().insertUserCoupon(coupon)
        }
    }

    // Triggered when coupon is redeemed (Simulates cashier redemption scanner scan)
    // Automatically converts to a stamp card point!
    suspend fun redeemCoupon(campaignId: Int): Boolean {
        val existingCoupon = db.userCouponDao().getCouponByCampaignId(campaignId)
        if (existingCoupon == null || existingCoupon.status != "added_to_wallet") {
            return false // Must be in wallet to redeem
        }

        // 1. Mark coupon as Redeemed
        val redeemedCoupon = existingCoupon.copy(
            status = "redeemed",
            redeemedAt = System.currentTimeMillis()
        )
        db.userCouponDao().updateUserCoupon(redeemedCoupon)

        // 2. Increment Stamps in Loyalty Card
        val existingCard = db.loyaltyCardDao().getLoyaltyCardByCampaignId(campaignId)
        if (existingCard == null) {
            // Create first stamp
            val newCard = LoyaltyCard(
                campaignId = campaignId,
                stampsCount = 1,
                maxStamps = 5,
                points = 100, // starting points
                lastUpdatedAt = System.currentTimeMillis()
            )
            db.loyaltyCardDao().insertLoyaltyCard(newCard)
        } else {
            var newStamps = existingCard.stampsCount + 1
            var msg = ""
            if (newStamps > existingCard.maxStamps) {
                // Reset card, reward collected
                newStamps = 1 // reset stamps, back to 1
                db.loyaltyCardDao().updateLoyaltyCard(
                    existingCard.copy(
                        stampsCount = newStamps,
                        points = existingCard.points + 250, // bonus reward points
                        lastUpdatedAt = System.currentTimeMillis()
                    )
                )
            } else {
                db.loyaltyCardDao().updateLoyaltyCard(
                    existingCard.copy(
                        stampsCount = newStamps,
                        points = existingCard.points + 100,
                        lastUpdatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
        return true
    }

    // For Geofencing simulation
    suspend fun logGeofenceTrigger(campaignId: Int, merchantName: String, msg: String, msgAr: String) {
        val log = GeofenceLog(
            campaignId = campaignId,
            merchantName = merchantName,
            message = msg,
            messageAr = msgAr,
            timestamp = System.currentTimeMillis()
        )
        db.geofenceLogDao().insertLog(log)
    }

    suspend fun clearAllData() {
        db.userCouponDao().clearAllCoupons()
        db.loyaltyCardDao().clearAllLoyaltyCards()
        db.geofenceLogDao().clearLogs()
    }

    // Populate default campaigns if database is empty
    suspend fun populateDefaultsIfNeeded() {
        val existingCampaigns = db.campaignDao().getAllCampaigns().firstOrNull() ?: emptyList()
        if (existingCampaigns.isEmpty()) {
            val defaults = listOf(
                Campaign(
                    id = 1,
                    merchantName = "Brew Haven Specialty Coffee",
                    merchantNameAr = "بريو هيفن للقهوة المختصة",
                    couponTitle = "Free Double Espresso Shot",
                    couponTitleAr = "إسبريسو مضاعف مجاني ☕",
                    couponDescription = "Get a free luxury double shot espresso using high-scoring Ethiopian single-origin beans.",
                    couponDescriptionAr = "احصل على كوب إسبريسو مضاعف مجاناً ومحضّر من خامات إثيوبية فاخرة.",
                    discountCode = "BREWHAVENFREE",
                    latitude = 180f,
                    longitude = 160f,
                    geofenceRadius = 110f,
                    colorHex = "#2E1C0C" // Rich Coffee Dark
                ),
                Campaign(
                    id = 2,
                    merchantName = "Golden Crema Roastery",
                    merchantNameAr = "محمصة الكريمة الذهبية",
                    couponTitle = "25% Off Saffron Matcha Latte",
                    couponTitleAr = "خصم ٢٥٪ لاتيه السافران والماتشا",
                    couponDescription = "Experience our signature cream latte infused with cold-pressed saffron extract and organic matcha.",
                    couponDescriptionAr = "جرّب لاتيه الكريمة الفاخر الممزوج بخلاصة الزعفران العضوي والماركا.",
                    discountCode = "GOLDENCREMA25",
                    latitude = 360f,
                    longitude = 180f,
                    geofenceRadius = 120f,
                    colorHex = "#B8860B" // Dark Goldenrod
                ),
                Campaign(
                    id = 3,
                    merchantName = "Glitch Cyber Cafe",
                    merchantNameAr = "جليتش كافيه الرقمي",
                    couponTitle = "Buy 1 Get 1 Nitro Brew",
                    couponTitleAr = "اشتر كولد برو واحصل على الآخر مجاناً",
                    couponDescription = "Cyber-infused cold nitrogenated brew with robust notes of chocolate and visual glitter syrup.",
                    couponDescriptionAr = "كولد برو بالنيتروجين مشبّع بنكهة الشوكولاتة الداكنة للحصول على طاقة مضاعفة.",
                    discountCode = "GLITCHBOGO",
                    latitude = 140f,
                    longitude = 360f,
                    geofenceRadius = 115f,
                    colorHex = "#311B92" // Dark Indigo
                )
            )
            for (camp in defaults) {
                db.campaignDao().insertCampaign(camp)
            }
        }
    }
}
