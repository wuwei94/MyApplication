import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// Wrap
/// Demonstrates the usage of Wrap widget for flow layout
class WrapDemoPage extends BasicLayoutPage {
  const WrapDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<WrapDemoPage> createState() => _WrapDemoPageState();
}

class _WrapDemoPageState extends BasicLayoutPageState<WrapDemoPage> {
  @override
  Widget buildPreview() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          _buildSectionTitle('Basic Wrap'),
          _buildBasicWrap(),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('Wrap with Spacing'),
          _buildWrapSpacing(),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('Wrap Alignment'),
          _buildWrapAlignment(),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('Vertical Wrap'),
          _buildVerticalWrap(),
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

  Widget _buildBasicWrap() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.blue.shade50,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
        border: Border.all(color: Colors.blue.shade200),
      ),
      child: Wrap(
        children: <Widget>[
          _buildChip('Flutter'),
          _buildChip('Dart'),
          _buildChip('Android'),
          _buildChip('iOS'),
          _buildChip('Web'),
          _buildChip('Desktop'),
          _buildChip('Mobile'),
          _buildChip('Cross Platform'),
        ],
      ),
    );
  }

  Widget _buildWrapSpacing() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.green.shade50,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
        border: Border.all(color: Colors.green.shade200),
      ),
      child: Wrap(
        spacing: 16,
        runSpacing: 12,
        children: <Widget>[
          _buildChip('Spacing'),
          _buildChip('Between'),
          _buildChip('Items'),
          _buildChip('Is'),
          _buildChip('16px'),
          _buildChip('Run'),
          _buildChip('Spacing'),
          _buildChip('12px'),
        ],
      ),
    );
  }

  Widget _buildWrapAlignment() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.orange.shade50,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
        border: Border.all(color: Colors.orange.shade200),
      ),
      child: Column(
        children: <Widget>[
          _buildAlignmentRow('start', WrapAlignment.start),
          const SizedBox(height: 8),
          _buildAlignmentRow('center', WrapAlignment.center),
          const SizedBox(height: 8),
          _buildAlignmentRow('end', WrapAlignment.end),
          const SizedBox(height: 8),
          _buildAlignmentRow('spaceBetween', WrapAlignment.spaceBetween),
        ],
      ),
    );
  }

  Widget _buildAlignmentRow(String label, WrapAlignment alignment) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
        Container(
          color: Colors.orange.shade100,
          child: Wrap(
            alignment: alignment,
            children: <Widget>[
              _buildSmallChip('A'),
              _buildSmallChip('B'),
              _buildSmallChip('C'),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildVerticalWrap() {
    return Container(
      width: double.infinity,
      height: 150,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.purple.shade50,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
        border: Border.all(color: Colors.purple.shade200),
      ),
      child: Wrap(
        direction: Axis.vertical,
        spacing: 8,
        children: <Widget>[
          _buildChip('1'),
          _buildChip('2'),
          _buildChip('3'),
          _buildChip('4'),
          _buildChip('5'),
          _buildChip('6'),
          _buildChip('7'),
          _buildChip('8'),
        ],
      ),
    );
  }

  Widget _buildChip(String label) {
    return Container(
      margin: const EdgeInsets.all(4),
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: Colors.blue.shade100,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.blue.shade300),
      ),
      child: Text(label, style: TextStyle(color: Colors.blue.shade800)),
    );
  }

  Widget _buildSmallChip(String label) {
    return Container(
      margin: const EdgeInsets.all(2),
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: Colors.orange.shade200,
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(label, style: const TextStyle(fontSize: 12)),
    );
  }
}
