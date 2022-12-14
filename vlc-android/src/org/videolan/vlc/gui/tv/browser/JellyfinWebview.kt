package org.videolan.vlc.gui.tv.browser

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.json.JSONArray
import org.json.JSONObject
import org.videolan.medialibrary.interfaces.media.AbstractMediaWrapper
import org.videolan.medialibrary.media.MediaWrapper
import org.videolan.vlc.R
import org.videolan.vlc.gui.ContentActivity
import org.videolan.vlc.media.MediaUtils

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class JellyfinWebview : ContentActivity() {

    private lateinit var mWebView: WebView

    val handler : Handler = Handler{
        when(it.what){
            1 -> {
                var mediaWrapper = MediaWrapper(Uri.parse(it.obj as String?))
                MediaUtils.openMedia(baseContext, mediaWrapper)
            }
            2 -> {
                var mediaWrapperList: ArrayList<MediaWrapper> = it.obj as ArrayList<MediaWrapper>
                MediaUtils.openList(baseContext, mediaWrapperList as ArrayList<AbstractMediaWrapper>, 0, false)
            }
        }
        false
    }

    @SuppressLint("JavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.browser_jellyfinweb)
        mWebView = findViewById(R.id.jellyfinWebview)

        val webSetting: WebSettings = mWebView.getSettings()
        webSetting.javaScriptEnabled = true
        webSetting.loadWithOverviewMode = true
        webSetting.builtInZoomControls = true
        webSetting.setGeolocationEnabled(true)
        webSetting.domStorageEnabled = true
        webSetting.databaseEnabled = true
        webSetting.useWideViewPort = true

        webSetting.allowFileAccess = true

        webSetting.setSupportZoom(true)

        webSetting.pluginState = WebSettings.PluginState.ON
        webSetting.cacheMode = WebSettings.LOAD_NO_CACHE

        webSetting.javaScriptEnabled = true
        webSetting.javaScriptCanOpenWindowsAutomatically = true
        webSetting.allowFileAccessFromFileURLs = true
        webSetting.allowUniversalAccessFromFileURLs = true
        webSetting.allowContentAccess = true
        webSetting.setSupportMultipleWindows(true)
        webSetting.setAppCacheEnabled(true)
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE)

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val mDensity: Int = metrics.densityDpi
        val textzoom: Int = mDensity / 10
        webSetting.textZoom = textzoom

        mWebView.setWebViewClient(WebViewClient())

        //JS????????????
        mWebView.addJavascriptInterface(this, "toVlcPlayer")

        mWebView.loadUrl("file:///android_asset/www/index.html")
    }

    override fun onBackPressed() {
        //webview????????????back??????????????????KEYCODE_BACK?????? Unimplemented WebView method onKeyDown called from
        mWebView.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE))
        //super.onBackPressed();
    }

    @JavascriptInterface
    fun toPlayer(videourl: String) {
        var msg = Message()
        msg.what = 1
        msg.obj = videourl
        handler.sendMessage(msg)
    }

    @JavascriptInterface
    fun toPlayList(items: String) {
        var msg = Message()
        msg.what = 2
        msg.obj = createMediaWrapperList(items)
        handler.sendMessage(msg)
    }

    //??????APP
    @JavascriptInterface
    fun appExit() {
        finish()
    }

    fun createMediaWrapperList(items: String) : ArrayList<MediaWrapper> {
        var mList : ArrayList<MediaWrapper> = ArrayList()
        var jsa = JSONArray(items);
        for(i in 0 .. (jsa.length()-1)){
            var jso:JSONObject = jsa.get(i) as JSONObject
            var mediaWrapper : MediaWrapper = MediaWrapper(Uri.parse(jso.getString("url")))
            mediaWrapper.title = jso.getString("title")
            //mediaWrapper.picture(jso.getString("picture"))
            //mediaWrapper.tracks =  jso.getString("tracks")
            mList.add(mediaWrapper)
        }

        return mList
    }
}

/**
 * ??????Android 5.0 5.1 webview ??????
 */
class MyJFWebView : WebView {

    companion object{
        private fun getFixedContext(context: Context): Context {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                return context.createConfigurationContext(Configuration())
            }
            return context
        }
    }

    constructor(context: Context) : super(getFixedContext(context))

    constructor(context: Context, attrs: AttributeSet) : super(getFixedContext(context), attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
            getFixedContext(context),
            attrs,
            defStyleAttr
    )

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
            getFixedContext(context),
            attrs,
            defStyleAttr,
            defStyleRes
    )
}
