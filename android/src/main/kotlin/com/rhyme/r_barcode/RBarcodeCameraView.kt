package com.rhyme.r_barcode

import android.app.Activity
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.params.MeteringRectangle
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import androidx.annotation.RequiresApi
import com.rhyme.r_barcode.RBarcodeCameraConfiguration.ResolutionPreset
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.TextureRegistry.SurfaceTextureEntry
import java.util.*
import android.graphics.Rect
import android.hardware.camera2.CameraCharacteristics

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class RBarcodeCameraView(private val activity: Activity,
                         texture: SurfaceTextureEntry,
                         private val cameraName: String,
                         resolutionPreset: String,
                         private val readerListener: ImageReader.OnImageAvailableListener) {
    private var imageStreamReader: ImageReader
    private val cameraManager: CameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraDevice: CameraDevice? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null
    private var captureSession: CameraCaptureSession? = null
    private val previewSize: Size
    private val textureEntry: SurfaceTextureEntry = texture
    private val frameThreadHandler = HandlerThread("frame thread")
    private val frameHandler: Handler
    private var zoomLevel: Float = 2f 

    init {
        val preset = ResolutionPreset.valueOf(resolutionPreset)
        previewSize = RBarcodeCameraConfiguration.get().computeBestPreviewSize(cameraName, preset)!!
        imageStreamReader = ImageReader.newInstance(previewSize.width, previewSize.height, ImageFormat.YUV_420_888, 3)
        frameThreadHandler.start()
        frameHandler = Handler(frameThreadHandler.looper)
    }

   
    fun open(result: MethodChannel.Result) {
        var isReplay = false
        cameraManager.openCamera(cameraName, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                try {
                    startPreview()
                } catch (e: CameraAccessException) {
                    isReplay = true
                    result.error("CameraAccessException", e.message, null)
                    return
                }
                val reply = mutableMapOf<String, Any>()
                reply["textureId"] = textureEntry.id()
                reply["previewWidth"] = previewSize.width
                reply["previewHeight"] = previewSize.height
                isReplay = true
                result.success(reply)
            }

            override fun onDisconnected(camera: CameraDevice) {
               
                camera.close()
            }

            override fun onError(camera: CameraDevice, error: Int) {
                if (!isReplay) {
                    isReplay = true
                    result.error("$error", "Open Camera Error", null)
                }
            }

            override fun onClosed(camera: CameraDevice) {
                super.onClosed(camera)
                
            }
        }, null)
    }

 
    @Throws(CameraAccessException::class)
    fun enableTorch(b: Boolean) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            cameraManager.setTorchMode(cameraName, b)
//        } else {
        captureRequestBuilder!!.set(
                CaptureRequest.FLASH_MODE, if (b) {
            CaptureRequest.FLASH_MODE_TORCH
        } else {
            CaptureRequest.FLASH_MODE_OFF
        })
//        captureRequestBuilder!!.set(CaptureRequest.CONTROL_AE_MODE, if (b) {
//            CaptureRequest.CONTROL_AE_MODE_ON
//        } else {
//            CaptureRequest.CONTROL_AE_MODE_OFF
//        })
        captureRequestBuilder!!.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
        captureSession!!.setRepeatingRequest(captureRequestBuilder!!.build(), null, null)
