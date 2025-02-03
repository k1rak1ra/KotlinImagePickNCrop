package net.k1ra.kotlin_image_pick_n_crop.platform.windows.win32

import com.sun.jna.Native
import com.sun.jna.Pointer

internal object Ole32 {
	init {
		Native.register("ole32")
	}

	external fun OleInitialize(pvReserved: Pointer?): Pointer?
	external fun CoTaskMemFree(pv: Pointer?)
}
