package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileCategory
import com.example.model.JarvisFileItem
import com.example.ui.components.HudCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FilesScreen(
    files: List<JarvisFileItem>,
    onAddFile: (JarvisFileItem) -> Unit,
    onDeleteFile: (String) -> Unit,
    onOpenFile: (JarvisFileItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<FileCategory?>(null) }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "document_${System.currentTimeMillis()}"
            val lower = fileName.lowercase(Locale.ROOT)
            val category = when {
                lower.contains("prescri") || lower.contains("rx") || lower.contains("doctor") || lower.contains("med") -> FileCategory.PRESCRIPTIONS
                lower.contains("jpg") || lower.contains("png") || lower.contains("jpeg") || lower.contains("photo") -> FileCategory.IMAGES
                lower.contains("receipt") || lower.contains("bill") || lower.contains("invoice") || lower.contains("memo") -> FileCategory.RECEIPTS
                lower.contains("pdf") || lower.contains("doc") || lower.contains("txt") -> FileCategory.DOCUMENTS
                else -> FileCategory.DOCUMENTS
            }
            val suggested = when (category) {
                FileCategory.PRESCRIPTIONS -> "Prescription_${SimpleDateFormat("MMM_yyyy", Locale.ENGLISH).format(Date())}.pdf"
                FileCategory.DOCUMENTS -> "Document_${fileName}"
                FileCategory.RECEIPTS -> "Receipt_${SimpleDateFormat("dd_MMM", Locale.ENGLISH).format(Date())}.pdf"
                FileCategory.IMAGES -> "Image_Scanned_${System.currentTimeMillis() % 10000}.jpg"
                FileCategory.OTHER -> "File_${fileName}"
            }

            val newFile = JarvisFileItem(
                id = System.currentTimeMillis().toString(),
                name = fileName,
                suggestedName = suggested,
                category = category,
                uriString = uri.toString(),
                sizeFormatted = "1.5 MB",
                dateAdded = SimpleDateFormat("dd MMMM yyyy", Locale("bn", "BD")).format(Date())
            )
            onAddFile(newFile)
        }
    }

    val filteredFiles = files.filter { file ->
        val matchesQuery = file.name.contains(searchQuery, ignoreCase = true) ||
                file.suggestedName.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryFilter == null || file.category == selectedCategoryFilter
        matchesQuery && matchesCategory
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
    ) {
        // Upload Button & Search
        item {
            HudCard(
                title = "ফাইল আপলোড ও ক্যাটাগরাইজার",
                icon = Icons.Default.FolderOpen
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("upload_file_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HudCyanGlow,
                            contentColor = HudVoidDark
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.UploadFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ফাইল আপলোড করুন (অটো-ক্যাটাগরি ও রিনেম)", fontWeight = FontWeight.Bold)
                    }

                    // Search TextField
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("ফাইলের নাম দিয়ে অনুসন্ধান...", color = HudTextMuted) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = HudCyanGlow)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "ক্লিয়ার", tint = HudTextMuted)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HudCyanGlow,
                            unfocusedBorderColor = HudBorderCyan,
                            focusedTextColor = HudTextPrimary,
                            unfocusedTextColor = HudTextPrimary
                        ),
                        singleLine = true
                    )
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    CategoryChip(
                        label = "সব ফাইল (${files.size})",
                        isSelected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null }
                    )
                }
                items(FileCategory.values()) { cat ->
                    val count = files.count { it.category == cat }
                    CategoryChip(
                        label = "${cat.labelBn} ($count)",
                        isSelected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = cat }
                    )
                }
            }
        }

        // File List Header
        item {
            Text(
                text = "সংরক্ষিত ফাইল সমূহ (${filteredFiles.size})",
                color = HudCyanGlow,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (filteredFiles.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "কোনো ফাইল পাওয়া যায়নি। উপরে 'ফাইল আপলোড করুন' চাপুন।",
                        color = HudTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(filteredFiles, key = { it.id }) { file ->
                FileItemCard(
                    file = file,
                    onOpen = { onOpenFile(file) },
                    onDelete = { onDeleteFile(file.id) }
                )
            }
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) HudCyanGlow.copy(alpha = 0.25f) else HudSurfaceVariant,
        border = BorderStroke(1.dp, if (isSelected) HudCyanGlow else HudBorderCyan),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (isSelected) HudCyanGlow else HudTextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun FileItemCard(
    file: JarvisFileItem,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, HudBorderCyan), RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = HudSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = when (file.category) {
                            FileCategory.PRESCRIPTIONS -> Icons.Default.MedicalServices
                            FileCategory.IMAGES -> Icons.Default.Image
                            FileCategory.RECEIPTS -> Icons.Default.Receipt
                            FileCategory.DOCUMENTS -> Icons.Default.Description
                            FileCategory.OTHER -> Icons.Default.InsertDriveFile
                        },
                        contentDescription = null,
                        tint = when (file.category) {
                            FileCategory.PRESCRIPTIONS -> HudAmberGold
                            FileCategory.IMAGES -> HudCyanGlow
                            else -> HudBlueElectric
                        },
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = file.name,
                            color = HudTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "প্রস্তাবিত নাম: ${file.suggestedName}",
                                color = HudCyanDim,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "মুছুন",
                        tint = HudAlertRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ক্যাটাগরি: ${file.category.labelBn}",
                        color = HudAmberGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(HudAmberGold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Text(
                        text = file.sizeFormatted,
                        color = HudTextSecondary,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = onOpen,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HudCyanGlow.copy(alpha = 0.2f),
                        contentColor = HudCyanGlow
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ভিউ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
