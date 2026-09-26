import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// Stack
/// Demonstrates layered layout with Stack
class StackDemoPage extends BasicLayoutPage {
  const StackDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<StackDemoPage> createState() => _StackDemoPageState();
}

class _StackDemoPageState extends BasicLayoutPageState<StackDemoPage> {
  @override
  Widget buildPreview() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          _buildSectionTitle('Basic Stack'),
          _buildBasicStack(),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('Stack Alignment'),
          _buildStackAlignment(),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('StackFit'),
          _buildStackFit(),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('Practical Badge Example'),
          _buildBadgeExample(),
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

  Widget _buildBasicStack() {
    return Container(
      width: double.infinity,
      height: 200,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.blue.shade50,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
        border: Border.all(color: Colors.blue.shade200),
      ),
      child: Stack(
        children: <Widget>[
          Container(width: 120, height: 120, color: Colors.blue),
          Container(width: 90, height: 90, color: Colors.green),
          Container(width: 60, height: 60, color: Colors.orange),
          Container(width: 30, height: 30, color: Colors.red),
        ],
      ),
    );
  }

  Widget _buildStackAlignment() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.green.shade50,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
        border: Border.all(color: Colors.green.shade200),
      ),
      child: Column(
        children: <Widget>[
          _buildAlignmentStack('topLeft', Alignment.topLeft),
          const SizedBox(height: 8),
          _buildAlignmentStack('center', Alignment.center),
          const SizedBox(height: 8),
          _buildAlignmentStack('bottomRight', Alignment.bottomRight),
        ],
      ),
    );
  }

  Widget _buildAlignmentStack(String label, Alignment alignment) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
        Container(
          height: 80,
          color: Colors.green.shade100,
          child: Stack(
            alignment: alignment,
            children: <Widget>[
              Container(width: 50, height: 50, color: Colors.green),
              Container(width: 30, height: 30, color: Colors.green.shade800),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildStackFit() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.orange.shade50,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
        border: Border.all(color: Colors.orange.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          const Text('StackFit.expand:'),
          const SizedBox(height: 8),
          SizedBox(
            height: 100,
            child: Stack(
              fit: StackFit.expand,
              children: <Widget>[
                Container(color: Colors.orange.shade100),
                const Center(child: Text('Expanded to fill Stack')),
              ],
            ),
          ),
          const SizedBox(height: 16),
          const Text('StackFit.loose (default):'),
          const SizedBox(height: 8),
          SizedBox(
            height: 100,
            child: Stack(
              fit: StackFit.loose,
              children: <Widget>[
                Container(width: 80, height: 80, color: Colors.orange),
                const Positioned(
                  left: 90,
                  top: 10,
                  child: Text('Loose fit child'),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBadgeExample() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.purple.shade50,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
        border: Border.all(color: Colors.purple.shade200),
      ),
      child: Row(
        children: <Widget>[
          Stack(
            clipBehavior: Clip.none,
            children: <Widget>[
              Container(
                width: 64,
                height: 64,
                decoration: BoxDecoration(
                  color: Colors.purple,
                  borderRadius: BorderRadius.circular(BasicDemoDimens.cornerSmall),
                ),
                child: const Icon(Icons.person, color: Colors.white, size: 36),
              ),
              Positioned(
                right: -4,
                top: -4,
                child: Container(
                  padding: const EdgeInsets.all(4),
                  decoration: const BoxDecoration(
                    color: Colors.red,
                    shape: BoxShape.circle,
                  ),
                  child: const Text(
                    '3',
                    style: TextStyle(color: Colors.white, fontSize: 10),
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(width: 16),
          const Expanded(
            child: Text('Avatar with notification badge (Stack + Positioned)'),
          ),
        ],
      ),
    );
  }
}
