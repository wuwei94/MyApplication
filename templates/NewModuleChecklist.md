# 新模块脚手架接入清单 (8 步闭环)

新建功能模块或库模块时，必须按此清单顺序依次完成配置：

1. **`settings.gradle.kts` 注册**
   ```kotlin
   include(":modules:module_<模块名>")
   ```
2. **选择精准 Convention 插件 (`build.gradle.kts`)**
   ```kotlin
   plugins {
       alias(libs.plugins.my.android.feature) // 或 my.android.feature.compose / my.android.library
   }
   android {
       namespace = "com.example.william.my.module.<模块名>"
       resourcePrefix = "<模块名>_"
   }
   ```
3. **注册主入口路由 (`basic/basic_shared/.../RouterPath.kt`)**
   ```kotlin
   object <模块名大驼峰> {
       const val Main = "/<模块名>/main"
       const val Sample = "/<模块名>/sample"
   }
   ```
4. **配置清单文件 (`src/main/AndroidManifest.xml`)**
   ```xml
   <manifest xmlns:android="http://schemas.android.com/apk/res/android">
       <application>
           <activity
               android:name=".activity.<模块名大驼峰>MainActivity"
               android:exported="false" />
       </application>
   </manifest>
   ```
5. **创建入口 Activity**
   - 继承 `RouterRecyclerActivity` 展现子项列表；
   - 标注 `@Route(path = RouterPath.<模块名大驼峰>.Main)`。
6. **挂载至 10 大分类导航 (`Category.kt` / `CategoryActivity.kt`)**
   - 将主入口登记到对应的业务领域分类。
7. **同步架构文档**
   - 更新 `docs/05-catalog/modules.md`；
   - 更新根目录 `README.md` 中的模块清单。
8. **工程规范校验**
   - 执行 `./gradlew :modules:module_<模块名>:spotlessApply`；
   - 运行 `./gradlew :modules:module_<模块名>:assembleDebug` 验证构建。
