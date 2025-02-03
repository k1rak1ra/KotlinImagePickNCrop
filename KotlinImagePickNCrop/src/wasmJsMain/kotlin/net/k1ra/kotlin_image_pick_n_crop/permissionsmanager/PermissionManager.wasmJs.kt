package net.k1ra.kotlin_image_pick_n_crop.permissionsmanager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import net.k1ra.kotlin_image_pick_n_crop.permissionsmanager.PermissionCallback
import net.k1ra.kotlin_image_pick_n_crop.permissionsmanager.PermissionCategory
import net.k1ra.kotlin_image_pick_n_crop.permissionsmanager.PermissionHandler
import net.k1ra.kotlin_image_pick_n_crop.permissionsmanager.PermissionManager

actual class PermissionManager actual constructor(callback: PermissionCallback) :
    PermissionHandler {
    @Composable
    actual override fun requestPermission(permission: PermissionCategory) {
    }

    @Composable
    actual override fun checkPermissionGranted(permission: PermissionCategory): Boolean {
        return true
    }

    @Composable
    actual override fun openSettings() {
    }

}

@Composable
actual fun createPermissionManager(callback: PermissionCallback): PermissionManager {
    return remember { PermissionManager(callback) }
}