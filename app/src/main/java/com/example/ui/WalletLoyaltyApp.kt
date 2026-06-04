package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Campaign
import com.example.data.GeofenceLog
import com.example.data.LoyaltyCard
import com.example.data.UserCoupon
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletLoyaltyApp(viewModel: LoyaltyViewModel) {
    var currentTab by remember { mutableStateOf(0) } // 0: Claims/Scanner, 1: Wallet & Loyalty, 2: Geofence Map, 3: Merchant Admin
    var isArabic by remember { mutableStateOf(true) } // Default to Arabic as requested by user's prompt language

    val campaigns by viewModel.campaigns.collectAsState()
    val userCoupons by viewModel.userCoupons.collectAsState()
    val loyaltyCards by viewModel.loyaltyCards.collectAsState()
    val geofenceLogs by viewModel.geofenceLogs.collectAsState()

    // Handle initial scanning overlays from VM
    if (viewModel.showScanSuccessDialog && viewModel.scannedCampaign != null) {
        ScanSuccessDialog(
            campaign = viewModel.scannedCampaign!!,
            userCoupon = userCoupons.find { it.campaignId == viewModel.scannedCampaign!!.id },
            isArabic = isArabic,
            onDismiss = { viewModel.showScanSuccessDialog = false },
            onAddToWallet = { viewModel.addCouponToWallet(viewModel.scannedCampaign!!.id) }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isArabic) "منظومة الولاء والمحفظة الذكية" else "Smart Wallet & Loyalty",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = CoffeeGold
                            )
                        )
                        Text(
                            text = if (isArabic) "محاكاة تحويل QR وإشعارات موقعية" else "QR-to-Wallet & Geofencing Active",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = SoftGrey
                            )
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isArabic = !isArabic },
                        modifier = Modifier.testTag("language_toggle_btn")
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(CoffeeGold.copy(alpha = 0.15f))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = if (isArabic) "EN" else "عربي",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CoffeeGold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = EspressoBlack
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = EspressoBlack,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Filled.QrCodeScanner, contentDescription = "Scanner") },
                    label = { Text(if (isArabic) "مسح الـ QR" else "Scan QR", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EspressoBlack,
                        selectedTextColor = CoffeeGold,
                        indicatorColor = CoffeeGold,
                        unselectedIconColor = SoftGrey,
                        unselectedTextColor = SoftGrey
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = "Wallet") },
                    label = { Text(if (isArabic) "محفظتي" else "My Wallet", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EspressoBlack,
                        selectedTextColor = CoffeeGold,
                        indicatorColor = CoffeeGold,
                        unselectedIconColor = SoftGrey,
                        unselectedTextColor = SoftGrey
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Filled.Map, contentDescription = "Map") },
                    label = { Text(if (isArabic) "المحاكاة الجغرافية" else "Geofence", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EspressoBlack,
                        selectedTextColor = CoffeeGold,
                        indicatorColor = CoffeeGold,
                        unselectedIconColor = SoftGrey,
                        unselectedTextColor = SoftGrey
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Filled.AdminPanelSettings, contentDescription = "Merchant") },
                    label = { Text(if (isArabic) "المتاجر" else "Merchant", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EspressoBlack,
                        selectedTextColor = CoffeeGold,
                        indicatorColor = CoffeeGold,
                        unselectedIconColor = SoftGrey,
                        unselectedTextColor = SoftGrey
                    )
                )
            }
        },
        containerColor = EspressoBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(EspressoBlack)
        ) {
            // Live banner showing if user is near any campaign (Geofencing visual highlight)
            AnimatedVisibility(
                visible = viewModel.nearCampaign != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                viewModel.nearCampaign?.let { near ->
                    val userCoupon = userCoupons.find { it.campaignId == near.id }
                    val isAlreadyInWallet = userCoupon != null && userCoupon.status == "added_to_wallet"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isAlreadyInWallet) Color(0xFF2E7D32) else Color(0xFFC62828))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.MyLocation,
                                contentDescription = "Location Active",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isArabic) "أنت داخل نطاق: ${near.merchantNameAr} 📍" else "You are inside: ${near.merchantName} 📍",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isAlreadyInWallet) {
                                        if (isArabic) "القسيمة نشطة في محفظتك! استردها الآن" else "Coupon active in your wallet! Redeem now."
                                    } else {
                                        if (isArabic) "امسح كود QR أولاً لإضافة العرض لمحفظتك" else "Scan cafe QR first to add coupon to wallet."
                                    },
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                when (currentTab) {
                    0 -> ScannerTab(viewModel = viewModel, campaigns = campaigns, isArabic = isArabic)
                    1 -> WalletTab(viewModel = viewModel, coupons = userCoupons, loyaltyCards = loyaltyCards, campaigns = campaigns, isArabic = isArabic)
                    2 -> GeofenceTab(viewModel = viewModel, campaigns = campaigns, isArabic = isArabic, logs = geofenceLogs)
                    3 -> MerchantAdminTab(viewModel = viewModel, campaigns = campaigns, isArabic = isArabic)
                }
            }
        }
    }
}

