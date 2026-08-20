package kr.co.greenenv.englishreview

import android.app.Activity
import android.media.AudioAttributes
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var webView: WebView
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var pendingText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(applicationContext, this)

        webView = WebView(this)
        setContentView(webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false

        webView.addJavascriptInterface(TtsBridge(), "AndroidTTS")
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()

        webView.loadUrl("file:///android_asset/index.html")
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return

        val engine = tts ?: return
        engine.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )

        var result = engine.setLanguage(Locale.US)
        if (result == TextToSpeech.LANG_MISSING_DATA ||
            result == TextToSpeech.LANG_NOT_SUPPORTED) {
            result = engine.setLanguage(Locale.ENGLISH)
        }

        ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED

        engine.setSpeechRate(0.88f)
        engine.setPitch(1.0f)

        if (ttsReady) {
            pendingText?.let {
                speakNow(it)
                pendingText = null
            }
        }
    }

    private fun speakNow(text: String) {
        if (text.isBlank()) return
        tts?.stop()
        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "english_review_${System.currentTimeMillis()}"
        )
    }

    inner class TtsBridge {
        @JavascriptInterface
        fun speak(text: String) {
            runOnUiThread {
                if (ttsReady) {
                    speakNow(text)
                } else {
                    pendingText = text
                }
            }
        }

        @JavascriptInterface
        fun stop() {
            runOnUiThread {
                tts?.stop()
            }
        }

        @JavascriptInterface
        fun isReady(): Boolean = ttsReady
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        webView.destroy()
        super.onDestroy()
    }
}
