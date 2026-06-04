# دليل ربط تطبيق المحفظة الرقمية مع المنصة البرمجية الخلفية (Backend Integration Guide) 🌐🔌
### **Client-Server Architecture, Security, API Contracts, and Room Synchronization**

تم تصميم بنية البيانات في هذا التطبيق وعزل البيانات محلياً لتكون متوافقة بنسبة **100%** مع عملية الانتقال السلسة من وضع العمل المستقل (Standalone Offline mode) إلى وضع التشغيل المتزامن الكامل سحابياً (Cloud Synchronized SaaS Multi-Tenant Platform / Online Mode).

يستعرض هذا الدليل الفني البنية المقترحة للربط، عقود واجهات برمجة التطبيقات (API Contracts)، وبروتوكول الأمان لضمان العزل التام للمتاجر والمستخدمين.

---

## 1. 📂 معمارية عزل البيانات للخدمات المصغرة (Multi-Tenant Architecture)
لضمان الفصل والأمان التام بين المتاجر الشريكة (Merchants)، يجب أن تتبع منصتك الجديدة الهيكل المعماري السحابي التالي:

```
               ┌──────────────────────────────────────────────┐
               │         لوحة تحكم المتجر (Merchant Portal)    │
               └──────────────────────┬───────────────────────┘
                                      │ (يدير الحملات والرموز)
                                      ▼
┌──────────────────┐           ┌──────────────┐           ┌──────────────────┐
│  العميل الفردي   │  APIs     │  منصة الخادم  │ SQL Sync │   قاعدة البيانات │
│  (أندرويد)       │◄─────────►│  (APIs SaaS)  ├─────────►│ (المستأجرين      │
│  Client App      │ JWT Auth  │  Backend App │ TenantID │  Tenant Database)│
└──────────────────┘           └──────────────┘           └──────────────────┘
```

* **مستوى قاعدة البيانات (Database Level Isolation):** يُنصح باستخدام عمود مستأجر مخصص `tenant_id` أو `merchant_id` في جميع جداول الحملات والكوبونات والمشتركين على السيرفر لتوجيه الاستعلامات ديناميكياً بحيث لا يمكن كتابة أو قراءة أي سجل إلا إذا طابق حساب المتجر المستخدم الجاري.
* **الأمان على مستوى الطلبات (Request Level Isolation):** تعتمد المصادقة بين تطبيق الأندرويد لخدمات السيرفر على مفاتيح **JSON Web Tokens (JWT)** والمحمية بروابط تشفير مميزة.

---

## 2. 🔐 بروتوكول الأمان والمصادقة (Authentication & Authorization Protocol)
* عند فتح المستخدم للتطبيق، يقوم بتسجيل الدخول أو تشغيل معرف فريد (Device Fingerprint) يولد رمز اعتماد مشفر ومميز على السيرفر ويخزنه في التطبيق بأمان.
* سيتضمن كل طلب مرسل من تطبيق الأندرويد الهيدر المشفر التالي:
  ```http
  Authorization: Bearer <JWT_TOKEN_HERE>
  ```
* يحتوي رمز الـ JWT المشفر في السيرفر على معلومات معرف المستأجر (`tenant_id`) ومعرف المستخدم (`user_id`). يمنع هذا الأسلوب بشكل كلي محاولات الانتحال أو محاولات المستخدمين للوصول غير المصرح به لبيانات متاجر غير مخصصة لهم.

---

## 3. 📑 عقود واجهات البرمجة السحابية (API Contracts & JSON Payloads)

تحتاج منصتك الخلفية لتنفيذ الواجهات الأربع الأساسية التالية لتغذية تطبيق أندرويد وتأمين عمل سليم لنظام الولاء:

### 1️⃣ الحصول على قائمة الحملات والمتاجر النشطة (Get Campaigns)
* **المسار:** `GET /api/v1/campaigns`
* **المسؤولية:** استرداد المتاجر الشريكة والنقاط الجغرافية وعروض الولاء الحالية للمستخدم.
* **نموذج الاستجابة (JSON Response):**
```json
[
  {
    "id": 1,
    "merchant_name": "Brew Haven Specialty Coffee",
    "merchant_name_ar": "بريو هيفن للقهوة المختصة",
    "coupon_title": "Free Double Espresso Shot",
    "coupon_title_ar": "إسبريسو مضاعف مجاني ☕",
    "coupon_description": "Get a free luxury double shot espresso using Ethiopian single-origin beans.",
    "coupon_description_ar": "احصل على كوب إسبريسو مضاعف مجاناً ومحضّر من خامات إثيوبية فاخرة.",
    "discount_code": "BREWHAVENFREE",
    "latitude": 180.0,
    "longitude": 160.0,
    "geofence_radius": 110.0,
    "color_hex": "#2E1C0C"
  }
]
```