//        }
    }


    
    fun isTorchOn(): Boolean {
        return try {
            val flashMode = captureRequestBuilder!!.get(CaptureRequest.FLASH_MODE)
            flashMode != null && flashMode != CaptureRequest.FLASH_MODE_OFF
        } catch (e: NullPointerException) {
            false
        }
    }

    fun startPreview() {
        createCaptureSession(imageStreamReader.surface)
        imageStreamReader.setOnImageAvailableListener(readerListener, frameHandler)

    }


   
    fun stopScan() {
        try {
            captureSession!!.stopRepeating()
        } catch (e: Exception) {

        }
    }

   
    fun startScan() {
        try {
            captureSession!!.setRepeatingRequest(captureRequestBuilder!!.build(), null, frameHandler)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        try {
            captureSession!!.capture(captureRequestBuilder!!.build(), null, frameHandler)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    private fun calculateZoomRect(activeArraySize: Rect, zoomLevel: Float): Rect {
        val centerX = activeArraySize.centerX()
        val centerY = activeArraySize.centerY()
        val deltaX = (activeArraySize.width() / (2 * zoomLevel)).toInt()
        val deltaY = (activeArraySize.height() / (2 * zoomLevel)).toInt()
        return Rect(centerX - deltaX, centerY - deltaY, centerX + deltaX, centerY + deltaY)
    }

  
    @Throws(CameraAccessException::class)
    private fun createCaptureSession(
            vararg surfaces: Surface) {
       
        closeCaptureSession()

        val cameraCharacteristics: CameraCharacteristics? = cameraManager.getCameraCharacteristics(cameraName)
        
        captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

        
        val surfaceTexture: SurfaceTexture = textureEntry.surfaceTexture()
       
        surfaceTexture.setDefaultBufferSize(previewSize.width, previewSize.height)

        
        val flutterSurface = Surface(surfaceTexture)
        captureRequestBuilder!!.addTarget(flutterSurface)
        val remainingSurfaces = Arrays.asList(*surfaces)
        for (surface in remainingSurfaces) {
            captureRequestBuilder!!.addTarget(surface)
        }
        val surfaceList: MutableList<Surface> = ArrayList()
        surfaceList.addAll(remainingSurfaces)
        surfaceList.add(flutterSurface)

        cameraDevice!!.createCaptureSession(surfaceList, object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) { 
                if (cameraDevice == null) { //                                rScanMessenger.send(
//              DartMessenger.EventType.ERROR, "The camera was closed during configuration.");
                    return
                }
                captureSession = session
                try {
                    captureRequestBuilder!!.set(
                            CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                    captureRequestBuilder!!.set(
                            CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH)
                    captureRequestBuilder!!.set(
                            CaptureRequest.JPEG_ORIENTATION, RBarcodeCameraConfiguration.get().getOrientation(activity, cameraManager, cameraName))
                    

                            
                   
                    cameraCharacteristics?.let { characteristics ->
                        val rect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
                        rect?.let {
                            val zoomedRect = calculateZoomRect(it, zoomLevel)
                            captureRequestBuilder!!.set(CaptureRequest.SCALER_CROP_REGION, zoomedRect)
                        }
                    }
                   
                    captureSession!!.setRepeatingRequest(captureRequestBuilder!!.build(), null, null)
                } catch (e: CameraAccessException) { 
                    e.printStackTrace()
//                        rScanMessenger.send(DartMessenger.EventType.ERROR, e.getMessage());
                }
            }

            override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) { 
//                        rScanMessenger.send(
//                                DartMessenger.EventType.ERROR, "Failed to configure camera session.");
            }
        }, null)
    }

    
    private fun closeCaptureSession() {
        if (captureSession != null) {
            captureSession!!.close()
            captureSession = null
            textureEntry.release()
        }
    }

   
    fun close() {
        closeCaptureSession()
        if (cameraDevice != null) {
            cameraDevice!!.close()
            cameraDevice = null
        }
        imageStreamReader.close()
    }


  
    fun requestFocus(rect: MeteringRectangle) {
        val rectangle = arrayOf<MeteringRectangle>(rect)
       
        captureRequestBuilder!!.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
        
        captureRequestBuilder!!.set(CaptureRequest.CONTROL_AE_REGIONS, rectangle)
       
        captureRequestBuilder!!.set(CaptureRequest.CONTROL_AF_REGIONS, rectangle)

        
        try {
            captureSession!!.setRepeatingRequest(captureRequestBuilder!!.build(), null, frameHandler)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }

       
        captureRequestBuilder!!.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START)
        try {
            captureSession!!.capture(captureRequestBuilder!!.build(), null, frameHandler)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

}
