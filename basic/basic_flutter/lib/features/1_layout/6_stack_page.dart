import 'package:flutter/material.dart';

/// Stack 堆叠布局示例页面
class StackPage extends StatelessWidget {
  const StackPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Stack 堆叠布局')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildSectionTitle('1. 基础 Stack'),
            _buildBasicStack(),
            const SizedBox(height: 24),
            _buildSectionTitle('2. Alignment 对齐'),
            _buildAlignmentDemo(),
            const SizedBox(height: 24),
            _buildSectionTitle('3. Stack 大小'),
            _buildStackSizeDemo(),
            const SizedBox(height: 24),
            _buildSectionTitle('4. 叠加效果'),
            _buildOverlayDemo(),
            const SizedBox(height: 24),
            _buildSectionTitle('5. 实际应用 - 图片卡片'),
            _buildCardDemo(),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Text(
        title,
        style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.blue),
      ),
    );
  }

  // 基础 Stack 示例
  Widget _buildBasicStack() {
    return Center(
      child: Container(
        color: Colors.grey.shade200,
        width: 200,
        height: 150,
        child: Stack(
          children: [
            Container(width: 200, height: 150, color: Colors.red),
            Container(width: 150, height: 100, color: Colors.green),
            Container(width: 100, height: 50, color: Colors.blue),
          ],
        ),
      ),
    );
  }

  // Alignment 对齐示例
  Widget _buildAlignmentDemo() {
    return Wrap(
      spacing: 8,
      runSpacing: 8,
      children: [
        _buildAlignmentItem('topLeft', Alignment.topLeft),
        _buildAlignmentItem('topCenter', Alignment.topCenter),
        _buildAlignmentItem('topRight', Alignment.topRight),
        _buildAlignmentItem('centerLeft', Alignment.centerLeft),
        _buildAlignmentItem('center', Alignment.center),
        _buildAlignmentItem('centerRight', Alignment.centerRight),
        _buildAlignmentItem('bottomLeft', Alignment.bottomLeft),
        _buildAlignmentItem('bottomCenter', Alignment.bottomCenter),
        _buildAlignmentItem('bottomRight', Alignment.bottomRight),
      ],
    );
  }

  Widget _buildAlignmentItem(String label, Alignment alignment) {
    return Column(
      children: [
        Text(label, style: const TextStyle(fontSize: 10, color: Colors.grey)),
        Container(
          width: 80,
          height: 60,
          color: Colors.grey.shade200,
          child: Stack(
            alignment: alignment,
            children: [
              Container(width: 30, height: 20, color: Colors.blue),
            ],
          ),
        ),
      ],
    );
  }

  // Stack 大小示例
  Widget _buildStackSizeDemo() {
    return Column(
      children: [
        _buildStackSizeItem('fit: loose (默认)', StackFit.loose),
        const SizedBox(height: 8),
        _buildStackSizeItem('fit: expand', StackFit.expand),
        const SizedBox(height: 8),
        _buildStackSizeItem('fit: passthrough', StackFit.passthrough),
      ],
    );
  }

  Widget _buildStackSizeItem(String label, StackFit fit) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
        Container(
          color: Colors.grey.shade200,
          width: double.infinity,
          height: 80,
          child: Stack(
            fit: fit,
            children: [
              Container(color: Colors.red.withOpacity(0.3)),
              Container(width: 50, height: 50, color: Colors.green),
            ],
          ),
        ),
      ],
    );
  }

  // 叠加效果示例
  Widget _buildOverlayDemo() {
    return Center(
      child: Container(
        width: 200,
        height: 120,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(8),
          boxShadow: [BoxShadow(color: Colors.black.withOpacity(0.2), blurRadius: 8)],
        ),
        child: Stack(
          children: [
            // 底层图片背景
            Container(
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(8),
                gradient: LinearGradient(
                  colors: [Colors.blue.shade300, Colors.purple.shade300],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
              ),
            ),
            // 半透明遮罩
            Container(
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(8),
                color: Colors.black.withOpacity(0.2),
              ),
            ),
            // 文字内容
            const Center(
              child: Text(
                'Stack\n叠加效果',
                textAlign: TextAlign.center,
                style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold),
              ),
            ),
            // 角标
            Positioned(
              top: 8,
              right: 8,
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                decoration: BoxDecoration(
                  color: Colors.red,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: const Text('NEW', style: TextStyle(color: Colors.white, fontSize: 10)),
              ),
            ),
          ],
        ),
      ),
    );
  }

  // 实际应用 - 图片卡片
  Widget _buildCardDemo() {
    return Center(
      child: Container(
        width: 250,
        height: 150,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(12),
          boxShadow: [BoxShadow(color: Colors.black.withOpacity(0.2), blurRadius: 8, offset: const Offset(0, 4))],
        ),
        child: ClipRRect(
          borderRadius: BorderRadius.circular(12),
          child: Stack(
            children: [
              // 背景色块模拟图片
              Container(
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    colors: [Colors.teal.shade400, Colors.cyan.shade400],
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
                ),
              ),
              // 渐变遮罩
              Positioned(
                bottom: 0,
                left: 0,
                right: 0,
                child: Container(
                  height: 60,
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      colors: [Colors.transparent, Colors.black.withOpacity(0.7)],
                      begin: Alignment.topCenter,
                      end: Alignment.bottomCenter,
                    ),
                  ),
                ),
              ),
              // 内容文字
              const Positioned(
                bottom: 12,
                left: 12,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Stack Layout', style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold)),
                    Text('堆叠布局示例', style: TextStyle(color: Colors.white70, fontSize: 12)),
                  ],
                ),
              ),
              // 收藏按钮
              const Positioned(
                top: 8,
                right: 8,
                child: Icon(Icons.favorite, color: Colors.red, size: 24),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
