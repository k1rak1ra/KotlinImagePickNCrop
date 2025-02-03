@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package net.k1ra.kotlin_image_pick_n_crop.permissionsmanager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.AVFoundation.AVAuthorizationStatus
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Foundation.NSURL
import platform.Photos.PHAuthorizationStatus
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusDenied
import platform.Photos.PHAuthorizationStatusNotDetermined
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

@Composable
actual fun createPermissionManager(callback: PermissionCallback): PermissionManager {
    return PermissionManager(callback)
}

actual class PermissionManager actual constructor(private val callback: PermissionCallback) :
    PermissionHandler {
    @Composable
    actual override fun requestPermission(permission: PermissionCategory) {
        when (permission) {
            PermissionCategory.CAMERA -> {
                val status: AVAuthorizationStatus =
                    remember { AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) }
                askCameraPermission(status, permission, callback)
            }

            PermissionCategory.GALLERY -> {
                val status: PHAuthorizationStatus =
                    remember { PHPhotoLibrary.authorizationStatus() }
                askGalleryPermission(status, permission, callback)
            }

        }
    }

    private fun askCameraPermission(
        status: AVAuthorizationStatus, permission: PermissionCategory, callback: PermissionCallback
    ) {
        when (status) {
            AVAuthorizationStatusAuthorized -> {
                callback.onPermissionResult(permission, PermissionState.GRANTED)
            }

            AVAuthorizationStatusNotDetermined -> {
                return AVCaptureDevice.Companion.requestAccessForMediaType(AVMediaTypeVideo) { isGranted ->
                    if (isGranted) {
                        callback.onPermissionResult(permission, PermissionState.GRANTED)
                    } else {
                        callback.onPermissionResult(permission, PermissionState.DENIED)
                    }
                }
            }

            AVAuthorizationStatusDenied -> {
                callback.onPermissionResult(permission, PermissionState.DENIED)
            }

            else -> error("unknown camera status $status")
        }
    }

    private fun askGalleryPermission(
        status: PHAuthorizationStatus, permission: PermissionCategory, callback: PermissionCallback
    ) {
        when (status) {
            PHAuthorizationStatusAuthorized -> {
                callback.onPermissionResult(permission, PermissionState.GRANTED)
            }

            PHAuthorizationStatusNotDetermined -> {
                PHPhotoLibrary.Companion.requestAuthorization { newStatus ->
                    askGalleryPermission(newStatus, permission, callback)
                }
            }

            PHAuthorizationStatusDenied -> {
                callback.onPermissionResult(
                    permission, PermissionState.DENIED
                )
            }

            else -> error("unknown gallery status $status")
        }
    }

    @Composable
    actual override fun checkPermissionGranted(permission: PermissionCategory): Boolean {
        return when (permission) {
            PermissionCategory.CAMERA -> {
                val status: AVAuthorizationStatus =
                    remember { AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) }
                status == AVAuthorizationStatusAuthorized
            }

            PermissionCategory.GALLERY -> {
                val status: PHAuthorizationStatus =
                    remember { PHPhotoLibrary.authorizationStatus() }
                status == PHAuthorizationStatusAuthorized
            }
        }
    }

    @Composable
    actual override fun openSettings() {
        NSURL.URLWithString(UIApplicationOpenSettingsURLString)?.let {
            UIApplication.sharedApplication.openURL(it)
        }
    }

}