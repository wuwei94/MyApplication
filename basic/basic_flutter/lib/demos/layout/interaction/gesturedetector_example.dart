import 'package:flutter/material.dart';

/// GestureDetector Example
/// Demonstrates various gesture detection capabilities
class GestureDetectorDemoPage extends StatelessWidget {
  const GestureDetectorDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return GestureDetectorDemoView(title: title);
  }
}

class GestureDetectorDemoView extends StatefulWidget {
  const GestureDetectorDemoView({super.key, required this.title});

  final String title;

  @override
  State<GestureDetectorDemoView> createState() => _GestureDetectorDemoViewState();
}

class _GestureDetectorDemoViewState extends State<GestureDetectorDemoView> {
  String _gesture = 'No gesture detected';
  double _scale = 1.0;
  double _rotation = 0.0;
  Offset _position = Offset.zero;

  void _updateGesture(String gesture) {
    setState(() {
      _gesture = gesture;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildSectionTitle('Gesture Status'),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.blue.shade50,
                borderRadius: BorderRadius.circular(8),
              ),
              child: Text(_gesture, style: const TextStyle(fontSize: 16)),
            ),
            const SizedBox(height: 24),
            _buildSectionTitle('Tap Gestures'),
            _buildTapGestures(),
            const SizedBox(height: 24),
            _buildSectionTitle('Pan & Drag'),
            _buildPanGesture(),
            const SizedBox(height: 24),
            _buildSectionTitle('Scale & Rotate'),
            _buildScaleRotate(),
            const SizedBox(height: 24),
            _buildSectionTitle('Long Press'),
            _buildLongPress(),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionTitle(String text) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Text(
        text,
        style: const TextStyle(
          fontSize: 18,
          fontWeight: FontWeight.bold,
          color: Colors.blue,
        ),
      ),
    );
  }

  Widget _buildTapGestures() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceAround,
      children: [
        GestureDetector(
          onTap: () => _updateGesture('Single Tap'),
          onDoubleTap: () => _updateGesture('Double Tap'),
          child: Container(
            width: 100,
            height: 100,
            decoration: BoxDecoration(
              color: Colors.green,
              borderRadius: BorderRadius.circular(8),
            ),
            child: const Center(
              child: Text(
                'Tap/Double\nTap',
                textAlign: TextAlign.center,
                style: TextStyle(color: Colors.white),
              ),
            ),
          ),
        ),
        GestureDetector(
          onTapDown: (_) => _updateGesture('Tap Down'),
          onTapUp: (_) => _updateGesture('Tap Up'),
          onTapCancel: () => _updateGesture('Tap Cancel'),
          child: Container(
            width: 100,
            height: 100,
            decoration: BoxDecoration(
              color: Colors.orange,
              borderRadius: BorderRadius.circular(8),
            ),
            child: const Center(
              child: Text(
                'Tap Down/Up',
                textAlign: TextAlign.center,
                style: TextStyle(color: Colors.white),
              ),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildPanGesture() {
    return GestureDetector(
      onPanUpdate: (details) {
        setState(() {
          _position += details.delta;
        });
        _updateGesture(
          'Pan: dx=${_position.dx.toStringAsFixed(1)}, dy=${_position.dy.toStringAsFixed(1)}',
        );
      },
      onPanEnd: (_) {
        setState(() {
          _position = Offset.zero;
        });
      },
      child: Container(
        width: double.infinity,
        height: 150,
        decoration: BoxDecoration(
          color: Colors.purple.shade100,
          borderRadius: BorderRadius.circular(8),
          border: Border.all(color: Colors.purple),
        ),
        child: Center(
          child: Transform.translate(
            offset: _position,
            child: Container(
              width: 80,
              height: 80,
              decoration: BoxDecoration(
                color: Colors.purple,
                borderRadius: BorderRadius.circular(8),
              ),
              child: const Icon(Icons.touch_app, color: Colors.white, size: 40),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildScaleRotate() {
    return GestureDetector(
      onScaleUpdate: (details) {
        setState(() {
          _scale = details.scale;
          _rotation = details.rotation;
        });
        _updateGesture(
          'Scale: ${_scale.toStringAsFixed(2)}, Rotation: ${_rotation.toStringAsFixed(2)}',
        );
      },
      onScaleEnd: (_) {
        setState(() {
          _scale = 1.0;
          _rotation = 0.0;
        });
      },
      child: Container(
        width: double.infinity,
        height: 200,
        decoration: BoxDecoration(
          color: Colors.teal.shade100,
          borderRadius: BorderRadius.circular(8),
          border: Border.all(color: Colors.teal),
        ),
        child: Center(
          child: Transform.scale(
            scale: _scale,
            child: Transform.rotate(
              angle: _rotation,
              child: Container(
                width: 100,
                height: 100,
                decoration: BoxDecoration(
                  color: Colors.teal,
                  borderRadius: BorderRadius.circular(8),
                ),
                child: const Icon(
                  Icons.crop_rotate,
                  color: Colors.white,
                  size: 50,
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildLongPress() {
    return GestureDetector(
      onLongPress: () => _updateGesture('Long Press'),
      onLongPressStart: (_) => _updateGesture('Long Press Start'),
      onLongPressEnd: (_) => _updateGesture('Long Press End'),
      onLongPressMoveUpdate: (details) => _updateGesture('Long Press Move'),
      child: Container(
        width: double.infinity,
        height: 100,
        decoration: BoxDecoration(
          color: Colors.red.shade100,
          borderRadius: BorderRadius.circular(8),
          border: Border.all(color: Colors.red),
        ),
        child: const Center(
          child: Text('Long Press Here', style: TextStyle(fontSize: 18)),
        ),
      ),
    );
  }
}
