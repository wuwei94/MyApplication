import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// PageView
/// Demonstrates page swiping
class PageViewDemoPage extends BasicLayoutPage {
  const PageViewDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<PageViewDemoPage> createState() =>
      _PageViewDemoPageState();
}

class _PageViewDemoPageState extends BasicLayoutPageState<PageViewDemoPage> {
  final PageController _controller = PageController();
  int _currentPage = 0;

  static const List<Color> _colors = <Color>[
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
  Widget buildPreview() {
    return Column(
      children: <Widget>[
        Padding(
          padding: const EdgeInsets.all(8),
          child: Text('Page ${_currentPage + 1}/5'),
        ),
        Expanded(
          child: PageView.builder(
            controller: _controller,
            onPageChanged: (int index) {
              setState(() {
                _currentPage = index;
              });
            },
            itemCount: 5,
            itemBuilder: (BuildContext context, int index) {
              return Container(
                color: _colors[index].withValues(alpha: 0.3),
                child: Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: <Widget>[
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
            children: List<Widget>.generate(5, (int index) {
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
    );
  }
}
