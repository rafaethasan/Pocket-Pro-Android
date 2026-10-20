package com.softx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.rounded.DirectionsBike
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Bathtub
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Cottage
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DirectionsBoat
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Diversity3
import androidx.compose.material.icons.rounded.DownhillSkiing
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.SmokingRooms
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Pets
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
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LunchDining
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Nightlife
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.RunCircle
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.SportsBasketball
import androidx.compose.material.icons.rounded.SportsBaseball
import androidx.compose.material.icons.rounded.SportsCricket
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.SportsTennis
import androidx.compose.material.icons.rounded.SportsVolleyball
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.TableBar
import androidx.compose.material.icons.rounded.TableRestaurant
import androidx.compose.material.icons.rounded.TempleHindu
import androidx.compose.material.icons.rounded.Toys
import androidx.compose.material.icons.rounded.Train
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.data.PocketNativeCategoryIconOption
import com.softx.app.data.PocketNativeCategoryManagerPayload
import com.softx.app.data.PocketNativeRepository
import com.softx.app.data.PocketNativeTransactionCategoryOption
import com.softx.app.ui.theme.AccentBlue
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentOrange
import com.softx.app.ui.theme.AccentPink
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.SurfaceCard
import com.softx.app.ui.theme.SurfaceCardSoft
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun CategorySettingsNativeScreen(
    repository: PocketNativeRepository,
    initialKind: String,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var currentKind by remember { mutableStateOf(if (initialKind.lowercase() == "income") "income" else "expense") }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }
    var payload by remember { mutableStateOf(PocketNativeCategoryManagerPayload()) }
    var editingCategory by remember { mutableStateOf<PocketNativeTransactionCategoryOption?>(null) }
    var creatingNew by remember { mutableStateOf(false) }
    var pendingDeleteCategory by remember { mutableStateOf<PocketNativeTransactionCategoryOption?>(null) }

    fun load(kind: String) {
        scope.launch {
            loading = true
            saving = false
            errorText = ""
            val result = repository.categories(kind)
            loading = false
            if (result.isSuccess) {
                payload = result.getOrNull()?.payload ?: PocketNativeCategoryManagerPayload(currentKind = kind)
            } else {
                errorText = result.exceptionOrNull()?.message ?: "Pocket Pro categories could not be loaded."
            }
        }
    }

    LaunchedEffect(currentKind) {
        load(currentKind)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = AccentPink,
            )
            return@Box
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                NativeFeatureTopBar(
                    title = "Category settings",
                    subtitle = "Create, arrange and refine your Pocket Pro categories",
                    onBack = onBack,
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CategoryKindLine("Expense", currentKind == "expense", Modifier.weight(1f)) { currentKind = "expense" }
                    CategoryKindLine("Income", currentKind == "income", Modifier.weight(1f)) { currentKind = "income" }
                }
            }
            if (errorText.isNotBlank()) {
                item { CategoryStatusCard(errorText, Color(0xFFFF5B6E)) }
            }
            items(payload.categories, key = { it.key }) { item ->
                CategoryRowCard(
                    item = item,
                    onEdit = { editingCategory = item },
                    onDelete = { pendingDeleteCategory = item },
                    onMoveUp = {
                        scope.launch {
                            val result = repository.moveCategory(currentKind, item.key, "up")
                            if (result.isSuccess) payload = result.getOrNull()?.payload ?: payload
                        }
                    },
                    onMoveDown = {
                        scope.launch {
                            val result = repository.moveCategory(currentKind, item.key, "down")
                            if (result.isSuccess) payload = result.getOrNull()?.payload ?: payload
                        }
                    },
                )
            }
            item {
                CategoryHeaderAction(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { creatingNew = true }
                        .padding(vertical = 10.dp),
                    label = "Add category",
                    icon = Icons.Rounded.Add,
                    tint = AccentOrange,
                )
            }
            item { Spacer(modifier = Modifier.height(22.dp)) }
        }
    }

    if (creatingNew || editingCategory != null) {
        CategoryEditDialog(
            title = if (editingCategory != null) "Edit category" else "Create category",
            category = editingCategory,
            colorSwatches = payload.colorSwatches,
            saving = saving,
            onDismiss = {
                creatingNew = false
                editingCategory = null
            },
            onSave = { originalKey, label, iconKey, colorHex ->
                scope.launch {
                    saving = true
                    val result = repository.saveCategory(currentKind, originalKey, label, iconKey, colorHex)
                    saving = false
                    if (result.isSuccess) {
                        payload = result.getOrNull()?.payload ?: payload
                        creatingNew = false
                        editingCategory = null
                    } else {
                        errorText = result.exceptionOrNull()?.message ?: "Category could not be saved."
                    }
                }
            },
        )
    }

    pendingDeleteCategory?.let { category ->
        PocketDeleteConfirmationDialog(
            title = "Delete category?",
            message = "Are you sure you want to delete ${category.label}? This category may no longer appear in Pocket Pro lists.",
            deleting = saving,
            onDismiss = { pendingDeleteCategory = null },
            onConfirm = {
                scope.launch {
                    saving = true
                    val result = repository.deleteCategory(currentKind, category.key)
                    saving = false
                    if (result.isSuccess) {
                        payload = result.getOrNull()?.payload ?: payload
                        pendingDeleteCategory = null
                    } else {
                        errorText = result.exceptionOrNull()?.message ?: "Category could not be deleted."
                    }
                }
            },
        )
    }
}

