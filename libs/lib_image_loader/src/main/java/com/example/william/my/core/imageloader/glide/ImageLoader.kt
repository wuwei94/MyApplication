package com.example.william.my.core.imageloader.glide

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.example.william.my.core.imageloader.IImageLoader
import com.example.william.my.core.imageloader.ImageOptions
import java.io.File

/**
 * 基于 Glide 的图片加载器实现
 */
object ImageLoader : IImageLoader {

    override fun pauseRequests(context: Context?) {
        context?.let {
            Glide.with(it)
                .pauseRequests()
        }
    }

    override fun resumeRequests(context: Context?) {
        context?.let {
            Glide.with(it)
                .resumeRequests()
        }
    }

    override fun ImageView.clear(context: Context?) {
        context?.let {
            Glide.with(it)
                .clear(this)
        }
    }

    override fun ImageView.loadImage(context: Context?, resourceId: Int) {
        context?.let {
            Glide.with(it)
                .load(resourceId)
                .into(this)
        }
    }

    override fun ImageView.loadImage(
        context: Context?,
        bitmap: Bitmap?,
    ) {
        context?.let {
            Glide.with(it)
                .load(bitmap)
                .into(this)
        }
    }

    override fun ImageView.loadImage(context: Context?, uri: Uri) {
        context?.let {
            Glide.with(it)
                .load(uri)
                .into(this)
        }
    }

    override fun ImageView.loadImage(context: Context?, file: File) {
        context?.let {
            Glide.with(it)
                .load(file)
                .into(this)
        }
    }

    override fun ImageView.loadImage(
        context: Context?,
        url: String?,
        options: ImageOptions?,
        onComplete: (() -> Unit)?,
    ) {
        context?.let {
            val request = Glide.with(it)
                .load(url)

            options?.toGlideRequestOptions()?.let { request.apply(it) }

            if (onComplete != null) {
                request.addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean,
                    ): Boolean {
                        onComplete.invoke()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable?>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean,
                    ): Boolean {
                        onComplete.invoke()
                        return false
                    }
                })
            }

            request.into(this)
        }
    }

    override fun ImageView.loadImageRound(
        context: Context?,
        resourceId: Int,
    ) {
        context?.let {
            Glide.with(it)
                .load(resourceId)
                .transform(CircleCrop())
                .into(this)
        }
    }

    override fun ImageView.loadImageRound(
        context: Context?,
        bitmap: Bitmap?,
    ) {
        context?.let {
            Glide.with(it)
                .load(bitmap)
                .transform(CircleCrop())
                .into(this)
        }
    }

    override fun ImageView.loadImageRound(context: Context?, file: File) {
        context?.let {
            Glide.with(it)
                .load(file)
                .transform(CircleCrop())
                .into(this)
        }
    }

    override fun ImageView.loadImageRound(context: Context?, uri: Uri) {
        context?.let {
            Glide.with(it)
                .load(uri)
                .transform(CircleCrop())
                .into(this)
        }
    }

    override fun ImageView.loadImageRound(
        context: Context?,
        url: String?,
    ) {
        context?.let {
            Glide.with(it)
                .load(url)
                .transform(CircleCrop())
                .into(this)
        }
    }

    override fun ImageView.loadImageRadius(
        context: Context?,
        resourceId: Int,
        radius: Int,
    ) {
        context?.let {
            Glide.with(it)
                .load(resourceId)
                .transform(CenterCrop(), RoundedCorners(radius))
                .into(this)
        }
    }

    override fun ImageView.loadImageRadius(
        context: Context?,
        bitmap: Bitmap?,
        radius: Int,
    ) {
        context?.let {
            Glide.with(it)
                .load(bitmap)
                .transform(CenterCrop(), RoundedCorners(radius))
                .into(this)
        }
    }

    override fun ImageView.loadImageRadius(context: Context?, uri: Uri, radius: Int) {
        context?.let {
            Glide.with(it)
                .load(uri)
                .transform(CenterCrop(), RoundedCorners(radius))
                .into(this)
        }
    }

    override fun ImageView.loadImageRadius(context: Context?, file: File, radius: Int) {
        context?.let {
            Glide.with(it)
                .load(file)
                .transform(CenterCrop(), RoundedCorners(radius))
                .into(this)
        }
    }

    override fun ImageView.loadImageRadius(
        context: Context?,
        url: String?,
        radius: Int,
    ) {
        context?.let {
            Glide.with(it)
                .load(url)
                .transform(CenterCrop(), RoundedCorners(radius))
                .into(this)
        }
    }

    override fun ImageView.loadGif(context: Context?, resourceId: Int) {
        context?.let {
            Glide.with(it)
                .asGif()
                .load(resourceId)
                .into(this)
        }
    }

    override fun ImageView.loadGif(context: Context?, url: String?) {
        context?.let {
            Glide.with(it)
                .asGif()
                .load(url)
                .into(this)
        }
    }

    override fun getImageDrawable(
        context: Context?,
        url: String?,
        onResourceReady: ((drawable: Drawable) -> Unit),
    ) {
        if (url.isNullOrEmpty()) {
            return
        }
        context?.let {
            Glide.with(it)
                .load(url)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?,
                    ) {
                        onResourceReady(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        }
    }

    override fun getImageBitmap(
        context: Context?,
        url: String?,
        onResourceReady: ((bitmap: Bitmap?) -> Unit),
    ) {
        if (url.isNullOrEmpty()) {
            return
        }
        context?.let {
            Glide.with(it)
                .asBitmap()
                .load(url)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?,
                    ) {
                        onResourceReady(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        }
    }

    private fun ImageOptions?.toGlideRequestOptions(): RequestOptions {
        val options = this ?: return RequestOptions()
        return RequestOptions().apply {
            when (options.cacheStrategy) {
                ImageOptions.CacheStrategy.ALL -> diskCacheStrategy(DiskCacheStrategy.ALL)
                ImageOptions.CacheStrategy.NONE -> diskCacheStrategy(DiskCacheStrategy.NONE)
                ImageOptions.CacheStrategy.DATA -> diskCacheStrategy(DiskCacheStrategy.DATA)
                ImageOptions.CacheStrategy.RESOURCE -> diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            }
            skipMemoryCache(options.skipMemoryCache)
        }
    }
}
