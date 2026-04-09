import 'package:flutter/material.dart';

/// PageView Example
/// Demonstrates page swiping
class PageViewExample extends StatelessWidget {
  const PageViewExample({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return PageViewRoute(title: title);
  }
}

class PageViewRoute extends StatefulWidget {
  const PageViewRoute({super.key, required this.title});

  final String title;

  @override
  State<PageViewRoute> createState() => _PageViewRouteState();
}

class _PageViewRouteState extends State<PageViewRoute> {
  final PageController _controller = PageController();
  int _currentPage = 0;

  final List<Color> _colors = [
    Colors.red,
    Colors.green,
    Colors.blue,
    Colors.orange,
    Colors.purple,
  ];

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        actions: [
          Text('Page ${_currentPage + 1}/5'),
          const SizedBox(width: 16),
        ],
      ),
      body: Column(
        children: [
          Expanded(
            child: PageView.builder(
              controller: _controller,
              onPageChanged: (index) {
                setState(() {
                  _currentPage = index;
                });
              },
              itemCount: 5,
              itemBuilder: (context, index) {
                return Container(
                  color: _colors[index].withValues(alpha: 0.3),
                  child: Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(Icons.image, size: 100, color: _colors[index]),
                        const SizedBox(height: 20),
                        Text(
                          'Page ${index + 1}',
                          style: TextStyle(
                            fontSize: 32,
                            color: _colors[index],
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),
          ),
          Container(
            padding: const EdgeInsets.all(16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: List.generate(5, (index) {
                return Container(
                  width: 10,
                  height: 10,
                  margin: const EdgeInsets.symmetric(horizontal: 4),
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: _currentPage == index ? Colors.blue : Colors.grey,
                  ),
                );
              }),
            ),
          ),
        ],
      ),
    );
  }
}