// ------------------- TAB 0: SCANNER & CLAIMS (M3 COMPOSABLES) -------------------
@Composable
fun ScannerTab(viewModel: LoyaltyViewModel, campaigns: List<Campaign>, isArabic: Boolean) {
    var scannedCodeInput by remember { mutableStateOf("") }
    var showActiveCameraSimulation by remember { mutableStateOf(false) }
    var targetSimulatedCampaign by remember { mutableStateOf<Campaign?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                border = BorderStroke(1.dp, GlassWhite),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.QrCodeScanner,
                            contentDescription = "Scan icon",
                            tint = CoffeeGold,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "محاكاة مسح الكود QR" else "QR Code Claim Simulator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isArabic)
                            "في الواقع، يطبع المقهى كود QR عند الكاشير أو الطاولات. امسح الكود أدناه للحصول على قسيمة فورية تضاف لمحفظتك."
                        else
                            "In production, cafes print static QR codes on tables. Scan any simulated merchant QR code below to pull the claim details immediately.",
                        fontSize = 13.sp,
                        color = SoftGrey
                    )
                }
            }
        }

        if (showActiveCameraSimulation && targetSimulatedCampaign != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black)
                        .border(2.dp, CoffeeGold, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Running vertical scan laser line
                    val infiniteTransition = rememberInfiniteTransition(label = "laser")
                    val laserOffset by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 200f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "laser_offset"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = if (isArabic) "جاري مسح كود QR للمتجر..." else "SCANNING MERCHANT QR...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoffeeGold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // QR Graphic
                        Icon(
                            imageVector = Icons.Filled.QrCode,
                            contentDescription = "QR Graphics",
                            tint = Color.White,
                            modifier = Modifier.size(110.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isArabic) "كود: ${targetSimulatedCampaign!!.discountCode}" else "Code: ${targetSimulatedCampaign!!.discountCode}",
                            fontSize = 13.sp,
                            color = SoftGrey
                        )
                    }

                    // Floating red laser line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .offset(y = (-50).dp + laserOffset.dp)
                            .background(Color.Red.copy(alpha = 0.8f))
                    )

                    // Overlay corner frames
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val length = 35f
                        val strokeWidth = 6f
                        // Top Left
                        drawLine(Color.White, Offset(40f, 40f), Offset(40f + length, 40f), strokeWidth)
                        drawLine(Color.White, Offset(40f, 40f), Offset(40f, 40f + length), strokeWidth)

                        // Top Right
                        drawLine(Color.White, Offset(size.width - 40f, 40f), Offset(size.width - 40f - length, 40f), strokeWidth)
                        drawLine(Color.White, Offset(size.width - 40f, 40f), Offset(size.width - 40f, 40f + length), strokeWidth)

                        // Bottom Left
                        drawLine(Color.White, Offset(40f, size.height - 40f), Offset(40f + length, size.height - 40f), strokeWidth)
                        drawLine(Color.White, Offset(40f, size.height - 40f), Offset(40f, size.height - 40f - length), strokeWidth)

                        // Bottom Right
                        drawLine(Color.White, Offset(size.width - 40f, size.height - 40f), Offset(size.width - 40f - length, size.height - 40f), strokeWidth)
                        drawLine(Color.White, Offset(size.width - 40f, size.height - 40f), Offset(size.width - 40f, size.height - 40f - length), strokeWidth)
                    }

                    // Auto-resolve scan after 1.8 seconds
                    LaunchedEffect(targetSimulatedCampaign) {
                        kotlinx.coroutines.delay(1800)
                        viewModel.simulateQRScan(targetSimulatedCampaign!!.discountCode)
                        showActiveCameraSimulation = false
                        targetSimulatedCampaign = null
                    }
                }
            }
        }

        item {
            Text(
                text = if (isArabic) "اختر مقهى لمحاكاة قراءة الكود QR المطبوع:" else "Pick a café to simulate a physical poster QR scan:",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = CoffeeGold
            )
        }

        if (campaigns.isEmpty()) {
            item {
                Text(
                    text = if (isArabic) "لا توجد حملات متاحة حالياً. تفقد صفحة المتاجر لإضافة وحدة جديدة." else "No active campaigns found. Add one in the Merchant Admin tab.",
                    fontSize = 13.sp,
                    color = SoftGrey,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(campaigns) { campaign ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                    border = BorderStroke(1.dp, Color(android.graphics.Color.parseColor(campaign.colorHex)).copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) campaign.merchantNameAr else campaign.merchantName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isArabic) campaign.couponTitleAr else campaign.couponTitle,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = CoffeeGold
                                )
                            }

                            // QR Scan Action Trigger
                            Button(
                                onClick = {
                                    targetSimulatedCampaign = campaign
                                    showActiveCameraSimulation = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(android.graphics.Color.parseColor(campaign.colorHex))
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("simulate_scan_btn_${campaign.id}")
                            ) {
                                Icon(Icons.Filled.QrCode, contentDescription = "QR", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isArabic) "مسح QR" else "Scan QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = GlassWhite, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Points Badge
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = AmberGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isArabic) "نقاط: +${campaign.pointsPerRedeem * 100}" else "Stamps: +${campaign.pointsPerRedeem}",
                                    fontSize = 11.sp,
                                    color = SoftGrey
                                )
                            }
                            // Radius Badge
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.PinDrop, contentDescription = null, tint = CoffeeGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isArabic) "تنبيه جواري: ${campaign.geofenceRadius.toInt()}م" else "Alert Dist: ${campaign.geofenceRadius.toInt()}m",
                                    fontSize = 11.sp,
                                    color = SoftGrey
                                )
                            }
                        }
                    }
                }
            }
        }

        // Custom manual keyboard code claim as alternative
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                border = BorderStroke(1.dp, GlassWhite),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (isArabic) "كتابة الرمز يدوياً" else "Manual Voucher Claims",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = scannedCodeInput,
                            onValueChange = { scannedCodeInput = it },
                            placeholder = { Text(if (isArabic) "مثال: BREWHAVENFREE" else "e.g. BREWHAVENFREE", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_code_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CoffeeGold,
                                unfocusedBorderColor = SoftGrey,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (scannedCodeInput.isNotBlank()) {
                                    viewModel.simulateQRScan(scannedCodeInput.trim().uppercase())
                                    scannedCodeInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CoffeeGold),
                            modifier = Modifier.testTag("manual_code_submit")
                        ) {
                            Text(if (isArabic) "تفعيل" else "Claim", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// Simulated claim sheet overlay (Added to Apple/Google Wallet UI mimicry)
@Composable
fun ScanSuccessDialog(
    campaign: Campaign,
    userCoupon: UserCoupon?,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onAddToWallet: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianDark),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, CoffeeGold.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianDark)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(campaign.colorHex)).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color(android.graphics.Color.parseColor(campaign.colorHex)),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isArabic) "تم التقاط كوبون بنجاح!" else "Voucher Captured Successfully!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isArabic) "مقدم من ${campaign.merchantNameAr}" else "Offered by ${campaign.merchantName}",
                    fontSize = 12.sp,
                    color = SoftGrey
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Simulated Wallet Ticket
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(android.graphics.Color.parseColor(campaign.colorHex)))
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isArabic) campaign.merchantNameAr else campaign.merchantName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Icon(
                                imageVector = Icons.Filled.LocalCafe,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = if (isArabic) campaign.couponTitleAr else campaign.couponTitle,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isArabic) campaign.couponDescriptionAr else campaign.couponDescription,
                            color = Color.White.copy(alpha = 0.80f),
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(if (isArabic) "الرمز البصري" else "CODE TYPE", fontSize = 8.sp, color = Color.White.copy(alpha = 0.6f))
                                Text(campaign.discountCode, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(if (isArabic) "تاريخ الإصدار" else "CLAIM DATE", fontSize = 8.sp, color = Color.White.copy(alpha = 0.6f))
                                Text("TODAY/اليوم", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // "Add to Wallet Button"
                if (userCoupon?.status == "added_to_wallet") {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.CardMembership, contentDescription = null, tint = CoffeeGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isArabic) "القسيمة موجودة بالفعل في محفظتك" else "Already Saved in Wallet", color = Color.White)
                    }
                } else {
                    Button(
                        onClick = onAddToWallet,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("add_to_wallet_btn"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        // Google Wallet styled visual highlights
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Colored Wallet logo lines simulator
                            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Box(modifier = Modifier.size(height = 16.dp, width = 4.dp).background(Color(0xFF4285F4)))
                                    Box(modifier = Modifier.size(height = 16.dp, width = 4.dp).background(Color(0xFFEA4335)))
                                    Box(modifier = Modifier.size(height = 16.dp, width = 4.dp).background(Color(0xFFFBBC05)))
                                    Box(modifier = Modifier.size(height = 16.dp, width = 4.dp).background(Color(0xFF34A853)))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic) "إضافة إلى Google Wallet" else "Add to Google Wallet",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onDismiss) {
                    Text(
                        text = if (isArabic) "إلغاء المطالبة" else "Skip / Cancel",
                        color = SoftGrey,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}


// ------------------- TAB 1: USER WALLET & LOYALTY CARD -------------------
@Composable
fun WalletTab(
    viewModel: LoyaltyViewModel,
    coupons: List<UserCoupon>,
    loyaltyCards: List<LoyaltyCard>,
    campaigns: List<Campaign>,
    isArabic: Boolean
) {
    var selectedWalletSection by remember { mutableStateOf(0) } // 0: Wallet Coupons, 1: Loyalty Stamps

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // Tab Headers in M3 style
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(ObsidianDark)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedWalletSection == 0) CoffeeGold else Color.Transparent)
                    .clickable { selectedWalletSection = 0 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isArabic) "قسائم المحفظة (${coupons.count { it.status == "added_to_wallet" }})" else "Coupons (${coupons.count { it.status == "added_to_wallet" }})",
                    fontWeight = FontWeight.Bold,
                    color = if (selectedWalletSection == 0) EspressoBlack else Color.White,
                    fontSize = 12.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedWalletSection == 1) CoffeeGold else Color.Transparent)
                    .clickable { selectedWalletSection = 1 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isArabic) "بطاقات الولاء (${loyaltyCards.size})" else "Loyalty Cards (${loyaltyCards.size})",
                    fontWeight = FontWeight.Bold,
                    color = if (selectedWalletSection == 1) EspressoBlack else Color.White,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedWalletSection == 0) {
            // Coupons Section
            val walletCoupons = coupons.filter { it.status == "added_to_wallet" }
            if (walletCoupons.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountBalanceWallet,
                            contentDescription = null,
                            tint = SoftGrey,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isArabic) "محفظتك فارغة حالياً!" else "Your digital wallet is empty!",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isArabic) "يمكنك مسح الكود للمقاهي المفضلة للحصول على العروض." else "Scan a coffee shop QR to claim and load deals to your phone.",
                            fontSize = 12.sp,
                            color = SoftGrey,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(walletCoupons) { coupon ->
                        val campaign = campaigns.find { it.id == coupon.campaignId }
                        if (campaign != null) {
                            WalletTicketCard(
                                campaign = campaign,
                                coupon = coupon,
                                isArabic = isArabic,
                                onRedeem = { viewModel.redeemWalletCoupon(campaign.id) }
                            )
                        }
                    }
                }
            }
        } else {
            // Loyalty Cards Section (conversion targets of coupons)
            if (loyaltyCards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = null,
                            tint = SoftGrey,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isArabic) "لا توجد بطاقات ولاء نشطة" else "No active loyalty stamps yet!",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isArabic) "عندما تصرف قسائم المقهى في الكاشير، ستبدأ بطاقة الولاء بجمع الأختام والنقاط تلقائياً!" else "Upon redeeming wallet vouchers, your loyalty stamps will accumulate automáticamente here!",
                            fontSize = 12.sp,
                            color = SoftGrey,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(loyaltyCards) { card ->
                        val campaign = campaigns.find { it.id == card.campaignId }
                        if (campaign != null) {
                            LoyaltyPassCard(
                                campaign = campaign,
                                card = card,
                                isArabic = isArabic
                            )
                        }
                    }
                }
            }
        }
    }
}