@Composable
private fun CategoryHeaderAction(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    tint: Color,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            Text(
                label,
                color = tint,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(min = 0.dp),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(tint.copy(alpha = 0.8f), RoundedCornerShape(99.dp)),
        )
    }
}

@Composable
private fun CategoryKindLine(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            label,
            color = if (selected) TextPrimary else TextSecondary,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(if (selected) AccentPurple else Color.Transparent, RoundedCornerShape(99.dp)),
        )
    }
}

@Composable
private fun CategoryRowCard(
    item: PocketNativeTransactionCategoryOption,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.label,
                modifier = Modifier.weight(1f),
                color = TextPrimary,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                fontSize = 18.sp,
            )
            IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, null, tint = AccentPink) }
            IconButton(onClick = onEdit) { Icon(Icons.Rounded.Edit, null, tint = TextPrimary) }
            IconButton(onClick = onMoveUp) { Icon(Icons.Rounded.ArrowUpward, null, tint = TextSecondary) }
            IconButton(onClick = onMoveDown) { Icon(Icons.Rounded.ArrowDownward, null, tint = TextSecondary) }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(TextSecondary.copy(alpha = 0.12f)),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryEditDialog(
    title: String,
    category: PocketNativeTransactionCategoryOption?,
    colorSwatches: List<String>,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit,
) {
    var label by remember(category) { mutableStateOf(category?.label.orEmpty()) }
    var selectedIconKey by remember(category) { mutableStateOf(category?.iconKey.orEmpty().ifBlank { "shopping" }) }
    var selectedColor by remember(category) { mutableStateOf(category?.colorHex.orEmpty().ifBlank { colorSwatches.firstOrNull() ?: "#7C4DFF" }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onSave(category?.key.orEmpty(), label, selectedIconKey, selectedColor) },
                enabled = !saving && label.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = Color.Black),
            ) {
                Text(if (saving) "Saving..." else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text(title, color = TextPrimary) },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 560.dp, max = 560.dp),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        OutlinedTextField(
                            value = label,
                            onValueChange = { label = it },
                            label = { Text("Category name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = SurfaceCardSoft,
                                focusedContainerColor = SurfaceCard,
                                unfocusedContainerColor = SurfaceCard,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                            ),
                        )
                    }
                    item {
                        Text("Choose color", color = TextSecondary, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    }
                    item {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            colorSwatches.forEach { swatch ->
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(Color(android.graphics.Color.parseColor(swatch)), CircleShape)
                                        .clickable { selectedColor = swatch },
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun CategoryStatusCard(message: String, accentColor: Color) {
    Text(
        text = message,
        color = accentColor,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
    )
}
