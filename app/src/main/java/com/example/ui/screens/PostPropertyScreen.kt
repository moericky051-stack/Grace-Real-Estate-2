package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Property
import com.example.ui.components.PropertyImage
import com.example.ui.theme.RealEstateGold
import com.example.ui.theme.RealEstateNavy
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostPropertyScreen(
    onBackClick: () -> Unit,
    existingProperty: Property? = null,
    onUpdateProperty: ((Property) -> Unit)? = null,
    onSubmitProperty: (
        title: String,
        listingType: String,
        propertyType: String,
        priceLakhs: Double,
        pricePeriod: String,
        city: String,
        township: String,
        address: String,
        areaSqft: Int,
        bedrooms: Int,
        bathrooms: Int,
        floorLevel: String,
        furnishing: String,
        deedType: String,
        description: String,
        agentName: String,
        agentPhone: String,
        imageResName: String?
    ) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(existingProperty?.title ?: "") }
    var listingType by remember { mutableStateOf(existingProperty?.listingType ?: "BUY") } // BUY or RENT
    var propertyType by remember { mutableStateOf(existingProperty?.propertyType ?: "Condo") }
    var priceLakhsText by remember { mutableStateOf(existingProperty?.priceLakhs?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var city by remember { mutableStateOf(existingProperty?.city ?: "Yangon") }
    var township by remember { mutableStateOf(existingProperty?.township ?: "") }
    var address by remember { mutableStateOf(existingProperty?.address ?: "") }
    var areaSqftText by remember { mutableStateOf(existingProperty?.areaSqft?.toString() ?: "") }
    var bedrooms by remember { mutableStateOf(existingProperty?.bedrooms?.toString() ?: "2") }
    var bathrooms by remember { mutableStateOf(existingProperty?.bathrooms?.toString() ?: "1") }
    var floorLevel by remember { mutableStateOf(existingProperty?.floorLevel ?: "3rd Floor") }
    var furnishing by remember { mutableStateOf(existingProperty?.furnishing ?: "Fully Furnished") }
    var deedType by remember { mutableStateOf(existingProperty?.deedType ?: "Grant Land (ဂရန်မြေ)") }
    var description by remember { mutableStateOf(existingProperty?.description ?: "") }
    var agentName by remember { mutableStateOf(existingProperty?.agentName ?: "") }
    var agentPhone by remember { mutableStateOf(existingProperty?.agentPhone ?: "") }
    
    val selectedImagePaths = remember {
        mutableStateListOf<String>().apply {
            existingProperty?.imageResName?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.let {
                addAll(it)
            }
        }
    }

    LaunchedEffect(existingProperty) {
        existingProperty?.let { p ->
            title = p.title
            listingType = p.listingType
            propertyType = p.propertyType
            priceLakhsText = if (p.priceLakhs % 1.0 == 0.0) p.priceLakhs.toInt().toString() else p.priceLakhs.toString()
            city = p.city
            township = p.township
            address = p.address
            areaSqftText = p.areaSqft.toString()
            bedrooms = p.bedrooms.toString()
            bathrooms = p.bathrooms.toString()
            floorLevel = p.floorLevel
            furnishing = p.furnishing
            deedType = p.deedType
            description = p.description
            agentName = p.agentName
            agentPhone = p.agentPhone
            selectedImagePaths.clear()
            p.imageResName.split(",").map { it.trim() }.filter { it.isNotEmpty() }.let {
                selectedImagePaths.addAll(it)
            }
        }
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val remainingSlots = 3 - selectedImagePaths.size
            if (remainingSlots > 0) {
                val newPaths = uris.take(remainingSlots).mapNotNull { saveImageToInternalStorage(context, it) }
                selectedImagePaths.addAll(newPaths)
            }
        }
    }

    val propertyTypes = listOf("Condo", "Apartment", "House", "Land", "Commercial")
    val cities = listOf("Yangon", "Mandalay", "Naypyidaw", "Pyin Oo Lwin", "Taunggyi")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (existingProperty != null) "ကြော်ငြာ ပြင်ဆင်ရန် (Edit Listing)" else "အိမ်ခြံမြေ တင်မည် (Post Listing)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // 1. Listing Type Toggle (ဝယ်ရန်/ရောင်းရန် vs ငှားရန်)
                    Text("ကြော်ငြာအမျိုးအစား (Listing Type)", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = listingType == "BUY",
                            onClick = { listingType = "BUY" },
                            label = { Text("ဝယ်ရန်/ရောင်းရန် (Sale)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RealEstateNavy,
                                selectedLabelColor = RealEstateGold
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = listingType == "RENT",
                            onClick = { listingType = "RENT" },
                            label = { Text("ငှားရန် (Rent)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RealEstateNavy,
                                selectedLabelColor = RealEstateGold
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Property Type
                    Text("အိမ်ခြံမြေ အမျိုးအစား (Property Type)", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(propertyTypes) { type ->
                            FilterChip(
                                selected = propertyType == type,
                                onClick = { propertyType = type },
                                label = { Text(type) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RealEstateNavy,
                                    selectedLabelColor = RealEstateGold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // PHOTO UPLOAD SECTION (MAX 3 PHOTOS)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ဓာတ်ပုံ တင်မည် (Photo Upload) *", fontWeight = FontWeight.Bold)
                        Text(
                            text = "${selectedImagePaths.size} / ၃ ပုံ",
                            fontWeight = FontWeight.ExtraBold,
                            color = RealEstateNavy,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "အိမ်ခြံမြေ ဓာတ်ပုံ အများဆုံး ၃ ပုံအထိ တင်နိုင်ပါသည်",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (selectedImagePaths.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            selectedImagePaths.forEachIndexed { index, path ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(110.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.LightGray)
                                ) {
                                    PropertyImage(
                                        imageResName = path,
                                        contentDescription = "Photo ${index + 1}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    // Badge
                                    Surface(
                                        color = RealEstateNavy.copy(alpha = 0.85f),
                                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                                        modifier = Modifier.align(Alignment.TopStart)
                                    ) {
                                        Text(
                                            text = if (index == 0) "အဓိကပုံ" else "ပုံ ${index + 1}",
                                            color = RealEstateGold,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    // Remove Photo Button
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.65f),
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .size(24.dp)
                                            .align(Alignment.TopEnd)
                                    ) {
                                        IconButton(onClick = { selectedImagePaths.removeAt(index) }) {
                                            Icon(
                                                imageVector = Icons.Filled.Close,
                                                contentDescription = "Remove",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Fill remaining empty slots if less than 3
                            repeat(3 - selectedImagePaths.size) {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(110.dp)
                                        .clickable { photoPickerLauncher.launch("image/*") },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AddAPhoto,
                                            contentDescription = "Add Photo",
                                            tint = RealEstateNavy,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("ပုံထပ်ထည့်ရန်", fontSize = 10.sp, color = RealEstateNavy, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { photoPickerLauncher.launch("image/*") },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AddPhotoAlternate,
                                    contentDescription = "Add Photo",
                                    tint = RealEstateNavy,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "ဖုန်းထဲမှ ဓာတ်ပုံများ တင်ရန် ဒီနေရာကို နှိပ်ပါ (အများဆုံး ၃ ပုံ)",
                                    fontWeight = FontWeight.Bold,
                                    color = RealEstateNavy,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "PNG, JPG သို့မဟုတ် JPEG ဓာတ်ပုံ ၃ ပုံအထိ ရွေးချယ်နိုင်ပါသည်",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    if (selectedImagePaths.size < 3) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("သို့မဟုတ် နမူနာ ဓာတ်ပုံများမှ ရွေးချယ်ပါ -", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        val sampleImages = listOf(
                            "img_hero_banner" to "ကွန်ဒို",
                            "img_property_villa" to "ဗီလာ",
                            "img_property_condo" to "တိုက်ခန်း",
                            "img_property_apartment" to "အပါတ်မန့်"
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(sampleImages) { (resName, label) ->
                                val isSelected = selectedImagePaths.contains(resName)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) {
                                            selectedImagePaths.remove(resName)
                                        } else if (selectedImagePaths.size < 3) {
                                            selectedImagePaths.add(resName)
                                        }
                                    },
                                    label = { Text(if (isSelected) "✓ $label" else "+ $label", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RealEstateNavy,
                                        selectedLabelColor = RealEstateGold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Title Input
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("ကြော်ငြာ ခေါင်းစဉ် (Title) *") },
                        placeholder = { Text("ဥပမာ - ကမာရွတ်မြို့နယ် ကွန်ဒို ပြင်ဆင်ပြီး ရောင်းမည်") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Price Lakhs Input
                    OutlinedTextField(
                        value = priceLakhsText,
                        onValueChange = { priceLakhsText = it },
                        label = { Text("စျေးနှုန်း (သိန်းကျပ်) *") },
                        placeholder = { Text("ဥပမာ - 3500 (သိန်း)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 5. City & Township Input
                    Text("တည်နေရာ (Location)", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(cities) { c ->
                            FilterChip(
                                selected = city == c,
                                onClick = { city = c },
                                label = { Text(c) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RealEstateNavy,
                                    selectedLabelColor = RealEstateGold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = township,
                        onValueChange = { township = it },
                        label = { Text("မြို့နယ် (Township) *") },
                        placeholder = { Text("ဥပမာ - ကမာရွတ်၊ ဗဟန်း၊ စသည်") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("အသေးစိတ် လိပ်စာ (Full Address)") },
                        placeholder = { Text("ဥပမာ - လှိုင်မြစ်လမ်းမကြီးအနီး၊ ရန်ကုန်") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 6. Specs: Area, Bedrooms, Bathrooms
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = areaSqftText,
                            onValueChange = { areaSqftText = it },
                            label = { Text("စတုရန်းပေ") },
                            placeholder = { Text("1200") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = bedrooms,
                            onValueChange = { bedrooms = it },
                            label = { Text("အိပ်ခန်း") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = bathrooms,
                            onValueChange = { bathrooms = it },
                            label = { Text("ရေချိုးခန်း") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Floor level & Furnishing
                    OutlinedTextField(
                        value = floorLevel,
                        onValueChange = { floorLevel = it },
                        label = { Text("အလွှာ/အထပ် (Floor/Level)") },
                        placeholder = { Text("3rd Floor, 2-Story, Ground Floor") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = deedType,
                        onValueChange = { deedType = it },
                        label = { Text("စာရွက်စာတမ်း အထောက်အထား") },
                        placeholder = { Text("ဂရန်မြေ၊ ဘိုးဘွားပိုင်၊ ကွန်ဒိုစာချုပ်") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 7. Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("အသေးစိတ် ဖော်ပြချက် (Description)") },
                        placeholder = { Text("အိမ်ခြံမြေ၏ အားသာချက်များ၊ ပါဝင်သောအရာများကို ဖော်ပြပါ...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 8. Contact Person Info
                    Text("ဆက်သွယ်ရန် ပိုင်ရှင် / အကျိုးဆောင်", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = agentName,
                        onValueChange = { agentName = it },
                        label = { Text("အမည် (Contact Name) *") },
                        placeholder = { Text("ဥပမာ - ဦးမင်းသူ") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = agentPhone,
                        onValueChange = { agentPhone = it },
                        label = { Text("ဖုန်းနံပါတ် (Phone Number) *") },
                        placeholder = { Text("09450012345") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            val priceVal = priceLakhsText.toDoubleOrNull()
                            if (title.isBlank()) {
                                errorMessage = "ကျေးဇူးပြု၍ ခေါင်းစဉ် ရေးသားပါ"
                                return@Button
                            }
                            if (priceVal == null || priceVal <= 0) {
                                errorMessage = "ကျေးဇူးပြု၍ မှန်ကန်သော စျေးနှုန်း ရိုက်ထည့်ပါ"
                                return@Button
                            }
                            if (township.isBlank()) {
                                errorMessage = "ကျေးဇူးပြု၍ မြို့နယ် ဖြည့်စွက်ပါ"
                                return@Button
                            }
                            if (agentPhone.isBlank()) {
                                errorMessage = "ကျေးဇူးပြု၍ ဖုန်းနံပါတ် ဖြည့်စွက်ပါ"
                                return@Button
                            }

                            errorMessage = null
                            val finalImgString = if (selectedImagePaths.isNotEmpty()) selectedImagePaths.joinToString(",") else "img_hero_banner"

                            if (existingProperty != null && onUpdateProperty != null) {
                                val updated = existingProperty.copy(
                                    title = title.ifBlank { "အိမ်ခြံမြေ ရောင်းရန်/ငှားရန်" },
                                    listingType = listingType,
                                    propertyType = propertyType,
                                    priceLakhs = priceVal,
                                    pricePeriod = if (listingType == "RENT") "PER_MONTH" else "TOTAL",
                                    city = city,
                                    township = township,
                                    address = address,
                                    areaSqft = areaSqftText.toIntOrNull() ?: 1000,
                                    bedrooms = bedrooms.toIntOrNull() ?: 2,
                                    bathrooms = bathrooms.toIntOrNull() ?: 1,
                                    floorLevel = floorLevel,
                                    furnishing = furnishing,
                                    deedType = deedType,
                                    description = description.ifBlank { "အသေးစိတ် မေးမြန်းနိုင်ပါသည်။" },
                                    agentName = agentName.ifBlank { "အိမ်ပိုင်ရှင်" },
                                    agentPhone = agentPhone.ifBlank { "0912345678" },
                                    imageResName = finalImgString
                                )
                                onUpdateProperty(updated)
                            } else {
                                onSubmitProperty(
                                    title,
                                    listingType,
                                    propertyType,
                                    priceVal,
                                    if (listingType == "RENT") "PER_MONTH" else "TOTAL",
                                    city,
                                    township,
                                    address,
                                    areaSqftText.toIntOrNull() ?: 1000,
                                    bedrooms.toIntOrNull() ?: 2,
                                    bathrooms.toIntOrNull() ?: 1,
                                    floorLevel,
                                    furnishing,
                                    deedType,
                                    description.ifBlank { "အသေးစိတ် မေးမြန်းနိုင်ပါသည်။" },
                                    agentName,
                                    agentPhone,
                                    finalImgString
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RealEstateNavy),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = if (existingProperty != null) Icons.Filled.Save else Icons.Filled.Publish,
                            contentDescription = "Submit",
                            tint = RealEstateGold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (existingProperty != null) "ပြင်ဆင်မှု သိမ်းဆည်းမည် (Update)" else "ကြော်ငြာ အသစ် တင်မည် (Publish)",
                            color = RealEstateGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val photosDir = File(context.filesDir, "property_photos")
        if (!photosDir.exists()) {
            photosDir.mkdirs()
        }
        val file = File(photosDir, "img_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
