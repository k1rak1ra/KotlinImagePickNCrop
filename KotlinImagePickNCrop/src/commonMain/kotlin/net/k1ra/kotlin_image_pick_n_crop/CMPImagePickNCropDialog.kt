package net.k1ra.kotlin_image_pick_n_crop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.k1ra.kotlin_image_pick_n_crop.cameramanager.rememberImageCaptureManager
import net.k1ra.kotlin_image_pick_n_crop.gallerymanager.rememberImageGalleryManager
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.DefaultCropShapes
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.DefaultImageCropperAspectRatios
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.ImageAspectRatio
import net.k1ra.kotlin_image_pick_n_crop.permissionsmanager.PermissionCallback
import net.k1ra.kotlin_image_pick_n_crop.permissionsmanager.PermissionState
import net.k1ra.kotlin_image_pick_n_crop.permissionsmanager.PermissionCategory
import net.k1ra.kotlin_image_pick_n_crop.permissionsmanager.createPermissionManager
import net.k1ra.kotlin_image_pick_n_crop.ui.ImageCropperDialogContainer
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.ImageCropError
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.ImageCropResult
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.ImageCropShape
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.ImageCropper
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.cropImage
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.rememberImageCropper
import net.k1ra.kotlin_image_pick_n_crop.utils.AlertMessageDialog
import net.k1ra.kotlin_image_pick_n_crop.utils.ChooseImageOptionBottomSheet

@Composable
fun CMPImagePickNCropDialog(
    imageCropper: ImageCropper = rememberImageCropper(),
    openImagePicker: Boolean,
    cropEnable: Boolean = true,
    showCameraOption: Boolean = true,
    showGalleryOption: Boolean = true,
    autoZoom: Boolean = true,
    enableRotationOption: Boolean = true,
    enabledFlipOption: Boolean = true,
    shapes: List<ImageCropShape>? = DefaultCropShapes,
    aspects: List<ImageAspectRatio>? = DefaultImageCropperAspectRatios,
    imagePickerDialogHandler: (Boolean) -> Unit,
    selectedImageCallback: (ImageBitmap) -> Unit
) {
    var selectedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var launchCamera by remember { mutableStateOf(value = false) }
    val coroutineScope = rememberCoroutineScope()
    var permissionRationalDialog by remember { mutableStateOf(value = false) }
    var launchSetting by remember { mutableStateOf(value = false) }
    var launchGallery by remember { mutableStateOf(value = false) }

    imageCropper.imageCropState?.let {
        ImageCropperDialogContainer(
            state = it,
            autoZoom = autoZoom,
            shapes = shapes,
            aspects = aspects,
            enableRotationOption = enableRotationOption,
            enabledFlipOption = enabledFlipOption
        )
    }

    val permissionsManager = createPermissionManager(object : PermissionCallback {
        override fun onPermissionResult(
            permissionCategory: PermissionCategory,
            status: PermissionState
        ) {
            when (status) {
                PermissionState.GRANTED -> {
                    when (permissionCategory) {
                        PermissionCategory.CAMERA -> launchCamera = true
                        PermissionCategory.GALLERY -> launchGallery = true
                    }
                }

                else -> {
                    permissionRationalDialog = true
                }
            }
        }
    })

    val galleryManager = rememberImageGalleryManager {
        coroutineScope.launch {
            if (cropEnable) {
                selectedImage = it?.toImageBitmap()
            } else {
                it?.toImageBitmap()?.let { it1 -> selectedImageCallback(it1) }
            }
        }
    }

    LaunchedEffect(selectedImage) {
        if (cropEnable) {
            selectedImage?.let {
                when (val result = imageCropper.cropImage(bmp = it)) {
                    ImageCropResult.Cancelled -> {
                        selectedImage = null
                    }

                    is ImageCropError -> {
                        selectedImage = null
                    }

                    is ImageCropResult.Success -> {
                        selectedImage = null
                        selectedImageCallback(result.bitmap)
                    }
                }
            }
        }
    }

    val cameraManager = rememberImageCaptureManager {
        if (cropEnable) {
            coroutineScope.launch {
                val bitmap = withContext(Dispatchers.Default) {
                    it?.toImageBitmap()
                }
                selectedImage = bitmap
            }
        } else {
            it?.toImageBitmap()?.let { it1 -> selectedImageCallback(it1) }
        }
    }

    if (openImagePicker) {
        ChooseImageOptionBottomSheet(
            showCameraOption = showCameraOption,
            showGalleryOption = showGalleryOption,
            onDismissRequest = {
                imagePickerDialogHandler(false)
            },
            onGalleryRequest = {
                imagePickerDialogHandler(false)
                launchGallery = true
            },
            onCameraRequest = {
                imagePickerDialogHandler(false)
                launchCamera = true
            }
        )
    }

    if (launchGallery) {
        if (permissionsManager.checkPermissionGranted(PermissionCategory.GALLERY)) {
            galleryManager.launch()
        } else {
            permissionsManager.requestPermission(PermissionCategory.GALLERY)
        }
        launchGallery = false
    }

    if (launchCamera) {
        if (permissionsManager.checkPermissionGranted(PermissionCategory.CAMERA)) {
            cameraManager.launch()
        } else {
            permissionsManager.requestPermission(PermissionCategory.CAMERA)
        }
        launchCamera = false
    }

    if (launchSetting) {
        permissionsManager.openSettings()
        launchSetting = false
    }

    if (permissionRationalDialog) {
        AlertMessageDialog(title = "Permission Required",
            message = "Please grant permission to access your camera and gallery. You can manage permissions in your device settings.",
            positiveButtonText = "Settings",
            negativeButtonText = "Cancel",
            onPositiveClick = {
                permissionRationalDialog = false
                launchSetting = true

            },
            onNegativeClick = {
                permissionRationalDialog = false
            })
    }
}

@Composable
fun CMPImageCropDialog(
    imageCropper: ImageCropper = rememberImageCropper()
) {
    imageCropper.imageCropState?.let { ImageCropperDialogContainer(state = it) }
}