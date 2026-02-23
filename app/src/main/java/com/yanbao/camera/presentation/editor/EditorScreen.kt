package com.yanbao.camera.presentation.editor

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yanbao.camera.R
import com.yanbao.camera.presentation.theme.GRADIENT_KUROMI
import com.yanbao.camera.presentation.theme.OBSIDIAN_BLACK
import com.yanbao.camera.presentation.theme.PRIMARY_PINK
import com.yanbao.camera.ui.theme.YanbaoBrandTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val KUROMI_PINK   = Color(0xFFEC4899)
private val KUROMI_PURPLE = Color(0xFF9D4EDD)

// ─── 编辑工具数据类 ───────────────────────────────────────────────────────────
private data class EditorTool(
    val name: String,
    val iconRes: Int,
    val hasSlider: Boolean = true,
    val sliderMin: Float = -1f,
    val sliderMax: Float = 1f,
    val sliderDefault: Float = 0f,
    val sliderLabel: String = ""
)

private val editorTools = listOf(
    EditorTool("裁剪",   R.drawable.ic_tool_crop_kuromi,       hasSlider = false),
    EditorTool("旋转",   R.drawable.ic_tool_rotate_kuromi,     hasSlider = true,  sliderMin = -180f, sliderMax = 180f, sliderDefault = 0f, sliderLabel = "旋转角度"),
    EditorTool("翻转",   R.drawable.ic_tool_flip_kuromi,       hasSlider = false),
    EditorTool("亮度",   R.drawable.ic_tool_brightness_kuromi, hasSlider = true,  sliderMin = -1f,   sliderMax = 1f,   sliderDefault = 0f, sliderLabel = "亮度"),
    EditorTool("对比度", R.drawable.ic_tool_contrast_kuromi,   hasSlider = true,  sliderMin = -1f,   sliderMax = 1f,   sliderDefault = 0f, sliderLabel = "对比度"),
    EditorTool("饱和度", R.drawable.ic_tool_saturation_kuromi, hasSlider = true,  sliderMin = -1f,   sliderMax = 1f,   sliderDefault = 0f, sliderLabel = "饱和度"),
    EditorTool("曲线",   R.drawable.ic_tool_curve_kuromi,      hasSlider = true,  sliderMin = -1f,   sliderMax = 1f,   sliderDefault = 0f, sliderLabel = "曲线"),
    EditorTool("HSL",    R.drawable.ic_tool_hsl_kuromi,        hasSlider = true,  sliderMin = -1f,   sliderMax = 1f,   sliderDefault = 0f, sliderLabel = "色相"),
    EditorTool("锐化",   R.drawable.ic_tool_sharpness_kuromi,  hasSlider = true,  sliderMin = 0f,    sliderMax = 1f,   sliderDefault = 0f, sliderLabel = "锐化"),
    EditorTool("降噪",   R.drawable.ic_tool_denoise_kuromi,    hasSlider = true,  sliderMin = 0f,    sliderMax = 1f,   sliderDefault = 0f, sliderLabel = "降噪"),
    EditorTool("暗角",   R.drawable.ic_tool_vignette_kuromi,   hasSlider = true,  sliderMin = 0f,    sliderMax = 1f,   sliderDefault = 0f, sliderLabel = "暗角"),
    EditorTool("色温",   R.drawable.ic_tool_temp_kuromi,       hasSlider = true,  sliderMin = -1f,   sliderMax = 1f,   sliderDefault = 0f, sliderLabel = "色温"),
    EditorTool("色调",   R.drawable.ic_tool_tint_kuromi,       hasSlider = true,  sliderMin = -1f,   sliderMax = 1f,   sliderDefault = 0f, sliderLabel = "色调"),
    EditorTool("滤镜",   R.drawable.ic_apply_filter_kuromi,    hasSlider = false),
    EditorTool("美颜",   R.drawable.ic_tool_beauty_kuromi,     hasSlider = true,  sliderMin = 0f,    sliderMax = 1f,   sliderDefault = 0f, sliderLabel = "美颜强度"),
    EditorTool("文字",   R.drawable.ic_tool_text_kuromi,       hasSlider = false),
    EditorTool("贴纸",   R.drawable.ic_tool_sticker_kuromi,    hasSlider = false),
    EditorTool("29D同步",R.drawable.ic_tool_29d_kuromi,        hasSlider = false)
)

