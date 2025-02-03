package net.k1ra.kotlin_image_pick_n_crop.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import net.k1ra.kotlin_image_pick_n_crop.ImageCropperDialogProperties
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.DefaultCropShapes
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.DefaultImageCropperAspectRatios
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.DraggableHandle
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.ImageAspectRatio
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.ImageCropShape
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.ImageCropState
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.ImageCropperStyle
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.LocalImageCropperStyle
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.ViewMat
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.animateImageTransformation
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.cropperInteractionModifier
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.eq0
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.flipHorizontal
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.flipVertical
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.rememberLoadedMedia
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.rotateLeft
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.rotateRight
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.setAspect
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.times
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.toMatrix
import net.k1ra.kotlin_image_pick_n_crop.kotlinimagepickncrop.generated.resources.Res
import net.k1ra.kotlin_image_pick_n_crop.kotlinimagepickncrop.generated.resources.flip_horizontal
import net.k1ra.kotlin_image_pick_n_crop.kotlinimagepickncrop.generated.resources.flip_vertical
import net.k1ra.kotlin_image_pick_n_crop.kotlinimagepickncrop.generated.resources.ic_resize
import net.k1ra.kotlin_image_pick_n_crop.kotlinimagepickncrop.generated.resources.rotate_left
import net.k1ra.kotlin_image_pick_n_crop.kotlinimagepickncrop.generated.resources.rotate_right
import org.jetbrains.compose.resources.painterResource

