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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.SpeedDial
import com.example.data.findMatchingSpeedDial

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
  url: String,
  speedDials: List<SpeedDial> = emptyList(),
  onUrlChange: (String) -> Unit,
  onPageTitleChange: (String) -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val isDark = isSystemInDarkTheme()
  val currentSpeedDial = remember(url, speedDials) {
    findMatchingSpeedDial(url, speedDials)
  }
  var webViewInstance by remember { mutableStateOf<WebView?>(null) }
  var currentLoadedUrl by remember { mutableStateOf("") }
  var renderRecoveryTrigger by remember { mutableStateOf(0) }
  var useSoftwareLayer by remember { mutableStateOf(true) }
  var pageProgress by remember { mutableFloatStateOf(0f) }
  var isLoading by remember { mutableStateOf(true) }
  var hasError by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }

  // Load new URL when url parameter changes externally
  LaunchedEffect(url) {
    if (url.isNotBlank() && url != currentLoadedUrl) {
      currentLoadedUrl = url
      hasError = false
      webViewInstance?.loadUrl(url)
    }
  }

  // Reactively apply dark or light theme when system theme changes
  LaunchedEffect(isDark) {
    webViewInstance?.let { wv ->
      @Suppress("DEPRECATION")
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        wv.settings.forceDark = if (isDark) WebSettings.FORCE_DARK_ON else WebSettings.FORCE_DARK_OFF
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        wv.settings.isAlgorithmicDarkeningAllowed = isDark
      }
      wv.setBackgroundColor(if (isDark) android.graphics.Color.parseColor("#10141D") else android.graphics.Color.WHITE)
      applyThemeToWebPage(wv, isDark)
    }
  }

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
    key(renderRecoveryTrigger) {
      AndroidView(
        factory = { ctx ->
          WebView(ctx).apply {
            // Preemptively enforce software layer to prevent missing GPU DRM rendernode crashes in container/emulator environments
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            layoutParams = ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT
            )

          // Full Cookie management
          val cookieManager = CookieManager.getInstance()
          cookieManager.setAcceptCookie(true)
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(this, true)
          }

          // Complete WebSettings for modern web compatibility
          settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            cacheMode = WebSettings.LOAD_DEFAULT
            allowFileAccess = true
            allowContentAccess = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            mediaPlaybackRequiresUserGesture = true // Prevents DRM lookup errors
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              safeBrowsingEnabled = false
            }
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
              forceDark = if (isDark) WebSettings.FORCE_DARK_ON else WebSettings.FORCE_DARK_OFF
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
              isAlgorithmicDarkeningAllowed = isDark
            }
          }

          setBackgroundColor(if (isDark) android.graphics.Color.parseColor("#10141D") else android.graphics.Color.WHITE)

          webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
              view: WebView?,
              request: WebResourceRequest?
            ): Boolean {
              val reqUrl = request?.url?.toString() ?: return false
              return handleNavigation(reqUrl)
            }

            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(
              view: WebView?,
              targetUrl: String?
            ): Boolean {
              val reqUrl = targetUrl ?: return false
              return handleNavigation(reqUrl)
            }

            private fun handleNavigation(reqUrl: String): Boolean {
              val lower = reqUrl.lowercase()
              if (lower.startsWith("http://") || lower.startsWith("https://") ||
                  lower.startsWith("about:") || lower.startsWith("data:") ||
                  lower.startsWith("blob:") || lower.startsWith("javascript:")) {
                // Return false: Allows WebView to load page natively without aborting or looping
                return false
              } else {
                // Handle external schemes: tel, mailto, sms, geo, intents
                return try {
                  val intent = Intent(Intent.ACTION_VIEW, Uri.parse(reqUrl))
                  context.startActivity(intent)
                  true
                } catch (e: Exception) {
                  true
                }
              }
            }

            override fun onReceivedSslError(
              view: WebView?,
              handler: android.webkit.SslErrorHandler?,
              error: android.net.http.SslError?
            ) {
              handler?.proceed()
            }

            override fun onPageStarted(view: WebView?, pageUrl: String?, favicon: Bitmap?) {
              super.onPageStarted(view, pageUrl, favicon)
              isLoading = true
              hasError = false
              if (!pageUrl.isNullOrBlank() && pageUrl != "about:blank") {
                currentLoadedUrl = pageUrl
                onUrlChange(pageUrl)
              }
            }

            override fun onPageCommitVisible(view: WebView?, pageUrl: String?) {
              super.onPageCommitVisible(view, pageUrl)
              applyThemeToWebPage(view, isDark)
            }

            override fun onPageFinished(view: WebView?, pageUrl: String?) {
              super.onPageFinished(view, pageUrl)
              isLoading = false
              if (!pageUrl.isNullOrBlank() && pageUrl != "about:blank") {
                currentLoadedUrl = pageUrl
                onUrlChange(pageUrl)
              }
              view?.title?.let { onPageTitleChange(it) }
              applyThemeToWebPage(view, isDark)
            }

            override fun onReceivedError(
              view: WebView?,
              request: WebResourceRequest?,
              error: WebResourceError?
            ) {
              super.onReceivedError(view, request, error)
              if (request?.isForMainFrame == true) {
                val errorCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                  error?.errorCode ?: 0
                } else {
                  0
                }
                // Ignore code -3 (ERR_ABORTED) which occurs normally on redirects
                if (errorCode != -3) {
                  isLoading = false
                  hasError = true
                  errorMessage = error?.description?.toString() ?: "Network error"
                }
              }
            }

            override fun onRenderProcessGone(
              view: WebView?,
              detail: RenderProcessGoneDetail?
            ): Boolean {
              view?.post {
                try {
                  (view.parent as? ViewGroup)?.removeView(view)
                  view.destroy()
                } catch (e: Exception) {
                  // Ignore
                }
              }
              webViewInstance = null
              hasError = false
              renderRecoveryTrigger++
              return true
            }
          }

          webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
              pageProgress = newProgress / 100f
              if (newProgress >= 100) {
                isLoading = false
              }
              if (newProgress in 20..95) {
                applyThemeToWebPage(view, isDark)
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

          currentLoadedUrl = url
          loadUrl(url)
          webViewInstance = this
        }
      },
      update = { /* Navigation and theme updates are reactively managed via LaunchedEffect */ },
      onRelease = { webView ->
        try {
          webView.stopLoading()
          webView.webViewClient = object : WebViewClient() {}
          webView.webChromeClient = null
          (webView.parent as? ViewGroup)?.removeView(webView)
          webView.destroy()
        } catch (e: Exception) {
          // Ignore
        }
        if (webViewInstance === webView) {
          webViewInstance = null
        }
      },
      modifier = Modifier.fillMaxSize()
    )
  }

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

    // Top Linear Progress Bar directly below status bar
    AnimatedVisibility(
      visible = isLoading && pageProgress < 1f,
      enter = fadeIn(tween(150)),
      exit = fadeOut(tween(250)),
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.TopCenter)
    ) {
      LinearProgressIndicator(
        progress = { if (pageProgress <= 0f) 0.08f else pageProgress },
        modifier = Modifier
          .fillMaxWidth()
          .height(3.5.dp)
          .testTag("webview_loading_progress_bar"),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
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
              try {
                webViewInstance?.reload() ?: webViewInstance?.loadUrl(url)
              } catch (e: Exception) {
                // Ignore
              }
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
}

/**
 * Injects color-scheme CSS styling and meta tags into the web page to ensure the website
 * dynamically renders in dark or light mode according to the app's current theme.
 * When the app theme is light, the website theme is set to light; when dark, the website theme is set to dark.
 */
private fun applyThemeToWebPage(view: WebView?, isDark: Boolean) {
  val themeScheme = if (isDark) "dark" else "light"
  val script = """
    (function() {
      try {
        if (document.documentElement) {
          document.documentElement.style.setProperty('color-scheme', '$themeScheme', 'important');
        }
        if (document.body) {
          document.body.style.setProperty('color-scheme', '$themeScheme', 'important');
        }
        var meta = document.querySelector('meta[name="color-scheme"]');
        if (!meta && (document.head || document.documentElement)) {
          meta = document.createElement('meta');
          meta.name = 'color-scheme';
          (document.head || document.documentElement).appendChild(meta);
        }
        if (meta) {
          meta.content = '$themeScheme';
        }
        var styleId = 'webdial-force-theme-style';
        var styleEl = document.getElementById(styleId);
        if (!styleEl && (document.head || document.documentElement)) {
          styleEl = document.createElement('style');
          styleEl.id = styleId;
          (document.head || document.documentElement).appendChild(styleEl);
        }
        if (styleEl) {
          styleEl.textContent = ':root, html, body { color-scheme: $themeScheme !important; }';
        }
      } catch(e) {}
    })();
  """.trimIndent()
  view?.evaluateJavascript(script, null)
}



