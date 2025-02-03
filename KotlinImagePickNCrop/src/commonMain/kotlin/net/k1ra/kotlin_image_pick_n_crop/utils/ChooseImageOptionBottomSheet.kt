package net.k1ra.kotlin_image_pick_n_crop.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.k1ra.kotlin_image_pick_n_crop.kotlinimagepickncrop.generated.resources.Res
import net.k1ra.kotlin_image_pick_n_crop.kotlinimagepickncrop.generated.resources.ic_camera
import net.k1ra.kotlin_image_pick_n_crop.kotlinimagepickncrop.generated.resources.ic_images
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseImageOptionBottomSheet(
    showCameraOption: Boolean = true,
    showGalleryOption: Boolean = true,
    onDismissRequest: () -> Unit,
    onGalleryRequest: () -> Unit = {},
    onCameraRequest: () -> Unit = {}
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { true }
    )

    CustomBottomSheet(
        onDismissRequest = {
            onDismissRequest()
        },
        sheetState = bottomSheetState,
        shape = RoundedCornerShape(14.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .height(4.dp)
                    .width(46.dp)
                    .background(color = Color.White, shape = RoundedCornerShape(3.dp))
            )
        },
        backgroundColor = Color.Gray,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Choose Option",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (showCameraOption)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 15.dp)
                            .clickable {
                                onCameraRequest.invoke()
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            tint = Color.Black,
                            modifier = Modifier.size(25.dp),
                            painter = painterResource(Res.drawable.ic_camera),
                            contentDescription = null
                        )
                        Text(text = "Camera", color = Color.Black)
                    }
                if (showGalleryOption)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 15.dp)
                            .clickable {
                                onGalleryRequest.invoke()
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            tint = Color.Black,
                            modifier = Modifier.size(25.dp),
                            painter = painterResource(Res.drawable.ic_images),
                            contentDescription = null
                        )
                        Text(text = "Photo Library", color = Color.Black)
                    }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
    shape: Shape = RoundedCornerShape(20.dp),
    tonalElevation: Dp = BottomSheetDefaults.Elevation,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    dragHandle: @Composable() (() -> Unit)? = { },
    properties: ModalBottomSheetProperties = ModalBottomSheetDefaults.properties,
    backgroundColor: Color = Color.White,
    paddingValue: PaddingValues = PaddingValues(start = 10.dp, end = 10.dp, bottom = 20.dp),
    content: @Composable () -> Unit,
) {
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = { onDismissRequest() },
        containerColor = Color.Transparent,
        sheetState = sheetState,
        scrimColor = scrimColor,
        dragHandle = dragHandle,
        sheetMaxWidth = sheetMaxWidth,
        tonalElevation = tonalElevation,
        properties = properties,
    ) {
        Box(
            modifier = Modifier
                .padding(paddingValue)
                .background(
                    color = backgroundColor,
                    shape = shape
                )
                .clip(shape)
        ) {
            content()
        }
    }
}