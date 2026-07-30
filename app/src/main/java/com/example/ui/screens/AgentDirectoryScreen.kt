package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RealEstateBlue
import com.example.ui.theme.RealEstateGold
import com.example.ui.theme.RealEstateNavy

data class AgentInfo(
    val id: String,
    val name: String,
    val agencyName: String,
    val phone: String,
    val city: String,
    val activeCount: Int,
    val rating: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentDirectoryScreen() {
    val context = LocalContext.current

    val agents = listOf(
        AgentInfo("1", "ဦးမင်းသူ", "Grace အကျိုးဆောင် (Grace Estate)", "09420011223", "ရန်ကုန်", 18, 4.9),
        AgentInfo("2", "ဒေါ်နန်းမိုး", "Yangon Luxury Realty Co., Ltd", "09790099887", "ရန်ကုန်", 24, 4.8),
        AgentInfo("3", "ကိုအောင်ကျော်", "City Property Agency", "09250123456", "ရန်ကုန်", 12, 4.7),
        AgentInfo("4", "ဦးကျော်စွာ", "မန္တလေး ရွှေမြေ အကျိုးဆောင်", "09400223344", "မန္တလေး", 15, 4.9),
        AgentInfo("5", "မသီတာ", "Mandalay Homes Agency", "09970112233", "မန္တလေး", 9, 4.6),
        AgentInfo("6", "စောဟန်လင်း", "Shan Hills Realty", "09880123999", "တောင်ကြီး / ပြင်ဦးလွင်", 11, 4.8)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = RealEstateGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "အကျိုးဆောင်များ လမ်းညွှန် (Agents)",
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
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "ယုံကြည်စိတ်ချရသော တရားဝင် အကျိုးဆောင်များ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RealEstateNavy
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "အိမ်ခြံမြေ ဝယ်ယူ/ငှားရမ်းရန်အတွက် တိုက်ရိုက် ဆက်သွယ် မေးမြန်းနိုင်ပါသည်။",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(agents, key = { it.id }) { agent ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = RealEstateNavy,
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.Person, contentDescription = null, tint = RealEstateGold, modifier = Modifier.size(30.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = agent.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Filled.Verified, contentDescription = "Verified", tint = RealEstateBlue, modifier = Modifier.size(16.dp))
                                    }

                                    Text(
                                        text = agent.agencyName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = RealEstateGold, modifier = Modifier.size(14.dp))
                                        Text(text = agent.city, fontSize = 11.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Icon(Icons.Filled.Star, contentDescription = null, tint = RealEstateGold, modifier = Modifier.size(14.dp))
                                        Text(text = "${agent.rating} (${agent.activeCount} ကြော်ငြာ)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:${agent.phone}")
                                        }
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = RealEstateNavy),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.Phone, contentDescription = null, tint = RealEstateGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ဖုန်းခေါ်မည်", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("smsto:${agent.phone}")
                                            putExtra("sms_body", "မင်္ဂလာပါ၊ ${agent.name} ရှေ့တွင် အိမ်ခြံမြေ မေးမြန်းလိုပါသည်။")
                                        }
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.Chat, contentDescription = null, tint = RealEstateBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("မက်ဆေ့ခ်ျ ပို့မည်", color = RealEstateBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
