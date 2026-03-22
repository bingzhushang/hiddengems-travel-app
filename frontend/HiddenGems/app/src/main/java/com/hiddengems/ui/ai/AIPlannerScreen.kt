package com.hiddengems.ui.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class
@Composable
fun AIPlannerScreen(
    onBackClick: () -> Unit,
    onItineraryGenerated: () -> Unit
) {
    // Form state
    var destination by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var budgetLevel by remember { mutableStateOf("medium") }
    var crowdPreference by remember { mutableStateOf("avoid") }
    val selectedStyles = remember { mutableStateListOf<String>() }
    var transportation by remember { mutableStateOf("self_drive") }

    // UI state
    var isGenerating by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf<DatePickerType?>(null) }
    val travelStyles = listOf("自然", "人文", "美食", "摄影", "亲子", "户外", "购物", "休闲")
    val budgetLevels = listOf(
        "economy" to "经济",
        "medium" to "中等",
        "luxury" to "豪华"
    )
    val crowdPreferences = listOf(
        "avoid" to "避开人群",
        "moderate" to "适度",
        "any" to "无所谓"
    )
    val transportations = listOf(
        "self_drive" to "自驾",
        "public" to "公共交通"
        "mixed" to "混合"
    )

    Scaffold(
        topBar = {
                TopAppBar(
                    title = { Text("AI 行程规划") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "智能行程规划",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "告诉我你的旅行偏好，AI为你定制专属行程",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        }
                    }
                }
            }

            // Destination
            OutlinedTextField(
                value = destination,
                onValueChange = { destination = it },
                label = { Text("目的地") },
                placeholder = { Text("例如:杭州、成都、大理") },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date Range
            DateRangeInput(
                startDate = startDate,
                endDate = endDate,
                onStartDateChange = { startDate = it },
                onEndDateChange = { endDate = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Budget Level
            Text(
                text = "预算水平",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                budgetLevels.forEach { (value, label ->
                    FilterChip(
                        selected = budgetLevel == value,
                        onClick = { budgetLevel = value },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Crowd Preference
            Text(
                text = "人流偏好",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                crowdPreferences.forEach { (value, label ->
                    FilterChip(
                        selected = crowdPreference == value,
                        onClick = { crowdPreference = value },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Travel Styles
            Text(
                text = "旅行风格 (可多选)",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                travelStyles.forEach { style ->
                    FilterChip(
                        selected = style in selectedStyles,
                        onClick = {
                            if (selectedStyles.contains(style)) {
                                selectedStyles.remove(style)
                            } else {
                                selectedStyles.add(style)
                            }
                        },
                        label = { Text(style) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transportation
            Text(
                text = "出行方式",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                transportations.forEach { (value, label ->
                    FilterChip(
                        selected = transportation == value,
                        onClick = { transportation = value },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp)

            // Generate Button
            Button(
                onClick = {
                    if (destination.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank()) {
                        generateItinerary()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isGenerating
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isGenerating) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = "生成行程..."
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "生成行程",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("行程生成成功!") },
            text = {
                Text(
                    text = "AI已为您生成了专属行程,是否立即查看?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onItineraryGenerated()
                    }
                ) {
                    Text("查看行程")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("关闭")
                }
            }
        )
    }
}

@Composable
private fun DateRangeInput(
    startDate: String,
    endDate: String,
    onStartDateChange: (String) -> Unit
    onEndDateChange: (String) -> Unit
) {
    var startError by remember { mutableStateOf<String?>(null) }
    var endError by remember { mutableStateOf<String?>(null) }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = startDate,
            onValueChange = {
                val parsed = try {
                    dateFormat.parse(it)
                    onStartDateChange(it)
                    startError = null
                } catch (e: Exception) {
                    startError = "请输入有效日期 (YYYY-MM-DD)"
                }
            },
            label = { Text("开始日期") },
            placeholder = { Text("YYYY-MM-DD") },
            isError = startError != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = endDate,
            onValueChange = {
                val parsed = try {
                    dateFormat.parse(it)
                    onEndDateChange(it)
                    endError = null
                } catch (e: Exception) {
                    endError = "请输入有效日期 (YYYY-MM-DD)"
                }
            },
            label = { Text("结束日期") },
            placeholder = { Text("YYYY-MM-DD") },
            isError = endError != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

enum class DatePickerType {
    START,
    END
}

private fun generateItinerary(
    destination: String,
    startDate: String,
    endDate: String
    budgetLevel: String
    crowdPreference: String
    travelStyles: List<String>
    transportation: String,
    onGeneratingChange: (Boolean) -> Unit,
    onSuccess: () -> Unit
) {
    // Simulate API call
    LaunchedEffect(destination, startDate, endDate, budgetLevel, crowdPreference, travelStyles, transportation) {
        kotlinx.coroutines.delay(2000)
        isGenerating = true
        onSuccess()
    }
}