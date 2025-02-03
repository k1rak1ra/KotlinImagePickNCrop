import java.awt.Window
import java.io.File
import net.k1ra.kotlin_image_pick_n_crop.platform.awt.AwtFilePicker
import net.k1ra.kotlin_image_pick_n_crop.platform.mac.MacOSFilePicker
import net.k1ra.kotlin_image_pick_n_crop.platform.windows.WindowsFilePicker

internal interface PlatformFilePicker {
    suspend fun pickFile(
        initialDirectory: String?,
        fileExtensions: List<String>?,
        title: String?,
        parentWindow: Window?,
    ): File?

    companion object {
        val current: PlatformFilePicker by lazy { createPlatformFilePicker() }

        private fun createPlatformFilePicker(): PlatformFilePicker {
            return when (PlatformUtil.current) {
                Platform.MacOS -> MacOSFilePicker()
				Platform.Windows -> WindowsFilePicker()
				Platform.Linux -> AwtFilePicker()
            }
        }
    }
}
