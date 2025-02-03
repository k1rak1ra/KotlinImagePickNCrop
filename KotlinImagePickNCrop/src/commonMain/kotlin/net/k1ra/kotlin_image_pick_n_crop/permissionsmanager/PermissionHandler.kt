package net.k1ra.kotlin_image_pick_n_crop.permissionsmanager

import androidx.compose.runtime.Composable

interface PermissionHandler {
    @Composable
    fun requestPermission(permission: PermissionCategory)

    @Composable
    fun checkPermissionGranted(permission: PermissionCategory): Boolean

    @Composable
    fun openSettings()
}