// Custom Coupon ticket layout that simulates Apple/Google Wallet cards beautifully
@Composable
fun WalletTicketCard(
    campaign: Campaign,
    coupon: UserCoupon,
    isArabic: Boolean,
    onRedeem: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = ObsidianDark),
        border = BorderStroke(1.dp, Color(android.graphics.Color.parseColor(campaign.colorHex)).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Color Strip and Logo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(android.graphics.Color.parseColor(campaign.colorHex)))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocalCafe,
                        contentDescription = "Cafe Logo",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) campaign.merchantNameAr else campaign.merchantName,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isArabic) "محفظة Google" else "Google Wallet",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Coupon Core body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = if (isArabic) campaign.couponTitleAr else campaign.couponTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isArabic) campaign.couponDescriptionAr else campaign.couponDescription,
                    fontSize = 12.sp,
                    color = SoftGrey,
                    maxLines = if (isExpanded) 10 else 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Barcoding simulation area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .clip(RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Draw clean vector vertical stripes simulating 1D Barcode
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        val stripeCount = 65
                        val randomWidths = listOf(3f, 6f, 9f, 2f, 12f, 5f)
                        var currentX = 0f
                        val widthOffset = size.width / stripeCount

                        for (i in 0 until stripeCount) {
                            val stripeWidth = randomWidths[i % randomWidths.size]
                            drawRect(
                                color = Color.Black,
                                size = androidx.compose.ui.geometry.Size(stripeWidth, size.height),
                                topLeft = Offset(currentX, 0f)
                            )
                            currentX += widthOffset
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = coupon.couponCode,
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Simulate Redeem in cash register (converts coupon to loyalty stamp)
                Button(
                    onClick = { onRedeem() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(android.graphics.Color.parseColor(campaign.colorHex))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("redeem_coupon_btn_${campaign.id}"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PointOfSale,
                        contentDescription = "Redeem",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "استرداد القسيمة في الكاشير" else "Redeem at Cash Register",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// Gorgeous wood/espresso textured loyalty stamp card visualizers
@Composable
fun LoyaltyPassCard(
    campaign: Campaign,
    card: LoyaltyCard,
    isArabic: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ObsidianDark),
        border = BorderStroke(1.dp, CoffeeGold.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // Subtle dynamic grid line patterns in background for luxury loyalty look
                    val strokeWidth = 1f
                    val step = 40f
                    for (i in 0 until (size.height / step).toInt()) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.03f),
                            start = Offset(0f, i * step),
                            end = Offset(size.width, i * step),
                            strokeWidth = strokeWidth
                        )
                    }
                }
                .padding(18.dp)
        ) {
            // Loyalty PassHeader
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isArabic) "${campaign.merchantNameAr} – بطاقة الولاء" else "${campaign.merchantName} - Gold Club",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = CoffeeGold
                    )
                    Text(
                        text = if (isArabic) "اجمع ٥ أختام للحصول على كوب مجاني!" else "Collect 5 stamps for a free specialty brew!",
                        fontSize = 11.sp,
                        color = SoftGrey
                    )
                }
                Icon(
                    imageVector = Icons.Filled.Stars,
                    contentDescription = null,
                    tint = CoffeeGold,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Points indicator bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "الرصيد: ${card.points} نقطة" else "Balance: ${card.points} PTS",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Text(
                    text = "${card.stampsCount} / ${card.maxStamps}",
                    color = CoffeeGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Stamps collected representation (coffee mugs)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 1..card.maxStamps) {
                    val isStamped = i <= card.stampsCount
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (isStamped) CoffeeGold else DarkBrown.copy(alpha = 0.6f))
                            .border(1.dp, if (isStamped) CoffeeGold else SoftGrey.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isStamped) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.Coffee,
                                    contentDescription = "Coffee Stamp",
                                    tint = EspressoBlack,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    "☕",
                                    fontSize = 10.sp,
                                    lineHeight = 10.sp
                                )
                            }
                        } else {
                            Text(
                                text = "$i",
                                color = SoftGrey,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = GlassWhite, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Status updates time
            Text(
                text = if (isArabic) "آخر تحديث قبل لحظات" else "Last automated sync: Just now",
                fontSize = 10.sp,
                color = SoftGrey,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}


// ------------------- TAB 2: GEOFENCE SIMULATION MAP -------------------
@Composable
fun GeofenceTab(viewModel: LoyaltyViewModel, campaigns: List<Campaign>, isArabic: Boolean, logs: List<GeofenceLog>) {
    var isSimulatingWalk by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                border = BorderStroke(1.dp, GlassWhite),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.MyLocation,
                            contentDescription = "Radar Symbol",
                            tint = CoffeeGold,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "محاكي الاقتراب الجغرافي (Geofencing)" else "Geofencing & Proximity Canvas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isArabic)
                            "انقر على الخريطة لتغيير موقعك أو اسحب النقطة الزرقاء. عند الاقتراب من دائرة مقهى تملك قسيمته في المحفظة، سيطلق النظام إشعار دفع حقيقي!"
                        else
                            "Click anywhere inside the radar map or use sliders to move your avatar dot. Proximity limits inside cafe geofences (glowing radial sectors) trigger high-priority alerts.",
                        fontSize = 13.sp,
                        color = SoftGrey
                    )
                }
            }
        }

        // Active Vector Map Canvas
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0C0907))
                    .border(1.dp, GlassWhite, RoundedCornerShape(20.dp))
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            // Update VM coordinates with tapped bounds (clamped to 500 max)
                            val scaledX = (offset.x / size.width) * 500f
                            val scaledY = (offset.y / size.height) * 500f
                            viewModel.updateUserPosition(scaledX, scaledY)
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val scaledX = (change.position.x / size.width) * 500f
                            val scaledY = (change.position.y / size.height) * 500f
                            viewModel.updateUserPosition(scaledX.coerceIn(0f, 500f), scaledY.coerceIn(0f, 500f))
                        }
                    }
            ) {
                // Jetpack canvas to draw cafes, geofence radius & user
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val mapWidth = size.width
                    val mapHeight = size.height

                    // Helper to translate virtual coordinates 0-500 to canvas pixels
                    fun getCanvasX(vX: Float): Float = (vX / 500f) * mapWidth
                    fun getCanvasY(vY: Float): Float = (vY / 500f) * mapHeight

                    // 1. Draw Grid lines and streets simulator (concentric radar lines)
                    for (r in 1..4) {
                        drawCircle(
                            color = CoffeeGold.copy(alpha = 0.05f * r),
                            radius = (mapWidth / 8) * r,
                            center = Offset(mapWidth / 2, mapHeight / 2),
                            style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f))
                        )
                    }

                    // Draw basic crosshairs
                    drawLine(
                        color = GlassWhite,
                        start = Offset(0f, mapHeight / 2),
                        end = Offset(mapWidth, mapHeight / 2),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = GlassWhite,
                        start = Offset(mapWidth / 2, 0f),
                        end = Offset(mapWidth / 2, mapHeight),
                        strokeWidth = 1f
                    )

                    // 2. Plot Campaigns with Geofences (circular sectors)
                    for (camp in campaigns) {
                        val cX = getCanvasX(camp.latitude)
                        val cY = getCanvasY(camp.longitude)
                        val radiusPx = (camp.geofenceRadius / 500f) * mapWidth

                        // Check if user is inside
                        val userDistVirtual = Math.hypot(
                            (viewModel.userLatitude - camp.latitude).toDouble(),
                            (viewModel.userLongitude - camp.longitude).toDouble()
                        )
                        val isUserInside = userDistVirtual <= camp.geofenceRadius

                        val baseColor = Color(android.graphics.Color.parseColor(camp.colorHex))

                        // Draw geofence circle filling
                        drawCircle(
                            color = baseColor.copy(alpha = if (isUserInside) 0.16f else 0.06f),
                            radius = radiusPx,
                            center = Offset(cX, cY)
                        )

                        // Draw boundary line (Vibrant Glowing Green if User is inside, otherwise dashed coffee gold)
                        drawCircle(
                            color = if (isUserInside) Color.Green else baseColor.copy(alpha = 0.6f),
                            radius = radiusPx,
                            center = Offset(cX, cY),
                            style = Stroke(
                                width = if (isUserInside) 3f else 1.5f,
                                pathEffect = if (isUserInside) null else PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            )
                        )

                        // Draw target shop center point
                        drawCircle(
                            color = baseColor,
                            radius = 12f,
                            center = Offset(cX, cY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 5f,
                            center = Offset(cX, cY)
                        )
                    }

                    // 3. Draw User Blue glowing location Dot
                    val uX = getCanvasX(viewModel.userLatitude)
                    val uY = getCanvasY(viewModel.userLongitude)

                    // Glow rings
                    drawCircle(
                        color = Color(0xFF2196F3).copy(alpha = 0.2f),
                        radius = 28f,
                        center = Offset(uX, uY)
                    )
                    drawCircle(
                        color = Color(0xFF2196F3).copy(alpha = 0.4f),
                        radius = 16f,
                        center = Offset(uX, uY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 8f,
                        center = Offset(uX, uY)
                    )
                    drawCircle(
                        color = Color(0xFF2196F3),
                        radius = 5f,
                        center = Offset(uX, uY)
                    )
                }

                // In-Canvas overlays for labels
                for (camp in campaigns) {
                    val virtualX = camp.latitude
                    val virtualY = camp.longitude

                    Box(
                        modifier = Modifier
                            .offset(
                                x = ((virtualX / 500f) * 320).dp - 40.dp,
                                y = ((virtualY / 500f) * 320).dp + 8.dp
                            )
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.8f))
                            .border(1.dp, GlassWhite, RoundedCornerShape(6.dp))
                            .padding(4.dp)
                    ) {
                        Text(
                            text = if (isArabic) camp.merchantNameAr.take(12) else camp.merchantName.take(12),
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                // Float indicator showing user position coordinates
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                        .padding(6.dp)
                ) {
                    Text(
                        text = "COORD: X=${viewModel.userLatitude.toInt()} Y=${viewModel.userLongitude.toInt()}",
                        color = Color(0xFF2196F3),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Quick teleport & Autowalk simulator triggers
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Auto teleport Brew Haven
                Button(
                    onClick = { viewModel.updateUserPosition(180f, 160f) },
                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianDark),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("teleport_brew_haven"),
                    border = BorderStroke(1.dp, CoffeeGold.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Text(if (isArabic) "موقع بريو هيفن" else "Brew Haven 📍", fontSize = 11.sp, maxLines = 1)
                }

                // Auto teleport Crema
                Button(
                    onClick = { viewModel.updateUserPosition(360f, 180f) },
                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianDark),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("teleport_golden_crema"),
                    border = BorderStroke(1.dp, CoffeeGold.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Text(if (isArabic) "موقع الكريمة" else "Crema 📍", fontSize = 11.sp, maxLines = 1)
                }

                // Teleport Cyber Café
                Button(
                    onClick = { viewModel.updateUserPosition(140f, 360f) },
                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianDark),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("teleport_glitch"),
                    border = BorderStroke(1.dp, CoffeeGold.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Text(if (isArabic) "موقع جليتش" else "Glitch 📍", fontSize = 11.sp, maxLines = 1)
                }

                // Reset position to center
                Button(
                    onClick = { viewModel.updateUserPosition(250f, 250f) },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBrown),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("teleport_reset"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                }
            }
        }

        // Live coordinate sliders
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                border = BorderStroke(1.dp, GlassWhite),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = if (isArabic) "منزلقات الإحداثيات اليدوية" else "Manual Pin Location Adjustments",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Coordinate X
                    Text("X Axis (محور أفق): ${viewModel.userLatitude.toInt()}", fontSize = 11.sp, color = SoftGrey)
                    Slider(
                        value = viewModel.userLatitude,
                        onValueChange = { viewModel.updateUserPosition(it, viewModel.userLongitude) },
                        valueRange = 0f..500f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF2196F3), activeTrackColor = Color(0xFF2196F3))
                    )

                    // Coordinate Y
                    Text("Y Axis (محور عمود): ${viewModel.userLongitude.toInt()}", fontSize = 11.sp, color = SoftGrey)
                    Slider(
                        value = viewModel.userLongitude,
                        onValueChange = { viewModel.updateUserPosition(viewModel.userLatitude, it) },
                        valueRange = 0f..500f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF2196F3), activeTrackColor = Color(0xFF2196F3))
                    )
                }
            }
        }

        // Rolling list of geofence alert logs triggered (Telemetry audits)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "سجل التنبيهات الجغرافية" else "Active Geofence Telemetry Logs",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = CoffeeGold
                )
                TextButton(
                    onClick = { viewModel.resetSimulation() },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text(if (isArabic) "تصفير السجل" else "Delete Logs", fontSize = 11.sp)
                }
            }
        }

        if (logs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(ObsidianDark, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isArabic) "لا توجد تنبيهات مسجلة بعد. اسحب النقطة الزرقاء داخل الدائرة لتوليد تنبيه موقعي." else "No alert logs yet. Walk inside the circles to trigger a virtual Geofence alert.",
                        fontSize = 12.sp,
                        color = SoftGrey,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(logs.take(15)) { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GlassWhite)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Green.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsActive,
                                contentDescription = null,
                                tint = Color.Green,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) log.merchantName else log.merchantName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                            Text(
                                text = if (isArabic) log.messageAr else log.message,
                                fontSize = 11.sp,
                                color = SoftGrey
                            )
                        }
                    }
                }
            }
        }
    }
}


