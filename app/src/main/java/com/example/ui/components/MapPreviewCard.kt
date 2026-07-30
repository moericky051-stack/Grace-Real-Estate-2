package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.RealEstateBlue
import com.example.ui.theme.RealEstateGold
import com.example.ui.theme.RealEstateNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPreviewCard(
    township: String,
    city: String,
    address: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isInteractiveMap by remember { mutableStateOf(true) }
    var showFullScreenMap by remember { mutableStateOf(false) }

    val (lat, lng) = getCityCoordinates(city)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Map Mode Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Map,
                        contentDescription = "Map Location",
                        tint = RealEstateNavy
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "တည်နေရာနှင့် မြေပုံ (Location Map Engine)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = RealEstateNavy
                        )
                        Text(
                            text = "$township, $city",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                // Interactive vs Canvas Toggle Button
                IconButton(
                    onClick = { isInteractiveMap = !isInteractiveMap },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isInteractiveMap) Icons.Filled.Layers else Icons.Filled.Public,
                        contentDescription = "Toggle Map Type",
                        tint = RealEstateNavy
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // MAP DISPLAY CONTAINER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE2E8F0))
            ) {
                if (isInteractiveMap) {
                    // Interactive Live OpenStreetMap Engine via WebView
                    InteractiveWebMapView(
                        lat = lat,
                        lng = lng,
                        locationName = "$township, $city",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Clean Vector Map Art Simulation
                    CanvasMapView(township = township)
                }

                // Overlay Controls (Full screen button & Navigation badge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .align(Alignment.TopEnd),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        IconButton(onClick = { showFullScreenMap = true }) {
                            Icon(
                                imageVector = Icons.Filled.Fullscreen,
                                contentDescription = "Full Screen Map",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Bottom badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = RealEstateNavy.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Place, contentDescription = null, tint = RealEstateGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Live Pin Point", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Address text
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Navigation, contentDescription = null, tint = RealEstateNavy, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = address.ifBlank { "$township, $city, Myanmar" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Open Google Maps & Get Directions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        openGoogleMaps(context, address, township, city, lat, lng)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = RealEstateNavy),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.Map, contentDescription = null, tint = RealEstateGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Google Maps တွင်ဖွင့်မည်", fontSize = 12.sp, color = Color.White)
                }

                OutlinedButton(
                    onClick = {
                        openDirections(context, township, city, lat, lng)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.Directions, contentDescription = null, tint = RealEstateNavy, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("လမ်းညွှန်ကြည့်မည်", fontSize = 12.sp, color = RealEstateNavy)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Nearby Landmarks Highlights
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LandmarkItem(icon = Icons.Filled.School, label = "ကျောင်း (Schools)")
                LandmarkItem(icon = Icons.Filled.LocalHospital, label = "ဆေးရုံ (Hospital)")
                LandmarkItem(icon = Icons.Filled.ShoppingCart, label = "ဈေး/မောလ် (Market)")
                LandmarkItem(icon = Icons.Filled.DirectionsBus, label = "ကားမှတ်တိုင် (Bus)")
            }
        }
    }

    // Full Screen Map Dialog
    if (showFullScreenMap) {
        Dialog(
            onDismissRequest = { showFullScreenMap = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Fullscreen Header
                    TopAppBar(
                        title = {
                            Column {
                                Text("$township, $city Map", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(address, fontSize = 12.sp, color = RealEstateGold)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { showFullScreenMap = false }) {
                                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                            }
                        },
                        actions = {
                            IconButton(onClick = { openGoogleMaps(context, address, township, city, lat, lng) }) {
                                Icon(Icons.Filled.OpenInNew, contentDescription = "Open External", tint = RealEstateGold)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = RealEstateNavy)
                    )

                    // Full Screen Live Web Map View
                    InteractiveWebMapView(
                        lat = lat,
                        lng = lng,
                        locationName = "$township, $city",
                        modifier = Modifier.weight(1f)
                    )

                    // Bottom navigation action bar
                    Surface(
                        color = RealEstateNavy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("တည်နေရာ Coordinates:", color = Color.Gray, fontSize = 11.sp)
                                Text("Lat: %.4f, Lng: %.4f".format(lat, lng), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Button(
                                onClick = { openDirections(context, township, city, lat, lng) },
                                colors = ButtonDefaults.buttonColors(containerColor = RealEstateGold)
                            ) {
                                Icon(Icons.Filled.Directions, contentDescription = null, tint = RealEstateNavy)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("လမ်းညွှန်ရှာမည်", color = RealEstateNavy, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveWebMapView(
    lat: Double,
    lng: Double,
    locationName: String,
    modifier: Modifier = Modifier
) {
    val htmlData = remember(lat, lng, locationName) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                body { margin: 0; padding: 0; font-family: sans-serif; }
                #map { width: 100vw; height: 100vh; }
                .leaflet-control-attribution { display: none !important; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', { zoomControl: false }).setView([$lat, $lng], 15);
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19
                }).addTo(map);
                
                L.control.zoom({ position: 'bottomright' }).addTo(map);

                var customIcon = L.divIcon({
                    className: 'custom-pin',
                    html: '<div style="background-color:#EF4444; width:22px; height:22px; border-radius:50%; border:3px solid white; box-shadow:0 2px 6px rgba(0,0,0,0.4);"></div>',
                    iconSize: [22, 22],
                    iconAnchor: [11, 11]
                });

                var marker = L.marker([$lat, $lng], {icon: customIcon}).addTo(map);
                marker.bindPopup("<b>" + "$locationName" + "</b><br>ဤအိမ်ခြံမြေ တည်နေရာ").openPopup();
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                webViewClient = WebViewClient()
                loadDataWithBaseURL("https://openstreetmap.org", htmlData, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://openstreetmap.org", htmlData, "text/html", "UTF-8", null)
        },
        modifier = modifier
    )
}

@Composable
private fun CanvasMapView(township: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val roadColor = Color.White
            val riverColor = Color(0xFF93C5FD)

            drawCircle(center = Offset(width * 0.1f, height * 0.8f), radius = 120f, color = riverColor)

            drawLine(color = roadColor, start = Offset(0f, height * 0.5f), end = Offset(width, height * 0.5f), strokeWidth = 24f)
            drawLine(color = roadColor, start = Offset(width * 0.5f, 0f), end = Offset(width * 0.5f, height), strokeWidth = 18f)
            drawLine(color = roadColor, start = Offset(width * 0.2f, 0f), end = Offset(width * 0.8f, height), strokeWidth = 12f)

            drawRect(color = Color(0xFFCBD5E1), topLeft = Offset(width * 0.1f, height * 0.1f), size = androidx.compose.ui.geometry.Size(width * 0.35f, height * 0.35f))
            drawRect(color = Color(0xFFCBD5E1), topLeft = Offset(width * 0.55f, height * 0.55f), size = androidx.compose.ui.geometry.Size(width * 0.35f, height * 0.35f))

            val pinX = width * 0.5f
            val pinY = height * 0.5f
            drawCircle(center = Offset(pinX, pinY), radius = 28f, color = Color(0x40EF4444))
            drawCircle(center = Offset(pinX, pinY), radius = 14f, color = Color(0xFFEF4444))
            drawCircle(center = Offset(pinX, pinY), radius = 6f, color = Color.White)
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = RealEstateNavy,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-32).dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Home, contentDescription = null, tint = RealEstateGold, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "ဤအိမ်ခြံမြေ တည်နေရာ ($township)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LandmarkItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = RealEstateNavy, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }
}

private fun getCityCoordinates(city: String): Pair<Double, Double> {
    return when (city.lowercase()) {
        "yangon", "ရန်ကုန်" -> Pair(16.8661, 96.1951)
        "mandalay", "မန္တလေး" -> Pair(21.9588, 96.0891)
        "naypyidaw", "နေပြည်တော်" -> Pair(19.7633, 96.0785)
        "pyin oo lwin", "ပြင်ဦးလွင်" -> Pair(22.0350, 96.4678)
        "taunggyi", "တောင်ကြီး" -> Pair(20.7831, 97.0378)
        else -> Pair(16.8661, 96.1951)
    }
}

private fun openGoogleMaps(context: Context, address: String, township: String, city: String, lat: Double, lng: Double) {
    val query = if (address.isNotBlank()) "$address, $township, $city, Myanmar" else "$township, $city, Myanmar"
    val gmmIntentUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    try {
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        // Fallback to browser
        context.startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri))
    }
}

private fun openDirections(context: Context, township: String, city: String, lat: Double, lng: Double) {
    val dirUri = Uri.parse("google.navigation:q=$lat,$lng")
    val mapIntent = Intent(Intent.ACTION_VIEW, dirUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    try {
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }
}
