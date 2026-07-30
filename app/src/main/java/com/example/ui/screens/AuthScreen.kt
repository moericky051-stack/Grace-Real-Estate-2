package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.UserProfile
import com.example.data.model.Property
import com.example.ui.theme.RealEstateBlue
import com.example.ui.theme.RealEstateGold
import com.example.ui.theme.RealEstateNavy
import com.example.ui.theme.RealEstateRed

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ui.components.PropertyImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    userProfile: UserProfile?,
    isSignedIn: Boolean,
    myListings: List<Property>,
    onBackClick: () -> Unit,
    onSignIn: (email: String, pass: String, onError: (String) -> Unit) -> Unit,
    onSignUp: (email: String, pass: String, name: String, phone: String, agency: String, profileImageUri: String?, onError: (String) -> Unit) -> Unit,
    onGoogleSignIn: (((onError: (String) -> Unit) -> Unit))? = null,
    onSignInAsDemo: (() -> Unit)? = null,
    onUpdateProfile: (name: String, phone: String, agency: String, onError: (String) -> Unit) -> Unit,
    onSignOut: () -> Unit,
    onPropertyClick: (Long) -> Unit,
    onEditProperty: (Property) -> Unit,
    onDeleteProperty: (Long) -> Unit
) {
    var activeTab by remember { mutableStateOf(if (isSignedIn) "PROFILE" else "SIGN_IN") }
    var errorMessage by remember { mutableStateOf("") }

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            RealEstateNavy,
            RealEstateNavy.copy(alpha = 0.92f),
            MaterialTheme.colorScheme.background
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isSignedIn) "အကောင့် နှင့် ကြော်ငြာများ" else "အကောင့် ဝင်ရောက်ရန်",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RealEstateNavy)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(gradientBackground)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Hero Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        color = RealEstateGold,
                        shadowElevation = 8.dp,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.HomeWork,
                                contentDescription = null,
                                tint = RealEstateNavy,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Grace Real Estate Myanmar",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "အိမ်၊ ခြံ၊ မြေ ဝယ်/ရောင်း/ငှား Cloud စနစ်",
                        fontSize = 13.sp,
                        color = RealEstateGold
                    )
                }
            }

            // Main Auth Form Container Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Tab Selector Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (isSignedIn) {
                            TabButton(
                                title = "ကိုယ်ရေးအချက်အလက်",
                                isSelected = activeTab == "PROFILE",
                                onClick = { activeTab = "PROFILE"; errorMessage = "" },
                                modifier = Modifier.weight(1f)
                            )
                            TabButton(
                                title = "ကျွန်ုပ်၏ ကြော်ငြာများ (${myListings.size})",
                                isSelected = activeTab == "MY_LISTINGS",
                                onClick = { activeTab = "MY_LISTINGS"; errorMessage = "" },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            TabButton(
                                title = "အကောင့်ဝင်မည်",
                                isSelected = activeTab == "SIGN_IN",
                                onClick = { activeTab = "SIGN_IN"; errorMessage = "" },
                                modifier = Modifier.weight(1f)
                            )
                            TabButton(
                                title = "အကောင့်သစ်ဖွင့်မည်",
                                isSelected = activeTab == "SIGN_UP",
                                onClick = { activeTab = "SIGN_UP"; errorMessage = "" },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (errorMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = RealEstateRed.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.ErrorOutline, contentDescription = "Error", tint = RealEstateRed)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(errorMessage, color = RealEstateRed, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    AnimatedContent(
                        targetState = activeTab,
                        label = "AuthTabTransition"
                    ) { tab ->
                        when (tab) {
                            "SIGN_IN" -> FullScreenSignInForm(
                                onSignIn = { email, pass ->
                                    errorMessage = ""
                                    onSignIn(email, pass) { err -> errorMessage = err }
                                },
                                onGoogleSignIn = if (onGoogleSignIn != null) {
                                    {
                                        errorMessage = ""
                                        onGoogleSignIn { err -> errorMessage = err }
                                    }
                                } else null,
                                onSignInAsDemo = onSignInAsDemo
                            )
                            "SIGN_UP" -> FullScreenSignUpForm(
                                onSignUp = { email, pass, name, phone, agency, photoUri ->
                                    errorMessage = ""
                                    onSignUp(email, pass, name, phone, agency, photoUri) { err -> errorMessage = err }
                                },
                                onGoogleSignIn = if (onGoogleSignIn != null) {
                                    {
                                        errorMessage = ""
                                        onGoogleSignIn { err -> errorMessage = err }
                                    }
                                } else null,
                                onSignInAsDemo = onSignInAsDemo
                            )
                            "PROFILE" -> FullScreenProfileView(
                                profile = userProfile,
                                onUpdateProfile = { name, phone, agency ->
                                    errorMessage = ""
                                    onUpdateProfile(name, phone, agency) { err -> errorMessage = err }
                                },
                                onSignOut = {
                                    onSignOut()
                                    activeTab = "SIGN_IN"
                                }
                            )
                            "MY_LISTINGS" -> FullScreenMyListingsView(
                                listings = myListings,
                                onPropertyClick = onPropertyClick,
                                onEditProperty = onEditProperty,
                                onDeleteProperty = onDeleteProperty
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isSignedIn) {
                TextButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "ဧည့်သည်အဖြစ် ဆက်လက်ကြည့်ရှုမည်",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) RealEstateNavy else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) RealEstateGold else Color.DarkGray
        )
    }
}

@Composable
private fun FullScreenSignInForm(
    onSignIn: (email: String, pass: String) -> Unit,
    onGoogleSignIn: (() -> Unit)? = null,
    onSignInAsDemo: (() -> Unit)? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPassVisible by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("အီးမေးလ် သို့မဟုတ် 'admin'") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = RealEstateNavy) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("စကားဝှက် (Password)") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = RealEstateNavy) },
            trailingIcon = {
                IconButton(onClick = { isPassVisible = !isPassVisible }) {
                    Icon(
                        imageVector = if (isPassVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle Password"
                    )
                }
            },
            visualTransformation = if (isPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Button(
            onClick = { onSignIn(email.trim(), password.trim()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RealEstateNavy),
            shape = RoundedCornerShape(14.dp),
            enabled = email.isNotBlank() && password.length >= 3
        ) {
            Icon(Icons.Filled.Login, contentDescription = null, tint = RealEstateGold)
            Spacer(modifier = Modifier.width(10.dp))
            Text("အကောင့်ဝင်မည်", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(modifier = Modifier.weight(1f))
            Text(" သို့မဟုတ် ", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
            Divider(modifier = Modifier.weight(1f))
        }

        if (onGoogleSignIn != null) {
            OutlinedButton(
                onClick = onGoogleSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color.Black)
            ) {
                Icon(Icons.Filled.AccountCircle, contentDescription = "Google", tint = RealEstateNavy, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Google အကောင့်ဖြင့် ဝင်မည် (Google Sign-In)", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
            }
        }

        if (onSignInAsDemo != null) {
            OutlinedButton(
                onClick = onSignInAsDemo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RealEstateNavy)
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = RealEstateGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("အစမ်းသုံး (Demo Acc) ဖြင့် ဝင်မည်", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
            }
        }
    }
}

@Composable
private fun FullScreenSignUpForm(
    onSignUp: (email: String, pass: String, name: String, phone: String, agency: String, profileImageUri: String?) -> Unit,
    onGoogleSignIn: (() -> Unit)? = null,
    onSignInAsDemo: (() -> Unit)? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var agency by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<String?>(null) }
    var isPassVisible by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            profileImageUri = uri.toString()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Profile Picture Selection Section
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(RealEstateNavy.copy(alpha = 0.1f))
                        .clickable { photoPickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (!profileImageUri.isNullOrBlank()) {
                        PropertyImage(
                            imageResName = profileImageUri!!,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(RealEstateNavy),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (name.take(1).ifBlank { "U" }).uppercase(),
                                color = RealEstateGold,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Camera Icon Overlay Badge
                    Surface(
                        shape = CircleShape,
                        color = RealEstateGold,
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.AddAPhoto,
                                contentDescription = "Choose Photo",
                                tint = RealEstateNavy,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (profileImageUri != null) "ဓာတ်ပုံ ရွေးချယ်ပြီးပါပြီ (ပြင်ရန်နှိပ်ပါ)" else "ပရိုဖိုင် ဓာတ်ပုံ ရွေးချယ်ရန် နှိပ်ပါ",
                    fontSize = 12.sp,
                    color = RealEstateNavy,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("အမည် (Full Name)") },
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = RealEstateNavy) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("အီးမေးလ် (Email)") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = RealEstateNavy) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("ဆက်သွယ်ရန် ဖုန်းနံပါတ်") },
            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = RealEstateNavy) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        OutlinedTextField(
            value = agency,
            onValueChange = { agency = it },
            label = { Text("အကျိုးဆောင် ကုမ္ပဏီ / အေဂျင်စီ (အဓိက)") },
            leadingIcon = { Icon(Icons.Filled.Business, contentDescription = null, tint = RealEstateNavy) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("စကားဝှက် (အနည်းဆုံး ၆ လုံး)") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = RealEstateNavy) },
            trailingIcon = {
                IconButton(onClick = { isPassVisible = !isPassVisible }) {
                    Icon(
                        imageVector = if (isPassVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle Password"
                    )
                }
            },
            visualTransformation = if (isPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Button(
            onClick = { onSignUp(email.trim(), password.trim(), name.trim(), phone.trim(), agency.trim(), profileImageUri) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RealEstateNavy),
            shape = RoundedCornerShape(14.dp),
            enabled = email.isNotBlank() && password.length >= 6 && name.isNotBlank()
        ) {
            Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = RealEstateGold)
            Spacer(modifier = Modifier.width(10.dp))
            Text("အကောင့် သစ်ပြုလုပ်မည်", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
        }

        if (onGoogleSignIn != null) {
            OutlinedButton(
                onClick = onGoogleSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color.Black)
            ) {
                Icon(Icons.Filled.AccountCircle, contentDescription = "Google", tint = RealEstateNavy, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Google အကောင့်ဖြင့် ဝင်မည် (Google Sign-In)", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
            }
        }

        if (onSignInAsDemo != null) {
            OutlinedButton(
                onClick = onSignInAsDemo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RealEstateNavy)
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = RealEstateGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("အစမ်းသုံး (Demo Acc) ဖြင့် ချက်ချင်းဝင်မည်", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun FullScreenProfileView(
    profile: UserProfile?,
    onUpdateProfile: (name: String, phone: String, agency: String) -> Unit,
    onSignOut: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(profile?.name ?: "") }
    var phone by remember { mutableStateOf(profile?.phone ?: "") }
    var agency by remember { mutableStateOf(profile?.agencyName ?: "") }

    LaunchedEffect(profile) {
        if (profile != null) {
            name = profile.name
            phone = profile.phone
            agency = profile.agencyName
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(RealEstateNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (profile?.name?.take(1) ?: "U").uppercase(),
                            color = RealEstateGold,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = profile?.name?.ifBlank { "အမည်မရှိပါ" } ?: "အသုံးပြုသူ",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = RealEstateNavy
                            )
                            if (profile?.isAdmin == true || profile?.email?.lowercase()?.contains("admin") == true) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = RealEstateGold,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        "👑 ADMIN",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RealEstateNavy
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = profile?.email?.ifBlank { "Firebase Authenticated" } ?: "",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                if (isEditing) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("အမည်") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("ဖုန်းနံပါတ်") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = agency,
                        onValueChange = { agency = it },
                        label = { Text("အကျိုးဆောင် / ကုမ္ပဏီ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { isEditing = false }) { Text("မလုပ်တော့ပါ") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onUpdateProfile(name, phone, agency)
                                isEditing = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RealEstateNavy)
                        ) {
                            Text("သိမ်းမည်", color = RealEstateGold)
                        }
                    }
                } else {
                    ProfileInfoRow(label = "ဖုန်းနံပါတ်", value = profile?.phone?.ifBlank { "မထည့်သွင်းရသေးပါ" } ?: "-")
                    ProfileInfoRow(label = "အကျိုးဆောင်", value = profile?.agencyName?.ifBlank { "အိမ်ပိုင်ရှင် / အကျိုးဆောင်" } ?: "-")

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = { isEditing = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, tint = RealEstateNavy)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ကိုယ်ရေးအချက်အလက် ပြင်မည်", color = RealEstateNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Button(
            onClick = onSignOut,
            colors = ButtonDefaults.buttonColors(containerColor = RealEstateRed),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.Filled.Logout, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(10.dp))
            Text("အကောင့်မှ ထွက်မည် (Sign Out)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
    }
}

@Composable
private fun FullScreenMyListingsView(
    listings: List<Property>,
    onPropertyClick: (Long) -> Unit,
    onEditProperty: (Property) -> Unit,
    onDeleteProperty: (Long) -> Unit
) {
    if (listings.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.HomeWork,
                    contentDescription = null,
                    modifier = Modifier.size(54.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("သင် တင်ထားသော ကြော်ငြာ မရှိသေးပါ", color = Color.Gray, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listings.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPropertyClick(item.id) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${item.city} · ${item.township} | ${item.priceLakhs.toInt()} သိန်း",
                                fontSize = 13.sp,
                                color = RealEstateNavy,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row {
                            IconButton(onClick = { onEditProperty(item) }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = RealEstateNavy)
                            }
                            IconButton(onClick = { onDeleteProperty(item.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = RealEstateRed)
                            }
                        }
                    }
                }
            }
        }
    }
}
