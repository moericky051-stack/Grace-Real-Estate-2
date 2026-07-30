package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RealEstateBlue
import com.example.ui.theme.RealEstateGold
import com.example.ui.theme.RealEstateGreen
import com.example.ui.theme.RealEstateNavy
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    initialPriceLakhs: Double = 3500.0
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Loan Estimator, 1: Rental Yield

    // Loan Estimator State
    var propertyPriceText by remember { mutableStateOf(initialPriceLakhs.toInt().toString()) }
    var downPaymentPercent by remember { mutableFloatStateOf(30f) }
    var interestRateYearly by remember { mutableFloatStateOf(8.5f) }
    var loanYears by remember { mutableIntStateOf(15) }

    // Rent Yield State
    var rentMonthlyText by remember { mutableStateOf("25") }
    var propValueText by remember { mutableStateOf("3500") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Calculate, contentDescription = null, tint = RealEstateGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "အိမ်ခြံမြေ တွက်ချက်စက် (Calculators)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                },
                modifier = Modifier.height(48.dp),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RealEstateNavy)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = RealEstateNavy,
                contentColor = RealEstateGold
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("ဘဏ်ချေးငွေ တွက်ချက်စက်", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("ငှားရမ်းမှု အကျိုးအမြတ် (Yield)", fontWeight = FontWeight.Bold) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (selectedTab == 0) {
                    // Loan Estimator Form
                    val price = propertyPriceText.toDoubleOrNull() ?: 3500.0
                    val downPaymentAmount = price * (downPaymentPercent / 100.0)
                    val loanAmount = price - downPaymentAmount

                    val monthlyRate = (interestRateYearly / 100.0) / 12.0
                    val totalMonths = loanYears * 12
                    val monthlyPayment = if (monthlyRate > 0) {
                        (loanAmount * (monthlyRate * (1 + monthlyRate).pow(totalMonths.toDouble()))) /
                                ((1 + monthlyRate).pow(totalMonths.toDouble()) - 1)
                    } else loanAmount / totalMonths

                    val totalPayment = monthlyPayment * totalMonths
                    val totalInterest = totalPayment - loanAmount

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("၁။ အိမ်ခြံမြေ တန်ဖိုး (သိန်းကျပ်)", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = propertyPriceText,
                                onValueChange = { propertyPriceText = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("၂။ စရိတ်ငွေ (Down Payment)", fontWeight = FontWeight.Bold)
                                Text(
                                    "${downPaymentPercent.toInt()}% (${String.format("%.1f", downPaymentAmount)} သိန်း)",
                                    fontWeight = FontWeight.Bold,
                                    color = RealEstateNavy
                                )
                            }
                            Slider(
                                value = downPaymentPercent,
                                onValueChange = { downPaymentPercent = it },
                                valueRange = 10f..50f,
                                steps = 7,
                                colors = SliderDefaults.colors(thumbColor = RealEstateGold, activeTrackColor = RealEstateNavy)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("၃။ နှစ်စဉ် အတိုးနှုန်း (Interest %)", fontWeight = FontWeight.Bold)
                                Text("${String.format("%.1f", interestRateYearly)}%", fontWeight = FontWeight.Bold, color = RealEstateNavy)
                            }
                            Slider(
                                value = interestRateYearly,
                                onValueChange = { interestRateYearly = it },
                                valueRange = 4f..15f,
                                steps = 21,
                                colors = SliderDefaults.colors(thumbColor = RealEstateGold, activeTrackColor = RealEstateNavy)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("၄။ ချေးငွေ သက်တမ်း (Loan Tenure)", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(5, 10, 15, 20, 25).forEach { yrs ->
                                    FilterChip(
                                        selected = loanYears == yrs,
                                        onClick = { loanYears = yrs },
                                        label = { Text("$yrs နှစ်") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = RealEstateNavy,
                                            selectedLabelColor = RealEstateGold
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Calculation Results Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = RealEstateNavy)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("တွက်ချက်မှု ရလဒ် (Loan Summary)", color = RealEstateGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("လစဉ် ဆပ်ရမည့် ပမာဏ:", color = Color.White, fontSize = 14.sp)
                                Text(
                                    "${String.format("%.2f", monthlyPayment)} သိန်း / လ",
                                    color = RealEstateGold,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(12.dp))

                            ResultRow("မူလ မတည်ငွေ (Down Payment):", "${String.format("%.1f", downPaymentAmount)} သိန်း")
                            ResultRow("ဘဏ်ချေးငွေ ပမာဏ (Principal):", "${String.format("%.1f", loanAmount)} သိန်း")
                            ResultRow("စုစုပေါင်း ပေးရမည့် အတိုး:", "${String.format("%.1f", totalInterest)} သိန်း")
                            ResultRow("စုစုပေါင်း ပေးချေရမည့် ပမာဏ:", "${String.format("%.1f", totalPayment + downPaymentAmount)} သိန်း")

                            Spacer(modifier = Modifier.height(16.dp))

                            // Visual Breakdown Bar Chart
                            Text("ငွေပေးချေမှု အချိုးအစား (Breakdown)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            val principalRatio = (loanAmount / (totalPayment + downPaymentAmount)).toFloat()
                            val interestRatio = (totalInterest / (totalPayment + downPaymentAmount)).toFloat()

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(20.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height
                                    val dpW = w * (downPaymentAmount / (totalPayment + downPaymentAmount)).toFloat()
                                    val pW = w * principalRatio

                                    drawRect(color = RealEstateGreen, topLeft = Offset(0f, 0f), size = Size(dpW, h))
                                    drawRect(color = RealEstateGold, topLeft = Offset(dpW, 0f), size = Size(pW, h))
                                    drawRect(color = Color(0xFFEF4444), topLeft = Offset(dpW + pW, 0f), size = Size(w - (dpW + pW), h))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                LegendItem(color = RealEstateGreen, text = "Down Payment")
                                LegendItem(color = RealEstateGold, text = "Principal Loan")
                                LegendItem(color = Color(0xFFEF4444), text = "Interest")
                            }
                        }
                    }

                } else {
                    // Rental Yield Calculator
                    val monthlyRent = rentMonthlyText.toDoubleOrNull() ?: 25.0
                    val propValue = propValueText.toDoubleOrNull() ?: 3500.0

                    val annualRent = monthlyRent * 12.0
                    val yieldPercent = if (propValue > 0) (annualRent / propValue) * 100.0 else 0.0
                    val sixMonthDeposit = monthlyRent * 6.0
                    val agentFee = monthlyRent * 1.0 // 1 month agent fee

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("၁။ လစဉ် အိမ်ငှားခ စျေးနှုန်း (သိန်းကျပ်)", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = rentMonthlyText,
                                onValueChange = { rentMonthlyText = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text("၂။ အိမ်ခြံမြေ ဝယ်ယူထားသည့် စျေးနှုန်း (သိန်းကျပ်)", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = propValueText,
                                onValueChange = { propValueText = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = RealEstateNavy)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("ငှားရမ်းမှု အကျိုးအမြတ် ရလဒ် (Rental Yield)", color = RealEstateGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("နှစ်စဉ် အကျိုးအမြတ် ရာခိုင်နှုန်း (Yield %):", color = Color.White, fontSize = 14.sp)
                                Text(
                                    "${String.format("%.2f", yieldPercent)}%",
                                    color = RealEstateGold,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(12.dp))

                            ResultRow("နှစ်စဉ် ရရှိမည့် အိမ်ငှားခ:", "${String.format("%.1f", annualRent)} သိန်း")
                            ResultRow("၆ လစာ စရိတ်ငွေ (Upfront Deposit):", "${String.format("%.1f", sixMonthDeposit)} သိန်း")
                            ResultRow("အကျိုးဆောင်ခ ခန့်မှန်း (၁ လစာ):", "${String.format("%.1f", agentFee)} သိန်း")
                            ResultRow("အိမ်ငှားစတင်ချိန် ပေးရမည့် စုစုပေါင်း:", "${String.format("%.1f", sixMonthDeposit + agentFee)} သိန်း")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, color = Color.White, fontSize = 10.sp)
    }
}
