package com.example.service

import java.util.UUID

object ReferralService {

    data class ReferralData(
        val userId: String,
        val referrerId: String,
        val referrerCode: String,
        val referredAt: Long,
        val status: String // pending, completed
    )

    fun generateReferralCode(): String {
        val random = UUID.randomUUID().toString().substring(0, 8).uppercase()
        return "REF-$random"
    }

    fun registerReferral(referredUserId: String, referrerCode: String): ReferralData? {
        val referrerId = "MOCK_REFERRER_ID" // Mocking real db check

        return ReferralData(
            userId = referredUserId,
            referrerId = referrerId,
            referrerCode = referrerCode,
            referredAt = System.currentTimeMillis(),
            status = "pending"
        )
    }

    fun completeReferral(referralId: String) {
        // Mock apply bonus logic
        addReferralBonus(referralId)
    }

    private fun addReferralBonus(referralId: String) {
        // Trigger coupon addition code here
    }
}
