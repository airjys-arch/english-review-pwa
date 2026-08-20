package kr.co.greenenv.englishreview
import android.app.Activity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
class MainActivity: Activity(){
 override fun onCreate(b:Bundle?){super.onCreate(b); val w=WebView(this); setContentView(w); w.settings.javaScriptEnabled=true; w.settings.domStorageEnabled=true; w.webChromeClient=WebChromeClient(); w.webViewClient=WebViewClient(); w.loadUrl("file:///android_asset/index.html")}
}
