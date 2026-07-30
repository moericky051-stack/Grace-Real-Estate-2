package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RealEstateGold
import com.example.ui.theme.RealEstateNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    selectedCity: String,
    onCitySelected: (String) -> Unit,
    selectedPropType: String,
    onPropTypeSelected: (String) -> Unit,
    maxPriceLakhs: Float,
    onPriceChanged: (Float) -> Unit,
    minBedrooms: Int,
    onBedroomsSelected: (Int) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val cities = listOf("ALL" to "အားလုံး", "Yangon" to "ရန်ကုန်", "Mandalay" to "မန္တလေး", "Naypyidaw" to "နေပြည်တော်", "Pyin Oo Lwin" to "ပြင်ဦးလွင်", "Taunggyi" to "တောင်ကြီး")
    val propTypes = listOf("ALL" to "အားလုံး", "Condo" to "ကွန်ဒို", "Apartment" to "တိုက်ခန်း", "House" to "လုံးချင်း", "Land" to "မြေကွက်", "Commercial" to "ဆိုင်/ရုံးခန်း")
    val bedOptions = listOf(0 to "မခွဲခြားပါ", 1 to "၁ ခန်း", 2 to "၂ ခန်း", 3 to "၃ ခန်း", 4 to "၄ ခန်း+")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "စစ်ထုတ်မည် (Filter Options)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RealEstateNavy
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // City Filter
            Text(
                text = "မြို့ပြ / တိုင်းဒေသကြီး (Location)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(cities) { (key, label) ->
                    FilterChip(
                        selected = selectedCity == key,
                        onClick = { onCitySelected(key) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RealEstateNavy,
                            selectedLabelColor = RealEstateGold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Property Type Filter
            Text(
                text = "အမျိုးအစား (Property Type)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(propTypes) { (key, label) ->
                    FilterChip(
                        selected = selectedPropType == key,
                        onClick = { onPropTypeSelected(key) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RealEstateNavy,
                            selectedLabelColor = RealEstateGold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Price Range Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "အများဆုံး စျေးနှုန်း (Max Price)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (maxPriceLakhs >= 20000f) "ကန့်သတ်မထားပါ" else "${maxPriceLakhs.toInt()} သိန်းကျပ်",
                    fontWeight = FontWeight.ExtraBold,
                    color = RealEstateNavy
                )
            }
            Slider(
                value = maxPriceLakhs,
                onValueChange = onPriceChanged,
                valueRange = 100f..20000f,
                steps = 19,
                colors = SliderDefaults.colors(
                    thumbColor = RealEstateGold,
                    activeTrackColor = RealEstateNavy
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bedrooms Filter
            Text(
                text = "အနည်းဆုံး အိပ်ခန်းအရေအတွက် (Bedrooms)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(bedOptions) { (beds, label) ->
                    FilterChip(
                        selected = minBedrooms == beds,
                        onClick = { onBedroomsSelected(beds) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RealEstateNavy,
                            selectedLabelColor = RealEstateGold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Bottom Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("မူလအတိုင်း")
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = RealEstateNavy)
                ) {
                    Text("စစ်ထုတ်မည် (Apply)", color = RealEstateGold, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
