package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class LoyaltyRepository(private val database: AppDatabase) {

    val campaigns: Flow<List<Campaign>> = database.loyaltyDao.getCampaigns()
    val userCoupons: Flow<List<UserCoupon>> = database.loyaltyDao.getUserCoupons()
    val loyaltyCards: Flow<List<LoyaltyCard>> = database.loyaltyDao.getLoyaltyCards()
    val geofenceLogs: Flow<List<GeofenceLog>> = database.loyaltyDao.getGeofenceLogs()

    suspend fun populateDefaultsIfNeeded() {
        val currentCampaignList = campaigns.firstOrNull() ?: emptyList()
        if (currentCampaignList.isEmpty()) {
            val camp1 = Campaign(
                merchantName = "Brew Haven Coffee",
                merchantNameAr = "مقهى ملاذ القهوة (Brew Haven)",
                couponTitle = "25% OFF Cold Brew Signature",
                couponTitleAr = "خصم ٢٥٪ على مشروب الكولد برو الخاص",
                couponDescription = "Valid for single use in active dining. Elevate your morning with premium single-origin Ethiopian beans steeped over 18 hours.",
                couponDescriptionAr = "صالح للاستخدام الفردي في صالة المقهى بالكامل. استمتع ببداية يوم مميزة مع حبوب البن الإثيوبية الفاخرة التي يتم تحضيرها ببطء على مدار ١٨ ساعة.",
                discountCode = "BREW_WARRIOR",
                latitude = 180f,
                longitude = 160f,
                geofenceRadius = 100f,
                colorHex = "#795548"
            )
            val id1 = database.loyaltyDao.insertCampaign(camp1).toInt()
            database.loyaltyDao.insertLoyaltyCard(
                LoyaltyCard(campaignId = id1, stampsCount = 0, maxStamps = 5, points = 0)
            )

            val camp2 = Campaign(
                merchantName = "Golden Crema Coffee",
                merchantNameAr = "مقهى الكريمة الذهبية (Golden Crema)",
                couponTitle = "Buy 1 Get 1 Latte Espresso",
                couponTitleAr = "اشترِ كوباً واحداً واحصل على الآخر مجاناً",
                couponDescription = "Treat a friend today. Crafted by certified baristas using organic whole milk and full-bodied roasted Arabica.",
                couponDescriptionAr = "دلّل صديقك اليوم. مصنوع يدوياً بأيدي باريستا معتمدين ومستخلص من حليب عضوي طازج وحبوب قهوة أرابيكا كاملة النضج والغنية بالقوام.",
                discountCode = "CREMA_DOUBLE",
                latitude = 360f,
                longitude = 180f,
                geofenceRadius = 120f,
                colorHex = "#D4AF37"
            )
            val id2 = database.loyaltyDao.insertCampaign(camp2).toInt()
            database.loyaltyDao.insertLoyaltyCard(
                LoyaltyCard(campaignId = id2, stampsCount = 0, maxStamps = 5, points = 0)
            )

            val camp3 = Campaign(
                merchantName = "Glitch Cyber Café",
                merchantNameAr = "مقهى جليتش الإلكتروني (Glitch)",
                couponTitle = "Free Cronut with any V60 drip",
                couponTitleAr = "كرونات فستق مجاني مع أي كوب تقطير ☕",
                couponDescription = "Valid from midnight to 6 AM for gamers and night owls. Baked fresh hourly. Flaky pastry meets premium cardamom and pistachio glazes.",
                couponDescriptionAr = "صالح من منتصف الليل وحتى ٦ صباحاً لعشاق الألعاب والسهر. يخبز طازجاً كل ساعة. عجين مورق ومقرمش يمتزج بطلاء الفستق والزعفران والهيل الفاخر.",
                discountCode = "GLITCH_CTRL",
                latitude = 140f,
                longitude = 360f,
                geofenceRadius = 90f,
                colorHex = "#00E5FF"
            )
            val id3 = database.loyaltyDao.insertCampaign(camp3).toInt()
            database.loyaltyDao.insertLoyaltyCard(
                LoyaltyCard(campaignId = id3, stampsCount = 0, maxStamps = 5, points = 0)
            )
        }
    }

    suspend fun insertCampaign(campaign: Campaign) {
        val campaignId = database.loyaltyDao.insertCampaign(campaign).toInt()
        database.loyaltyDao.insertLoyaltyCard(
            LoyaltyCard(campaignId = campaignId, stampsCount = 0, maxStamps = 5, points = 0)
        )
    }

    suspend fun scanAndClaim(campaignId: Int): Boolean {
        val alreadyExists = database.loyaltyDao.getUserCouponByCampaign(campaignId)
        if (alreadyExists == null) {
            val campaign = database.loyaltyDao.getCampaignById(campaignId)
            val code = campaign?.discountCode ?: "COUPON_${(100..999).random()}"
            database.loyaltyDao.insertUserCoupon(
                UserCoupon(campaignId = campaignId, couponCode = code, status = "added_to_wallet")
            )
            return true
        }
        return false
    }

    suspend fun addToWallet(campaignId: Int) {
        val alreadyExists = database.loyaltyDao.getUserCouponByCampaign(campaignId)
        if (alreadyExists == null) {
            val campaign = database.loyaltyDao.getCampaignById(campaignId)
            val code = campaign?.discountCode ?: "COUPON_VAL"
            database.loyaltyDao.insertUserCoupon(
                UserCoupon(campaignId = campaignId, couponCode = code, status = "added_to_wallet")
            )
        } else {
            database.loyaltyDao.updateUserCouponStatus(campaignId, "added_to_wallet")
        }
    }

    suspend fun redeemCoupon(campaignId: Int) {
        database.loyaltyDao.updateUserCouponStatus(campaignId, "redeemed")
        val card = database.loyaltyDao.getLoyaltyCardByCampaign(campaignId) 
            ?: LoyaltyCard(campaignId = campaignId, stampsCount = 0, maxStamps = 5, points = 0)
        val nextStamps = card.stampsCount + 1
        val isReward = nextStamps >= card.maxStamps
        
        val updatedStamps = if (isReward) 0 else nextStamps
        val updatedPoints = card.points + 100
        
        database.loyaltyDao.insertLoyaltyCard(
            card.copy(stampsCount = updatedStamps, points = updatedPoints)
        )
    }

    suspend fun logGeofenceTrigger(campaignId: Int, merchantName: String, msg: String, msgAr: String) {
        database.loyaltyDao.insertGeofenceLog(
            GeofenceLog(
                campaignId = campaignId,
                merchantName = merchantName,
                message = msg,
                messageAr = msgAr
            )
        )
    }

    suspend fun clearAllData() {
        database.loyaltyDao.clearCampaigns()
        database.loyaltyDao.clearUserCoupons()
        database.loyaltyDao.clearLoyaltyCards()
        database.loyaltyDao.clearGeofenceLogs()
    }
}
