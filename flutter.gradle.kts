val flutterModulePath = "basic/basic_flutter"
val flutterInclude = File(settingsDir, "$flutterModulePath/.android/include_flutter.groovy")

check(flutterInclude.exists()) {
    "Flutter module include script not found: ${flutterInclude.absolutePath}"
}

apply(from = flutterInclude)
