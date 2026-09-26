import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// Positioned
/// Demonstrates absolute positioning inside Stack with Positioned
class PositionedDemoPage extends BasicLayoutPage {
  const PositionedDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<PositionedDemoPage> createState() =>
      _PositionedDemoPageState();
}

class _PositionedDemoPageState extends BasicLayoutPageState<PositionedDemoPage> {
  @override
  Widget buildPreview() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          _buildSectionTitle('Basic Positioned'),
          _buildBasicPositioned(),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('Positioned Edges'),
          _buildPositionedEdges(),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('Positioned.fill'),
          _buildPositionedFill(),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('Practical Corner Tag'),
          _buildCornerTag(),
        ],
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

  Widget _buildBasicPositioned() {
    return Container(
      width: double.infinity,
      height: 180,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.blue.shade50,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
        border: Border.all(color: Colors.blue.shade200),
      ),
      child: Stack(
        children: <Widget>[
          Container(
            width: double.infinity,
            height: double.infinity,
            color: Colors.blue.shade100,
          ),
          Positioned(
            left: 20,
            top: 20,
            child: _buildDot('left:20 top:20'),
          ),
          Positioned(
            right: 20,
            top: 20,
            child: _buildDot('right:20 top:20'),
          ),
          Positioned(
            left: 20,
            bottom: 20,
            child: _buildDot('left:20 bottom:20'),
          ),
          Positioned(
            right: 20,
            bottom: 20,
            child: _buildDot('right:20 bottom:20'),
          ),
        ],
      ),
    );
  }

  Widget _buildDot(String label) {
    return Container(
      padding: const EdgeInsets.all(8),
      decoration: BoxDecoration(
        color: Colors.blue,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerSmall),
      ),
      child: Text(
        label,
        style: const TextStyle(color: Colors.white, fontSize: 10),
      ),
    );
  }

  Widget _buildPositionedEdges() {
    return Container(
      width: double.infinity,
      height: 140,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.green.shade50,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
        border: Border.all(color: Colors.green.shade200),
      ),
      child: Stack(
        children: <Widget>[
          Container(
            width: double.infinity,
            height: double.infinity,
            color: Colors.green.shade100,
          ),
          Positioned(
            left: 0,
            right: 0,
            top: 10,
            child: Container(
              height: 30,
              color: Colors.green,
              child: const Center(
                child: Text(
                  'left:0 right:0 (stretched horizontally)',
                  style: TextStyle(color: Colors.white, fontSize: 12),
                ),
              ),
            ),
          ),
          Positioned(
            top: 0,
            bottom: 0,
            right: 10,
            child: Container(
              width: 30,
              color: Colors.green.shade700,
              child: const Center(
                child: RotatedBox(
                  quarterTurns: 1,
                  child: Text(
                    'top:0 bottom:0',
                    style: TextStyle(color: Colors.white, fontSize: 10),
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildPositionedFill() {
    return Container(
      width: double.infinity,
      height: 140,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.orange.shade50,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
        border: Border.all(color: Colors.orange.shade200),
      ),
      child: Stack(
        children: <Widget>[
          Container(
            width: double.infinity,
            height: double.infinity,
            color: Colors.orange.shade100,
          ),
          Positioned.fill(
            child: Container(
              color: Colors.black.withValues(alpha: 0.3),
              child: const Center(
                child: Text(
                  'Positioned.fill',
                  style: TextStyle(color: Colors.white, fontSize: 16),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCornerTag() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.purple.shade50,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
        border: Border.all(color: Colors.purple.shade200),
      ),
      child: Center(
        child: Container(
          width: 160,
          height: 120,
          decoration: BoxDecoration(
            color: Colors.purple.shade100,
            borderRadius: BorderRadius.circular(BasicDemoDimens.cornerSmall),
          ),
          child: Stack(
            clipBehavior: Clip.none,
            children: <Widget>[
              const Center(child: Text('Product Card')),
              Positioned(
                right: -8,
                top: -8,
                child: Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 8,
                    vertical: 4,
                  ),
                  decoration: BoxDecoration(
                    color: Colors.red,
                    borderRadius: BorderRadius.circular(
                      BasicDemoDimens.cornerSmall,
                    ),
                  ),
                  child: const Text(
                    'HOT',
                    style: TextStyle(color: Colors.white, fontSize: 10),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
