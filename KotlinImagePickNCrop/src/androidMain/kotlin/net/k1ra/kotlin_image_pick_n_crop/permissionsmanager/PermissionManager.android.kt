@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package net.k1ra.kotlin_image_pick_n_crop.permissionsmanager

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch

@Composable
actual fun createPermissionManager(callback: PermissionCallback): PermissionManager {
    return remember { PermissionManager(callback) }
}

actual class PermissionManager actual constructor(private val callback: PermissionCallback) :
    PermissionHandler {

    @Composable
    actual override fun requestPermission(permission: PermissionCategory) {
        TakePermission(permission)
    }

    @Composable
    @OptIn(ExperimentalPermissionsApi::class)
    private fun TakePermission(permission: PermissionCategory) {
        val lifecycleOwner = LocalLifecycleOwner.current

        when (permission) {
            PermissionCategory.CAMERA -> {
                val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
                LaunchedEffect(cameraPermissionState.status) {
                    val permissionResult = cameraPermissionState.status
                    if (!permissionResult.isGranted) {
                        if (permissionResult.shouldShowRationale) {
                            callback.onPermissionResult(
                                permission, PermissionState.SHOW_RATIONAL
                            )
                        } else {
                            lifecycleOwner.lifecycleScope.launch {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        }
                    } else {
                        callback.onPermissionResult(
                            permission, PermissionState.GRANTED
                        )
                    }
                }
            }

            PermissionCategory.GALLERY -> {
                callback.onPermissionResult(
                    permission, PermissionState.GRANTED
                )
            }
        }
    }

    @Composable
    actual override fun checkPermissionGranted(permission: PermissionCategory): Boolean {
        return permissionGranted(permission)
    }

    @Composable
    @OptIn(ExperimentalPermissionsApi::class)
    private fun permissionGranted(permission: PermissionCategory): Boolean {
        return when (permission) {
            PermissionCategory.CAMERA -> {
                val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
                cameraPermissionState.status.isGranted
            }

            PermissionCategory.GALLERY -> {
                true
            }
        }
    }

    @Composable
    actual override fun openSettings() {
        val context = LocalContext.current
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        ).also {
            context.startActivity(it)
        }
    }
}