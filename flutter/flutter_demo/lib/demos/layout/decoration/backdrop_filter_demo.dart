import 'dart:ui';

import 'package:flutter/material.dart';

/// BackdropFilter
/// Demonstrates blur and frosted glass effects
class BackdropFilterDemoPage extends StatelessWidget {
  const BackdropFilterDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return BackdropFilterDemoView(title: title);
  }
}

class BackdropFilterDemoView extends StatelessWidget {
  const BackdropFilterDemoView({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: Stack(
        fit: StackFit.expand,
        children: [
          // Background
          Container(
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: [
                  Colors.blue.shade300,
                  Colors.purple.shade300,
                  Colors.pink.shade300,
                ],
              ),
            ),
            child: Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.wb_sunny, size: 100, color: Colors.yellow),
                  const SizedBox(height: 20),
                  Text(
                    'Background Content',
                    style: TextStyle(
                      fontSize: 28,
                      color: Colors.white.withValues(alpha: 0.8),
                    ),
                  ),
                  const SizedBox(height: 40),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(Icons.star, size: 50, color: Colors.yellow.shade700),
                      const SizedBox(width: 20),
                      Icon(
                        Icons.favorite,
                        size: 50,
                        color: Colors.red.shade400,
                      ),
                      const SizedBox(width: 20),
                      Icon(
                        Icons.cloud,
                        size: 50,
                        color: Colors.white.withValues(alpha: 0.8),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
          // Frosted glass cards
          Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                _buildFrostedCard(
                  'Light Blur',
                  ImageFilter.blur(sigmaX: 5, sigmaY: 5),
                ),
                const SizedBox(height: 20),
                _buildFrostedCard(
                  'Heavy Blur',
                  ImageFilter.blur(sigmaX: 15, sigmaY: 15),
                ),
                const SizedBox(height: 20),
                _buildFrostedCard(
                  'Medium Blur',
                  ImageFilter.blur(sigmaX: 10, sigmaY: 10),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFrostedCard(String label, ImageFilter filter) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(16),
      child: BackdropFilter(
        filter: filter,
        child: Container(
          width: 280,
          padding: const EdgeInsets.all(20),
          decoration: BoxDecoration(
            color: Colors.white.withValues(alpha: 0.2),
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: Colors.white.withValues(alpha: 0.3)),
          ),
          child: Column(
            children: [
              Text(
                label,
                style: const TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  color: Colors.white,
                ),
              ),
              const SizedBox(height: 8),
              const Text(
                'Frosted Glass Effect',
                style: TextStyle(color: Colors.white70),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
