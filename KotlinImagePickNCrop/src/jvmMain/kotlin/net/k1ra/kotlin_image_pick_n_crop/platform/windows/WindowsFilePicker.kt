package net.k1ra.kotlin_image_pick_n_crop.platform.windows

import PlatformFilePicker
import java.awt.Window
import java.io.File
import net.k1ra.kotlin_image_pick_n_crop.platform.windows.api.JnaFileChooser

internal class WindowsFilePicker : PlatformFilePicker {
    override suspend fun pickFile(
        initialDirectory: String?,
        fileExtensions: List<String>?,
        title: String?,
        parentWindow: Window?,
    ): File? {
        val fileChooser = JnaFileChooser()

        // Setup file chooser
        fileChooser.apply {
            // Set mode
            mode = JnaFileChooser.Mode.Files

            // Only allow single selection
            isMultiSelectionEnabled = false

            // Set initial directory, title and file extensions
            setup(initialDirectory, fileExtensions, title)
        }

        // Show file chooser
        fileChooser.showOpenDialog(parentWindow)

        // Return selected file
        return fileChooser.selectedFile
    }

    private fun JnaFileChooser.setup(
        initialDirectory: String?,
        fileExtensions: List<String>?,
        title: String?
    ) {
        // Set title
        title?.let(::setTitle)

        // Set initial directory
        initialDirectory?.let(::setCurrentDirectory)

        // Set file extension
        if (!fileExtensions.isNullOrEmpty()) {
            val filterName = fileExtensions.joinToString(", ", "Supported Files (", ")")
            addFilter(filterName, *fileExtensions.toTypedArray())
        }
    }
}
