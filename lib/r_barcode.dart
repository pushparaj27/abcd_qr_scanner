library r_barcode;

import 'dart:async';
import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:r_barcode/r_barcode_exception.dart';
import 'dart:math' as math;

export 'r_barcode_indicator.dart';

part 'r_barcode_format.dart';

part 'r_barcode_camera.dart';

part 'r_barcode_result.dart';

part 'r_barcode_frame.dart';

const _kPluginType = 'com.rhyme_lph/r_barcode';


class RBarcode {
  static const MethodChannel _channel = const MethodChannel(_kPluginType);
  static List<RBarcodeFormat>? _globalFormat;

  static Future<void> initBarcodeEngine({
    List<RBarcodeFormat> formats= RBarcodeFormat.kAll,
    bool? isDebug,
    bool isReturnImage = false,
  }) {
    _globalFormat = formats;
    return _channel.invokeMethod('initBarcodeEngine', {
      'formats': formats.map((e) => e._value).toList(),
      'isDebug': isDebug,
      'isReturnImage': isReturnImage,
    });
  }

  static Future<List<RBarcodeCameraDescription>?>
      availableBarcodeCameras() async {
    try {
      final List<Map<dynamic, dynamic>>? cameras = await _channel
          .invokeListMethod<Map<dynamic, dynamic>>('availableCameras');
      return cameras
          ?.map((Map<dynamic, dynamic> camera) => RBarcodeCameraDescription(
                name: camera['name'],
                lensDirection: _parseCameraLensDirection(camera['lensFacing']),
              ))
          .toList();
    } on PlatformException catch (e) {
      throw RBarcodeException(e.code, e.message);
    }
  }

  static Future<void> _setBarcodeFormats(List<RBarcodeFormat> formats) async =>
      await _channel.invokeMethod("setBarcodeFormats",
          {"formats": formats.map((e) => e._value).toList()});

  static Future<Map<String, dynamic>?> _initialize(
          String? cameraName, String resolutionPreset, bool? isDebug) async =>
      await _channel.invokeMapMethod('initialize', <String, dynamic>{
        'cameraName': cameraName,
        'resolutionPreset': resolutionPreset,
        'isDebug': isDebug,
      });

  static Future<void> _disposeTexture(int? textureId) async =>
      await _channel.invokeMethod('dispose', {
        'textureId': textureId,
      });

 
  static Future<bool?> _isTorchOn() async =>
      await _channel.invokeMethod('isTorchOn');


  static Future<bool?> _enableTorch(bool isTorchOn) async =>
      await _channel.invokeMethod('enableTorch', {
        'isTorchOn': isTorchOn,
      });

 
  static Future<void> _stopScan() async =>
      await _channel.invokeMethod('stopScan');

 
  static Future<void> _startScan() async =>
      await _channel.invokeMethod('startScan');

  static Future<void> _requestFocus(
          double x, double y, double width, double height) async =>
      await _channel.invokeMethod('requestFocus', {
        'x': x,
        'y': y,
        'width': width,
        'height': height,
      });


  static Future<List<RBarcodeResult>?> decodeImagePath(String path) async =>
      await _channel.invokeMethod('decodeImagePath', {
        "path": path,
      }).then((value) => value == null
          ? null
          : (value as List).map((e) => RBarcodeResult.formMap(e)).toList());

 
  static Future<RBarcodeResult?> decodeImageUrl(String url) async =>
      await _channel.invokeMethod('decodeImageUrl', {
        "url": url,
      }).then((value) => value == null ? null : RBarcodeResult.formMap(value));

  
  static Future<RBarcodeResult?> decodeImageMemory(Uint8List uint8list) async =>
      await _channel.invokeMethod('decodeImageMemory', {
        "uint8list": uint8list,
      }).then((value) => value == null ? null : RBarcodeResult.formMap(value));
}


class RBarcodeCamera extends StatelessWidget {
  final RBarcodeCameraController controller;
  final Widget? initWidget;

  const RBarcodeCamera(this.controller, {Key? key, this.initWidget})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    return controller.value.isInitialized!
        ? Texture(textureId: controller._textureId!)
        : (initWidget ??
            Container(
              color: Colors.black,
            ));
  }
}
