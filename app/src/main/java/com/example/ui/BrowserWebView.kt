package com.example.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.RenderProcessGoneDetail
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
  url: String,
  onUrlChange: (String) -> Unit,
  onPageTitleChange: (String) -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var webViewInstance by remember { mutableStateOf<WebView?>(null) }
  var pageProgress by remember { mutableFloatStateOf(0f) }
  var isLoading by remember { mutableStateOf(true) }
  var hasError by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }

  // Fullscreen video support
  var customView by remember { mutableStateOf<View?>(null) }
  var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

  // File upload chooser support
  var fileChooserCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
  val fileChooserLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    val data = result.data
    val results = WebChromeClient.FileChooserParams.parseResult(result.resultCode, data)
    fileChooserCallback?.onReceiveValue(results)
    fileChooserCallback = null
  }

  // Handle system back navigation:
  // 1) Exit fullscreen video if active
  // 2) Go back in WebView history
  // 3) Close WebView and return to home page
  BackHandler {
    if (customView != null) {
      customViewCallback?.onCustomViewHidden()
      customView = null
      customViewCallback = null
    } else if (webViewInstance?.canGoBack() == true) {
      webViewInstance?.goBack()
    } else {
      onClose()
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    AndroidView(
      factory = { ctx ->
        WebView(ctx).apply {
          layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
          )

          // Safe layer composition
          setLayerType(View.LAYER_TYPE_NONE, null)

          // Full Cookie management
          val cookieManager = CookieManager.getInstance()
          cookieManager.setAcceptCookie(true)
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(this, true)
          }

          // Complete WebSettings for full modern web compatibility
          settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            cacheMode = WebSettings.LOAD_DEFAULT
            allowFileAccess = true
            allowContentAccess = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              safeBrowsingEnabled = false
            }
          }

          webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
              view: WebView?,
              request: WebResourceRequest?
            ): Boolean {
              val reqUrl = request?.url?.toString() ?: return false
              if (reqUrl.startsWith("http://") || reqUrl.startsWith("https://")) {
                view?.loadUrl(reqUrl)
                onUrlChange(reqUrl)
                return true
              } else {
                // Handle external schemes: tel, mailto, sms, geo, intents
                try {
                  val intent = Intent(Intent.ACTION_VIEW, Uri.parse(reqUrl))
                  context.startActivity(intent)
                } catch (e: Exception) {
                  // Fallback: ignore unhandled intent
                }
                return true
              }
            }

            override fun onPageStarted(view: WebView?, currentUrl: String?, favicon: Bitmap?) {
              super.onPageStarted(view, currentUrl, favicon)
              isLoading = true
              hasError = false
              currentUrl?.let { onUrlChange(it) }
            }

            override fun onPageFinished(view: WebView?, currentUrl: String?) {
              super.onPageFinished(view, currentUrl)
              isLoading = false
              currentUrl?.let { onUrlChange(it) }
              view?.title?.let { onPageTitleChange(it) }
            }

            override fun onReceivedError(
              view: WebView?,
              request: WebResourceRequest?,
              error: WebResourceError?
            ) {
              super.onReceivedError(view, request, error)
              if (request?.isForMainFrame == true) {
                isLoading = false
                hasError = true
                errorMessage = error?.description?.toString() ?: "Network error"
              }
            }

            override fun onRenderProcessGone(
              view: WebView?,
              detail: RenderProcessGoneDetail?
            ): Boolean {
              view?.let {
                (it.parent as? ViewGroup)?.removeView(it)
                it.destroy()
              }
              webViewInstance = null
              hasError = true
              errorMessage = "Web engine refreshed. Tap reload."
              return true
            }
          }

          webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
              pageProgress = newProgress / 100f
              if (newProgress >= 100) {
                isLoading = false
              }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
              super.onReceivedTitle(view, title)
              title?.let { onPageTitleChange(it) }
            }

            override fun onGeolocationPermissionsShowPrompt(
              origin: String?,
              callback: GeolocationPermissions.Callback?
            ) {
              callback?.invoke(origin, true, false)
            }

            override fun onShowFileChooser(
              webView: WebView?,
              filePathCallback: ValueCallback<Array<Uri>>?,
              fileChooserParams: FileChooserParams?
            ): Boolean {
              fileChooserCallback?.onReceiveValue(null)
              fileChooserCallback = filePathCallback
              return try {
                val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                  type = "*/*"
                  addCategory(Intent.CATEGORY_OPENABLE)
                }
                fileChooserLauncher.launch(intent)
                true
              } catch (e: Exception) {
                fileChooserCallback?.onReceiveValue(null)
                fileChooserCallback = null
                false
              }
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
              customView = view
              customViewCallback = callback
            }

            override fun onHideCustomView() {
              customView = null
              customViewCallback = null
            }
          }

          loadUrl(url)
          webViewInstance = this
        }
      },
      update = { webView ->
        if (webView.url != url && url.isNotBlank()) {
          webView.loadUrl(url)
        }
      },
      modifier = Modifier.fillMaxSize()
    )

    // Fullscreen video overlay
    if (customView != null) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black)
      ) {
        AndroidView(
          factory = { customView!! },
          modifier = Modifier.fillMaxSize()
        )
      }
    }

    // Top Linear Progress Bar
    AnimatedVisibility(
      visible = isLoading,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.TopCenter)
    ) {
      LinearProgressIndicator(
        progress = { pageProgress },
        modifier = Modifier
          .fillMaxWidth()
          .height(3.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
      )
    }

    // Offline / Error screen
    if (hasError) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.background)
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onErrorContainer,
              modifier = Modifier.size(32.dp)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "Unable to load page",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = errorMessage.ifEmpty { "Check your connection and try again." },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(20.dp))

          Button(
            onClick = {
              hasError = false
              isLoading = true
              webViewInstance?.reload() ?: webViewInstance?.loadUrl(url)
            },
            modifier = Modifier.testTag("webview_retry_button")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            Text("Reload")
          }
        }
      }
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      webViewInstance?.apply {
        stopLoading()
        clearHistory()
        loadUrl("about:blank")
        onPause()
        removeAllViews()
        destroy()
      }
      webViewInstance = null
    }
  }
}