### 2️⃣ تسجيل مطالبة كوبون جديدة كود المسح (Scan & Claim Coupon)
* **المسار:** `POST /api/v1/coupons/claim`
* **المسؤولية:** التحقق من صلاحية مسح الكود، منع التكرار، وتوليد الكود المشفر للمستخدم.
* **نموذج الطلب (JSON Request):**
```json
{
  "campaign_id": 1
}
```
* **نموذج الاستجابة (JSON Response) - نجاح المسح:**
```json
{
  "success": true,
  "coupon": {
    "id": 420,
    "campaign_id": 1,
    "coupon_code": "COUPON_1_34902",
    "status": "scanned",
    "scanned_at": 1780526969
  }
}
```

### 3️⃣ استرداد الكوبون والتحويل لنقاط طوابع (Redeem Coupon & Add Point)
* **المسار:** `POST /api/v1/coupons/redeem`
* **المسؤولية:** يقوم البائع بمسح الباركود، فتتغير الحالة إلى مستخدم (`redeemed`) وتُضاف النقاط والطابع للعميل.
* **نموذج الطلب (JSON Request):**
```json
{
  "campaign_id": 1,
  "coupon_code": "COUPON_1_34902"
}
```
* **نموذج الاستجابة (JSON Response):**
```json
{
  "success": true,
  "loyalty_card": {
    "campaign_id": 1,
    "stamps_count": 3,
    "max_stamps": 5,
    "points": 300,
    "triggered_reward": false
  }
}
```

---

## 4. 🚀 تشييد كود الربط الفعلي في الأندرويد (Kotlin/Retrofit Bridge Implementation)

لتحويل البيانات المحلية المنعزلة حالياً في تطبيق أندرويد لتعمل عبر السيرفر بشكل حي، يرجى استيراد مكتبة **Retrofit** واتباع الآلية المنظمة التالية لربط قاعدة بيانات Room المحلية مع السيرفر السحابي (Offline-first architecture with Cloud Sync):

### أ. تعريف وواجهات الاتصال (API Client Interface)
```kotlin
package com.example.data.api

import com.example.data.Campaign
import com.example.data.UserCoupon
import com.example.data.LoyaltyCard
import retrofit2.http.*

interface LoyaltyApiService {

    @GET("api/v1/campaigns")
    suspend fun getCampaigns(
        @Header("Authorization") token: String
    ): List<Campaign>

    @POST("api/v1/coupons/claim")
    suspend fun claimCoupon(
        @Header("Authorization") token: String,
        @Body request: ClaimRequest
    ): ClaimResponse

    @POST("api/v1/coupons/redeem")
    suspend fun redeemCoupon(
        @Header("Authorization") token: String,
        @Body request: RedeemRequest
    ): RedeemResponse
}

data class ClaimRequest(val campaign_id: Int)
data class ClaimResponse(val success: Boolean, val coupon: UserCoupon)
data class RedeemRequest(val campaign_id: Int, val coupon_code: String)
data class RedeemResponse(val success: Boolean, val loyalty_card: LoyaltyCard)
```

### ب. إستراتيجية المزامنة السلسة المتكاملة (Synchronization Strategy - WorkManager)
بدلاً من الاستدعاء المباشر الذي قد يفشل عند انقطاع الإنترنت، استخدم نمط **Offline-First**:
1. عند تفعيل "مطالبة" أو "إضافة للمحفظة" أثناء عدم اتصال العميل بالبيانات، يتم تعديل القيمة منطقياً وحفظها فورياً في **Room Database** محلياً على الجهاز ليعمل التطبيق بمرونة تامة لراحة المستخدم.
2. استخدام معالج المهمة التلقائي لأندرويد `WorkManager` ليزامن تلقائياً وبأمان في الخلفية فور استقرار إشارة الإنترنت، ليرسل السجلات المتراكمة في قاعدة البيانات المحلية بالطلب `POST` للـ API السحابي للتحقق والتدوين الشامل لملفات المتاجر.

تضمن هذه الآلية الأمان العالي، والاعتمادية السلسة للتطبيقات السحابية الكبرى ونظام إدارة المتاجر المتكامل.
