import 'package:flutter/material.dart';


class RBarCodeCircleIndicator extends StatefulWidget {
  final Size? size;

  const RBarCodeCircleIndicator({
    Key? key,
    this.size,
  }) : super(key: key);

  @override
  _RBarCodeCircleIndicatorState createState() =>
      _RBarCodeCircleIndicatorState();
}

class _RBarCodeCircleIndicatorState extends State<RBarCodeCircleIndicator>
    with TickerProviderStateMixin {
  late AnimationController _controller;
  late CurvedAnimation curve;
  late AnimationController _finishController;
  late Animation<double> _finishValue;
  @override
  void initState() {
    super.initState();
    _controller =
        AnimationController(vsync: this, duration: Duration(seconds: 1));
    curve = CurvedAnimation(parent: _controller, curve: Curves.bounceOut);
    _finishController =
        AnimationController(vsync: this, duration: Duration(seconds: 1));
    _finishValue = Tween(begin: 1.0,end: 0.6).animate(_finishController);

    _controller.addStatusListener((status) {
      if (status == AnimationStatus.completed) {
        _finishController.repeat(reverse: true);
      }
    });

    _controller.forward();
  }

  @override
  void dispose() {
    _controller.dispose();
    _finishController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _controller,
      builder: _buildAnimatedWidget,
    );
  }

  Widget _buildAnimatedWidget(BuildContext context, Widget? child) {
    if(_controller.status == AnimationStatus.completed){
      return AnimatedBuilder(animation: _finishController, builder: (ctx,child)=>CustomPaint(
        painter: RBarCodeCircleIndicatorPainter(
          innerColor: Theme.of(context).primaryColor,
          radius: 10,
          progress: _finishValue.value,
        ),
        size: Size(1, 1),
      ));
    }else{
      return CustomPaint(
        painter: RBarCodeCircleIndicatorPainter(
          innerColor: Theme.of(context).primaryColor,
          radius: 10,
          progress: curve.value,
        ),
        size: Size(1, 1),
      );
    }
  }
}

class RBarCodeCircleIndicatorPainter extends CustomPainter {
  final double? progress;
  final Color outColor;
  final Color innerColor;
  final double? radius;
  Paint? _mPaint1;
  Paint? _mPaint2;

  RBarCodeCircleIndicatorPainter(
      {this.progress,
      this.outColor= Colors.white,
      this.innerColor= Colors.green,
      this.radius});

  void initPaint() {
    _mPaint1 = Paint()
      ..color = innerColor
      ..style = PaintingStyle.fill;

    _mPaint2 = Paint()
      ..color = outColor
      ..style = PaintingStyle.fill;
  }

  @override
  void paint(Canvas canvas, Size size) {
    if (_mPaint2 == null || _mPaint1 == null) initPaint();

    double innerRadius = radius! * 2 / 3;

    canvas.drawCircle(
        Offset(size.width / 2, size.height / 2), radius! * progress!, _mPaint2!);

    canvas.drawCircle(Offset(size.width / 2, size.height / 2),
        innerRadius * progress!, _mPaint1!);
  }

  @override
  bool shouldRepaint(RBarCodeCircleIndicatorPainter oldDelegate) =>
      oldDelegate.progress != progress ||
      oldDelegate.outColor != outColor ||
      oldDelegate.innerColor != innerColor;
}