// ------------------- TAB 3: MERCHANT CAMPAIGN CREATION -------------------
data class CampaignTemplate(
    val titleEn: String,
    val titleAr: String,
    val descEn: String,
    val descAr: String,
    val code: String,
    val color: String
)

@Composable
fun MerchantAdminTab(viewModel: LoyaltyViewModel, campaigns: List<Campaign>, isArabic: Boolean) {
    var currentPlan by remember { mutableStateOf("Free Trial") }
    var selectedTemplate by remember { mutableStateOf<CampaignTemplate?>(null) }

    if (!viewModel.isMerchantOnboarded) {
        MerchantOnboardingScreen(isArabic, viewModel)
    } else if (!viewModel.isMerchantSetupComplete) {
        MerchantSetupScreen(
            isArabic = isArabic,
            currentPlan = currentPlan,
            onPlanSelected = { currentPlan = it },
            selectedTemplate = selectedTemplate,
            onTemplateSelected = { selectedTemplate = it },
            onComplete = { 
                if (selectedTemplate != null) {
                    viewModel.completeSetup(currentPlan, selectedTemplate!!)
                }
            }
        )
    } else {
        MerchantDashboard(
            viewModel = viewModel,
            campaigns = campaigns,
            isArabic = isArabic
        )
    }
}

