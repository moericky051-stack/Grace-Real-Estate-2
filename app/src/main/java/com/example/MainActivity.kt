package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.LoadingOverlay
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RealEstateGold
import com.example.ui.theme.RealEstateNavy
import com.example.ui.viewmodel.RealEstateViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: RealEstateViewModel = viewModel()
            val currentTheme by viewModel.selectedTheme.collectAsStateWithLifecycle()
            
            MyApplicationTheme(themeOption = currentTheme) {
                RealEstateApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun RealEstateApp(
    viewModel: RealEstateViewModel = viewModel()
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val filteredProperties by viewModel.filteredProperties.collectAsStateWithLifecycle()
    val favoriteProperties by viewModel.favoriteProperties.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val myListings by viewModel.myListings.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedListingTypeTab.collectAsStateWithLifecycle()
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val selectedPropType by viewModel.selectedPropertyType.collectAsStateWithLifecycle()
    val maxPriceLakhs by viewModel.maxPriceLakhs.collectAsStateWithLifecycle()
    val minBedrooms by viewModel.minBedrooms.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val loadingMessage by viewModel.loadingMessage.collectAsStateWithLifecycle()
    val currentTheme by viewModel.selectedTheme.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem("home", "ရှာဖွေမည်", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem("favorites", "သိမ်းဆည်းထားသော", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder, badgeCount = favoriteProperties.size),
        BottomNavItem("calculator", "တွက်ချက်စက်", Icons.Filled.Calculate, Icons.Outlined.Calculate),
        BottomNavItem("agents", "အကျိုးဆောင်များ", Icons.Filled.SupportAgent, Icons.Outlined.SupportAgent)
    )

    val showBottomBar = currentRoute in listOf("home", "favorites", "calculator", "agents")

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    Surface(
                        color = currentTheme.headerColor,
                        shadowElevation = 16.dp,
                        border = BorderStroke(1.dp, RealEstateGold.copy(alpha = 0.5f))
                    ) {
                        NavigationBar(
                            containerColor = currentTheme.headerColor,
                            contentColor = RealEstateGold,
                            tonalElevation = 0.dp,
                            modifier = Modifier.height(60.dp),
                            windowInsets = WindowInsets(0, 0, 0, 0)
                        ) {
                            bottomNavItems.forEach { item ->
                                val isSelected = currentRoute == item.route
                                NavigationBarItem(
                                    selected = isSelected,
                                    alwaysShowLabel = true,
                                    onClick = {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (item.badgeCount > 0) {
                                                    Badge(containerColor = RealEstateGold) {
                                                        Text("${item.badgeCount}", color = currentTheme.headerColor, fontWeight = FontWeight.ExtraBold, fontSize = 9.sp)
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = item.label,
                                                tint = if (isSelected) RealEstateGold else Color.White.copy(alpha = 0.85f),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = item.label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) RealEstateGold else Color.White.copy(alpha = 0.85f)
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = RealEstateGold.copy(alpha = 0.25f),
                                        selectedIconColor = RealEstateGold,
                                        unselectedIconColor = Color.White.copy(alpha = 0.85f),
                                        selectedTextColor = RealEstateGold,
                                        unselectedTextColor = Color.White.copy(alpha = 0.85f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                // 1. Home Feed
                composable("home") {
                    HomeScreen(
                        properties = filteredProperties,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.searchQuery.value = it },
                        selectedTab = selectedTab,
                        onTabSelected = { viewModel.selectedListingTypeTab.value = it },
                        selectedCity = selectedCity,
                        onCitySelected = { viewModel.selectedCity.value = it },
                        selectedPropType = selectedPropType,
                        onPropTypeSelected = { viewModel.selectedPropertyType.value = it },
                        maxPriceLakhs = maxPriceLakhs,
                        onPriceChanged = { viewModel.maxPriceLakhs.value = it },
                        minBedrooms = minBedrooms,
                        onBedroomsSelected = { viewModel.minBedrooms.value = it },
                        onFavoriteToggle = { property ->
                            viewModel.toggleFavorite(property)
                            val statusText = if (property.isFavorite) "အကြိုက်ဆုံးမှ ဖယ်ရှားလိုက်ပါပြီ" else "အကြိုက်ဆုံးစာရင်းသို့ ထည့်သွင်းလိုက်ပါပြီ"
                            scope.launch { snackbarHostState.showSnackbar(statusText) }
                        },
                        onPropertyClick = { propertyId ->
                            navController.navigate("detail/$propertyId")
                        },
                        onPostNewClick = {
                            if (viewModel.isUserSignedIn()) {
                                navController.navigate("post")
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Guest အသုံးပြုသူများ ကြော်ငြာ တင်ခွင့်မရှိပါ။ ကြော်ငြာတင်ရန် အကောင့်ဝင်ပါ သို့မဟုတ် ဖွင့်ပါ")
                                }
                                navController.navigate("auth")
                            }
                        },
                        onResetFilters = {
                            viewModel.resetFilters()
                        },
                        isSyncing = isSyncing,
                        onRefreshSync = {
                            viewModel.refreshCloudData { success ->
                                scope.launch {
                                    val msg = if (success) "အချက်အလက်များကို Cloud database နှင့် ချိတ်ဆက် update လုပ်ပြီးပါပြီ" else "Cloud ချိတ်ဆက်မှု မအောင်မြင်ပါ။ နောက်မှ ပြန်လည် ကြိုးစားပါ။"
                                    snackbarHostState.showSnackbar(msg)
                                }
                            }
                        },
                        selectedTheme = currentTheme,
                        onThemeSelected = { viewModel.setTheme(it) },
                        userProfile = userProfile,
                        isSignedIn = viewModel.isUserSignedIn(),
                        myListings = myListings,
                        onSignIn = { email, pass, onError ->
                            viewModel.signIn(email, pass, {
                                scope.launch { snackbarHostState.showSnackbar("အကောင့်ဝင်ရောက်ခြင်း အောင်မြင်ပါသည်။") }
                            }, onError)
                        },
                        onSignUp = { email, pass, name, phone, agency, photoUri, onError ->
                            viewModel.signUp(email, pass, name, phone, agency, photoUri, {
                                scope.launch { snackbarHostState.showSnackbar("အကောင့်သစ် အောင်မြင်စွာ ပြုလုပ်ပြီးပါပြီ။") }
                            }, onError)
                        },
                        onUpdateProfile = { name, phone, agency, onError ->
                            viewModel.updateProfileInfo(name, phone, agency, {
                                scope.launch { snackbarHostState.showSnackbar("ကိုယ်ရေးအချက်အလက် ပြင်ဆင်ပြီးပါပြီ။") }
                            }, onError)
                        },
                        onSignOut = {
                            viewModel.signOut()
                            scope.launch { snackbarHostState.showSnackbar("အကောင့်မှ ထွက်လိုက်ပါပြီ။") }
                        },
                        onEditProperty = { property ->
                            navController.navigate("edit/${property.id}")
                        },
                        onDeleteProperty = { propertyId ->
                            viewModel.deleteProperty(propertyId = propertyId, onSuccess = {
                                scope.launch { snackbarHostState.showSnackbar("ကြော်ငြာ ဖျက်ပြီးပါပြီ။") }
                            }, onError = { err ->
                                scope.launch { snackbarHostState.showSnackbar(err) }
                            })
                        },
                        onOpenAuthClick = {
                            navController.navigate("auth")
                        }
                    )
                }

                // Auth Screen (Full Screen)
                composable("auth") {
                    AuthScreen(
                        userProfile = userProfile,
                        isSignedIn = viewModel.isUserSignedIn(),
                        myListings = myListings,
                        onBackClick = { navController.popBackStack() },
                        onSignIn = { email, pass, onError ->
                            viewModel.signIn(email, pass, {
                                scope.launch { snackbarHostState.showSnackbar("အကောင့်ဝင်ရောက်ခြင်း အောင်မြင်ပါသည်။") }
                                navController.popBackStack()
                            }, onError)
                        },
                        onSignUp = { email, pass, name, phone, agency, photoUri, onError ->
                            viewModel.signUp(email, pass, name, phone, agency, photoUri, {
                                scope.launch { snackbarHostState.showSnackbar("အကောင့်သစ် အောင်မြင်စွာ ပြုလုပ်ပြီးပါပြီ။") }
                                navController.popBackStack()
                            }, onError)
                        },
                        onGoogleSignIn = { onError ->
                            viewModel.signInWithGoogle(onSuccess = {
                                scope.launch { snackbarHostState.showSnackbar("Google အကောင့်ဖြင့် အောင်မြင်စွာ ဝင်ရောက်လိုက်ပါပြီ။") }
                                navController.popBackStack()
                            }, onError = onError)
                        },
                        onSignInAsDemo = {
                            viewModel.signInAsDemoUser {
                                scope.launch { snackbarHostState.showSnackbar("Demo အကောင့်ဖြင့် အောင်မြင်စွာ ဝင်ရောက်လိုက်ပါပြီ။") }
                                navController.popBackStack()
                            }
                        },
                        onUpdateProfile = { name, phone, agency, onError ->
                            viewModel.updateProfileInfo(name, phone, agency, {
                                scope.launch { snackbarHostState.showSnackbar("ကိုယ်ရေးအချက်အလက် ပြင်ဆင်ပြီးပါပြီ။") }
                            }, onError)
                        },
                        onSignOut = {
                            viewModel.signOut()
                            scope.launch { snackbarHostState.showSnackbar("အကောင့်မှ ထွက်လိုက်ပါပြီ။") }
                        },
                        onPropertyClick = { propertyId ->
                            navController.navigate("detail/$propertyId")
                        },
                        onEditProperty = { property ->
                            navController.navigate("edit/${property.id}")
                        },
                        onDeleteProperty = { propertyId ->
                            viewModel.deleteProperty(propertyId = propertyId, onSuccess = {
                                scope.launch { snackbarHostState.showSnackbar("ကြော်ငြာ ဖျက်ပြီးပါပြီ။") }
                            }, onError = { err ->
                                scope.launch { snackbarHostState.showSnackbar(err) }
                            })
                        }
                    )
                }

                // 2. Property Detail Screen
                composable(
                    route = "detail/{propertyId}",
                    arguments = listOf(navArgument("propertyId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val propId = backStackEntry.arguments?.getLong("propertyId") ?: 0L
                    val property by viewModel.getPropertyById(propId).collectAsStateWithLifecycle()
                    val isSignedIn = viewModel.isUserSignedIn()
                    val isAdmin = viewModel.isAdmin()
                    val currentUid = viewModel.currentUserId()
                    val isAgencyListing = property != null && property?.agentType != "User Post" && property?.agentType != "Direct Post"
                    val isOwner = isSignedIn && property != null && (
                        (property?.userId?.isNotBlank() == true && property?.userId == currentUid) ||
                        (property?.userId.isNullOrBlank() && (property?.agentType == "Direct Post" || property?.agentType == "User Post"))
                    )
                    val canModify = if (isAgencyListing) isAdmin else (isOwner || isAdmin)

                    PropertyDetailScreen(
                        property = property,
                        isOwner = isOwner,
                        isAdmin = isAdmin,
                        isSignedIn = isSignedIn,
                        onBackClick = { navController.popBackStack() },
                        onFavoriteToggle = { prop ->
                            viewModel.toggleFavorite(prop)
                            val statusText = if (prop.isFavorite) "အကြိုက်ဆုံးမှ ဖယ်ရှားလိုက်ပါပြီ" else "အကြိုက်ဆုံးစာရင်းသို့ ထည့်သွင်းလိုက်ပါပြီ"
                            scope.launch { snackbarHostState.showSnackbar(statusText) }
                        },
                        onCalculateLoanClick = { priceLakhs ->
                            navController.navigate("calculator?price=$priceLakhs")
                        },
                        onEditClick = if (canModify) {
                            { prop -> navController.navigate("edit/${prop.id}") }
                        } else null,
                        onDeleteClick = if (canModify) {
                            { id ->
                                viewModel.deleteProperty(id, onSuccess = {
                                    scope.launch { snackbarHostState.showSnackbar("ကြော်ငြာ ဖျက်လိုက်ပါပြီ။") }
                                    navController.popBackStack()
                                }, onError = { err ->
                                    scope.launch { snackbarHostState.showSnackbar(err) }
                                })
                            }
                        } else null,
                        onOpenAuthClick = { navController.navigate("auth") }
                    )
                }

                // 3. Post Listing Screen
                composable("post") {
                    val isSignedIn = viewModel.isUserSignedIn()
                    if (!isSignedIn) {
                        LaunchedEffect(Unit) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Guest အသုံးပြုသူများ ကြော်ငြာ တင်ခွင့်မရှိပါ။ ကြော်ငြာတင်ရန် အကောင့်ဝင်ရောက်ပါ။")
                            }
                            navController.navigate("auth") {
                                popUpTo("post") { inclusive = true }
                            }
                        }
                    } else {
                        PostPropertyScreen(
                            onBackClick = { navController.popBackStack() },
                            onSubmitProperty = { title, listingType, propType, priceLakhs, pricePeriod, city, township, address, area, beds, baths, floor, furn, deed, desc, name, phone, imgRes ->
                                viewModel.postNewProperty(
                                    title, listingType, propType, priceLakhs, pricePeriod, city, township, address, area, beds, baths, floor, furn, deed, desc, name, phone, imgRes,
                                    onSuccess = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("ကြော်ငြာ အောင်မြင်စွာ တင်ပြီးပါပြီ။")
                                        }
                                        navController.popBackStack()
                                    },
                                    onError = { err ->
                                        scope.launch { snackbarHostState.showSnackbar(err) }
                                    }
                                )
                            }
                        )
                    }
                }

                // 3b. Edit Listing Screen
                composable(
                    route = "edit/{propertyId}",
                    arguments = listOf(navArgument("propertyId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val propId = backStackEntry.arguments?.getLong("propertyId") ?: 0L
                    val property by viewModel.getPropertyById(propId).collectAsStateWithLifecycle()
                    val isSignedIn = viewModel.isUserSignedIn()
                    val isAdmin = viewModel.isAdmin()
                    val currentUid = viewModel.currentUserId()
                    val isAgencyListing = property != null && property?.agentType != "User Post" && property?.agentType != "Direct Post"
                    val isOwner = isSignedIn && property != null && (
                        (property?.userId?.isNotBlank() == true && property?.userId == currentUid) ||
                        (property?.userId.isNullOrBlank() && (property?.agentType == "Direct Post" || property?.agentType == "User Post"))
                    )
                    val canEdit = if (isAgencyListing) isAdmin else (isOwner || isAdmin)

                    if (!isSignedIn || !canEdit) {
                        LaunchedEffect(Unit) {
                            val msg = if (!isSignedIn) "Guest အသုံးပြုသူများသည် ကြော်ငြာ ပြင်ဆင်ပိုင်ခွင့် မရှိပါ။ အကောင့်ဝင်ပါ။" 
                                      else if (isAgencyListing) "အကျိုးဆောင် ကြော်ငြာများကို Admin တစ်ဦးတည်းသာ ပြင်ဆင်ပိုင်ခွင့် ရှိပါသည်။"
                                      else "မိမိ ပိုင်ဆိုင်သော ကြော်ငြာများ သို့မဟုတ် Admin သာလျှင် ပြင်ဆင်ပိုင်ခွင့် ရှိပါသည်။"
                            snackbarHostState.showSnackbar(msg)
                            navController.popBackStack()
                        }
                    } else {
                        PostPropertyScreen(
                            onBackClick = { navController.popBackStack() },
                            existingProperty = property,
                            onUpdateProperty = { updated ->
                                viewModel.updateProperty(updated, onSuccess = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("ကြော်ငြာ ပြင်ဆင်ပြီးပါပြီ။")
                                    }
                                    navController.popBackStack()
                                }, onError = { err ->
                                    scope.launch { snackbarHostState.showSnackbar(err) }
                                })
                            },
                            onSubmitProperty = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> }
                        )
                    }
                }

                // 4. Saved / Favorites Screen
                composable("favorites") {
                    FavoritesScreen(
                        favoriteProperties = favoriteProperties,
                        onPropertyClick = { propertyId ->
                            navController.navigate("detail/$propertyId")
                        },
                        onFavoriteToggle = { property ->
                            viewModel.toggleFavorite(property)
                            scope.launch { snackbarHostState.showSnackbar("အကြိုက်ဆုံးမှ ဖယ်ရှားလိုက်ပါပြီ") }
                        }
                    )
                }

                // 5. Calculator Screen
                composable(
                    route = "calculator?price={price}",
                    arguments = listOf(navArgument("price") {
                        type = NavType.FloatType
                        defaultValue = 3500f
                    })
                ) { backStackEntry ->
                    val priceVal = backStackEntry.arguments?.getFloat("price") ?: 3500f
                    CalculatorScreen(initialPriceLakhs = priceVal.toDouble())
                }

                // 6. Agents Directory
                composable("agents") {
                    AgentDirectoryScreen()
                }
            }
        }

        LoadingOverlay(isLoading = isLoading, message = loadingMessage)
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val badgeCount: Int = 0
)