@Composable
fun ImageCropperDialogContainer(
    state: ImageCropState,
    autoZoom: Boolean = true,
    enableRotationOption: Boolean = true,
    enabledFlipOption: Boolean = true,
    shapes: List<ImageCropShape>? = DefaultCropShapes,
    aspects: List<ImageAspectRatio>? = DefaultImageCropperAspectRatios
) {
    val imageCropStyle = ImageCropperStyle(
        shapes = shapes,
        aspects = aspects,
        autoZoom = autoZoom,
        enableRotationOption = enableRotationOption,
        enabledFlipOption = enabledFlipOption
    )
    CompositionLocalProvider(LocalImageCropperStyle provides imageCropStyle) {
        Dialog(
            onDismissRequest = { state.done(accept = false) },
            properties = ImageCropperDialogProperties,
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    DefaultCropperTopBar(state)
                    DefaultCropperControls(state)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clipToBounds()
                    ) {
                        ImageCropperPreview(state = state, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@Composable
private fun DefaultCropperControls(
    state: ImageCropState
) {
    var shapeSelectionMenu by remember { mutableStateOf(false) }
    var aspectSelectionMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        ButtonBarLayout(modifier = Modifier.padding(top = (0.5).dp).fillMaxWidth()) {
            if (LocalImageCropperStyle.current.enableRotationOption) {
                IconButton(onClick = {
                    shapeSelectionMenu = false
                    aspectSelectionMenu = false
                    state.rotateLeft()
                }) {
                    Icon(painterResource(Res.drawable.rotate_left), null, tint = Color.Black)
                }
                IconButton(onClick = {
                    shapeSelectionMenu = false
                    aspectSelectionMenu = false
                    state.rotateRight()
                }) {
                    Icon(painterResource(Res.drawable.rotate_right), null, tint = Color.Black)
                }
            }

            if (LocalImageCropperStyle.current.enabledFlipOption) {
                IconButton(onClick = {
                    shapeSelectionMenu = false
                    aspectSelectionMenu = false
                    state.flipHorizontal()
                }) {
                    Icon(painterResource(Res.drawable.flip_horizontal), null, tint = Color.Black)
                }
                IconButton(onClick = {
                    shapeSelectionMenu = false
                    aspectSelectionMenu = false
                    state.flipVertical()
                }) {
                    Icon(painterResource(Res.drawable.flip_vertical), null, tint = Color.Black)
                }
            }

            LocalImageCropperStyle.current.aspects?.let { aspects ->
                if (aspects.isNotEmpty()) {
                    Box {
                        IconButton(onClick = {
                            shapeSelectionMenu = false
                            aspectSelectionMenu = !aspectSelectionMenu
                        }) {
                            Icon(painterResource(Res.drawable.ic_resize), null, tint = Color.Black)
                        }
                    }
                }
            }

            LocalImageCropperStyle.current.shapes?.let { shapes ->
                if (shapes.isNotEmpty()) {
                    Box {
                        IconButton(onClick = {
                            aspectSelectionMenu = false
                            shapeSelectionMenu = !shapeSelectionMenu
                        }) {
                            Icon(Icons.Default.Star, null, tint = Color.Black)
                        }
                    }
                }
            }
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            LocalImageCropperStyle.current.shapes?.let { shapes ->
                if (shapeSelectionMenu)
                    ShapePickerMenu(
                        shapes = shapes,
                        selected = state.shape,
                        onSelect = { state.shape = it }
                    )
            }
            if (aspectSelectionMenu) {
                AspectRatioMenu(
                    region = state.region,
                    onRegion = { state.region = it },
                    lock = state.aspectLock,
                    onLock = { state.aspectLock = it }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultCropperTopBar(state: ImageCropState) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = { state.done(accept = false) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
        },
        actions = {
            IconButton(onClick = { state.reset() }) {
                Icon(Icons.Default.Refresh, null)
            }
            IconButton(onClick = { state.done(accept = true) }, enabled = !state.accepted) {
                Icon(Icons.Default.Done, null)
            }
        },
        colors = TopAppBarColors(
            containerColor = Color.White,
            scrolledContainerColor = Color.White,
            navigationIconContentColor = Color.Black,
            titleContentColor = Color.Black,
            actionIconContentColor = Color.Black
        )
    )
}

private fun Size.hasAspectRatio(aspect: ImageAspectRatio): Boolean {
    return ((width / height) - (aspect.x.toFloat() / aspect.y)).eq0()
}

@Composable
private fun ButtonBarLayout(
    modifier: Modifier = Modifier, buttons: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = Color.White,
        contentColor = contentColorFor(Color.Black)
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            buttons()
        }
    }
}


@Composable
private fun ShapePickerMenu(
    shapes: List<ImageCropShape>,
    selected: ImageCropShape,
    onSelect: (ImageCropShape) -> Unit,
) {
    PopupMenu(optionCount = shapes.size) { i ->
        val shape = shapes[i]
        MenuItem(shape = shape, selected = selected == shape, onSelect = { onSelect(shape) })
    }
}


@Composable
private fun MenuItem(
    shape: ImageCropShape, selected: Boolean, onSelect: () -> Unit, modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        targetValue = if (!selected) Color.Black
        else Color.Blue
    )
    IconButton(
        modifier = modifier, onClick = onSelect
    ) {
        val shapeState by rememberUpdatedState(newValue = shape)
        Box(modifier = Modifier.size(20.dp).drawWithCache {
            val path = shapeState.asPath(size.toRect())
            val strokeWidth = 2.dp.toPx()
            onDrawWithContent {
                drawPath(path = path, color = color, style = Stroke(strokeWidth))
            }
        })
    }
}


@Composable
private fun AspectRatioMenu(
    region: Rect,
    onRegion: (Rect) -> Unit,
    lock: Boolean,
    onLock: (Boolean) -> Unit
) {
    val aspectList = LocalImageCropperStyle.current.aspects
    aspectList?.let { aspects ->
        PopupMenu(optionCount = 1 + aspects.size) { i ->
            val unselectedTint = Color.Black
            val selectedTint = Color.Blue
            if (i == 0) {
                IconButton(onClick = { onLock(!lock) }) {
                    Icon(
                        Icons.Default.Lock, null, tint = if (lock) selectedTint else unselectedTint
                    )
                }
            } else {
                val aspect = aspects[i - 1]
                val isSelected = region.size.hasAspectRatio(aspect)
                IconButton(onClick = { onRegion(region.setAspect(aspect)) }) {
                    Text(
                        "${aspect.x}:${aspect.y}",
                        color = if (isSelected) selectedTint else unselectedTint
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageCropperPreview(
    state: ImageCropState,
    modifier: Modifier = Modifier
) {
    val style = LocalImageCropperStyle.current
    val imgTransform by animateImageTransformation(target = state.transform)
    val imgMat = remember(imgTransform, state.src.size) { imgTransform.toMatrix(state.src.size) }
    val viewMat = remember { ViewMat() }
    var view by remember { mutableStateOf(IntSize.Zero) }
    var pendingDrag by remember { mutableStateOf<DraggableHandle?>(null) }
    val viewPadding = LocalDensity.current.run { style.touchRad.toPx() }
    val totalMat = remember(viewMat.matrix, imgMat) { imgMat * viewMat.matrix }
    val image = rememberLoadedMedia(state.src, view, totalMat)
    val cropRect = remember(state.region, viewMat.matrix) {
        viewMat.matrix.map(state.region)
    }
    val cropPath = remember(state.shape, cropRect) { state.shape.asPath(cropRect) }
    AdjustToView(
        enabled = style.autoZoom,
        hasOverride = pendingDrag != null,
        outer = view.toSize().toRect().deflate(viewPadding),
        mat = viewMat, local = state.region,
    )
    Canvas(
        modifier = modifier
            .onGloballyPositioned { view = it.size }
            .background(color = style.backgroundColor)
            .cropperInteractionModifier(
                region = state.region,
                onRegion = { state.region = it },
                touchRad = style.touchRad, handles = style.handles,
                viewMat = viewMat,
                pending = pendingDrag,
                onPending = { pendingDrag = it },
            )
    ) {
        withTransform({ transform(totalMat) }) {
            image?.let { (params, bitmap) ->
                drawImage(
                    bitmap, dstOffset = params.subset.topLeft,
                    dstSize = params.subset.size
                )
            }
        }
        with(style) {
            clipPath(cropPath, ClipOp.Difference) {
                drawRect(color = overlayColor)
            }
            drawCropRect(cropRect)
        }
    }
}

@Composable
private fun AdjustToView(
    enabled: Boolean,
    hasOverride: Boolean,
    outer: Rect,
    mat: ViewMat,
    local: Rect
) {
    if (outer.isEmpty) return
    DisposableEffect(Unit) {
        mat.snapFit(mat.matrix.map(local), outer)
        onDispose { }
    }
    if (!enabled) return
    var overrideBlock by remember { mutableStateOf(false) }
    LaunchedEffect(hasOverride, outer, local) {
        if (hasOverride) overrideBlock = true
        else {
            if (overrideBlock) {
                delay(500)
                overrideBlock = false
            }
            mat.fit(mat.matrix.map(local), outer)
        }
    }
}

@Composable
private fun PopupMenu(
    optionCount: Int,
    option: @Composable (Int) -> Unit,
) {
    Box(
        modifier = Modifier.padding(top = (0.5).dp).fillMaxWidth().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Surface(modifier = Modifier) {
            Row(modifier = Modifier.background(color = Color.White)) {
                repeat(optionCount) { i ->
                    option(i)
                }
            }
        }
    }
}