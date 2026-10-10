package com.example.aisameer

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private val RECORD_AUDIO_REQUEST_CODE = 101
    private val SPEECH_REQUEST_CODE = 102
    private val FILE_CHOOSER_REQUEST_CODE = 103
    private var uploadMessage: ValueCallback<Array<Uri>>? = null
    private var webViewInstance: WebView? = null
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_REQUEST_CODE)
        }

        val webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            webViewClient = WebViewClient()
            
            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                    request.grant(request.resources)
                }

                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    if (uploadMessage != null) {
                        uploadMessage?.onReceiveValue(null)
                        uploadMessage = null
                    }
                    uploadMessage = filePathCallback
                    val intent = fileChooserParams?.createIntent()
                    try {
                        startActivityForResult(intent!!, FILE_CHOOSER_REQUEST_CODE)
                    } catch (e: Exception) {
                        uploadMessage = null
                        return false
                    }
                    return true
                }
            }
            addJavascriptInterface(WebAppInterface(this@MainActivity), "AndroidApp")
        }
        
        webViewInstance = webView

        val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                * { box-sizing: border-box; margin: 0; padding: 0; }
                body { background: #050B18; color: #FFF; font-family: sans-serif; padding: 16px; padding-bottom: 120px; }
                .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
                .title { font-size: 18px; font-weight: bold; color: #38BDF8; }
                .btn { background: #111833; border: 1px solid #1E2D4A; color: #FFF; padding: 6px 12px; border-radius: 8px; cursor: pointer; font-size: 12px; }
                .chat-box { background: #111833; border: 1px solid #1E2938; padding: 14px; border-radius: 12px; margin-bottom: 12px; font-size: 13px; line-height: 1.5; }
                .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 16px; }
                .grid-btn { background: #111833; border: 1px solid #1E2D4A; padding: 12px; border-radius: 12px; font-size: 12px; color: #E2E8F0; text-align: center; cursor: pointer; }
                .input-bar { position: fixed; bottom: 20px; left: 16px; right: 16px; background: #0F172A; border: 1px solid #1E2938; border-radius: 25px; padding: 8px 14px; display: flex; align-items: center; gap: 10px; }
                .input-bar input { background: transparent; border: none; color: #FFF; width: 100%; outline: none; font-size: 13px; }
                .send-btn { background: #00C6FF; color: #000; border: none; width: 32px; height: 32px; border-radius: 50%; font-weight: bold; cursor: pointer; flex-shrink: 0; }
            </style>
        </head>
        <body>
            <div class="header">
                <div class="title">AI Sameer</div>
                <button class="btn" onclick="clearChat()">New Chat</button>
            </div>

            <div id="chatArea">
                <div class="chat-box">
                    <b>Hello! I am AI Sameer.</b><br>How can I help you today? Ask anything or use voice input.
                </div>
            </div>

            <div class="grid">
                <div class="grid-btn" onclick="runAction('YouTube Script')">▶ YouTube Script</div>
                <div class="grid-btn" onclick="runAction('Create Image')">🖼 Create Image</div>
                <div class="grid-btn" onclick="runAction('Text to Video')">🎥 Text to Video</div>
                <div class="grid-btn" onclick="runAction('Summarize')">📄 Summarize</div>
            </div>

            <div class="input-bar">
                <input type="text" id="userInput" placeholder="Ask AI Sameer...">
                <button class="btn" onclick="AndroidApp.startVoiceInput()">Mic</button>
                <button class="send-btn" onclick="sendQuery()">➔</button>
            </div>

            <script>
                function clearChat() {
                    document.getElementById('userInput').value = '';
                    document.getElementById('chatArea').innerHTML = '<div class="chat-box"><b>New Chat Started.</b></div>';
                }

                function setVoiceResult(text) {
                    document.getElementById('userInput').value = text;
                    sendQuery();
                }

                function runAction(actionType) {
                    var area = document.getElementById('chatArea');
                    area.innerHTML += '<div class="chat-box"><b>Feature:</b> ' + actionType + ' activated successfully. Ready for processing!</div>';
                    AndroidApp.speakText(actionType + " activated");
                }

                function sendQuery() {
                    var val = document.getElementById('userInput').value.trim();
                    if(!val) return;
                    document.getElementById('userInput').value = '';
                    
                    var area = document.getElementById('chatArea');
                    area.innerHTML += '<div class="chat-box"><b>You:</b> ' + val + '</div>';
                    area.innerHTML += '<div class="chat-box"><b>AI Sameer:</b> మీరు అడిగిన ప్రశ్న "' + val + '" కి తక్షణ సమాధానం ఇక్కడ అందించబడింది.</div>';
                    
                    AndroidApp.speakText("AI Sameer answered");
                }
            </script>
        </body>
        </html>
        """.trimIndent()

        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        setContentView(webView)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("te", "IN")
        }
    }

    class WebAppInterface(private val activity: MainActivity) {
        @JavascriptInterface
        fun startVoiceInput() {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "te-IN")
            }
            try {
                activity.startActivityForResult(intent, activity.SPEECH_REQUEST_CODE)
            } catch (e: Exception) {
                Toast.makeText(activity, "Not supported", Toast.LENGTH_SHORT).show()
            }
        }

        @JavascriptInterface
        fun speakText(text: String) {
            activity.tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0) ?: ""
            if (spokenText.isNotEmpty()) {
                webViewInstance?.post {
                    webViewInstance?.evaluateJavascript("setVoiceResult('$spokenText');", null)
                }
            }
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
