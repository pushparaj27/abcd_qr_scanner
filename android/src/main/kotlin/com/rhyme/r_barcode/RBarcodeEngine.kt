package com.rhyme.r_barcode

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.os.HandlerThread
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.rhyme.r_barcode.utils.ImageUtils
import com.rhyme.r_barcode.utils.RBarcodeFormatUtils
import com.rhyme.r_barcode.utils.RBarcodeNative
import io.flutter.plugin.common.BinaryMessenger
import java.io.File
import java.lang.Exception
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.collections.ArrayList


class RBarcodeEngine {

    private var isDebug: Boolean? = true
    private fun log(msg: String) {
        if (isDebug!!) Log.d("RBarCodeEngine", msg)
    }

    private val ALL_FORMATS: MutableList<BarcodeFormat> = ArrayList()
    private val mFormats: MutableList<BarcodeFormat>? = mutableListOf()
    private val reader: MultiFormatReader

    //    private val scanner:ImageScanner
    private var eventChannel: RBarcodeEventChannel? = null
    private var isScan: Boolean = true
    private var cropRect: Rect? = null
    private var isReturnImage: Boolean = false
    private var threadHandler: HandlerThread = HandlerThread("RBarcodeThread")
    private val executor: Executor = Executors.newSingleThreadExecutor()
    private val executorImage: Executor = Executors.newSingleThreadExecutor()

   
    init {
        ALL_FORMATS.add(BarcodeFormat.AZTEC)
        ALL_FORMATS.add(BarcodeFormat.CODABAR)
        ALL_FORMATS.add(BarcodeFormat.CODE_39)
        ALL_FORMATS.add(BarcodeFormat.CODE_93)
        ALL_FORMATS.add(BarcodeFormat.CODE_128)
        ALL_FORMATS.add(BarcodeFormat.DATA_MATRIX)
        ALL_FORMATS.add(BarcodeFormat.EAN_8)
        ALL_FORMATS.add(BarcodeFormat.EAN_13)
        ALL_FORMATS.add(BarcodeFormat.ITF)
        ALL_FORMATS.add(BarcodeFormat.MAXICODE)
        ALL_FORMATS.add(BarcodeFormat.PDF_417)
        ALL_FORMATS.add(BarcodeFormat.QR_CODE)
        ALL_FORMATS.add(BarcodeFormat.RSS_14)
        ALL_FORMATS.add(BarcodeFormat.RSS_EXPANDED)
        ALL_FORMATS.add(BarcodeFormat.UPC_A)
        ALL_FORMATS.add(BarcodeFormat.UPC_E)
        ALL_FORMATS.add(BarcodeFormat.UPC_EAN_EXTENSION)

        val hints: MutableMap<DecodeHintType, Any?> =
            EnumMap<DecodeHintType, Any>(DecodeHintType::class.java)
        hints[DecodeHintType.POSSIBLE_FORMATS] = getFormats()
        reader = MultiFormatReader()
        reader.setHints(hints)
        threadHandler.start()
    }

  
    fun initBarCodeEngine(isDebug: Boolean, formats: List<String>, isReturnImage: Boolean) {
        this.isDebug = isDebug
        this.isReturnImage = isReturnImage
        this.mFormats!!.clear()
        this.mFormats.addAll(RBarcodeFormatUtils.transitionFromFlutterCode(formats))
//        log(RBarcodeNative.get().stringFromJNI())
    }

  
    fun initEventChannel(messenger: BinaryMessenger, eventId: Long, isDebug: Boolean) {
        if (eventChannel != null) {
            eventChannel!!.dispose()
        } else {
            eventChannel = RBarcodeEventChannel(messenger, eventId)
        }
        this.isDebug = isDebug
    }

    fun setCropRect(left: Int, top: Int, right: Int, bottom: Int) {
        cropRect = Rect(left, top, right, bottom)
    }


  
    private fun getFormats(): Collection<BarcodeFormat?> {
        return mFormats ?: ALL_FORMATS
    }

    private fun isOnlyQrCodeFormat(): Boolean {
        return getFormats().size == 1 && getFormats().contains(BarcodeFormat.QR_CODE)
    }

   
    fun setFormats(formats: List<String>) {
        log("setFormats ${formats.joinToString(separator = "\n")}")
        this.mFormats!!.clear()
        this.mFormats.addAll(RBarcodeFormatUtils.transitionFromFlutterCode(formats))
        val hints: MutableMap<DecodeHintType, Any?> =
            EnumMap<DecodeHintType, Any>(DecodeHintType::class.java)
        hints[DecodeHintType.POSSIBLE_FORMATS] = getFormats()
        reader.setHints(hints)
    }


  
    fun startScan() {
        isScan = true
    }

    
    fun stopScan() {
        isScan = false
    }

