#!/usr/bin/env dart
// ignore_for_file: avoid_print

import 'dart:io';

/// This script fixes the flutter.compileSdkVersion unresolved reference issue
/// by replacing flutter.compileSdkVersion with hardcoded SDK version (36)
/// in all third-party Flutter plugins in pub-cache.
///
/// Usage: dart tool/fix_flutter_sdk_references.dart

const String compileSdkVersion = '36';
const String targetSdkVersion = '36';
const String minSdkVersion = '24';
const String ndkVersion = '"28.2.13676358"';

final List<Replacement> replacements = [
  Replacement(
    pattern: RegExp(r'flutter\.compileSdkVersion'),
    replacement: compileSdkVersion,
    description: 'flutter.compileSdkVersion -> $compileSdkVersion',
  ),
  Replacement(
    pattern: RegExp(r'flutter\.targetSdkVersion'),
    replacement: targetSdkVersion,
    description: 'flutter.targetSdkVersion -> $targetSdkVersion',
  ),
  Replacement(
    pattern: RegExp(r'flutter\.minSdkVersion'),
    replacement: minSdkVersion,
    description: 'flutter.minSdkVersion -> $minSdkVersion',
  ),
  Replacement(
    pattern: RegExp(r'flutter\.ndkVersion'),
    replacement: ndkVersion,
    description: 'flutter.ndkVersion -> $ndkVersion',
  ),
];

class Replacement {
  final RegExp pattern;
  final String replacement;
  final String description;

  Replacement({
    required this.pattern,
    required this.replacement,
    required this.description,
  });
}

Future<void> main() async {
  final pubCacheDir = Directory('${Platform.environment['HOME']}/.pub-cache/hosted/pub.flutter-io.cn');

  if (!pubCacheDir.existsSync()) {
    print('Error: Pub cache directory not found: ${pubCacheDir.path}');
    exit(1);
  }

  print('Scanning for Flutter plugins in: ${pubCacheDir.path}');
  print('');

  int filesModified = 0;
  int replacementsMade = 0;

  await for (final entity in pubCacheDir.list(recursive: true)) {
    if (entity is File) {
      final path = entity.path;
      if (path.endsWith('/android/build.gradle') || path.endsWith('/android/build.gradle.kts')) {
        final content = await entity.readAsString();
        var newContent = content;
        var fileModified = false;

        for (final replacement in replacements) {
          if (replacement.pattern.hasMatch(newContent)) {
            newContent = newContent.replaceAll(replacement.pattern, replacement.replacement);
            fileModified = true;
            replacementsMade++;
            print('  ${replacement.description} in ${path.replaceFirst(pubCacheDir.path, '')}');
          }
        }

        if (fileModified) {
          // Add a comment at the top of the file to indicate it was modified
          if (!newContent.contains('// FIXED: flutter.compileSdkVersion replaced with hardcoded value')) {
            newContent = '// FIXED: flutter.compileSdkVersion replaced with hardcoded value\n// This is needed when Flutter module is integrated as a library in Android\n$newContent';
          }
          await entity.writeAsString(newContent);
          filesModified++;
        }
      }
    }
  }

  print('');
  print('========================================');
  print('Fix complete!');
  print('Files modified: $filesModified');
  print('Total replacements: $replacementsMade');
  print('========================================');
}
