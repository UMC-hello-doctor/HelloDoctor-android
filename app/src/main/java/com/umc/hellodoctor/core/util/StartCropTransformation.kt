package com.umc.hellodoctor.core.util

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import java.security.MessageDigest

/**
 * 왼쪽(시작점) 기준으로 crop하는 Transformation
 */
class StartCropTransformation : Transformation<Bitmap> {
    override fun transform(
        context: Context,
        resource: Resource<Bitmap>,
        outWidth: Int,
        outHeight: Int,
    ): Resource<Bitmap> {
        val toTransform = resource.get()
        val width = toTransform.width
        val height = toTransform.height

        // 이미 원하는 크기면 그대로 반환
        if (width == outWidth && height == outHeight) {
            return resource
        }

        // 가로세로 비율 계산
        val aspectRatio = outWidth.toFloat() / outHeight.toFloat()
        val bitmapAspectRatio = width.toFloat() / height.toFloat()

        var cropWidth = width
        var cropHeight = height

        when {
            // 이미지가 더 넓은 경우 (너비를 기준으로 crop)
            bitmapAspectRatio > aspectRatio -> {
                cropWidth = (height * aspectRatio).toInt()
                cropHeight = height
            }
            // 이미지가 더 높은 경우 (높이를 기준으로 crop)
            else -> {
                cropWidth = width
                cropHeight = (width / aspectRatio).toInt()
            }
        }

        // 왼쪽(0, 0)부터 crop한 bitmap 생성
        val croppedBitmap = Bitmap.createBitmap(toTransform, 0, 0, cropWidth, cropHeight)

        // BitmapPool을 context에서 얻어서 사용
        val bitmapPool = Glide.get(context).bitmapPool
        return BitmapResource.obtain(croppedBitmap, bitmapPool)!!
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("StartCropTransformation".toByteArray())
    }

    override fun equals(other: Any?): Boolean = other is StartCropTransformation

    override fun hashCode(): Int = ID.hashCode()

    companion object {
        private const val ID = "com.umc.hellodoctor.StartCropTransformation"
    }
}
