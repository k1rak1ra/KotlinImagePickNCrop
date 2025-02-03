package net.k1ra.kotlin_image_pick_n_crop.permissionsmanager

import androidx.compose.runtime.Composable

expect class PermissionManager(callback: PermissionCallback) : PermissionHandler {
    @Composable
    override fun requestPermission(permission: PermissionCategory)

    @Composable
    override fun checkPermissionGranted(permission: PermissionCategory): Boolean

    @Composable
    override fun openSettings()
}

interface PermissionCallback {
    fun onPermissionResult(permissionCategory: PermissionCategory, status: PermissionState)
}

@Composable
expect fun createPermissionManager(callback: PermissionCallback): PermissionManager

enum class PermissionState {
    GRANTED,
    DENIED,
    SHOW_RATIONAL
}

enum class PermissionCategory {
    GALLERY,
    CAMERA
}