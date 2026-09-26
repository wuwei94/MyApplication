import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// Row
/// Demonstrates horizontal layout with Row
class RowDemoPage extends BasicLayoutPage {
  const RowDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<RowDemoPage> createState() => _RowDemoPageState();
}

class _RowDemoPageState extends BasicLayoutPageState<RowDemoPage> {
  @override
  Widget buildPreview() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          _buildSectionTitle('Basic Row'),
          _buildBasicRow(),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('MainAxisAlignment'),
          _buildMainAxisAlignment(),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('CrossAxisAlignment'),
          _buildCrossAxisAlignment(),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('Expanded'),
          _buildExpanded(),
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

  Widget _buildBasicRow() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.blue.shade50,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
        border: Border.all(color: Colors.blue.shade200),
      ),
      child: Column(
        children: <Widget>[
          Row(
            children: <Widget>[
              _buildBox(Colors.red, 'A'),
              _buildBox(Colors.green, 'B'),
              _buildBox(Colors.blue, 'C'),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildMainAxisAlignment() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.green.shade50,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
        border: Border.all(color: Colors.green.shade200),
      ),
      child: Column(
        children: <Widget>[
          _buildAlignmentRow('start', MainAxisAlignment.start),
          const SizedBox(height: 8),
          _buildAlignmentRow('center', MainAxisAlignment.center),
          const SizedBox(height: 8),
          _buildAlignmentRow('end', MainAxisAlignment.end),
          const SizedBox(height: 8),
          _buildAlignmentRow('spaceBetween', MainAxisAlignment.spaceBetween),
          const SizedBox(height: 8),
          _buildAlignmentRow('spaceAround', MainAxisAlignment.spaceAround),
          const SizedBox(height: 8),
          _buildAlignmentRow('spaceEvenly', MainAxisAlignment.spaceEvenly),
        ],
      ),
    );
  }

  Widget _buildAlignmentRow(String label, MainAxisAlignment alignment) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
        Container(
          color: Colors.green.shade100,
          child: Row(
            mainAxisAlignment: alignment,
            children: <Widget>[
              Container(width: 30, height: 30, color: Colors.green),
              Container(width: 30, height: 30, color: Colors.green.shade400),
              Container(width: 30, height: 30, color: Colors.green.shade800),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildCrossAxisAlignment() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.orange.shade50,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
        border: Border.all(color: Colors.orange.shade200),
      ),
      child: Column(
        children: <Widget>[
          _buildCrossRow('start', CrossAxisAlignment.start),
          const SizedBox(height: 8),
          _buildCrossRow('center', CrossAxisAlignment.center),
          const SizedBox(height: 8),
          _buildCrossRow('end', CrossAxisAlignment.end),
        ],
      ),
    );
  }

  Widget _buildCrossRow(String label, CrossAxisAlignment alignment) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
        Container(
          height: 60,
          color: Colors.orange.shade100,
          child: Row(
            crossAxisAlignment: alignment,
            children: <Widget>[
              Container(width: 30, height: 20, color: Colors.orange),
              Container(width: 30, height: 40, color: Colors.orange.shade400),
              Container(width: 30, height: 30, color: Colors.orange.shade800),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildExpanded() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.purple.shade50,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
        border: Border.all(color: Colors.purple.shade200),
      ),
      child: Column(
        children: <Widget>[
          Container(
            color: Colors.purple.shade100,
            child: Row(
              children: <Widget>[
                _buildBox(Colors.purple, 'Fixed'),
                Expanded(
                  child: Container(
                    height: 50,
                    margin: const EdgeInsets.symmetric(horizontal: 8),
                    color: Colors.purple.shade400,
                    child: const Center(
                      child: Text(
                        'Expanded',
                        style: TextStyle(color: Colors.white),
                      ),
                    ),
                  ),
                ),
                _buildBox(Colors.purple.shade800, 'Fixed'),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBox(Color color, String text) {
    return Container(
      width: 50,
      height: 50,
      color: color,
      child: Center(
        child: Text(
          text,
          style: const TextStyle(color: Colors.white, fontSize: 12),
        ),
      ),
    );
  }
}
