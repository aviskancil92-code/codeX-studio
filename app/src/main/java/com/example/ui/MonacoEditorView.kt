package com.example.ui

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONObject

interface MonacoController {
    fun setContent(code: String, language: String, path: String)
    fun gotoLine(line: Int, column: Int)
    fun insertText(text: String)
    fun formatDocument()
    fun undo()
    fun redo()
    fun find()
    fun setTheme(theme: String)
    fun setWordWrap(wrap: Boolean)
    fun setMinimap(show: Boolean)
    fun setFontSize(size: Int)
}

class MonacoControllerImpl(private var webView: WebView?) : MonacoController {

    fun attach(view: WebView) {
        webView = view
    }

    fun detach() {
        webView = null
    }

    override fun setContent(code: String, language: String, path: String) {
        val safeCode = JSONObject.quote(code)
        val safeLang = JSONObject.quote(language)
        val safePath = JSONObject.quote(path)
        webView?.evaluateJavascript("window.codexSetContent($safeCode, $safeLang, $safePath);", null)
    }

    override fun gotoLine(line: Int, column: Int) {
        webView?.evaluateJavascript("window.codexGotoLine($line, $column);", null)
    }

    override fun insertText(text: String) {
        val safe = JSONObject.quote(text)
        webView?.evaluateJavascript("window.codexInsertText($safe);", null)
    }

    override fun formatDocument() {
        webView?.evaluateJavascript("window.codexFormat();", null)
    }

    override fun undo() {
        webView?.evaluateJavascript("window.codexUndo();", null)
    }

    override fun redo() {
        webView?.evaluateJavascript("window.codexRedo();", null)
    }

    override fun find() {
        webView?.evaluateJavascript("window.codexFind();", null)
    }

    override fun setTheme(theme: String) {
        val safe = JSONObject.quote(theme)
        webView?.evaluateJavascript("window.codexSetTheme($safe);", null)
    }

    override fun setWordWrap(wrap: Boolean) {
        webView?.evaluateJavascript("window.codexSetWordWrap($wrap);", null)
    }

    override fun setMinimap(show: Boolean) {
        webView?.evaluateJavascript("window.codexSetMinimap($show);", null)
    }

    override fun setFontSize(size: Int) {
        webView?.evaluateJavascript("window.codexSetFontSize($size);", null)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MonacoEditorView(
    controller: MonacoControllerImpl,
    initialContent: String,
    language: String,
    filePath: String,
    onContentChange: (String) -> Unit,
    onCursorPosition: (line: Int, col: Int) -> Unit,
    onSaveRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isReady by remember { mutableStateOf(false) }

    val currentContent by rememberUpdatedState(initialContent)
    val currentLang by rememberUpdatedState(language)
    val currentPath by rememberUpdatedState(filePath)

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = true
                    allowContentAccess = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    displayZoomControls = false
                    builtInZoomControls = false
                }

                setBackgroundColor(0xFF161B22.toInt())

                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onEditorReady() {
                        post {
                            isReady = true
                            controller.setContent(currentContent, currentLang, currentPath)
                        }
                    }

                    @JavascriptInterface
                    fun onContentChanged(content: String) {
                        post {
                            onContentChange(content)
                        }
                    }

                    @JavascriptInterface
                    fun onCursorPosition(json: String) {
                        post {
                            try {
                                val obj = JSONObject(json)
                                onCursorPosition(obj.optInt("line", 1), obj.optInt("column", 1))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    @JavascriptInterface
                    fun onSaveShortcut() {
                        post {
                            onSaveRequested()
                        }
                    }
                }, "AndroidBridge")

                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                    }
                }

                controller.attach(this)
                loadUrl("file:///android_asset/monaco/editor.html")
            }
        },
        update = {
            if (isReady) {
                // If path or content changed externally
                controller.setContent(currentContent, currentLang, currentPath)
            }
        },
        modifier = modifier.fillMaxSize()
    )

    DisposableEffect(Unit) {
        onDispose {
            controller.detach()
        }
    }
}