    fun isScanning(): Boolean {
        return isScan
    }

    private var latestAcquireImageTime: Long = System.currentTimeMillis()
    private var latestScanSuccessTime: Long = System.currentTimeMillis()

    
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun getImage(it: ImageReader): Image? {
        if (System.currentTimeMillis() - latestAcquireImageTime > 150L && System.currentTimeMillis() - latestScanSuccessTime > 150L) {
            latestAcquireImageTime = System.currentTimeMillis()
            val image = it.acquireLatestImage() ?: return null
            if (image.format != ImageFormat.YUV_420_888 || !isScan) {
                image.close()
                return null
            }
            return image
        } else {
            return null
        }
    }

//    private lateinit var imageByte: WeakReference<ByteArray>

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    val imageListener: ImageReader.OnImageAvailableListener = ImageReader.OnImageAvailableListener {
        executor.execute {
            val image = getImage(it) ?: return@execute
            val width = image.width
            val height = image.height
            val yBuffer = image.planes[0].buffer
            val yLen = yBuffer!!.remaining() 
            val yByte = ByteArray(yLen)
//        var rotateYByte: ByteArray? 
            yBuffer.get(yByte, 0, yLen) 
            yBuffer.clear()

            val uBuffer = image.planes[1].buffer 
            val uLen = uBuffer!!.remaining() 
            val uByte = ByteArray(uLen)
            uBuffer.get(uByte, 0, uLen) 
            uBuffer.clear()

            val vBuffer = image.planes[2].buffer 
            val vLen = vBuffer!!.remaining() 
            val vByte = ByteArray(vLen) 
            vBuffer.get(vByte, 0, vLen) 
            vBuffer.clear()
            image.close()
            decodeImageResult(RBarcodeEntity(yByte, uByte, vByte, yLen, uLen, vLen, width, height))
        }
    }


