package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Property
import com.example.ui.components.PropertyImage
import com.example.ui.components.MapPreviewCard
import com.example.ui.theme.RealEstateBlue
import com.example.ui.theme.RealEstateGold
import com.example.ui.theme.RealEstateGreen
import com.example.ui.theme.RealEstateNavy
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailScreen(
    property: Property?,
    isOwner: Boolean = false,
    isAdmin: Boolean = false,
    isSignedIn: Boolean = false,
    onBackClick: () -> Unit,
    onFavoriteToggle: (Property) -> Unit,
    onCalculateLoanClick: (Double) -> Unit,
    onEditClick: ((Property) -> Unit)? = null,
    onDeleteClick: ((Long) -> Unit)? = null,
    onOpenAuthClick: (() -> Unit)? = null
) {
    if (property == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = RealEstateNavy)
        }
        return
    }

    val context = LocalContext.current
    val imageResId = context.resources.getIdentifier(
        property.imageResName, "drawable", context.packageName
    ).let { id -> if (id != 0) id else R.drawable.img_hero_banner }

    var showContactDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val imageList = remember(property.imageResName) {
        if (property.imageResName.contains(",")) {
            property.imageResName.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            listOf(property.imageResName)
        }
    }
    var currentImageIndex by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            // Bottom Action Bar with Call & Chat Buttons
            Surface(
                shadowElevation = 12.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "စျေးနှုန်း", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = if (property.priceLakhs >= 1000) {
                                String.format("%.1f", property.priceLakhs / 1000.0) + " သောင်း"
                            } else {
                                "${property.priceLakhs.toInt()} သိန်း"
                            },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = RealEstateNavy
                        )
                    }

                    // Direct Phone Call Button
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${property.agentPhone}")
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RealEstateNavy),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Filled.Phone, contentDescription = "Call", tint = RealEstateGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ဖုန်းခေါ်မည်", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    // SMS / Chat Button
                    OutlinedButton(
                        onClick = { showContactDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Filled.Chat, contentDescription = "Message", tint = RealEstateBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("မေးမြန်းမည်", color = RealEstateBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Image Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                val activeImg = imageList.getOrNull(currentImageIndex) ?: property.imageResName
                PropertyImage(
                    imageResName = activeImg,
                    contentDescription = property.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.5f)
                                )
                            )
                        )
                )

                // Top Nav Buttons (Back, Favorite, Share)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isOwner && onEditClick != null) {
                            IconButton(
                                onClick = { onEditClick(property) },
                                modifier = Modifier
                                    .background(RealEstateNavy.copy(alpha = 0.85f), CircleShape)
                                    .size(40.dp)
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit Listing", tint = RealEstateGold)
                            }
                        }

                        if (isOwner && onDeleteClick != null) {
                            IconButton(
                                onClick = { showDeleteConfirmDialog = true },
                                modifier = Modifier
                                    .background(Color.Red.copy(alpha = 0.85f), CircleShape)
                                    .size(40.dp)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete Listing", tint = Color.White)
                            }
                        }

                        IconButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "${property.title}\nစျေးနှုန်း: ${property.priceLakhs} သိန်း\nတည်နေရာ: ${property.township}, ${property.city}\nဆက်သွယ်ရန်: ${property.agentPhone}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "အိမ်ခြံမြေ မျှဝေမည်"))
                            },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(Icons.Outlined.Share, contentDescription = "Share", tint = Color.White)
                        }

                        IconButton(
                            onClick = { onFavoriteToggle(property) },
                            modifier = Modifier
                                .background(Color.White, CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (property.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (property.isFavorite) Color.Red else Color.Gray
                            )
                        }
                    }
                }

                // Photo Counter Badge and Dots/Thumbnails if multiple photos exist
                if (imageList.size > 1) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopEnd)
                            .offset(y = 52.dp)
                    ) {
                        Text(
                            text = "ဓာတ်ပုံ ${currentImageIndex + 1}/${imageList.size}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // Navigation Chevrons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .align(Alignment.Center),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                currentImageIndex = if (currentImageIndex > 0) currentImageIndex - 1 else imageList.size - 1
                            },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(Icons.Filled.ChevronLeft, contentDescription = "Prev Photo", tint = Color.White)
                        }

                        IconButton(
                            onClick = {
                                currentImageIndex = (currentImageIndex + 1) % imageList.size
                            },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(Icons.Filled.ChevronRight, contentDescription = "Next Photo", tint = Color.White)
                        }
                    }
                }

                // Listing Tag Badge
                val isRent = property.listingType == "RENT"
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isRent) RealEstateBlue else RealEstateGreen,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomStart)
                ) {
                    Text(
                        text = if (isRent) "ငှားရန် (Rent)" else "ဝယ်ရန်/ရောင်းရန် (Sale)",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Property Body Content
            Column(modifier = Modifier.padding(16.dp)) {
                // Title and Location
                Text(
                    text = property.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = RealEstateNavy
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Location",
                        tint = RealEstateGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${property.township}၊ ${property.city}၊ မြန်မာ။",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Key Property Specs Grid (Area, Bedrooms, Bathrooms, Floor, Furnishing, Deed)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "အဓိက အချက်အလက်များ (Property Highlights)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = RealEstateNavy
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SpecDetailItem(icon = Icons.Filled.SquareFoot, label = "အကျယ်အဝန်း", value = "${property.areaSqft} sqft")
                            SpecDetailItem(icon = Icons.Filled.Bed, label = "အိပ်ခန်း", value = "${property.bedrooms} ခန်း")
                            SpecDetailItem(icon = Icons.Filled.Bathtub, label = "ရေချိုးခန်း", value = "${property.bathrooms} ခန်း")
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SpecDetailItem(icon = Icons.Filled.Layers, label = "အလွှာ / အထပ်", value = property.floorLevel)
                            SpecDetailItem(icon = Icons.Filled.Chair, label = "ပရိဘောဂ", value = property.furnishing)
                            SpecDetailItem(icon = Icons.Filled.Assignment, label = "စာရွက်စာတမ်း", value = property.deedType)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Description
                Text(
                    text = "အသေးစိတ် ဖော်ပြချက် (Description)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RealEstateNavy
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = property.description,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Map & Neighborhood Card
                MapPreviewCard(
                    township = property.township,
                    city = property.city,
                    address = property.address
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Loan Estimator Preview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = RealEstateNavy),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("အိမ်ခြံမြေ ချေးငွေ တွက်ချက်စက်", color = RealEstateGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("လစဉ် ဆပ်ရမည့် ခန့်မှန်းပမာဏ တွက်ချက်ပါ", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            }
                            IconButton(onClick = { onCalculateLoanClick(property.priceLakhs) }) {
                                Icon(Icons.Filled.Calculate, contentDescription = "Calculate", tint = RealEstateGold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick estimate formula (e.g. 15-year term, 8% interest, 30% downpayment)
                        val principalLakhs = property.priceLakhs * 0.7
                        val monthlyRate = 0.08 / 12.0
                        val totalMonths = 15 * 12
                        val monthlyPaymentLakhs = (principalLakhs * (monthlyRate * (1 + monthlyRate).pow(totalMonths.toDouble()))) /
                                ((1 + monthlyRate).pow(totalMonths.toDouble()) - 1)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ခန့်မှန်း လစဉ်ပေးသွင်းငွေ (၁၅ နှစ်):", color = Color.White, fontSize = 12.sp)
                            Text(
                                text = "ခန့်မှန်း ~${String.format("%.2f", monthlyPaymentLakhs)} သိန်း / လ",
                                color = RealEstateGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { onCalculateLoanClick(property.priceLakhs) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("အသေးစိတ် တွက်ချက်ရန် ကြည့်မည် →", color = RealEstateGold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Agent Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = RealEstateNavy,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = RealEstateGold, modifier = Modifier.size(28.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = property.agentName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Filled.Verified, contentDescription = "Verified", tint = RealEstateBlue, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "${property.agentType} · ${property.agentPhone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Manage Listing Card (Visible to property owner for direct posts, or ONLY Admin for agency listings)
                val isAgencyListing = property?.agentType != "User Post" && property?.agentType != "Direct Post"
                val canManageListing = if (isAgencyListing) isAdmin else (isOwner || isAdmin)

                if (canManageListing) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAdmin) Color(0xFF1E3A8A).copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isAdmin) Icons.Filled.AdminPanelSettings else Icons.Filled.Settings,
                                    contentDescription = null,
                                    tint = RealEstateNavy,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isAdmin) "👑 Admin Full Control (ကြော်ငြာ စီမံခန့်ခွဲရန်)" else "ကြော်ငြာ စီမံခန့်ခွဲရန် (Manage Listing)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RealEstateNavy
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onEditClick?.invoke(property) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = RealEstateNavy),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = RealEstateGold, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ပြင်ဆင်မည်", color = RealEstateGold, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { showDeleteConfirmDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ဖျက်မည်", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else if (!isSignedIn) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "ကြော်ငြာ ပြင်ဆင်/ဖျက်ပိုင်ခွင့် အသိပေးချက်",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = RealEstateNavy
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "အကောင့်ဝင်ထားသော ပိုင်ရှင်များသာ မိမိတို့ ကြော်ငြာကို ပြင်ဆင်/ဖျက်ပိုင်ခွင့် (Edit/Delete) ရှိပါသည်။ Guest အဖြစ် ကြည့်ရှုသူများသည် Edit/Delete ပြုလုပ်ခွင့် မရှိပါ။",
                                fontSize = 11.5.sp,
                                color = Color.DarkGray
                            )
                            if (onOpenAuthClick != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                TextButton(
                                    onClick = onOpenAuthClick,
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("အကောင့်ဝင်ရန် နှိပ်ပါ →", color = RealEstateNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(32.dp))
            },
            title = {
                Text("ကြော်ငြာ ဖျက်ရန် သေချာပါသလား။", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("ဒီအိမ်ခြံမြေ ကြော်ငြာ (${property.title}) ကို ဖျက်ပစ်ပါက ပြန်လည်ရယူ၍ ရတော့မည် မဟုတ်ပါ။")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteClick?.invoke(property.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("သေချာသည်၊ ဖျက်မည်", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("မဖျက်တော့ပါ")
                }
            }
        )
    }

    // Contact Dialog
    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = { Text("အကျိုးဆောင် ဆက်သွယ်ရန်") },
            text = {
                Column {
                    Text("အမည်: ${property.agentName}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("ဖုန်း: ${property.agentPhone}")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Viber, SMS သို့မဟုတ် ဖုန်းဖြင့် တိုက်ရိုက် မေးမြန်းနိုင်ပါသည်။")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("smsto:${property.agentPhone}")
                            putExtra("sms_body", "မင်္ဂလာပါ၊ ${property.title} နှင့် ပတ်သက်ပြီး မေးမြန်းလိုပါသည်။")
                        }
                        context.startActivity(intent)
                        showContactDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RealEstateNavy)
                ) {
                    Text("SMS/Viber ပို့မည်")
                }
            },
            dismissButton = {
                TextButton(onClick = { showContactDialog = false }) {
                    Text("ပိတ်မည်")
                }
            }
        )
    }
}

@Composable
private fun SpecDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = RealEstateNavy, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 10.sp, color = Color.Gray)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RealEstateNavy)
    }
}
