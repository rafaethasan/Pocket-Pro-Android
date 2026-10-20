package com.softx.app.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Bathtub
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Cottage
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.DirectionsBoat
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Diversity3
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Icecream
import androidx.compose.material.icons.rounded.KebabDining
import androidx.compose.material.icons.rounded.Kitchen
import androidx.compose.material.icons.rounded.Liquor
import androidx.compose.material.icons.rounded.LocalAtm
import androidx.compose.material.icons.rounded.LocalBar
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalDining
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.LunchDining
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Paid
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.RunCircle
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SmokingRooms
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.SportsBaseball
import androidx.compose.material.icons.rounded.SportsBasketball
import androidx.compose.material.icons.rounded.SportsCricket
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.SportsTennis
import androidx.compose.material.icons.rounded.SportsVolleyball
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material.icons.rounded.TableBar
import androidx.compose.material.icons.rounded.TableRestaurant
import androidx.compose.material.icons.rounded.TempleHindu
import androidx.compose.material.icons.rounded.Toys
import androidx.compose.material.icons.rounded.Train
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.vector.ImageVector

private val pocketCategoryFallbackIcons = listOf(
    Icons.Rounded.Category,
    Icons.Rounded.ShoppingBag,
    Icons.Rounded.Restaurant,
    Icons.Rounded.PhoneAndroid,
    Icons.Rounded.DirectionsBus,
    Icons.Rounded.Build,
    Icons.Rounded.Home,
    Icons.Rounded.LocalHospital,
    Icons.Rounded.School,
    Icons.Rounded.Movie,
    Icons.Rounded.Flight,
    Icons.Rounded.Payments,
    Icons.Rounded.Savings,
    Icons.Rounded.Receipt,
    Icons.Rounded.Work,
)