    private fun decodeImageResult(entity: RBarcodeEntity) {
        val firstTime = System.currentTimeMillis()
        var decodeResult: Result? = null
        var isRotate = false
        try {
            decodeResult = decodeImage(entity.y, entity.width, entity.height)
            log("thread:" + Thread.currentThread() + "  decodeImage first:" + (System.currentTimeMillis() - firstTime) + "ms")
            val secondTime = System.currentTimeMillis()
            if (decodeResult == null && !isOnlyQrCodeFormat()) {
                
                val rotateYByte = ByteArray(entity.width * entity.height * 3 / 2)
                ImageUtils.rotateYUVDegree90(entity.y, rotateYByte, entity.width, entity.height)
                decodeResult = decodeImage(rotateYByte, entity.width, entity.height)
                log("thread:" + Thread.currentThread() + "  decodeImage second:" + (System.currentTimeMillis() - secondTime) + "ms")
                isRotate = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (decodeResult != null) {
            log("scan image success!!!!!")
            latestScanSuccessTime = System.currentTimeMillis()

            if (isReturnImage) {
                
                var resultByte: ByteArray? = null
                if (isRotate) {
                    
                    val sourceByte = ByteArray(entity.yLen + entity.uLen + entity.vLen)
                    resultByte = ByteArray(entity.width * entity.height * 3 / 2)
                    System.arraycopy(entity.y, 0, sourceByte, 0, entity.yLen)
                    System.arraycopy(entity.u, 0, sourceByte, entity.yLen, entity.uLen)
                    System.arraycopy(
                        entity.v,
                        0,
                        sourceByte,
                        entity.yLen + entity.uLen,
                        entity.vLen
                    )
                    ImageUtils.rotateYUVDegree90(
                        sourceByte,
                        resultByte,
                        entity.width,
                        entity.height
                    )
                } else {
                    
                    resultByte = ByteArray(entity.yLen + entity.uLen + entity.vLen)
                    System.arraycopy(entity.y, 0, resultByte, 0, entity.yLen)
                    System.arraycopy(entity.u, 0, resultByte, entity.yLen, entity.uLen)
                    System.arraycopy(
                        entity.v,
                        0,
                        resultByte,
                        entity.yLen + entity.uLen,
                        entity.vLen
                    )
                }

                val byte: ByteArray? =
                    ImageUtils.nv212Flutter(resultByte, entity.width, entity.height, isRotate)

                if (isRotate) {
                    resultToMap(
                        decodeResult,
                        byte!!,
                        entity.height,
                        entity.width,
                        isRotate
                    )?.let { it1 ->
                        if (!isScan) return@let
                        eventChannel?.sendMessage(it1)
                    }
                } else {
                    resultToMap(
                        decodeResult,
                        byte!!,
                        entity.width,
                        entity.height,
                        isRotate
                    )?.let { it1 ->
                        if (!isScan) return@let
                        eventChannel?.sendMessage(it1)
                    }
                }

            } else {
               
                resultToMapNoImage(
                    decodeResult,
                    entity.width,
                    entity.height,
                    isRotate
                )?.let { it1 ->
                    if (!isScan) return@let
                    eventChannel?.sendMessage(it1)
                }
            }
        }
        log("thread:" + Thread.currentThread() + "  decodeImage time consuming:" + (System.currentTimeMillis() - firstTime) + "ms")

    }

    fun decodeImagePath(file: File, onResult: (result: Map<String, Any>?) -> Unit) {
        executorImage.execute {
            val bitmap = BitmapFactory.decodeFile(file.path)
            val bytes = bitmap.byteCount
            val buf = ByteBuffer.allocate(bytes)
            bitmap.copyPixelsToBuffer(buf)
            val width = bitmap.width
            val height = bitmap.height
            val yBuffer = ByteArray(width * height)
            val uBuffer = ByteArray(width * height * 1 / 4)
            val vBuffer = ByteArray(width * height * 1 / 4)
            RBarcodeNative.get().aRGBToYUV(buf.array(), width, height, yBuffer, uBuffer, vBuffer)
            val result = decodeImage(yBuffer, width, height)
            if (result != null) {
                onResult(resultToMapNoImage(result, width = width, height = height, false))
            } else {
                onResult(null)
            }
        }
    }

   
    private fun decodeImage(byte: ByteArray, width: Int, height: Int): Result? {
        var dataWidth = width
        var dataHeight = height
        if (cropRect != null) {
            dataWidth = cropRect!!.right - cropRect!!.left
            dataHeight = cropRect!!.bottom - cropRect!!.top
        }

        var result: Result? = null
        val source = PlanarYUVLuminanceSource(
            byte,
            width,
            height,
            cropRect?.left ?: 0,
            cropRect?.top ?: 0,
            dataWidth,
            dataHeight,
            false
        )
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        try {
            result = reader.decodeWithState(bitmap)
        } catch (ex: Exception) {
//            reader.reset()
        }
//        try {
//            result = reader.decodeWithState(BinaryBitmap(HybridBinarizer(source)))
//        } catch (ex: Exception) {
//        } finally {
//            reader.reset()
//        }
//        if (result == null) {
//            val invertedSource = source.invert()
//            try {
//                result = reader.decodeWithState(BinaryBitmap(HybridBinarizer(invertedSource)))
//            } catch (ex: Exception) {
//            } finally {
//                reader.reset()
//            }
//        }
        return result
    }

    
    private fun resultToMap(
        result: Result?,
        image: ByteArray,
        width: Int,
        height: Int,
        isRotate: Boolean
    ): Map<String, Any>? {
        if (result == null) return null
        val data: MutableMap<String, Any> = HashMap()
        data["text"] = result.text
        data["format"] = RBarcodeFormatUtils.transitionToFlutterCode(result.barcodeFormat)
        data["image"] = image
        data["imageWidth"] = width;
        data["imageHeight"] = height;
        if (result.resultPoints != null) {
            
            val resultPoints: MutableList<Map<String, Any>> = java.util.ArrayList()
            for (point in result.resultPoints) {
                val pointMap: MutableMap<String, Any> = HashMap()
                if (isRotate) {
                    Log.d("rotate", "resultToMap: ${point.x} , $width , ${point.y}, $height")
                    pointMap["x"] = point.x / width
                    pointMap["y"] = point.y / height
                } else {
                    Log.d("not rotate", "resultToMap: ${point.x} , $width , ${point.y}, $height")
                    pointMap["y"] = point.x / width
                    pointMap["x"] = (height - point.y) / height
                }
                resultPoints.add(pointMap)
            }
            data["points"] = resultPoints
        }
        return data
    }

    
    private fun resultToMapNoImage(
        result: Result?,
        width: Int,
        height: Int,
        isRotate: Boolean
    ): Map<String, Any>? {
        if (result == null) return null
        val data: MutableMap<String, Any> = HashMap()
        data["text"] = result.text
        data["format"] = RBarcodeFormatUtils.transitionToFlutterCode(result.barcodeFormat)
        if (result.resultPoints != null) {
            val resultPoints: MutableList<Map<String, Any>> = java.util.ArrayList()
            for (point in result.resultPoints) {
                val pointMap: MutableMap<String, Any> = HashMap()
                if (isRotate) {
                    pointMap["x"] = point.x / height
                    pointMap["y"] = point.y / width
                } else {
                    pointMap["y"] = point.x / width
                    pointMap["x"] = (height - point.y) / height
                }
                resultPoints.add(pointMap)
            }
            data["points"] = resultPoints
        }
        return data
    }

}