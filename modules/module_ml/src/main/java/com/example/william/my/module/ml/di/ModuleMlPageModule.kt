package com.example.william.my.module.ml.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.ml.MlMainActivity
import com.example.william.my.module.ml.asr.TFLiteSpeechRecognitionActivity
import com.example.william.my.module.ml.digit.TFLiteDigitClassifierActivity
import com.example.william.my.module.ml.image.TFLiteImageClassificationActivity
import com.example.william.my.module.ml.segmentation.TFLiteImageSegmentationActivity
import com.example.william.my.module.ml.text.TFLiteTextClassificationActivity
import com.example.william.my.module.ml.tts.TFLiteTextToSpeechActivity
import com.example.william.my.module.ml.tts.TFLiteTextToSpeechLmActivity
import com.example.william.my.module.ml.tts.TFLiteTextToSpeechStreamingActivity
import com.example.william.my.module.ml.zsc.TFLiteZeroShotClassificationActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_ml 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleMlPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Ml.SpeechRecognition)
    fun provideTFLiteSpeechRecognitionActivity(): Class<out Activity> = TFLiteSpeechRecognitionActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Ml.DigitClassifier)
    fun provideTFLiteDigitClassifierActivity(): Class<out Activity> = TFLiteDigitClassifierActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Ml.ImageClassification)
    fun provideTFLiteImageClassificationActivity(): Class<out Activity> = TFLiteImageClassificationActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Ml.Main)
    fun provideMlMainActivity(): Class<out Activity> = MlMainActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Ml.ImageSegmentation)
    fun provideTFLiteImageSegmentationActivity(): Class<out Activity> = TFLiteImageSegmentationActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Ml.TextClassification)
    fun provideTFLiteTextClassificationActivity(): Class<out Activity> = TFLiteTextClassificationActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Ml.TextToSpeech)
    fun provideTFLiteTextToSpeechActivity(): Class<out Activity> = TFLiteTextToSpeechActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Ml.TextToSpeechLm)
    fun provideTFLiteTextToSpeechLmActivity(): Class<out Activity> = TFLiteTextToSpeechLmActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Ml.TextToSpeechStreaming)
    fun provideTFLiteTextToSpeechStreamingActivity(): Class<out Activity> = TFLiteTextToSpeechStreamingActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Ml.ZeroShotClassification)
    fun provideTFLiteZeroShotClassificationActivity(): Class<out Activity> = TFLiteZeroShotClassificationActivity::class.java
}
