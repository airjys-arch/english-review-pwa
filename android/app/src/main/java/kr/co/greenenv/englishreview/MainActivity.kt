package kr.co.greenenv.englishreview

import android.app.Activity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        webView = WebView(this)
        setContentView(webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false

        webView.addJavascriptInterface(TtsBridge(), "AndroidTTS")

        webView.webChromeClient = WebChromeClient()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                webView.evaluateJavascript(
                    """
                    (function() {
                      try {
                        if (window.speechSynthesis) {
                          window.speechSynthesis.speak = function(utterance) {
                            var text = "";
                            if (utterance && utterance.text) {
                              text = utterance.text;
                            } else {
                              text = String(utterance || "");
                            }

                            if (window.AndroidTTS && text) {
                              window.AndroidTTS.speak(text);
                            }
                          };

                          window.speechSynthesis.cancel = function() {
                            if (window.AndroidTTS) {
                              window.AndroidTTS.stop();
                            }
                          };
                        }
                      } catch(e) {
                        console.log("Android TTS bridge error", e);
                      }
                    })();
                    """.trimIndent(),
                    null
                )
            }
        }

        webView.loadUrl("file:///android_asset/index.html")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            ttsReady =
                result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED

            tts?.setSpeechRate(0.9f)
            tts?.setPitch(1.0f)
        }
    }

    inner class TtsBridge {

        @JavascriptInterface
        fun speak(text: String) {
            if (!ttsReady || text.isBlank()) return

            runOnUiThread {
                tts?.stop()
                tts?.speak(
                    text,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "english_review_lc"
                )
            }
        }

        @JavascriptInterface
        fun stop() {
            runOnUiThread {
                tts?.stop()
            }
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        webView.destroy()
        super.onDestroy()
    }
}
