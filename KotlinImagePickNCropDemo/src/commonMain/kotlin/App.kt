import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import net.k1ra.kotlin_image_pick_n_crop.CMPImagePickNCropDialog
import net.k1ra.kotlin_image_pick_n_crop.imagecropper.rememberImageCropper
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        val imageCropper = rememberImageCropper()
        var selectedImage by remember { mutableStateOf<ImageBitmap?>(null) }
        var openImagePicker by remember { mutableStateOf(value = false) }

        CMPImagePickNCropDialog(
            imageCropper = imageCropper,
            openImagePicker = openImagePicker,
            showCameraOption = false,
            autoZoom = true,
            imagePickerDialogHandler = {
                openImagePicker = it
            },
            selectedImageCallback = {
                selectedImage = it
            })

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .safeContentPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            selectedImage?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    modifier = Modifier.weight(1f)
                )
            }
            if (selectedImage == null)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("No image selected !", color = Color.Black)
                }

            Button(
                onClick = {
                    openImagePicker = true
                },
            ) { Text("Choose Image") }
        }
    }
}