// FILE: app/src/main/java/com/sandeep/ganitabigyan/UnitConverterScreen.kt
package com.sandeep.ganitabigyan

import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sandeep.ganitabigyan.utils.toLocaleNumerals
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import kotlin.math.pow

// --- Data Models ---
data class UnitItem(@StringRes val nameResId: Int, val symbol: String, val toBase: (Double) -> Double, val fromBase: (Double) -> Double)
data class ConversionCategory(@StringRes val nameResId: Int, val icon: ImageVector, val colors: List<Color>, val units: List<UnitItem>)

// --- Main Composable ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(navController: NavController) {
    val categories = remember { getConversionCategories() }
    var selectedCategory by remember { mutableStateOf(categories.first()) }

    // inputValue stores the ENGLISH number for calculation (e.g., "123.5")
    var inputValue by remember { mutableStateOf("1") }

    var fromUnit by remember { mutableStateOf(selectedCategory.units.first()) }
    var toUnit by remember { mutableStateOf(selectedCategory.units.getOrNull(1) ?: selectedCategory.units.first()) }

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var isSelectingFromUnit by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // Reset input when category changes
    LaunchedEffect(selectedCategory) {
        inputValue = "1"
        fromUnit = selectedCategory.units.first()
        toUnit = selectedCategory.units.getOrNull(1) ?: selectedCategory.units.first()
    }

    val outputValue = remember(inputValue, fromUnit, toUnit) {
        if (inputValue.isEmpty() || inputValue == "-") return@remember ""
        val input = inputValue.toDoubleOrNull() ?: 0.0
        val baseValue = fromUnit.toBase(input)
        val result = toUnit.fromBase(baseValue)

        // Smart formatting: avoids scientific notation for common numbers
        val df = DecimalFormat("#.##########")
        df.format(result)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.unit_converter_title), fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back_button_description)) } }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Category Selector
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(categories) { category ->
                    CategoryChip(
                        category = category,
                        isSelected = category == selectedCategory,
                        onClick = { selectedCategory = category }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Display Area (Scrollable if screen is small)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Input Card
                UnitDisplayCard(
                    label = stringResource(id = R.string.unit_converter_from),
                    value = inputValue.toLocaleNumerals(context), // Localize displayed number
                    unitSymbol = fromUnit.symbol,
                    onUnitClicked = {
                        isSelectingFromUnit = true
                        showBottomSheet = true
                    },
                    activeColor = selectedCategory.colors.first()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Swap Button
                SwapButton(onClick = {
                    val temp = fromUnit
                    fromUnit = toUnit
                    toUnit = temp
                }, color = selectedCategory.colors.last())

                Spacer(modifier = Modifier.height(12.dp))

                // Output Card
                UnitDisplayCard(
                    label = stringResource(id = R.string.unit_converter_to),
                    value = outputValue.toLocaleNumerals(context), // Localize displayed number
                    unitSymbol = toUnit.symbol,
                    onUnitClicked = {
                        isSelectingFromUnit = false
                        showBottomSheet = true
                    },
                    activeColor = Color.Gray, // Output is read-only style
                    isReadOnly = true
                )
            }

            // 3. Custom Numeric Keyboard
            CustomKeyboard(
                onKeyPressed = { key ->
                    when (key) {
                        "C" -> inputValue = ""
                        "⌫" -> if (inputValue.isNotEmpty()) inputValue = inputValue.dropLast(1)
                        "." -> if (!inputValue.contains(".")) inputValue += "."
                        "-" -> {
                            if (inputValue.startsWith("-")) inputValue = inputValue.substring(1)
                            else inputValue = "-$inputValue"
                        }
                        else -> inputValue += key
                    }
                },
                themeColor = selectedCategory.colors.last(),
                showMinus = selectedCategory.nameResId == R.string.category_temperature
            )
        }

        // Bottom Sheet for Unit Selection
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                Text(
                    text = stringResource(if (isSelectingFromUnit) R.string.unit_converter_from else R.string.unit_converter_to),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
                )
                LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
                    items(selectedCategory.units) { unit ->
                        ListItem(
                            headlineContent = { Text(stringResource(id = unit.nameResId)) },
                            trailingContent = { Text(unit.symbol, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.clickable {
                                if (isSelectingFromUnit) fromUnit = unit else toUnit = unit
                                scope.launch { sheetState.hide() }.invokeOnCompletion { if (!sheetState.isVisible) showBottomSheet = false }
                            }
                        )
                    }
                }
            }
        }
    }
}

