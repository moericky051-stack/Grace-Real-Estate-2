package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthProfileSheet(
    userProfile: UserProfile?,
    isSignedIn: Boolean,
    myListings: List<Property>,
    onDismiss: () -> Unit,
    onSignIn: (email: String, pass: String, onError: (String) -> Unit) -> Unit,
    onSignUp: (email: String, pass: String, name: String, phone: String, agency: String, profileImageUri: String?, onError: (String) -> Unit) -> Unit,
    onUpdateProfile: (name: String, phone: String, agency: String, onError: (String) -> Unit) -> Unit,
    onSignOut: () -> Unit,
    onPropertyClick: (Long) -> Unit,
    onEditProperty: (Property) -> Unit,
    onDeleteProperty: (Long) -> Unit
) {
    var activeTab by remember { mutableStateOf(if (isSignedIn) "PROFILE" else "SIGN_IN") }
    var errorMessage by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            // Sheet Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "User Profile",
                        tint = RealEstateNavy,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isSignedIn) "အကောင့် နှင့် ကြော်ငြာများ" else "Firebase အကောင့်ဝင်ရန်",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = RealEstateNavy
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub Navigation Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
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
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = RealEstateRed.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp),
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

            Spacer(modifier = Modifier.height(16.dp))

            when (activeTab) {
                "SIGN_IN" -> SignInForm(
                    onSignIn = { email, pass ->
                        errorMessage = ""
                        onSignIn(email, pass) { err -> errorMessage = err }
                    }
                )
                "SIGN_UP" -> SignUpForm(
                    onSignUp = { email, pass, name, phone, agency, photo ->
                        errorMessage = ""
                        onSignUp(email, pass, name, phone, agency, photo) { err -> errorMessage = err }
                    }
                )
                "PROFILE" -> ProfileView(
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
                "MY_LISTINGS" -> MyListingsView(
                    listings = myListings,
                    onPropertyClick = onPropertyClick,
                    onEditProperty = onEditProperty,
                    onDeleteProperty = onDeleteProperty
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
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
            .padding(vertical = 10.dp),
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
private fun SignInForm(
    onSignIn: (email: String, pass: String) -> Unit
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
            shape = RoundedCornerShape(12.dp)
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
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = { onSignIn(email.trim(), password.trim()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RealEstateNavy),
            shape = RoundedCornerShape(12.dp),
            enabled = email.isNotBlank() && password.length >= 3
        ) {
            Icon(Icons.Filled.Login, contentDescription = null, tint = RealEstateGold)
            Spacer(modifier = Modifier.width(8.dp))
            Text("အကောင့်ဝင်မည်", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
        }


    }
}

@Composable
private fun SignUpForm(
    onSignUp: (email: String, pass: String, name: String, phone: String, agency: String, profileImageUri: String?) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var agency by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<String?>(null) }
    var isPassVisible by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("အမည်") },
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = RealEstateNavy) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("အီးမေးလ် (Email)") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = RealEstateNavy) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("ဆက်သွယ်ရန် ဖုန်းနံပါတ်") },
            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = RealEstateNavy) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = agency,
            onValueChange = { agency = it },
            label = { Text("အကျိုးဆောင် ကုမ္ပဏီ / အေဂျင်စီ (အဓိက)") },
            leadingIcon = { Icon(Icons.Filled.Business, contentDescription = null, tint = RealEstateNavy) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
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
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = { onSignUp(email.trim(), password.trim(), name.trim(), phone.trim(), agency.trim(), profileImageUri) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RealEstateNavy),
            shape = RoundedCornerShape(12.dp),
            enabled = email.isNotBlank() && password.length >= 6 && name.isNotBlank()
        ) {
            Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = RealEstateGold)
            Spacer(modifier = Modifier.width(8.dp))
            Text("အကောင့် သစ်ပြုလုပ်မည်", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
        }
    }
}

@Composable
private fun ProfileView(
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

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(RealEstateNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (profile?.name?.take(1) ?: "U").uppercase(),
                            color = RealEstateGold,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = profile?.name?.ifBlank { "အမည်မရှိပါ" } ?: "အသုံးပြုသူ",
                                fontSize = 17.sp,
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
                        Text(
                            text = profile?.email?.ifBlank { "Firebase Authenticated" } ?: "",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

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
                    InfoRow(label = "ဖုန်းနံပါတ်", value = profile?.phone?.ifBlank { "မထည့်သွင်းရသေးပါ" } ?: "-")
                    InfoRow(label = "အကျိုးဆောင်", value = profile?.agencyName?.ifBlank { "အိမ်ပိုင်ရှင် / အကျိုးဆောင်" } ?: "-")

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = { isEditing = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, tint = RealEstateNavy)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ကိုယ်ရေးအချက်အလက် ပြင်မည်", color = RealEstateNavy)
                    }
                }
            }
        }

        Button(
            onClick = onSignOut,
            colors = ButtonDefaults.buttonColors(containerColor = RealEstateRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Logout, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("အကောင့်မှ ထွက်မည် (Sign Out)", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
    }
}

@Composable
private fun MyListingsView(
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
                    modifier = Modifier.size(48.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("သင် တင်ထားသော ကြော်ငြာ မရှိသေးပါ", color = Color.Gray, fontSize = 15.sp)
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.heightIn(max = 380.dp)
        ) {
            items(listings, key = { it.id }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPropertyClick(item.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${item.city} · ${item.township} | ${item.priceLakhs.toInt()} သိန်း",
                                fontSize = 12.sp,
                                color = RealEstateNavy
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