fun pocketCategoryVisualIcon(key: String, iconClass: String = ""): ImageVector {
    val clean = pocketNormalizeIconLookup("$key $iconClass")
    return when {
        "loan given" in clean || "lent" in clean -> Icons.Rounded.Savings
        "loan received" in clean || "take loan" in clean || "borrow" in clean -> Icons.Rounded.AttachMoney
        "due collection" in clean || "get back" in clean || "receive" in clean -> Icons.Rounded.Payments
        "loan payment" in clean || "pay back" in clean || "repay" in clean -> Icons.Rounded.Receipt
        "service income" in clean -> Icons.Rounded.Work
        "home" in clean || "house heart" in clean || "shield home" in clean -> Icons.Rounded.Home
        "building" in clean || "apartment" in clean || "warehouse" in clean || "garage" in clean -> Icons.Rounded.Cottage
        "door" in clean || "window" in clean || "stairs" in clean || "brick" in clean -> Icons.Rounded.Home
        "couch" in clean || "bed" in clean || "lamp" in clean || "fan" in clean || "furniture" in clean -> Icons.Rounded.TableBar
        "toolbox" in clean || "paint" in clean || "hammer" in clean || "repair" in clean || "fix" in clean || "wrench" in clean -> Icons.Rounded.Build
        "lock" in clean || "key" in clean || "security" in clean -> Icons.Rounded.Security
        "bolt" in clean || "zap" in clean || "battery" in clean || "charging" in clean || "plug" in clean -> Icons.Rounded.Wifi
        "droplet" in clean || "water" in clean -> Icons.Rounded.WaterDrop
        "router" in clean || "signal" in clean || "wifi" in clean || "satellite" in clean -> Icons.Rounded.Wifi
        "smartphone" in clean || "phone" in clean || "mobile" in clean -> Icons.Rounded.PhoneAndroid
        "tv" in clean || "monitor" in clean || "screen" in clean || "desktop" in clean -> Icons.Rounded.Tv
        "receipt" in clean || "invoice" in clean || "barcode" in clean || "file" in clean || "billing" in clean || "payment" in clean || "subscription" in clean || "utility" in clean || "bill" in clean -> Icons.Rounded.Receipt
        "shopping" in clean || "cart" in clean || "retail" in clean -> Icons.Rounded.ShoppingBag
        "basket" in clean || "storefront" in clean || "store" in clean || "hanger" in clean || "package" in clean || "delivery" in clean || "tag" in clean || "boutique" in clean -> Icons.Rounded.Store
        "food" in clean || "utensils" in clean || "restaurant" in clean -> Icons.Rounded.Restaurant
        "spoon" in clean || "fork" in clean || "knife" in clean -> Icons.Rounded.Restaurant
        "apple" in clean || "carrot" in clean || "leaf" in clean || "wheat" in clean || "vegetable" in clean -> Icons.Rounded.Eco
        "fish" in clean || "drumstick" in clean || "egg" in clean || "snack" in clean || "meal" in clean || "burger" in clean -> Icons.Rounded.Fastfood
        "pizza" in clean -> Icons.Rounded.LocalDining
        "coffee" in clean || "tea" in clean -> Icons.Rounded.LocalCafe
        "bar" in clean || "drink" in clean || "nightlife" in clean -> Icons.Rounded.LocalBar
        "lunch" in clean || "dinner" in clean || "breakfast" in clean -> Icons.Rounded.LunchDining
        "ice" in clean || "dessert" in clean -> Icons.Rounded.Icecream
        "kebab" in clean || "bbq" in clean -> Icons.Rounded.KebabDining
        "transport" in clean || "bus" in clean -> Icons.Rounded.DirectionsBus
        "car" in clean || "taxi" in clean || "ride" in clean || "rental car" in clean -> Icons.Rounded.DirectionsCar
        "bike" in clean || "bicycle" in clean -> Icons.AutoMirrored.Rounded.DirectionsBike
        "train" in clean || "metro" in clean -> Icons.Rounded.Train
        "ship" in clean || "boat" in clean || "cruise" in clean -> Icons.Rounded.DirectionsBoat
        "truck" in clean || "scooter" in clean -> Icons.Rounded.DirectionsCar
        "fuel" in clean || "engine" in clean || "steering" in clean || "road" in clean || "route" in clean -> Icons.Rounded.LocalGasStation
        "navigation" in clean || "compass" in clean || "map" in clean || "parking" in clean || "traffic" in clean -> Icons.Rounded.Map
        "gift" in clean || "present" in clean || "reward" in clean || "rebate" in clean || "incentive" in clean -> Icons.Rounded.CardGiftcard
        "donation" in clean || "charity" in clean || "help" in clean -> Icons.Rounded.VolunteerActivism
        "lottery" in clean || "casino" in clean -> Icons.Rounded.Casino
        "pet" in clean || "dog" in clean || "cat" in clean -> Icons.Rounded.Pets
        "alcohol" in clean || "wine" in clean || "beer" in clean -> Icons.Rounded.Liquor
        "electronic" in clean || "computer" in clean || "screen" in clean || "project income" in clean || "freelance" in clean -> Icons.Rounded.Work
        "laptop" in clean || "tablet" in clean || "device" in clean -> Icons.Rounded.Devices
        "printer" in clean -> Icons.Rounded.Print
        "kid" in clean || "child" in clean -> Icons.Rounded.ChildCare
        "baby" in clean || "stroller" in clean || "bottle" in clean || "milk" in clean || "diaper" in clean -> Icons.Rounded.ChildCare
        "toy" in clean || "teddy" in clean || "balloon" in clean || "blocks" in clean || "playground" in clean -> Icons.Rounded.Toys
        "festival" in clean || "party" in clean || "birthday" in clean || "wedding" in clean -> Icons.Rounded.Celebration
        "eid" in clean || "moon" in clean || "puja" in clean || "temple" in clean -> Icons.Rounded.TempleHindu
        "beauty" in clean || "scissors" in clean -> Icons.Rounded.Favorite
        "sport" in clean || "gamepad" in clean || "gaming" in clean -> Icons.Rounded.SportsEsports
        "cricket" in clean -> Icons.Rounded.SportsCricket
        "football" in clean || "soccer" in clean -> Icons.Rounded.SportsSoccer
        "basketball" in clean -> Icons.Rounded.SportsBasketball
        "tennis" in clean -> Icons.Rounded.SportsTennis
        "volley" in clean -> Icons.Rounded.SportsVolleyball
        "baseball" in clean -> Icons.Rounded.SportsBaseball
        "gym" in clean || "fitness" in clean -> Icons.Rounded.FitnessCenter
        "run" in clean || "walk" in clean || "yoga" in clean || "swim" in clean || "ski" in clean -> Icons.Rounded.RunCircle
        "social" in clean || "users" in clean -> Icons.Rounded.Groups
        "community" in clean || "people" in clean || "meeting" in clean || "family" in clean || "couple" in clean || "handshake" in clean || "support" in clean || "contact" in clean || "hug" in clean -> Icons.Rounded.Diversity3
        "cloth" in clean || "shirt" in clean || "dress" in clean || "shoe" in clean -> Icons.Rounded.Checkroom
        "kitchen" in clean -> Icons.Rounded.Kitchen
        "bath" in clean || "bathroom" in clean -> Icons.Rounded.Bathtub
        "gas" in clean -> Icons.Rounded.LocalGasStation
        "garden" in clean || "flower" in clean -> Icons.Rounded.LocalFlorist
        "clean" in clean || "laundry" in clean || "detergent" in clean -> Icons.Rounded.CleaningServices
        "health" in clean || "medical" in clean || "heart" in clean || "hospital" in clean || "doctor" in clean || "clinic" in clean || "medicine" in clean || "pharmacy" in clean || "vitamin" in clean -> Icons.Rounded.LocalHospital
        "education" in clean || "graduation" in clean || "school" in clean || "university" in clean || "certificate" in clean || "award" in clean || "brain" in clean || "lightbulb" in clean || "book" in clean || "library" in clean || "course" in clean || "pencil" in clean || "pen" in clean || "notebook" in clean || "ruler" in clean || "eraser" in clean || "clipboard" in clean || "exam" in clean -> Icons.Rounded.School
        "movie" in clean || "film" in clean || "theater" in clean -> Icons.Rounded.Movie
        "music" in clean || "podcast" in clean || "concert" in clean || "speaker" in clean || "mic" in clean || "headphone" in clean || "play" in clean || "pause" in clean -> Icons.Rounded.MusicNote
        "camera" in clean || "photo" in clean -> Icons.Rounded.CameraAlt
        "palette" in clean || "art" in clean -> Icons.Rounded.Palette
        "travel" in clean || "plane" in clean -> Icons.Rounded.Flight
        "hotel" in clean || "camp" in clean || "holiday" in clean || "beach" in clean || "mountain" in clean || "backpack" in clean || "passport" in clean || "luggage" in clean -> Icons.Rounded.Park
        "salary" in clean || "wallet" in clean || "money" in clean || "cash" in clean || "banknote" in clean || "coins" in clean || "hand coins" in clean || "badge dollar sign" in clean || "dollar" in clean || "commission" in clean || "capital injection" in clean || "other income" in clean || "asset sale" in clean -> Icons.Rounded.Paid
        "save" in clean || "piggy" in clean || "savings" in clean || "interest" in clean || "investment" in clean -> Icons.Rounded.Savings
        "bank" in clean || "atm" in clean -> Icons.Rounded.LocalAtm
        "profit" in clean || "loss" in clean || "analytics" in clean || "chart" in clean || "graph" in clean || "percent" in clean || "growth" in clean || "finance" in clean || "tax" in clean -> Icons.Rounded.PieChart
        "insurance" in clean || "protect" in clean || "shield" in clean -> Icons.Rounded.Shield
        "office" in clean || "work" in clean || "company" in clean || "business" in clean || "briefcase" in clean || "project" in clean || "workflow" in clean || "desk" in clean || "chair" in clean || "service" in clean -> Icons.Rounded.Work
        "code" in clean || "software" in clean || "server" in clean || "app window" in clean -> Icons.Rounded.Code
        "mail" in clean || "paperclip" in clean -> Icons.Rounded.Edit
        "monitor" in clean || "browser" in clean -> Icons.Rounded.Tv
        "love" in clean || "dating" in clean -> Icons.Rounded.FavoriteBorder
        "spa" in clean || "perfume" in clean || "skincare" in clean -> Icons.Rounded.Spa
        "emergency" in clean || "ambulance" in clean || "first aid" in clean || "bandage" in clean || "medical cross" in clean || "siren" in clean || "fire" in clean || "police" in clean || "warning" in clean || "alert" in clean -> Icons.Rounded.LocalHospital
        "settings" in clean || "plus" in clean -> Icons.Rounded.Build
        "all" in clean || "other" in clean || "misc" in clean -> Icons.Rounded.Category
        else -> pocketCategoryFallbackIcons[(clean.hashCode().let { if (it < 0) -it else it }) % pocketCategoryFallbackIcons.size]
    }
}
