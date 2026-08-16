import 'package:flutter/material.dart';

/// Align
/// Demonstrates various alignment methods of Align
class AlignDemoPage extends StatelessWidget {
  const AlignDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return AlignDemoView(title: title);
  }
}

class AlignDemoView extends StatelessWidget {
  const AlignDemoView({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildSectionTitle('9 Standard Alignment Types'),
            _buildStandardAlignments(),
            const SizedBox(height: 24),
            _buildSectionTitle('Alignment Coordinate System'),
            _buildAlignmentCoordinate(),
            const SizedBox(height: 24),
            _buildSectionTitle('FractionalOffset'),
            _buildFractionalOffset(),
            const SizedBox(height: 24),
            _buildSectionTitle('Align with Factor'),
            _buildAlignWithFactor(),
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

  Widget _buildStandardAlignments() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.blue.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.blue.shade200),
      ),
      child: Column(
        children: [
          _buildAlignmentRow([
            Alignment.topLeft,
            Alignment.topCenter,
            Alignment.topRight,
          ]),
          const SizedBox(height: 8),
          _buildAlignmentRow([
            Alignment.centerLeft,
            Alignment.center,
            Alignment.centerRight,
          ]),
          const SizedBox(height: 8),
          _buildAlignmentRow([
            Alignment.bottomLeft,
            Alignment.bottomCenter,
            Alignment.bottomRight,
          ]),
        ],
      ),
    );
  }

  Widget _buildAlignmentRow(List<Alignment> alignments) {
    return Row(
      children: alignments.map((alignment) {
        return Expanded(
          child: Container(
            height: 80,
            margin: const EdgeInsets.symmetric(horizontal: 4),
            color: Colors.blue.shade100,
            child: Align(
              alignment: alignment,
              child: Container(
                width: 30,
                height: 30,
                decoration: BoxDecoration(
                  color: Colors.blue,
                  borderRadius: BorderRadius.circular(4),
                ),
              ),
            ),
          ),
        );
      }).toList(),
    );
  }

  Widget _buildAlignmentCoordinate() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.green.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.green.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('Alignment(x, y) range: (-1, -1) to (1, 1)'),
          const SizedBox(height: 12),
          Container(
            height: 200,
            color: Colors.green.shade100,
            child: Stack(
              children: [
                // Center point
                Align(
                  alignment: Alignment.center,
                  child: Container(
                    width: 8,
                    height: 8,
                    decoration: const BoxDecoration(
                      color: Colors.red,
                      shape: BoxShape.circle,
                    ),
                  ),
                ),
                // Four corners
                Align(
                  alignment: const Alignment(-0.8, -0.8),
                  child: _buildCoordinateDot('(-0.8, -0.8)'),
                ),
                Align(
                  alignment: const Alignment(0.8, -0.8),
                  child: _buildCoordinateDot('(0.8, -0.8)'),
                ),
                Align(
                  alignment: const Alignment(-0.8, 0.8),
                  child: _buildCoordinateDot('(-0.8, 0.8)'),
                ),
                Align(
                  alignment: const Alignment(0.8, 0.8),
                  child: _buildCoordinateDot('(0.8, 0.8)'),
                ),
                // Custom position
                Align(
                  alignment: const Alignment(-0.5, 0.3),
                  child: _buildCoordinateDot(
                    '(-0.5, 0.3)',
                    color: Colors.orange,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCoordinateDot(String label, {Color color = Colors.green}) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          width: 16,
          height: 16,
          decoration: BoxDecoration(
            color: color,
            shape: BoxShape.circle,
            border: Border.all(color: Colors.white, width: 2),
          ),
        ),
        Text(label, style: const TextStyle(fontSize: 10)),
      ],
    );
  }

  Widget _buildFractionalOffset() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.orange.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.orange.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('FractionalOffset range: (0, 0) to (1, 1)'),
          const SizedBox(height: 12),
          Container(
            height: 150,
            color: Colors.orange.shade100,
            child: Stack(
              children: [
                Align(
                  alignment: const FractionalOffset(0.0, 0.0),
                  child: _buildFractionalDot('(0, 0)'),
                ),
                Align(
                  alignment: const FractionalOffset(1.0, 0.0),
                  child: _buildFractionalDot('(1, 0)'),
                ),
                Align(
                  alignment: const FractionalOffset(0.0, 1.0),
                  child: _buildFractionalDot('(0, 1)'),
                ),
                Align(
                  alignment: const FractionalOffset(1.0, 1.0),
                  child: _buildFractionalDot('(1, 1)'),
                ),
                Align(
                  alignment: const FractionalOffset(0.5, 0.5),
                  child: _buildFractionalDot('(0.5, 0.5)', color: Colors.red),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFractionalDot(String label, {Color color = Colors.orange}) {
    return Container(
      padding: const EdgeInsets.all(8),
      decoration: BoxDecoration(
        color: color,
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(
        label,
        style: const TextStyle(color: Colors.white, fontSize: 12),
      ),
    );
  }

  Widget _buildAlignWithFactor() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.purple.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.purple.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('widthFactor and heightFactor:'),
          const SizedBox(height: 12),
          Container(
            width: double.infinity,
            color: Colors.purple.shade100,
            child: Align(
              widthFactor: 0.5,
              heightFactor: 2,
              child: Container(
                width: 100,
                height: 50,
                color: Colors.purple,
                child: const Center(
                  child: Text(
                    'w:0.5, h:2',
                    style: TextStyle(color: Colors.white),
                  ),
                ),
              ),
            ),
          ),
          const SizedBox(height: 16),
          const Text('widthFactor: 0.3 (width is 0.3x of child)'),
          Container(
            width: double.infinity,
            color: Colors.purple.shade200,
            child: Align(
              widthFactor: 0.3,
              child: Container(
                width: 200,
                height: 40,
                color: Colors.purple.shade700,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
