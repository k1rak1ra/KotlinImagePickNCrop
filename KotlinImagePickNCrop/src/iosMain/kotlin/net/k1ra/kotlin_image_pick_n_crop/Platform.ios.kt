package net.k1ra.kotlin_image_pick_n_crop

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties

actual val platform: String
    get() = "IOS"

@OptIn(ExperimentalComposeUiApi::class)
@get:Composable
actual val ImageCropperDialogProperties: DialogProperties
    get() = DialogProperties(
        usePlatformDefaultWidth = false,
        dismissOnBackPress = false,
        dismissOnClickOutside = false,
        usePlatformInsets = false,
        scrimColor = Color.White
    )