@Composable
fun MerchantOnboardingScreen(isArabic: Boolean, viewModel: LoyaltyViewModel) {
    var storeName by remember { mutableStateOf("") }
    var storeType by remember { mutableStateOf("") }
    var currentStep by remember { mutableStateOf(1) } // 1: Welcome/Auth, 2: Store details, 3: Success

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (currentStep) {
            1 -> {
                Icon(Icons.Filled.Storefront, contentDescription = null, tint = CoffeeGold, modifier = Modifier.size(80.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = if (isArabic) "مرحباً بك في بوابة التجار" else "Welcome to Merchant Portal",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isArabic) "منصة SaaS احترافية لزيادة مبيعاتك والاحتفاظ بعملائك." else "Professional SaaS platform to boost sales & retain customers.",
                    color = SoftGrey,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = EspressoBlack),
                    border = BorderStroke(1.dp, GlassWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(if (isArabic) "✓ مساحة مستقلة لحسابك" else "✓ Dedicated workspace", color = Color(0xFF81C784), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(if (isArabic) "✓ بيانات معزولة تماماً (Isolated Data)" else "✓ Fully isolated data (Merchant ID)", color = Color(0xFF81C784), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(if (isArabic) "✓ تحكم كامل بحملاتك" else "✓ Full control over your campaigns", color = Color(0xFF81C784), fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { currentStep = 2 },
                    colors = ButtonDefaults.buttonColors(containerColor = CoffeeGold),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(if (isArabic) "إنشاء متجر (Create Store)" else "Create Store", color = EspressoBlack, fontWeight = FontWeight.Bold)
                }
            }
            2 -> {
                Text(
                    text = if (isArabic) "بيانات المتجر" else "Store Details",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text(if (isArabic) "اسم المتجر" else "Store Name", color = SoftGrey) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = CoffeeGold, unfocusedBorderColor = SoftGrey
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = storeType,
                    onValueChange = { storeType = it },
                    label = { Text(if (isArabic) "نشاط المتجر (مثال: مقهى، مطعم)" else "Store Type (e.g. Cafe, Retail)", color = SoftGrey) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = CoffeeGold, unfocusedBorderColor = SoftGrey
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { currentStep = 3 },
                    colors = ButtonDefaults.buttonColors(containerColor = CoffeeGold),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(if (isArabic) "التالي (Next)" else "Next", color = EspressoBlack, fontWeight = FontWeight.Bold)
                }
            }
            3 -> {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(80.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = if (isArabic) "تم إنشاء متجرك بنجاح!" else "Store Created Successfully!",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isArabic) "تم تخصيص Merchant ID آمن لك. بياناتك الآن معزولة وجاهزة للاستخدام." else "Secure Merchant ID allocated. Your data is isolated and ready.",
                    color = SoftGrey,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { viewModel.onboardMerchant(storeName, storeType) },
                    colors = ButtonDefaults.buttonColors(containerColor = CoffeeGold),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(if (isArabic) "الذهاب للوحة النمو (Proceed to Dashboard)" else "Proceed to Dashboard", color = EspressoBlack, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MerchantSetupScreen(
    isArabic: Boolean,
    currentPlan: String,
    onPlanSelected: (String) -> Unit,
    selectedTemplate: CampaignTemplate?,
    onTemplateSelected: (CampaignTemplate) -> Unit,
    onComplete: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = if (isArabic) "إعداد حسابك" else "Setup Your Account",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = if (isArabic) "اختر باقة تناسب حجم متجرك وقالب حملة للبدء السريع." else "Choose a plan that fits your business and a template for a quick start.",
                color = SoftGrey,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
        }
        item {
            MerchantPlanSelectorCard(isArabic, currentPlan, onPlanSelected)
        }
        item {
            CampaignTemplateSelector(isArabic, selectedTemplate, onTemplateSelected)
        }
        item {
            Button(
                onClick = onComplete,
                colors = ButtonDefaults.buttonColors(containerColor = CoffeeGold),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = selectedTemplate != null
            ) {
                Text(
                    if (isArabic) "متابعة للوحة النمو (Continue to Dashboard)" else "Continue to Dashboard", 
                    color = EspressoBlack, 
                    fontWeight = FontWeight.Bold
                )
            }
            if (selectedTemplate == null) {
                Text(
                    if (isArabic) "* يرجى اختيار قالب للاستمرار" else "* Please select a template to continue",
                    color = Color(0xFFFFB74D),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Text(
                    if (isArabic) "💡 نصيحة: ابدأ بإحدى الباقات الأساسية ثم وسّع أرباحك لاحقاً." else "💡 Tip: Start with a basic plan and scale your revenue later.",
                    color = SoftGrey,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun MerchantDashboard(
    viewModel: LoyaltyViewModel,
    campaigns: List<Campaign>,
    isArabic: Boolean
) {
    val scans by viewModel.merchantScans.collectAsStateWithLifecycle(initialValue = 0)
    val conversion by viewModel.merchantConversionRate.collectAsStateWithLifecycle(initialValue = 0)
    val revenue by viewModel.merchantExpectedRevenue.collectAsStateWithLifecycle(initialValue = 0)
    
    val currentPlan = viewModel.merchantSelectedPlan
    val selectedTemplate = viewModel.merchantSelectedTemplate
    val storeName = viewModel.merchantStoreName

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            MerchantPanelTitle(isArabic)
        }

        if (campaigns.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                    border = BorderStroke(1.dp, CoffeeGold),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.Campaign, contentDescription = null, tint = CoffeeGold, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isArabic) "مرحباً يا ${storeName.ifEmpty { "تاجرنا العزيز" }}!" else "Welcome, ${storeName.ifEmpty { "Merchant" }}!",
                            color = CoffeeGold,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isArabic) "حسابك جاهز ضمن باقة ($currentPlan)." else "Your workspace is ready on the ($currentPlan) plan.",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isArabic) "ابدأ بإنشاء أول حملة لزيادة مبيعاتك واكتشف القوة في الاحتفاظ بالعملاء." else "Create your first campaign to boost sales and discover the power of retention.",
                            color = SoftGrey,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            item {
                MerchantKpiHeader(isArabic, currentPlan, campaigns.size, scans, conversion, revenue) { viewModel.merchantSelectedPlan = "Pro" }
            }
        }

        item {
            RetentionFunnelCard(isArabic)
        }

        item {
            CampaignEstimatorCard(isArabic)
        }
        item {
            GeoBoostUpsellCard(isArabic, currentPlan) { viewModel.merchantSelectedPlan = "Pro" }
        }
        item {
            CampaignCreationForm(isArabic, viewModel, selectedTemplate, campaigns.size)
        }
        item {
            MerchantAuditLogCard(isArabic, viewModel)
        }
    }
}

@Composable
fun MerchantPanelTitle(isArabic: Boolean) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = CoffeeGold, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isArabic) "لوحة نمو المبيعات (Merchant Growth Panel)" else "Merchant Growth Panel",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Text(
            text = if (isArabic) "نـظرة حية على الإيرادات ومعدلات الاحتفاظ بالعملاء" else "Live revenue & retention at a glance",
            color = SoftGrey,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 36.dp, top = 2.dp)
        )
    }
}

@Composable
fun MerchantKpiHeader(isArabic: Boolean, currentPlan: String, campaignsCount: Int, scans: Int, conversion: Int, revenue: Int, onUpgrade: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianDark),
        border = BorderStroke(1.dp, GlassWhite),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(if (isArabic) "الباقة الحالية: $currentPlan" else "Current Plan: $currentPlan", color = CoffeeGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(if (isArabic) "حملات مستخدمة: $campaignsCount/3 هذا الشهر" else "Used Campaigns: $campaignsCount/3 this month", color = SoftGrey, fontSize = 12.sp)
                }
                if (currentPlan != "Pro") {
                    Button(
                        onClick = onUpgrade,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(if (isArabic) "ترقية (Upgrade)" else "Upgrade", color = EspressoBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(if (isArabic) "أنت على أفضل باقة" else "You are on the best plan", color = Color(0xFF81C784), fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = GlassWhite, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$scans", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                    Text(if (isArabic) "مسحات اليوم" else "Scans Today", color = SoftGrey, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$conversion%", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), fontSize = 18.sp)
                    Text(if (isArabic) "معدل التحويل" else "Conversion", color = SoftGrey, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$revenue SR", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                    Text(if (isArabic) "إيراد متوقع" else "Expected Rev", color = SoftGrey, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun MerchantPlanSelectorCard(isArabic: Boolean, currentPlan: String, onPlanSelected: (String) -> Unit) {
    var expandedPlan by remember { mutableStateOf<String?>(null) }

    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianDark),
        border = BorderStroke(1.dp, GlassWhite),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(if (isArabic) "اختر باقتك (Choose your plan)" else "Choose your plan", color = CoffeeGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val onPlanClick = { plan: String ->
                    onPlanSelected(plan)
                    expandedPlan = if (expandedPlan == plan) null else plan
                }
                PlanOption(isArabic, "Free Trial", currentPlan == "Free Trial", Modifier.weight(1f)) { onPlanClick("Free Trial") }
                PlanOption(isArabic, "Basic", currentPlan == "Basic", Modifier.weight(1f)) { onPlanClick("Basic") }
                PlanOption(isArabic, "Pro", currentPlan == "Pro", Modifier.weight(1f)) { onPlanClick("Pro") }
            }
            
            androidx.compose.animation.AnimatedVisibility(
                visible = expandedPlan != null,
                enter = androidx.compose.animation.expandVertically(expandFrom = Alignment.Top),
                exit = androidx.compose.animation.shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    val features = when (expandedPlan) {
                        "Free Trial" -> if (isArabic) listOf("حملة واحدة نشطة", "تحليلات أساسية", "بدون تنبيهات جغرافية", "مجاني لمدة ١٤ يوم") 
                                        else listOf("1 Active Campaign", "Basic Analytics", "No Geofencing", "Free for 14 days")
                        "Basic" -> if (isArabic) listOf("٣ حملات نشطة", "تحليلات قياسية", "بدون تنبيهات جغرافية", "١٩٩ ريال / شهر") 
                                   else listOf("3 Active Campaigns", "Standard Analytics", "No Geofencing", "199 SR / month")
                        "Pro" -> if (isArabic) listOf("حملات غير محدودة", "تحليلات متقدمة", "تنبيهات جغرافية (Geo-Boost)", "٣٩٩ ريال / شهر") 
                                 else listOf("Unlimited Campaigns", "Advanced Analytics", "Geofencing Included", "399 SR / month")
                        else -> emptyList()
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EspressoBlack, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = if (isArabic) "مميزات الباقة:" else "Plan Features:", 
                            color = Color.White, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        features.forEach { feature ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically, 
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(feature, color = SoftGrey, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlanOption(isArabic: Boolean, planTitle: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CoffeeGold.copy(alpha = 0.2f) else EspressoBlack)
            .border(1.dp, if (isSelected) CoffeeGold else GlassWhite, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(planTitle, color = if (isSelected) CoffeeGold else SoftGrey, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun RetentionFunnelCard(isArabic: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = EspressoBlack),
        border = BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isArabic) "كيف تضاعف هذه الحملة أرباحك؟" else "Why this campaign works?",
                color = Color(0xFF81C784),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val stepsEn = listOf("Scan QR", "Save Coupon", "Redeem", "Return")
                val stepsAr = listOf("مسح QR", "حفظ لمحفظة", "استرداد", "عودة للزيارة")
                val steps = if (isArabic) stepsAr else stepsEn
                val icons = listOf(Icons.Filled.QrCodeScanner, Icons.Filled.AccountBalanceWallet, Icons.Filled.Redeem, Icons.Filled.Repeat)

                steps.forEachIndexed { index, step ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF2E7D32).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icons[index], contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(step, fontSize = 9.sp, color = Color.White, textAlign = TextAlign.Center)
                    }
                    if (index < steps.size - 1) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = SoftGrey, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CampaignTemplateSelector(isArabic: Boolean, selectedTemplate: CampaignTemplate?, onSelect: (CampaignTemplate) -> Unit) {
    val templates = listOf(
        CampaignTemplate("Free Coffee", "قهوة مجانية", "Get 1 free coffee on your first visit", "احصل على قهوة مجانية في زيارتك الأولى", "FREECOFFEE", "#4E342E"),
        CampaignTemplate("Buy 1 Get 1", "اشتر ١ واحصل على ١", "Buy one drink get one free", "اشتر مشروب واحصل على الثاني مجاناً", "BOGO", "#5E35B1"),
        CampaignTemplate("Referral Bonus", "مكافأة دعوة", "Refer a friend and get 20% off", "قم بدعوة صديق واحصل على خصم ٢٠٪", "REFER20", "#1E88E5")
    )
    
    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianDark),
        border = BorderStroke(1.dp, GlassWhite),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AutoAwesomeMosaic, contentDescription = null, tint = CoffeeGold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isArabic) "اختر قالب حملة جاهز (Templates)" else "Choose a Campaign Template",
                    color = CoffeeGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(templates) { template ->
                    val isSelected = selectedTemplate?.code == template.code
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) CoffeeGold.copy(alpha = 0.1f) else EspressoBlack),
                        border = BorderStroke(1.dp, if (isSelected) CoffeeGold else GlassWhite),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .width(160.dp)
                            .clickable { onSelect(template) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(android.graphics.Color.parseColor(template.color))))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(if (isArabic) template.titleAr else template.titleEn, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(if (isArabic) template.descAr else template.descEn, color = SoftGrey, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CampaignEstimatorCard(isArabic: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianDark),
        border = BorderStroke(1.dp, GlassWhite),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Analytics, contentDescription = null, tint = CoffeeGold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isArabic) "مؤشرات الأداء المتوقعة للحملة" else "Campaign Performance Estimator",
                    color = CoffeeGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (isArabic) "المسحات المتوقعة (Scans):" else "Estimated Scans:", color = SoftGrey, fontSize = 12.sp)
                Text("500 - 800", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (isArabic) "الاسترداد المتوقع (Redemptions):" else "Estimated Redemptions:", color = SoftGrey, fontSize = 12.sp)
                Text("150 - 200 (25%)", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (isArabic) "معدل الزيارات المتكررة (Repeat Rate):" else "Repeat Visit Rate:", color = SoftGrey, fontSize = 12.sp)
                Text("+ 30%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (isArabic) "نمو الإيراد (Revenue Uplift):" else "Revenue Uplift:", color = SoftGrey, fontSize = 12.sp)
                Text("▲ +15%", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun GeoBoostUpsellCard(isArabic: Boolean, currentPlan: String, onUpgrade: () -> Unit) {
    if (currentPlan == "Pro") {
         Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E88E5).copy(alpha = 0.1f)),
            border = BorderStroke(1.dp, Color(0xFF1E88E5).copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF1E88E5).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Radar, contentDescription = null, tint = Color(0xFF90CAF9), modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) "التنبيهات الجغرافية مفعلة ✓" else "Geofencing Active ✓",
                        color = Color(0xFF90CAF9),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (isArabic) "الحملة الآن ترسل إشعارات تلقائية للعملاء القريبين." else "Campaign is ready to send automated push notifications to nearby users.",
                        color = SoftGrey,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    } else {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E88E5).copy(alpha = 0.1f)),
            border = BorderStroke(1.dp, Color(0xFF1E88E5).copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF1E88E5).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Radar, contentDescription = null, tint = Color(0xFF90CAF9), modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) "فعّل التنبيهات الجغرافية (Geo-Boost) 🚀" else "Unlock Geofencing (Geo-Boost) 🚀",
                        color = Color(0xFF90CAF9),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (isArabic) "أرسل إشعارات تلقائية للعملاء القريبين لزيادة الزيارات 4x. ترقية للباقة المتقدمة مطلوبة." else "Send automated push notifications to nearby users to multiply visits 4x. Pro plan required.",
                        color = SoftGrey,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
                Button(
                    onClick = onUpgrade,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp).padding(start = 8.dp)
                ) {
                     Text(if (isArabic) "ترقية" else "Upgrade", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CampaignCreationForm(isArabic: Boolean, viewModel: LoyaltyViewModel, selectedTemplate: CampaignTemplate?, campaignCount: Int) {
    // Campaign Input form states
    var cafeNameEn by remember { mutableStateOf("") }
    var cafeNameAr by remember { mutableStateOf("") }
    var couponTitleEn by remember(selectedTemplate) { mutableStateOf(selectedTemplate?.titleEn ?: "") }
    var couponTitleAr by remember(selectedTemplate) { mutableStateOf(selectedTemplate?.titleAr ?: "") }
    var couponDescEn by remember(selectedTemplate) { mutableStateOf(selectedTemplate?.descEn ?: "") }
    var couponDescAr by remember(selectedTemplate) { mutableStateOf(selectedTemplate?.descAr ?: "") }
    var customCode by remember(selectedTemplate) { mutableStateOf(selectedTemplate?.code ?: "") }
    var selectionColorHex by remember(selectedTemplate) { mutableStateOf(selectedTemplate?.color ?: "#4E342E") }

    val presetColors = listOf("#4E342E", "#5E35B1", "#1E88E5", "#00897B", "#558B2F", "#D81B60")

    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianDark),
        border = BorderStroke(1.dp, GlassWhite),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (isArabic) "أضف ببيانات المتجر والحملة" else "Campaign Launch Form",
                color = CoffeeGold,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            // Merchant Name
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = cafeNameEn,
                    onValueChange = { cafeNameEn = it },
                    label = { Text("Name (English)", fontSize = 11.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("merchant_en_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = CoffeeGold, unfocusedBorderColor = SoftGrey
                    ),
                    singleLine = true
                )
                OutlinedTextField(
                    value = cafeNameAr,
                    onValueChange = { cafeNameAr = it },
                    label = { Text("الاسم (بالعربي)", fontSize = 11.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("merchant_ar_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = CoffeeGold, unfocusedBorderColor = SoftGrey
                    ),
                    singleLine = true
                )
            }

            // Coupon Title
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = couponTitleEn,
                    onValueChange = { couponTitleEn = it },
                    label = { Text("Voucher (English)", fontSize = 11.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("coupon_en_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = CoffeeGold, unfocusedBorderColor = SoftGrey
                    ),
                    singleLine = true
                )
                OutlinedTextField(
                    value = couponTitleAr,
                    onValueChange = { couponTitleAr = it },
                    label = { Text("العرض (بالعربي)", fontSize = 11.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("coupon_ar_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = CoffeeGold, unfocusedBorderColor = SoftGrey
                    ),
                    singleLine = true
                )
            }

            // Coupon Description
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = couponDescEn,
                    onValueChange = { couponDescEn = it },
                    label = { Text("Terms (English)", fontSize = 11.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("terms_en_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = CoffeeGold, unfocusedBorderColor = SoftGrey
                    ),
                    maxLines = 2
                )
                OutlinedTextField(
                    value = couponDescAr,
                    onValueChange = { couponDescAr = it },
                    label = { Text("الشروط والوصف بالعربية", fontSize = 11.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("terms_ar_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = CoffeeGold, unfocusedBorderColor = SoftGrey
                    ),
                    maxLines = 2
                )
            }

            // Coupon Code
            OutlinedTextField(
                value = customCode,
                onValueChange = { customCode = it },
                label = { Text(if (isArabic) "كود القسيمة (مثال: FREECOFFEE)" else "Promo Code Symbol", fontSize = 11.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("code_symbol_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = CoffeeGold, unfocusedBorderColor = SoftGrey
                ),
                singleLine = true
            )

            // Select Color theme
            Text(
                text = if (isArabic) "اختر لون بطاقة المحفظة:" else "Choose Wallet Theme Tint:",
                fontSize = 12.sp,
                color = SoftGrey
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                presetColors.forEach { col ->
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(col)))
                            .border(
                                width = if (selectionColorHex == col) 2.dp else 0.dp,
                                color = if (selectionColorHex == col) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectionColorHex = col }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Launch button
            val plan = viewModel.merchantSelectedPlan
            val canCreate = when (plan) {
                "Free Trial" -> campaignCount < 1
                "Basic" -> campaignCount < 3
                else -> true // Pro
            }

            Button(
                onClick = {
                    if (!canCreate) return@Button
                    if (cafeNameEn.isNotBlank() && cafeNameAr.isNotBlank() && customCode.isNotBlank()) {
                        viewModel.createMerchantCampaign(
                            name = cafeNameEn.trim(),
                            nameAr = cafeNameAr.trim(),
                            title = if (couponTitleEn.isBlank()) "Promo" else couponTitleEn.trim(),
                            titleAr = if (couponTitleAr.isBlank()) "عرض خاص" else couponTitleAr.trim(),
                            desc = if (couponDescEn.isBlank()) "No terms" else couponDescEn.trim(),
                            descAr = if (couponDescAr.isBlank()) "تسري طبقاً للشروط" else couponDescAr.trim(),
                            code = customCode.trim().uppercase(),
                            color = selectionColorHex
                        )
                        viewModel.merchantActivityLogs.add("Created campaign: ${couponTitleEn.trim().ifEmpty { "Promo" }}")

                        // Clear the form
                        cafeNameEn = ""
                        cafeNameAr = ""
                        couponTitleEn = ""
                        couponTitleAr = ""
                        couponDescEn = ""
                        couponDescAr = ""
                        customCode = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (canCreate) CoffeeGold else SoftGrey),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("merchant_submit_campaign"),
                shape = RoundedCornerShape(10.dp),
                enabled = canCreate
            ) {
                Text(
                    text = if (!canCreate) {
                        if (isArabic) "عذراً، وصلت للحد الأقصى بحسب باقتك" else "Plan limit reached"
                    } else {
                        if (isArabic) "إطلاق الحملة (Launch Campaign)" else "Deploy Campaign"
                    },
                    fontWeight = FontWeight.Bold, 
                    color = EspressoBlack
                )
            }
        }
    }
}

@Composable
fun MerchantAuditLogCard(isArabic: Boolean, viewModel: LoyaltyViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = EspressoBlack),
        border = BorderStroke(1.dp, GlassWhite),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.History, contentDescription = null, tint = SoftGrey, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isArabic) "سجل النشاطات (Activity Logs)" else "Activity Logs",
                    color = SoftGrey,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            if (viewModel.merchantActivityLogs.isEmpty()) {
                Text(
                    text = if (isArabic) "لا توجد نشاطات مسجلة بعد." else "No activity logged yet.",
                    color = SoftGrey,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                viewModel.merchantActivityLogs.toList().reversed().take(5).forEach { log ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(CoffeeGold))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = log,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                    Divider(color = GlassWhite.copy(alpha = 0.5f), thickness = 0.5.dp)
                }
            }
        }
    }
}
