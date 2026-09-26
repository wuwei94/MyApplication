import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// Column
/// Demonstrates vertical layout with Column
class ColumnDemoPage extends BasicLayoutPage {
  const ColumnDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<ColumnDemoPage> createState() => _ColumnDemoPageState();
}

class _ColumnDemoPageState extends BasicLayoutPageState<ColumnDemoPage> {
  @override
  Widget buildPreview() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          _buildSectionTitle('Basic Column'),
          _buildBasicColumn(),
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

  Widget _buildBasicColumn() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.blue.shade50,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
        border: Border.all(color: Colors.blue.shade200),
      ),
      child: Column(
        children: <Widget>[
          _buildBox(Colors.red, 'A'),
          _buildBox(Colors.green, 'B'),
          _buildBox(Colors.blue, 'C'),
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
          _buildAlignmentColumn('start', MainAxisAlignment.start),
          const SizedBox(height: 8),
          _buildAlignmentColumn('center', MainAxisAlignment.center),
          const SizedBox(height: 8),
          _buildAlignmentColumn('end', MainAxisAlignment.end),
          const SizedBox(height: 8),
          _buildAlignmentColumn('spaceBetween', MainAxisAlignment.spaceBetween),
          const SizedBox(height: 8),
          _buildAlignmentColumn('spaceAround', MainAxisAlignment.spaceAround),
          const SizedBox(height: 8),
          _buildAlignmentColumn('spaceEvenly', MainAxisAlignment.spaceEvenly),
        ],
      ),
    );
  }

  Widget _buildAlignmentColumn(String label, MainAxisAlignment alignment) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
        Container(
          height: 100,
          color: Colors.green.shade100,
          child: Column(
            mainAxisAlignment: alignment,
            children: <Widget>[
              Container(width: 30, height: 20, color: Colors.green),
              Container(width: 30, height: 20, color: Colors.green.shade400),
              Container(width: 30, height: 20, color: Colors.green.shade800),
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
          _buildCrossColumn('start', CrossAxisAlignment.start),
          const SizedBox(height: 8),
          _buildCrossColumn('center', CrossAxisAlignment.center),
          const SizedBox(height: 8),
          _buildCrossColumn('end', CrossAxisAlignment.end),
        ],
      ),
    );
  }

  Widget _buildCrossColumn(String label, CrossAxisAlignment alignment) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
        Container(
          width: double.infinity,
          color: Colors.orange.shade100,
          child: Column(
            crossAxisAlignment: alignment,
            children: <Widget>[
              Container(width: 40, height: 30, color: Colors.orange),
              Container(width: 80, height: 30, color: Colors.orange.shade400),
              Container(width: 60, height: 30, color: Colors.orange.shade800),
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
            height: 150,
            color: Colors.purple.shade100,
            child: Column(
              children: <Widget>[
                _buildBox(Colors.purple, 'Fixed'),
                Expanded(
                  child: Container(
                    width: double.infinity,
                    margin: const EdgeInsets.symmetric(vertical: 4),
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