// ─── 编辑历史（撤销/重做）─────────────────────────────────────────────────────
private data class EditState(
    val brightness: Float = 0f,
    val contrast: Float = 0f,
    val saturation: Float = 0f,
    val rotation: Float = 0f,
    val sharpen: Float = 0f,
    val denoise: Float = 0f,
    val vignette: Float = 0f,
    val temperature: Float = 0f,
    val tint: Float = 0f,
    val curves: Float = 0f,
    val hsl: Float = 0f,
    val beauty: Float = 0f
)

@Composable
fun EditorScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var historyStack by remember { mutableStateOf(listOf(EditState())) }
    var historyIndex by remember { mutableIntStateOf(0) }
    val currentEdit = historyStack[historyIndex]
    var selectedTool by remember { mutableStateOf<EditorTool?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        historyStack = listOf(EditState())
        historyIndex = 0
        selectedTool = null
    }

    fun updateEdit(newEdit: EditState) {
        val newStack = historyStack.subList(0, historyIndex + 1).toMutableList()
        newStack.add(newEdit)
        if (newStack.size > 20) newStack.removeAt(0)
        historyStack = newStack
        historyIndex = newStack.size - 1
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OBSIDIAN_BLACK)
    ) {
        // 顶部工具栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(OBSIDIAN_BLACK)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back_kuromi),
                    contentDescription = "返回",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { if (historyIndex > 0) { historyIndex--; selectedTool = null } }, enabled = historyIndex > 0) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_undo_kuromi),
                        contentDescription = "撤销",
                        tint = if (historyIndex > 0) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = { if (historyIndex < historyStack.size - 1) { historyIndex++; selectedTool = null } }, enabled = historyIndex < historyStack.size - 1) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_redo_kuromi),
                        contentDescription = "重做",
                        tint = if (historyIndex < historyStack.size - 1) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            YanbaoBrandTitle()
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(brush = GRADIENT_KUROMI)
                    .clickable {
                        if (selectedImageUri != null && !isSaving) {
                            isSaving = true
                            scope.launch {
                                val ok = saveEditedImage(context, selectedImageUri!!, currentEdit)
                                isSaving = false
                                saveMessage = if (ok) "已保存到相册" else "保存失败"
                                delay(2000)
                                saveMessage = null
                            }
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = if (isSaving) "保存中..." else "保存", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // 图片预览区
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Color(0xFF1A1A1A)),
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                val colorMatrix = remember(currentEdit) { buildColorMatrix(currentEdit) }
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "编辑中的照片",
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.colorMatrix(colorMatrix),
                    modifier = Modifier.fillMaxSize().padding(4.dp)
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(painter = painterResource(id = R.drawable.ic_album_kuromi), contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(56.dp))
                    Text(text = "从相册选择照片开始编辑", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(brush = GRADIENT_KUROMI)
                            .clickable { imagePicker.launch("image/*") }
                            .padding(horizontal = 28.dp, vertical = 12.dp)
                    ) {
                        Text(text = "选择照片", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            saveMessage?.let { msg ->
                Box(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
                        .background(Color(0xFF10B981), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) { Text(text = msg, color = Color.White, fontSize = 13.sp) }
            }
        }

        // 参数滑块区
        AnimatedVisibility(visible = selectedTool?.hasSlider == true, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            selectedTool?.let { tool ->
                if (tool.hasSlider) {
                    val sliderValue = when (tool.name) {
                        "亮度" -> currentEdit.brightness; "对比度" -> currentEdit.contrast
                        "饱和度" -> currentEdit.saturation; "旋转" -> currentEdit.rotation
                        "锐化" -> currentEdit.sharpen; "降噪" -> currentEdit.denoise
                        "暗角" -> currentEdit.vignette; "色温" -> currentEdit.temperature
                        "色调" -> currentEdit.tint; "曲线" -> currentEdit.curves
                        "HSL" -> currentEdit.hsl; "美颜" -> currentEdit.beauty
                        else -> 0f
                    }
                    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF1A1A1A)).padding(horizontal = 20.dp, vertical = 12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = tool.sliderLabel, color = KUROMI_PINK, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = if (tool.name == "旋转") "${sliderValue.toInt()}°" else String.format("%.2f", sliderValue), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        }
                        Slider(
                            value = sliderValue,
                            onValueChange = { v ->
                                val ne = when (tool.name) {
                                    "亮度" -> currentEdit.copy(brightness = v); "对比度" -> currentEdit.copy(contrast = v)
                                    "饱和度" -> currentEdit.copy(saturation = v); "旋转" -> currentEdit.copy(rotation = v)
                                    "锐化" -> currentEdit.copy(sharpen = v); "降噪" -> currentEdit.copy(denoise = v)
                                    "暗角" -> currentEdit.copy(vignette = v); "色温" -> currentEdit.copy(temperature = v)
                                    "色调" -> currentEdit.copy(tint = v); "曲线" -> currentEdit.copy(curves = v)
                                    "HSL" -> currentEdit.copy(hsl = v); "美颜" -> currentEdit.copy(beauty = v)
                                    else -> currentEdit
                                }
                                updateEdit(ne)
                            },
                            valueRange = tool.sliderMin..tool.sliderMax,
                            colors = SliderDefaults.colors(thumbColor = KUROMI_PINK, activeTrackColor = KUROMI_PINK, inactiveTrackColor = Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TextButton(onClick = { selectedTool = null }) { Text("关闭", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp) }
                            TextButton(onClick = {
                                val re = when (tool.name) {
                                    "亮度" -> currentEdit.copy(brightness = 0f); "对比度" -> currentEdit.copy(contrast = 0f)
                                    "饱和度" -> currentEdit.copy(saturation = 0f); "旋转" -> currentEdit.copy(rotation = 0f)
                                    "锐化" -> currentEdit.copy(sharpen = 0f); "降噪" -> currentEdit.copy(denoise = 0f)
                                    "暗角" -> currentEdit.copy(vignette = 0f); "色温" -> currentEdit.copy(temperature = 0f)
                                    "色调" -> currentEdit.copy(tint = 0f); "曲线" -> currentEdit.copy(curves = 0f)
                                    "HSL" -> currentEdit.copy(hsl = 0f); "美颜" -> currentEdit.copy(beauty = 0f)
                                    else -> currentEdit
                                }
                                updateEdit(re)
                            }) { Text("重置", color = KUROMI_PINK, fontSize = 12.sp) }
                        }
                    }
                }
            }
        }

        // 编辑工具网格
        Column(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(text = "编辑工具", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(editorTools.size, key = { editorTools[it].name }) { index ->
                    val tool = editorTools[index]
                    val isSelected = selectedTool?.name == tool.name
                    Column(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) KUROMI_PINK.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.07f))
                            .border(if (isSelected) 1.5.dp else 0.dp, if (isSelected) KUROMI_PINK else Color.Transparent, RoundedCornerShape(14.dp))
                            .clickable {
                                selectedTool = if (isSelected) null else tool
                                if (!tool.hasSlider) {
                                    Log.d("EditorScreen", "打开工具: ${tool.name}")
                                    selectedTool = null
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = tool.iconRes),
                            contentDescription = tool.name,
                            tint = if (isSelected) KUROMI_PINK else Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = tool.name,
                            color = if (isSelected) KUROMI_PINK else Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

private fun buildColorMatrix(edit: EditState): ColorMatrix {
    val matrix = ColorMatrix()
    if (edit.brightness != 0f) {
        val b = edit.brightness * 255f
        matrix.postConcat(ColorMatrix(floatArrayOf(1f,0f,0f,0f,b, 0f,1f,0f,0f,b, 0f,0f,1f,0f,b, 0f,0f,0f,1f,0f)))
    }
    if (edit.contrast != 0f) {
        val c = edit.contrast + 1f; val t = (1f - c) / 2f * 255f
        matrix.postConcat(ColorMatrix(floatArrayOf(c,0f,0f,0f,t, 0f,c,0f,0f,t, 0f,0f,c,0f,t, 0f,0f,0f,1f,0f)))
    }
    if (edit.saturation != 0f) { matrix.setToSaturation(1f + edit.saturation) }
    if (edit.temperature != 0f) {
        val r = 1f + edit.temperature * 0.3f; val b = 1f - edit.temperature * 0.3f
        matrix.postConcat(ColorMatrix(floatArrayOf(r,0f,0f,0f,0f, 0f,1f,0f,0f,0f, 0f,0f,b,0f,0f, 0f,0f,0f,1f,0f)))
    }
    return matrix
}

private suspend fun saveEditedImage(context: Context, sourceUri: Uri, edit: EditState): Boolean = withContext(Dispatchers.IO) {
    try {
        val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return@withContext false
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        val filename = "yanbao_edit_${System.currentTimeMillis()}.jpg"
        val cv = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/YanbaoAI")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv) ?: return@withContext false
        context.contentResolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            cv.clear(); cv.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(uri, cv, null, null)
        }
        Log.d("EditorScreen", "图片已保存: $uri")
        true
    } catch (e: Exception) {
        Log.e("EditorScreen", "保存失败: ${e.message}", e)
        false
    }
}
