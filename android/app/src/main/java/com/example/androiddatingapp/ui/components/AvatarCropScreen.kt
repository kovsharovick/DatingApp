package com.example.androiddatingapp.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.example.androiddatingapp.ui.theme.AppButtonDefaults
import com.example.androiddatingapp.ui.util.AvatarCropUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AvatarCropScreen(
    imageUri: Uri,
    onDismiss: () -> Unit,
    onConfirm: (Uri) -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var bitmap by remember(imageUri) { mutableStateOf<Bitmap?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var cropSidePx by remember { mutableFloatStateOf(0f) }
    var containerWidthPx by remember { mutableFloatStateOf(0f) }
    var containerHeightPx by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(imageUri) {
        loadError = null
        bitmap = withContext(Dispatchers.IO) {
            AvatarCropUtils.loadOrientedBitmap(context, imageUri)
        }
        if (bitmap == null) {
            loadError = "Не удалось открыть изображение"
        }
    }

    LaunchedEffect(bitmap, cropSidePx) {
        val bmp = bitmap ?: return@LaunchedEffect
        if (cropSidePx > 0f) {
            scale = AvatarCropUtils.initialFitScale(bmp, cropSidePx)
            offsetX = 0f
            offsetY = 0f
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(Modifier.fillMaxSize()) {
            Text(
                text = "Выберите область для аватара",
                fontSize = scaleSp(18f),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = scaleDp(16f), vertical = scaleDp(14f)),
            )
            Text(
                text = "Перемещайте и приближайте фото. Уменьшить меньше исходного размера нельзя.",
                fontSize = scaleSp(13f),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                modifier = Modifier.padding(horizontal = scaleDp(16f)),
            )
            Spacer(Modifier.height(scaleDp(10f)))

            when {
                loadError != null -> {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = loadError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = scaleSp(14f),
                        )
                    }
                }
                bitmap == null -> {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Загрузка…",
                            fontSize = scaleSp(14f),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        )
                    }
                }
                else -> {
                    val bmp = bitmap!!
                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = scaleDp(16f)),
                    ) {
                        val density = LocalDensity.current
                        val side = with(density) {
                            minOf(maxWidth, maxHeight * 0.92f).toPx()
                        }
                        cropSidePx = side
                        containerWidthPx = with(density) { maxWidth.toPx() }
                        containerHeightPx = with(density) { maxHeight.toPx() }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.08f))
                                .pointerInput(bmp, cropSidePx) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        val minScale = AvatarCropUtils.initialFitScale(bmp, cropSidePx)
                                        scale = (scale * zoom).coerceIn(minScale, minScale * 4f)
                                        offsetX += pan.x
                                        offsetY += pan.y
                                        val clamped = AvatarCropUtils.clampPan(
                                            bitmap = bmp,
                                            scale = scale,
                                            cropSide = cropSidePx,
                                            containerWidth = containerWidthPx,
                                            containerHeight = containerHeightPx,
                                            offsetX = offsetX,
                                            offsetY = offsetY,
                                        )
                                        offsetX = clamped.first
                                        offsetY = clamped.second
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(
                                        width = with(density) { bmp.width.toDp() },
                                        height = with(density) { bmp.height.toDp() },
                                    )
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        translationX = offsetX
                                        translationY = offsetY
                                    },
                            )

                            Canvas(Modifier.fillMaxSize()) {
                                val cropLeft = (size.width - cropSidePx) / 2f
                                val cropTop = (size.height - cropSidePx) / 2f
                                drawRect(Color.Black.copy(alpha = 0.55f), topLeft = Offset.Zero, size = Size(size.width, cropTop))
                                drawRect(
                                    Color.Black.copy(alpha = 0.55f),
                                    topLeft = Offset(0f, cropTop + cropSidePx),
                                    size = Size(size.width, size.height - cropTop - cropSidePx),
                                )
                                drawRect(
                                    Color.Black.copy(alpha = 0.55f),
                                    topLeft = Offset(0f, cropTop),
                                    size = Size(cropLeft, cropSidePx),
                                )
                                drawRect(
                                    Color.Black.copy(alpha = 0.55f),
                                    topLeft = Offset(cropLeft + cropSidePx, cropTop),
                                    size = Size(size.width - cropLeft - cropSidePx, cropSidePx),
                                )
                                drawRect(
                                    color = Color.White,
                                    topLeft = Offset(cropLeft, cropTop),
                                    size = Size(cropSidePx, cropSidePx),
                                    style = Stroke(width = 3f),
                                )
                            }
                        }
                    }
                }
            }

            RowActions(
                enabled = bitmap != null && loadError == null,
                onDismiss = onDismiss,
                onApply = {
                    val bmp = bitmap ?: return@RowActions
                    val cropped = AvatarCropUtils.cropSquare(
                        bitmap = bmp,
                        containerWidth = containerWidthPx,
                        containerHeight = containerHeightPx,
                        cropSide = cropSidePx,
                        scale = scale,
                        offsetX = offsetX,
                        offsetY = offsetY,
                    )
                    val uri = AvatarCropUtils.saveToCache(context, cropped)
                    if (cropped != bmp) cropped.recycle()
                    onConfirm(uri)
                },
                scaleDp = scaleDp,
                scaleSp = scaleSp,
            )
        }
    }
}

@Composable
private fun RowActions(
    enabled: Boolean,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(scaleDp(16f)),
    ) {
        Button(
            onClick = onApply,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            colors = AppButtonDefaults.blue(),
        ) {
            Text("Применить", fontSize = scaleSp(14f))
        }
        Spacer(Modifier.height(scaleDp(10f)))
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = AppButtonDefaults.outlinedBlue(),
        ) {
            Text("Отмена", fontSize = scaleSp(14f))
        }
    }
}
