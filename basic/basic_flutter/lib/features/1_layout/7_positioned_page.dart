import 'package:flutter/material.dart';

/// Positioned 定位布局示例页面
class PositionedPage extends StatelessWidget {
  const PositionedPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Positioned 定位布局')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildSectionTitle('1. 基础 Positioned'),
            _buildBasicPositioned(),
            const SizedBox(height: 24),
            _buildSectionTitle('2. 四角定位'),
            _buildCornerPositioned(),
            const SizedBox(height: 24),
            _buildSectionTitle('3. 填充定位 (fill)'),
            _buildFillPositioned(),
            const SizedBox(height: 24),
            _buildSectionTitle('4. 相对定位 (PositionedDirectional)'),
            _buildDirectionalPositioned(),
            const SizedBox(height: 24),
            _buildSectionTitle('5. 实际应用 - 悬浮按钮'),
            _buildFabDemo(),
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

  // 基础 Positioned 示例
  Widget _buildBasicPositioned() {
    return Center(
      child: Container(
        color: Colors.grey.shade200,
        width: 250,
        height: 150,
        child: Stack(
          children: [
            Container(color: Colors.blue.withOpacity(0.2)),
            Positioned(
              left: 20,
              top: 20,
              child: Container(width: 50, height: 50, color: Colors.red),
            ),
            Positioned(
              right: 30,
              bottom: 40,
              child: Container(width: 60, height: 40, color: Colors.green),
            ),
          ],
        ),
      ),
    );
  }

  // 四角定位示例
  Widget _buildCornerPositioned() {
    return Center(
      child: Container(
        color: Colors.grey.shade200,
        width: 250,
        height: 150,
        child: Stack(
          children: [
            // 左上角
            Positioned(
              left: 10,
              top: 10,
              child: Container(
                width: 40,
                height: 40,
                color: Colors.red,
                alignment: Alignment.center,
                child: const Text('TL', style: TextStyle(color: Colors.white, fontSize: 10)),
              ),
            ),
            // 右上角
            Positioned(
              right: 10,
              top: 10,
              child: Container(
                width: 40,
                height: 40,
                color: Colors.green,
                alignment: Alignment.center,
                child: const Text('TR', style: TextStyle(color: Colors.white, fontSize: 10)),
              ),
            ),
            // 左下角
            Positioned(
              left: 10,
              bottom: 10,
              child: Container(
                width: 40,
                height: 40,
                color: Colors.blue,
                alignment: Alignment.center,
                child: const Text('BL', style: TextStyle(color: Colors.white, fontSize: 10)),
              ),
            ),
            // 右下角
            Positioned(
              right: 10,
              bottom: 10,
              child: Container(
                width: 40,
                height: 40,
                color: Colors.orange,
                alignment: Alignment.center,
                child: const Text('BR', style: TextStyle(color: Colors.white, fontSize: 10)),
              ),
            ),
            // 中心
            Positioned(
              left: 0,
              right: 0,
              top: 0,
              bottom: 0,
              child: Center(
                child: Container(
                  width: 50,
                  height: 30,
                  color: Colors.purple,
                  alignment: Alignment.center,
                  child: const Text('Center', style: TextStyle(color: Colors.white, fontSize: 10)),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  // 填充定位示例
  Widget _buildFillPositioned() {
    return Center(
      child: Container(
        color: Colors.grey.shade200,
        width: 250,
        height: 150,
        child: Stack(
          children: [
            // 背景图
            Container(
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  colors: [Colors.blue.shade300, Colors.purple.shade300],
                ),
              ),
            ),
            // 顶部填充条
            Positioned(
              left: 0,
              right: 0,
              top: 0,
              height: 30,
              child: Container(
                color: Colors.black.withOpacity(0.5),
                alignment: Alignment.center,
                child: const Text('顶部标题栏', style: TextStyle(color: Colors.white)),
              ),
            ),
            // 底部填充条
            Positioned(
              left: 0,
              right: 0,
              bottom: 0,
              height: 30,
              child: Container(
                color: Colors.black.withOpacity(0.5),
                alignment: Alignment.center,
                child: const Text('底部工具栏', style: TextStyle(color: Colors.white)),
              ),
            ),
          ],
        ),
      ),
    );
  }

  // PositionedDirectional 示例
  Widget _buildDirectionalPositioned() {
    return Center(
      child: Container(
        color: Colors.grey.shade200,
        width: 250,
        height: 150,
        child: Stack(
          children: [
            Container(color: Colors.teal.withOpacity(0.2)),
            // start 表示文本开始方向（LTR为左，RTL为右）
            PositionedDirectional(
              start: 20,
              top: 20,
              child: Container(
                width: 60,
                height: 40,
                color: Colors.red,
                alignment: Alignment.center,
                child: const Text('start', style: TextStyle(color: Colors.white, fontSize: 10)),
              ),
            ),
            // end 表示文本结束方向（LTR为右，RTL为左）
            PositionedDirectional(
              end: 20,
              bottom: 20,
              child: Container(
                width: 60,
                height: 40,
                color: Colors.green,
                alignment: Alignment.center,
                child: const Text('end', style: TextStyle(color: Colors.white, fontSize: 10)),
              ),
            ),
          ],
        ),
      ),
    );
  }

  // 悬浮按钮示例
  Widget _buildFabDemo() {
    return Center(
      child: Container(
        width: 250,
        height: 180,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(12),
          boxShadow: [BoxShadow(color: Colors.black.withOpacity(0.2), blurRadius: 8)],
        ),
        child: ClipRRect(
          borderRadius: BorderRadius.circular(12),
          child: Stack(
            children: [
              // 内容区域
              Container(
                color: Colors.grey.shade100,
                padding: const EdgeInsets.all(16),
                child: const Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('内容区域', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                    SizedBox(height: 8),
                    Text('这是一些示例文本内容...'),
                  ],
                ),
              ),
              // 右上角关闭按钮
              Positioned(
                top: 8,
                right: 8,
                child: Container(
                  width: 24,
                  height: 24,
                  decoration: const BoxDecoration(
                    color: Colors.red,
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(Icons.close, color: Colors.white, size: 16),
                ),
              ),
              // 左下角头像
              Positioned(
                left: 16,
                bottom: 16,
                child: Container(
                  width: 40,
                  height: 40,
                  decoration: const BoxDecoration(
                    color: Colors.blue,
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(Icons.person, color: Colors.white),
                ),
              ),
              // 右下角悬浮按钮
              Positioned(
                right: 16,
                bottom: 16,
                child: Container(
                  width: 48,
                  height: 48,
                  decoration: BoxDecoration(
                    color: Colors.green,
                    shape: BoxShape.circle,
                    boxShadow: [BoxShadow(color: Colors.black.withOpacity(0.3), blurRadius: 4, offset: const Offset(0, 2))],
                  ),
                  child: const Icon(Icons.add, color: Colors.white),
                ),
              ),
              // 顶部居中标签
              Positioned(
                top: 8,
                left: 0,
                right: 0,
                child: Center(
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                    decoration: BoxDecoration(
                      color: Colors.orange,
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: const Text('标签', style: TextStyle(color: Colors.white, fontSize: 10)),
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