// --- UI Components ---

@Composable
private fun UnitDisplayCard(
    label: String,
    value: String,
    unitSymbol: String,
    onUnitClicked: () -> Unit,
    activeColor: Color,
    isReadOnly: Boolean = false
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth().height(100.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                // Auto-resizing Text Logic
                val fontSize = calculateFontSize(value.length)

                Text(
                    text = if(value.isEmpty()) "0" else value,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = if (isReadOnly) MaterialTheme.colorScheme.onSurface else activeColor,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Unit Selector Button
            Surface(
                onClick = onUnitClicked,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = unitSymbol, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
        }
    }
}

// Helper to resize text based on length
private fun calculateFontSize(length: Int): TextUnit {
    return when {
        length > 15 -> 18.sp
        length > 10 -> 24.sp
        length > 7 -> 32.sp
        else -> 40.sp
    }
}

@Composable
private fun CategoryChip(category: ConversionCategory, isSelected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(targetValue = if (isSelected) 1.1f else 1.0f, label = "scale")
    val alpha = if (isSelected) 1f else 0.6f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scale).clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(colors = category.colors))
                .border(if (isSelected) 3.dp else 0.dp, MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = category.icon, contentDescription = null, tint = Color.White.copy(alpha = alpha), modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(id = category.nameResId),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if(isSelected) 1f else 0.7f)
        )
    }
}

@Composable
private fun SwapButton(onClick: () -> Unit, color: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.SwapVert, contentDescription = "Swap", tint = Color.White)
    }
}

// --- CUSTOM KEYBOARD ---

@Composable
fun CustomKeyboard(
    onKeyPressed: (String) -> Unit,
    themeColor: Color,
    showMinus: Boolean
) {
    val context = LocalContext.current

    // Keys layout
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9")
    )
    val bottomRow = if (showMinus) listOf(".", "0", "⌫", "-") else listOf(".", "0", "⌫")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = 16.dp, top = 8.dp)
    ) {
        keys.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth().height(60.dp)) {
                row.forEach { key ->
                    KeyboardKey(
                        key = key,
                        weight = 1f,
                        color = themeColor,
                        context = context,
                        onClick = onKeyPressed
                    )
                }
            }
        }
        // Bottom Row
        Row(modifier = Modifier.fillMaxWidth().height(60.dp)) {
            bottomRow.forEach { key ->
                KeyboardKey(
                    key = key,
                    weight = 1f,
                    color = themeColor,
                    context = context,
                    onClick = onKeyPressed
                )
            }
        }
    }
}

