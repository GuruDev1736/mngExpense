package com.guruprasad.mngexpense.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.guruprasad.mngexpense.domain.Category

/**
 * Icons a category can have. The key is what gets stored, so keys must never change;
 * new icons can be added at the end.
 */
val CategoryIcons: List<Pair<String, ImageVector>> = listOf(
    "restaurant" to Icons.Filled.Restaurant,
    "grocery" to Icons.Filled.LocalGroceryStore,
    "coffee" to Icons.Filled.LocalCafe,
    "car" to Icons.Filled.DirectionsCar,
    "fuel" to Icons.Filled.LocalGasStation,
    "bus" to Icons.Filled.DirectionsBus,
    "train" to Icons.Filled.Train,
    "flight" to Icons.Filled.Flight,
    "shopping" to Icons.Filled.ShoppingBag,
    "clothes" to Icons.Filled.Checkroom,
    "gift" to Icons.Filled.CardGiftcard,
    "receipt" to Icons.Filled.Receipt,
    "bolt" to Icons.Filled.Bolt,
    "water" to Icons.Filled.WaterDrop,
    "wifi" to Icons.Filled.Wifi,
    "phone" to Icons.Filled.PhoneAndroid,
    "home" to Icons.Filled.Home,
    "hotel" to Icons.Filled.Hotel,
    "movie" to Icons.Filled.Movie,
    "music" to Icons.Filled.MusicNote,
    "games" to Icons.Filled.SportsEsports,
    "subscriptions" to Icons.Filled.Subscriptions,
    "celebration" to Icons.Filled.Celebration,
    "health" to Icons.Filled.LocalHospital,
    "medicine" to Icons.Filled.Medication,
    "fitness" to Icons.Filled.FitnessCenter,
    "beauty" to Icons.Filled.ContentCut,
    "spa" to Icons.Filled.Spa,
    "school" to Icons.Filled.School,
    "book" to Icons.AutoMirrored.Filled.MenuBook,
    "child" to Icons.Filled.ChildCare,
    "pets" to Icons.Filled.Pets,
    "plants" to Icons.Filled.LocalFlorist,
    "work" to Icons.Filled.Work,
    "laptop" to Icons.Filled.Laptop,
    "repair" to Icons.Filled.Build,
    "card" to Icons.Filled.CreditCard,
    "bank" to Icons.Filled.AccountBalance,
    "savings" to Icons.Filled.Savings,
    "category" to Icons.Filled.Category
)

fun categoryIcon(key: String): ImageVector =
    CategoryIcons.firstOrNull { it.first == key }?.second ?: Icons.Filled.Category

val Category.icon: ImageVector get() = categoryIcon(info.icon)

/** The category's icon on a circle of its colour. */
@Composable
fun CategoryBadge(category: Category, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    CategoryBadge(category.icon, category.color, modifier, size)
}

@Composable
fun CategoryBadge(icon: ImageVector, color: Color, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    Box(modifier.size(size).background(color, CircleShape), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(size * 0.55f))
    }
}
