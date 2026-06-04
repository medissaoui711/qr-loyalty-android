package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LoyaltyDomainTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: LoyaltyRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Create an in-memory database for clean, isolated transaction tests
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = LoyaltyRepository(db)
    }

    @After
    fun closeDb() {
        db.close()
    }

    private suspend fun insertTestCampaign(): Campaign {
        val campaign = Campaign(
            id = 42,
            merchantName = "Test Café",
            merchantNameAr = "مقهى التجربة",
            couponTitle = "Free Latte",
            couponTitleAr = "لاتيه مجاني",
            couponDescription = "Try our organic hot latte",
            couponDescriptionAr = "جرب اللاتيه الساخن",
            discountCode = "TESTLATTE",
            latitude = 200f,
            longitude = 200f,
            geofenceRadius = 100f
        )
        repository.insertCampaign(campaign)
        return campaign
    }

    @Test
    fun testClaimDuplicatePrevention() = runBlocking {
        // Arrange
        val camp = insertTestCampaign()

        // Act - First scan & claim
        val firstClaimResult = repository.scanAndClaim(camp.id)
        val couponsAfterFirst = repository.userCoupons.first()

        // Assert
        assertTrue("First scan should succeed", firstClaimResult)
        assertEquals(1, couponsAfterFirst.size)
        assertEquals("scanned", couponsAfterFirst[0].status)

        // Act - Attempt duplicate claim
        val secondClaimResult = repository.scanAndClaim(camp.id)
        val couponsAfterSecond = repository.userCoupons.first()

        // Assert
        assertFalse("Duplicate claim should be prevented if existing is not redeemed", secondClaimResult)
        assertEquals(1, couponsAfterSecond.size) // No extra coupon added
    }

    @Test
    fun testClaimAllowedAfterRedemption() = runBlocking {
        // Arrange
        val camp = insertTestCampaign()

        // Scan, add to wallet, and redeem the first coupon
        repository.scanAndClaim(camp.id)
        repository.addToWallet(camp.id)
        val redeemed = repository.redeemCoupon(camp.id)
        assertTrue("Redeem should succeed", redeemed)

        val couponsBeforeSecond = repository.userCoupons.first()
        assertEquals(1, couponsBeforeSecond.size)
        assertEquals("redeemed", couponsBeforeSecond[0].status)

        // Act - Try claiming again since previous coupon was redeemed
        val claimAfterRedeemed = repository.scanAndClaim(camp.id)
        val couponsAfterSecond = repository.userCoupons.first()

        // Assert
        assertTrue("User should be allowed to claim a new voucher if the previous one is already redeemed", claimAfterRedeemed)
        assertEquals(2, couponsAfterSecond.size)
        // One is redeemed, one is the newly scanned and active coupon
        assertTrue(couponsAfterSecond.any { it.status == "scanned" })
        assertTrue(couponsAfterSecond.any { it.status == "redeemed" })
    }

    @Test
    fun testAddToWalletChangesStatus() = runBlocking {
        // Arrange
        val camp = insertTestCampaign()
        repository.scanAndClaim(camp.id)

        // Act
        repository.addToWallet(camp.id)
        val coupons = repository.userCoupons.first()

        // Assert
        assertEquals(1, coupons.size)
        assertEquals("added_to_wallet", coupons[0].status)
    }

    @Test
    fun testRedeemConvertsToLoyaltyStamps() = runBlocking {
        // Arrange
        val camp = insertTestCampaign()
        repository.scanAndClaim(camp.id)
        repository.addToWallet(camp.id)

        // Act
        val success = repository.redeemCoupon(camp.id)

        // Assert
        assertTrue(success)
        
        // 1. Check coupon is converted to redeemed status
        val coupons = repository.userCoupons.first()
        assertEquals("redeemed", coupons[0].status)
        assertNotNull(coupons[0].redeemedAt)

        // 2. Check loyalty stamp card is initialized and gets exactly 1 stamp/points
        val cards = repository.loyaltyCards.first()
        assertEquals(1, cards.size)
        assertEquals(camp.id, cards[0].campaignId)
        assertEquals(1, cards[0].stampsCount)
        assertEquals(100, cards[0].points)
    }

    @Test
    fun testRedeemStampsAccumulateAndRewardRules() = runBlocking {
        // Arrange
        val camp = insertTestCampaign()

        // Let's perform multiple claim -> wallet -> redeem cycles to simulate a repeat customer
        for (i in 1..5) {
            repository.scanAndClaim(camp.id)
            repository.addToWallet(camp.id)
            repository.redeemCoupon(camp.id)
        }

        // 1. Assert cards have accumulated 5 stamps
        var cards = repository.loyaltyCards.first()
        assertEquals(1, cards.size)
        assertEquals(5, cards[0].stampsCount) // full card
        // 100 points starting + 4 times increment of 100 points = 500 points
        assertEquals(500, cards[0].points)

        // 2. Scan and redeem the 6th time -> should trigger reward loop: resets stamps back to 1 and rewards 250 extra bonus points!
        repository.scanAndClaim(camp.id)
        repository.addToWallet(camp.id)
        repository.redeemCoupon(camp.id)

        cards = repository.loyaltyCards.first()
        assertEquals(1, cards[0].stampsCount) // reset back to 1
        // 500 previous points + 250 bonus reward points = 750 points (or more based on progression logic)
        assertEquals(750, cards[0].points)
    }

    @Test
    fun testGeofenceLogsStorageAndClearing() = runBlocking {
        // Arrange
        val camp = insertTestCampaign()

        // Act
        repository.logGeofenceTrigger(
            campaignId = camp.id,
            merchantName = camp.merchantName,
            msg = "Entered Test Cafe zone",
            msgAr = "تم الدخول لمنطقة مقهى التجربة"
        )

        // Assert log was registered correctly
        val logs = repository.geofenceLogs.first()
        assertEquals(1, logs.size)
        assertEquals(camp.id, logs[0].campaignId)
        assertEquals("Entered Test Cafe zone", logs[0].message)
        assertEquals("تم الدخول لمنطقة مقهى التجربة", logs[0].messageAr)

        // Act
        repository.clearAllData()

        // Assert database clean reset
        assertTrue("Coupons should be empty after clear", repository.userCoupons.first().isEmpty())
        assertTrue("Loyalty cards should be empty after clear", repository.loyaltyCards.first().isEmpty())
        assertTrue("Geofence logs should be empty after clear", repository.geofenceLogs.first().isEmpty())
    }
}
