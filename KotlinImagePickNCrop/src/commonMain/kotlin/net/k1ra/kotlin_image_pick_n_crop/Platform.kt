package net.k1ra.kotlin_image_pick_n_crop

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

expect val platform: String

@get:Composable
expect val ImageCropperDialogProperties: DialogProperties