@Composable
fun RowScope.KeyboardKey(
    key: String,
    weight: Float,
    color: Color,
    context: android.content.Context,
    onClick: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = color),
                onClick = { onClick(key) }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (key == "⌫") {
            Icon(Icons.Default.Backspace, contentDescription = "Backspace", tint = color)
        } else if (key == "-") {
            // Plus-Minus symbol for temperature
            Text(text = "±", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
        } else {
            // Localize the number on the button
            val displayKey = if (key.all { it.isDigit() }) key.toLocaleNumerals(context) else key
            Text(
                text = displayKey,
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


// --- Corrected Data Source (Binary Storage Logic) ---

private fun getConversionCategories(): List<ConversionCategory> {
    return listOf(
        // 1. Length
        ConversionCategory(R.string.category_length, Icons.Default.Straighten, listOf(Color(0xFF4FC3F7), Color(0xFF0288D1)), listOf(
            UnitItem(R.string.unit_millimetre, "mm", { it / 1000 }, { it * 1000 }),
            UnitItem(R.string.unit_centimetre, "cm", { it / 100 }, { it * 100 }),
            UnitItem(R.string.unit_metre, "m", { it }, { it }),
            UnitItem(R.string.unit_kilometre, "km", { it * 1000 }, { it / 1000 }),
            UnitItem(R.string.unit_inch, "in", { it * 0.0254 }, { it / 0.0254 }),
            UnitItem(R.string.unit_foot, "ft", { it * 0.3048 }, { it / 0.3048 }),
            UnitItem(R.string.unit_yard, "yd", { it * 0.9144 }, { it / 0.9144 }),
            UnitItem(R.string.unit_mile, "mi", { it * 1609.34 }, { it / 1609.34 })
        )),
        // 2. Weight
        ConversionCategory(R.string.category_weight, Icons.Default.Scale, listOf(Color(0xFFAED581), Color(0xFF689F38)), listOf(
            UnitItem(R.string.unit_milligram, "mg", { it / 1000 }, { it * 1000 }),
            UnitItem(R.string.unit_gram, "g", { it }, { it }),
            UnitItem(R.string.unit_kilogram, "kg", { it * 1000 }, { it / 1000 }),
            UnitItem(R.string.unit_quintal, "q", { it * 100000 }, { it / 100000 }),
            UnitItem(R.string.unit_tonne, "t", { it * 1000000 }, { it / 1000000 }),
            UnitItem(R.string.unit_pound, "lb", { it * 453.592 }, { it / 453.592 })
        )),
        // 3. Capacity
        ConversionCategory(R.string.category_capacity, Icons.Default.Opacity, listOf(Color(0xFFCE93D8), Color(0xFF8E24AA)), listOf(
            UnitItem(R.string.unit_millilitre, "ml", { it / 1000 }, { it * 1000 }),
            UnitItem(R.string.unit_litre, "l", { it }, { it }),
            UnitItem(R.string.unit_kilolitre, "kl", { it * 1000 }, { it / 1000 }),
            UnitItem(R.string.unit_cubic_centimetre, "cc", { it / 1000 }, { it * 1000 })
        )),
        // 4. Temperature
        ConversionCategory(R.string.category_temperature, Icons.Default.Thermostat, listOf(Color(0xFFEF9A9A), Color(0xFFD32F2F)), listOf(
            UnitItem(R.string.unit_celsius, "°C", { it }, { it }),
            UnitItem(R.string.unit_fahrenheit, "°F", { (it - 32) * 5 / 9 }, { (it * 9 / 5) + 32 }),
            UnitItem(R.string.unit_kelvin, "K", { it - 273.15 }, { it + 273.15 })
        )),
        // 5. Time
        ConversionCategory(R.string.category_time, Icons.Default.Schedule, listOf(Color(0xFFFFCC80), Color(0xFFF57C00)), listOf(
            UnitItem(R.string.unit_second, "sec", { it }, { it }),
            UnitItem(R.string.unit_minute, "min", { it * 60 }, { it / 60 }),
            UnitItem(R.string.unit_hour, "hr", { it * 3600 }, { it / 3600 }),
            UnitItem(R.string.unit_day, "d", { it * 86400 }, { it / 86400 }),
            UnitItem(R.string.unit_week, "wk", { it * 604800 }, { it / 604800 }),
            UnitItem(R.string.unit_month, "mo", { it * 2628000 }, { it / 2628000 }),
            UnitItem(R.string.unit_year, "yr", { it * 31536000 }, { it / 31536000 })
        )),
        // 6. Storage (Moved to Last)
        ConversionCategory(R.string.category_storage, Icons.Default.SdStorage, listOf(Color(0xFF607D8B), Color(0xFF455A64)), listOf(
            // Base unit: Bit
            UnitItem(R.string.unit_bit, "b", { it }, { it }),
            UnitItem(R.string.unit_byte, "B", { it * 8 }, { it / 8 }),
            // Binary Prefixes (1024 based) - Standard for OS/Memory
            UnitItem(R.string.unit_kilobit, "Kb", { it * 1024 }, { it / 1024 }),
            UnitItem(R.string.unit_kilobyte, "KB", { it * 8 * 1024 }, { it / (8 * 1024) }),
            UnitItem(R.string.unit_megabit, "Mb", { it * 1024.0.pow(2) }, { it / 1024.0.pow(2) }),
            UnitItem(R.string.unit_megabyte, "MB", { it * 8 * 1024.0.pow(2) }, { it / (8 * 1024.0.pow(2)) }),
            UnitItem(R.string.unit_gigabit, "Gb", { it * 1024.0.pow(3) }, { it / 1024.0.pow(3) }),
            UnitItem(R.string.unit_gigabyte, "GB", { it * 8 * 1024.0.pow(3) }, { it / (8 * 1024.0.pow(3)) }),
            UnitItem(R.string.unit_terabit, "Tb", { it * 1024.0.pow(4) }, { it / 1024.0.pow(4) }),
            UnitItem(R.string.unit_terabyte, "TB", { it * 8 * 1024.0.pow(4) }, { it / (8 * 1024.0.pow(4)) })
        ))
    )
}