import 'package:flutter/material.dart';

/// Flex 弹性布局示例页面
class FlexPage extends StatelessWidget {
  const FlexPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Flex 弹性布局')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildSectionTitle('1. Flex 水平方向 (direction: Axis.horizontal)'),
            _buildHorizontalFlex(),
            const SizedBox(height: 24),
            _buildSectionTitle('2. Flex 垂直方向 (direction: Axis.vertical)'),
            _buildVerticalFlex(),
            const SizedBox(height: 24),
            _buildSectionTitle('3. Flex + Expanded'),
            _buildFlexWithExpanded(),
            const SizedBox(height: 24),
            _buildSectionTitle('4. Flex 动态方向'),
            _buildDynamicDirectionFlex(),
            const SizedBox(height: 24),
            _buildSectionTitle('5. Flex 综合示例'),
            _buildComprehensiveFlex(),
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

  // 水平方向 Flex
  Widget _buildHorizontalFlex() {
    return Container(
      color: Colors.grey.shade200,
      height: 80,
      child: const Flex(
        direction: Axis.horizontal,
        children: [
          Expanded(child: ColoredBox(color: Colors.red, child: Center(child: Text('1', style: TextStyle(color: Colors.white))))),
          Expanded(child: ColoredBox(color: Colors.green, child: Center(child: Text('2', style: TextStyle(color: Colors.white))))),
          Expanded(child: ColoredBox(color: Colors.blue, child: Center(child: Text('3', style: TextStyle(color: Colors.white))))),
        ],
      ),
    );
  }

  // 垂直方向 Flex
  Widget _buildVerticalFlex() {
    return Container(
      color: Colors.grey.shade200,
      height: 150,
      child: const Flex(
        direction: Axis.vertical,
        children: [
          Expanded(flex: 1, child: ColoredBox(color: Colors.red, child: Center(child: Text('1', style: TextStyle(color: Colors.white))))),
          Expanded(flex: 2, child: ColoredBox(color: Colors.green, child: Center(child: Text('2', style: TextStyle(color: Colors.white))))),
          Expanded(flex: 1, child: ColoredBox(color: Colors.blue, child: Center(child: Text('1', style: TextStyle(color: Colors.white))))),
        ],
      ),
    );
  }

  // Flex + Expanded
  Widget _buildFlexWithExpanded() {
    return Container(
      color: Colors.grey.shade200,
      height: 100,
      child: Flex(
        direction: Axis.horizontal,
        children: [
          Container(width: 60, color: Colors.orange, child: const Center(child: Text('固定', style: TextStyle(color: Colors.white, fontSize: 10)))),
          const Expanded(
            flex: 2,
            child: ColoredBox(color: Colors.red, child: Center(child: Text('Expanded 2', style: TextStyle(color: Colors.white)))),
          ),
          const Expanded(
            flex: 1,
            child: ColoredBox(color: Colors.green, child: Center(child: Text('Expanded 1', style: TextStyle(color: Colors.white)))),
          ),
        ],
      ),
    );
  }

  // 动态方向 Flex
  Widget _buildDynamicDirectionFlex() {
    return LayoutBuilder(
      builder: (context, constraints) {
        final isWide = constraints.maxWidth > 400;
        return Container(
          color: Colors.grey.shade200,
          padding: const EdgeInsets.all(8),
          child: Flex(
            direction: isWide ? Axis.horizontal : Axis.vertical,
            children: [
              Container(
                width: isWide ? 80 : double.infinity,
                height: 50,
                color: Colors.red,
                alignment: Alignment.center,
                child: Text(isWide ? '横向' : '纵向', style: const TextStyle(color: Colors.white)),
              ),
              const SizedBox(width: 8, height: 8),
              Container(
                width: isWide ? 80 : double.infinity,
                height: 50,
                color: Colors.green,
                alignment: Alignment.center,
                child: Text(isWide ? '布局' : '布局', style: const TextStyle(color: Colors.white)),
              ),
              const SizedBox(width: 8, height: 8),
              Container(
                width: isWide ? 80 : double.infinity,
                height: 50,
                color: Colors.blue,
                alignment: Alignment.center,
                child: Text(isWide ? '自适应' : '自适应', style: const TextStyle(color: Colors.white)),
              ),
            ],
          ),
        );
      },
    );
  }

  // 综合示例
  Widget _buildComprehensiveFlex() {
    return Container(
      color: Colors.grey.shade200,
      height: 200,
      child: Flex(
        direction: Axis.vertical,
        children: [
          // 头部
          Container(
            height: 40,
            color: Colors.purple,
            alignment: Alignment.center,
            child: const Text('Header', style: TextStyle(color: Colors.white)),
          ),
          // 内容区域 - 水平 Flex
          Expanded(
            child: Flex(
              direction: Axis.horizontal,
              children: [
                // 侧边栏
                Container(
                  width: 60,
                  color: Colors.orange,
                  alignment: Alignment.center,
                  child: const Text('Sidebar', style: TextStyle(color: Colors.white, fontSize: 10)),
                ),
                // 主内容
                const Expanded(
                  child: ColoredBox(
                    color: Colors.teal,
                    child: Center(child: Text('Main Content', style: TextStyle(color: Colors.white))),
                  ),
                ),
              ],
            ),
          ),
          // 底部
          Container(
            height: 30,
            color: Colors.indigo,
            alignment: Alignment.center,
            child: const Text('Footer', style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );
  }
}
