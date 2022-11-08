/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.image

import android.os.Build
import ca.gosyer.jui.uicore.vm.ContextWrapper
import com.seiko.imageloader.ImageLoaderBuilder
import com.seiko.imageloader.cache.disk.DiskCache
import com.seiko.imageloader.cache.disk.DiskCacheBuilder
import com.seiko.imageloader.cache.memory.MemoryCache
import com.seiko.imageloader.cache.memory.MemoryCacheBuilder
import com.seiko.imageloader.request.Options
import okio.Path.Companion.toOkioPath

actual val imageConfig: Options.ImageConfig = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
    Options.ImageConfig.ARGB_8888
} else {
    Options.ImageConfig.HARDWARE
}

actual fun imageLoaderBuilder(contextWrapper: ContextWrapper): ImageLoaderBuilder {
    return ImageLoaderBuilder(contextWrapper)
}

actual fun diskCache(contextWrapper: ContextWrapper, cacheDir: String): DiskCache {
    return DiskCacheBuilder()
        .directory(contextWrapper.cacheDir.toOkioPath() / cacheDir)
        .maxSizeBytes(1024 * 1024 * 150) // 150 MB
        .build()
}

actual fun memoryCache(contextWrapper: ContextWrapper): MemoryCache {
    return MemoryCacheBuilder(contextWrapper)
        .build()
}
