part of 'r_barcode.dart';

class RBarcodeResult {
  final RBarcodeFormat? format;
  final String? text;
  final List<RBarcodePoint>? points;
  final Uint8List? image;
  final int? imageWidth;
  final int? imageHeight;

  const RBarcodeResult(
      {this.imageWidth,
      this.imageHeight,
      this.image,
      this.format,
      this.text,
      this.points});

  factory RBarcodeResult.formMap(Map? map) {
    return map == null
        ? RBarcodeResult()
        : RBarcodeResult(
            format:
                map['format'] != null ? RBarcodeFormat(map['format']) : null,
            text: map['text'] as String?,
            points: map['points'] != null
                ? (map['points'] as List)
                    .map(
                      (data) => RBarcodePoint(
                        data['x'],
                        data['y'],
                      ),
                    )
                    .toList()
                : null,
            image: map['image'],
            imageWidth: map['imageWidth'],
            imageHeight: map['imageHeight'],
          );
  }

  @override
  String toString() {
    return 'RScanResult{format: $format, text: $text,width:$imageWidth,height:$imageHeight,points: $points}';
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is RBarcodeResult &&
          runtimeType == other.runtimeType &&
          format == other.format &&
          text == other.text &&
          points == other.points &&
          imageWidth == other.imageWidth &&
          imageHeight == other.imageHeight;

  @override
  int get hashCode => format.hashCode ^ text.hashCode ^ points.hashCode^ imageWidth.hashCode^ imageHeight.hashCode;
}

class RBarcodePoint {
  final double? x;
  final double? y;

  RBarcodePoint(this.x, this.y);

  @override
  String toString() {
    return 'RScanPoint{x: $x , y: $y}';
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is RBarcodePoint &&
          runtimeType == other.runtimeType &&
          x == other.x &&
          y == other.y;

  @override
  int get hashCode => x.hashCode ^ y.hashCode;
}
