package com.example.william.my.core.imageloader.coil

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.gif.GifDecoder
import coil3.load
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.transformations
import coil3.transform.CircleCropTransformation
import coil3.transform.RoundedCornersTransformation
import com.example.william.my.core.imageloader.IImageLoader
import com.example.william.my.core.imageloader.ImageOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.Path.Companion.toOkioPath
import java.io.File

/**
 * 基于 Coil 的图片加载器实现
 */
object ImageLoader : IImageLoader {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var imageLoader: ImageLoader? = null

    private fun getImageLoader(context: Context): ImageLoader = imageLoader ?: ImageLoader.Builder(context.applicationContext)
        .components {
            add(GifDecoder.Factory())
        }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("image_cache").toOkioPath())
                .build()
        }
        .build()
        .also { imageLoader = it }

    override fun pauseRequests(context: Context?) {
        scope.coroutineContext.cancelChildren()
    }

    override fun resumeRequests(context: Context?) {
    }

    override fun ImageView.clear(context: Context?) {
        setImageDrawable(null)
    }

    override fun ImageView.loadImage(context: Context?, resourceId: Int) {
        load(resourceId)
    }

    override fun ImageView.loadImage(context: Context?, bitmap: Bitmap?) {
        load(bitmap)
    }

    override fun ImageView.loadImage(context: Context?, uri: Uri) {
        load(uri)
    }

    override fun ImageView.loadImage(context: Context?, file: File) {
        load(file)
    }

    override fun ImageView.loadImage(
        context: Context?,
        url: String?,
        options: ImageOptions?,
        onComplete: (() -> Unit)?,
    ) {
        load(url) {
            if (options != null) {
                diskCachePolicy(
                    when (options.cacheStrategy) {
                        ImageOptions.CacheStrategy.ALL -> CachePolicy.ENABLED
                        ImageOptions.CacheStrategy.NONE -> CachePolicy.DISABLED
                        ImageOptions.CacheStrategy.DATA -> CachePolicy.ENABLED
                        ImageOptions.CacheStrategy.RESOURCE -> CachePolicy.ENABLED
                    },
                )
                memoryCachePolicy(
                    if (options.skipMemoryCache) {
                        CachePolicy.DISABLED
                    } else {
                        CachePolicy.ENABLED
                    },
                )
            }
            listener(
                onSuccess = { _, _ -> onComplete?.invoke() },
                onError = { _, _ -> onComplete?.invoke() },
            )
        }
    }

    override fun ImageView.loadImageRound(context: Context?, resourceId: Int) {
        load(resourceId) { transformations(CircleCropTransformation()) }
    }

    override fun ImageView.loadImageRound(context: Context?, bitmap: Bitmap?) {
        load(bitmap) { transformations(CircleCropTransformation()) }
    }

    override fun ImageView.loadImageRound(context: Context?, file: File) {
        load(file) { transformations(CircleCropTransformation()) }
    }

    override fun ImageView.loadImageRound(context: Context?, uri: Uri) {
        load(uri) { transformations(CircleCropTransformation()) }
    }

    override fun ImageView.loadImageRound(context: Context?, url: String?) {
        load(url) { transformations(CircleCropTransformation()) }
    }

    override fun ImageView.loadImageRadius(context: Context?, resourceId: Int, radius: Int) {
        load(resourceId) { transformations(RoundedCornersTransformation(radius.toFloat())) }
    }

    override fun ImageView.loadImageRadius(context: Context?, bitmap: Bitmap?, radius: Int) {
        load(bitmap) { transformations(RoundedCornersTransformation(radius.toFloat())) }
    }

    override fun ImageView.loadImageRadius(context: Context?, uri: Uri, radius: Int) {
        load(uri) { transformations(RoundedCornersTransformation(radius.toFloat())) }
    }

    override fun ImageView.loadImageRadius(context: Context?, file: File, radius: Int) {
        load(file) { transformations(RoundedCornersTransformation(radius.toFloat())) }
    }

    override fun ImageView.loadImageRadius(context: Context?, url: String?, radius: Int) {
        load(url) { transformations(RoundedCornersTransformation(radius.toFloat())) }
    }

    override fun ImageView.loadGif(context: Context?, resourceId: Int) {
        load(resourceId)
    }

    override fun ImageView.loadGif(context: Context?, url: String?) {
        load(url)
    }

    override fun getImageDrawable(context: Context?, url: String?, onResourceReady: (Drawable) -> Unit) {
        context?.let {
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    getImageLoader(it).execute(ImageRequest.Builder(it).data(url).build())
                }
                if (result is SuccessResult) onResourceReady(result.image as Drawable)
            }
        }
    }

    override fun getImageBitmap(context: Context?, url: String?, onResourceReady: (Bitmap?) -> Unit) {
        context?.let {
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    getImageLoader(it).execute(ImageRequest.Builder(it).data(url).build())
                }
                val bitmap = (result as? SuccessResult)?.image.let { image ->
                    (image as? BitmapDrawable)?.bitmap
                }
                onResourceReady(bitmap)
            }
        }
    }
}
