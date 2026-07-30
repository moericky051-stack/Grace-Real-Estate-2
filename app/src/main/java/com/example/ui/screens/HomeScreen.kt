package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.firebase.UserProfile
import com.example.data.model.Property
import com.example.ui.components.AuthProfileSheet
import com.example.ui.components.FilterBottomSheet
import com.example.ui.components.PropertyCard
import com.example.ui.theme.AppThemeOption
import com.example.ui.theme.RealEstateBlue
import com.example.ui.theme.RealEstateGold
import com.example.ui.theme.RealEstateGreen
import com.example.ui.theme.RealEstateNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    properties: List<Property>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    selectedCity: String,
    onCitySelected: (String) -> Unit,
    selectedPropType: String,
    onPropTypeSelected: (String) -> Unit,
    maxPriceLakhs: Float,
    onPriceChanged: (Float) -> Unit,
    minBedrooms: Int,
    onBedroomsSelected: (Int) -> Unit,
    onFavoriteToggle: (Property) -> Unit,
    onPropertyClick: (Long) -> Unit,
    onPostNewClick: () -> Unit,
    onResetFilters: () -> Unit,
    isSyncing: Boolean = false,
    onRefreshSync: () -> Unit = {},
    selectedTheme: AppThemeOption = AppThemeOption.NAVY_GOLD,
    onThemeSelected: (AppThemeOption) -> Unit = {},
    userProfile: UserProfile? = null,
    isSignedIn: Boolean = false,
    myListings: List<Property> = emptyList(),
    onSignIn: (email: String, pass: String, onError: (String) -> Unit) -> Unit = { _, _, _ -> },
    onSignUp: (email: String, pass: String, name: String, phone: String, agency: String, onError: (String) -> Unit) -> Unit = { _, _, _, _, _, _ -> },
    onUpdateProfile: (name: String, phone: String, agency: String, onError: (String) -> Unit) -> Unit = { _, _, _, _ -> },
    onSignOut: () -> Unit = {},
    onEditProperty: (Property) -> Unit = {},
    onDeleteProperty: (Long) -> Unit = {},
    onOpenAuthClick: () -> Unit = {}
) {
    var isFilterSheetOpen by remember { mutableStateOf(false) }
    var isThemeDialogVisible by remember { mutableStateOf(false) }
    var isProfileSheetOpen by remember { mutableStateOf(false) }

    val cities = listOf(
        "ALL" to "မြို့အားလုံး",
        "Yangon" to "ရန်ကုန်",
        "Mandalay" to "မန္တလေး",
        "Naypyidaw" to "နေပြည်တော်",
        "Pyin Oo Lwin" to "ပြင်ဦးလွင်",
        "Taunggyi" to "တောင်ကြီး"
    )

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onPostNewClick,
                containerColor = RealEstateNavy,
                contentColor = RealEstateGold,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Icon(Icons.Filled.AddHome, contentDescription = "Post Property")
                Spacer(modifier = Modifier.width(8.dp))
                Text("အိမ်ခြံမြေ တင်မည်", fontWeight = FontWeight.Bold)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Bar
            Surface(
                color = selectedTheme.headerColor,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = RealEstateGold,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Home,
                                        contentDescription = null,
                                        tint = RealEstateNavy,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Grace အိမ် ခြံ မြေ",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "ဝယ် · ရောင်း · ငှား ရှာဖွေပါ",
                                    fontSize = 10.sp,
                                    color = RealEstateGold
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Firebase Profile Button
                            IconButton(
                                onClick = onOpenAuthClick,
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                    .size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSignedIn) Icons.Filled.AccountCircle else Icons.Filled.PersonOutline,
                                    contentDescription = "User Profile",
                                    tint = RealEstateGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Theme Change Palette Button
                            IconButton(
                                onClick = { isThemeDialogVisible = true },
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                    .size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Palette,
                                    contentDescription = "Change Theme",
                                    tint = RealEstateGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Sync Refresh Button
                            IconButton(
                                onClick = onRefreshSync,
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                    .size(32.dp)
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = RealEstateGold,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.CloudSync,
                                        contentDescription = "Sync Cloud",
                                        tint = RealEstateGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Filter Button with active indicator
                            BadgedBox(
                                badge = {
                                    if (selectedCity != "ALL" || selectedPropType != "ALL" || maxPriceLakhs < 20000f || minBedrooms > 0) {
                                        Badge(containerColor = RealEstateGold) {
                                            Text("!", color = RealEstateNavy, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                        }
                                    }
                                }
                            ) {
                                IconButton(
                                    onClick = { isFilterSheetOpen = true },
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                        .size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.FilterList,
                                        contentDescription = "Filter",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Listing Type Selector Tabs (အားလုံး | ဝယ်ရန် | ငှားရန် | ကျွန်ုပ်၏ ကြော်ငြာများ)
                    TabRow(
                        selectedTabIndex = when (selectedTab) {
                            "BUY" -> 1
                            "RENT" -> 2
                            "MY_LISTINGS" -> 3
                            else -> 0
                        },
                        containerColor = Color.Transparent,
                        contentColor = RealEstateGold,
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == "ALL",
                            onClick = { onTabSelected("ALL") },
                            text = { Text("အားလုံး", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.height(34.dp)
                        )
                        Tab(
                            selected = selectedTab == "BUY",
                            onClick = { onTabSelected("BUY") },
                            text = { Text("ဝယ်/ရောင်း", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.height(34.dp)
                        )
                        Tab(
                            selected = selectedTab == "RENT",
                            onClick = { onTabSelected("RENT") },
                            text = { Text("ငှားရန်", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.height(34.dp)
                        )
                        Tab(
                            selected = selectedTab == "MY_LISTINGS",
                            onClick = { onTabSelected("MY_LISTINGS") },
                            text = { Text("ကျွန်ုပ်၏ ကြော်ငြာများ", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.height(34.dp)
                        )
                    }
                }
            }

            // Quick City Chips Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(cities) { (key, label) ->
                        val isSelected = selectedCity == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCitySelected(key) },
                            label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = if (key != "ALL") {
                                { Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RealEstateNavy,
                                selectedLabelColor = RealEstateGold,
                                selectedLeadingIconColor = RealEstateGold
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }

            // Main Content Area (Property Feed in 2 Columns)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Header title & count (Spans both 2 columns)
                item(span = { GridItemSpan(2) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedTab == "MY_LISTINGS") "ကျွန်ုပ်၏ ကြော်ငြာများ (${properties.size})" else "အိမ်ခြံမြေ စာရင်းများ (${properties.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RealEstateNavy
                        )
                        if (selectedCity != "ALL" || selectedPropType != "ALL" || maxPriceLakhs < 20000f) {
                            TextButton(onClick = onResetFilters) {
                                Text("စစ်ထုတ်မှု ဖျက်မည်", fontSize = 12.sp, color = RealEstateBlue)
                            }
                        }
                    }
                }

                // Empty state if no properties found
                if (properties.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (selectedTab == "MY_LISTINGS") "သင် တင်ထားသော ကြော်ငြာ မရှိသေးပါ" else "ရှာဖွေမှုနှင့် ကိုက်ညီသော အိမ်ခြံမြေ မတွေ့ရှိပါ",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (selectedTab == "MY_LISTINGS") "'အိမ်ခြံမြေ တင်မည်' ခလုတ်ကို နှိပ်၍ အသစ် တင်နိုင်ပါသည်" else "အခြား မြို့ သို့မဟုတ် ဈေးနှုန်း အတိုင်းအတာဖြင့် ပြန်လည် ရှာဖွေကြည့်ပါ",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = onResetFilters,
                                    colors = ButtonDefaults.buttonColors(containerColor = RealEstateNavy)
                                ) {
                                    Text("မူလအတိုင်း ပြန်ကြည့်မည်", color = RealEstateGold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    items(properties, key = { it.id }) { property ->
                        PropertyCard(
                            property = property,
                            onFavoriteClick = { onFavoriteToggle(property) },
                            onCardClick = { onPropertyClick(property.id) }
                        )
                    }
                }
            }
        }
    }

    // Filter Bottom Sheet
    if (isFilterSheetOpen) {
        FilterBottomSheet(
            selectedCity = selectedCity,
            onCitySelected = onCitySelected,
            selectedPropType = selectedPropType,
            onPropTypeSelected = onPropTypeSelected,
            maxPriceLakhs = maxPriceLakhs,
            onPriceChanged = onPriceChanged,
            minBedrooms = minBedrooms,
            onBedroomsSelected = onBedroomsSelected,
            onReset = onResetFilters,
            onDismiss = { isFilterSheetOpen = false }
        )
    }

    // Auth & Profile Bottom Sheet
    if (isProfileSheetOpen) {
        AuthProfileSheet(
            userProfile = userProfile,
            isSignedIn = isSignedIn,
            myListings = myListings,
            onDismiss = { isProfileSheetOpen = false },
            onSignIn = onSignIn,
            onSignUp = onSignUp,
            onUpdateProfile = onUpdateProfile,
            onSignOut = onSignOut,
            onPropertyClick = { id ->
                isProfileSheetOpen = false
                onPropertyClick(id)
            },
            onEditProperty = { prop ->
                isProfileSheetOpen = false
                onEditProperty(prop)
            },
            onDeleteProperty = { id ->
                onDeleteProperty(id)
            }
        )
    }

    // Theme Selection Dialog
    if (isThemeDialogVisible) {
        AlertDialog(
            onDismissRequest = { isThemeDialogVisible = false },
            title = { Text("အပလီကေးရှင်း အရောင် Theme ရွေးချယ်ရန်", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppThemeOption.values().forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedTheme == theme) RealEstateNavy.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable {
                                    onThemeSelected(theme)
                                    isThemeDialogVisible = false
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(theme.headerColor)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = theme.titleMm,
                                fontWeight = if (selectedTheme == theme) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTheme == theme) RealEstateNavy else Color.Black
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isThemeDialogVisible = false }) {
                    Text("ပိတ်မည်")
                }
            }
        )
    }
}
