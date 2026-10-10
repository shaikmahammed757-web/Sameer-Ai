package com.example.aisameer

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.PermissionRequest
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : Activity() {
    private val RECORD_AUDIO_REQUEST_CODE = 101
    private val SPEECH_REQUEST_CODE = 102
    private var webViewInstance: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // మైక్ పర్మిషన్ చెక్
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_REQUEST_CODE)
        }

        val webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()
            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                    request.grant(request.resources)
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
                body {
                    background: linear-gradient(180deg, #050B18 0%, #0A1228 100%);
                    color: #FFFFFF;
                    font-family: sans-serif;
                    padding: 16px;
                    padding-bottom: 130px;
                }
                .header {
                    display: flex;
                    align-items: center;
                    gap: 12px;
                    margin-bottom: 16px;
                }
                .app-logo-img {
                    width: 42px;
                    height: 42px;
                    border-radius: 12px;
                    object-fit: cover;
                    border: 1.5px solid #00C6FF;
                    box-shadow: 0 0 10px rgba(0, 198, 255, 0.5);
                    flex-shrink: 0;
                }
                .hero-logo-img {
                    width: 75px;
                    height: 75px;
                    border-radius: 20px;
                    object-fit: cover;
                    margin: 0 auto 12px auto;
                    display: block;
                    border: 2px solid #AB55F7;
                    box-shadow: 0 0 20px rgba(168, 85, 247, 0.7);
                }
                .app-title { font-size: 20px; font-weight: 800; background: linear-gradient(90deg, #00D2FF, #AB55F7); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
                .app-sub { font-size: 11px; color: #94A3B8; }
                .hero-card {
                    background: radial-gradient(circle at center, #1E294B 0%, #0F172A 100%);
                    border: 1px solid #1E2938;
                    border-radius: 24px;
                    padding: 20px;
                    text-align: center;
                    margin-bottom: 20px;
                }
                .greeting-title { font-size: 18px; font-weight: 700; color: #F8FAFC; margin-bottom: 4px; }
                .greeting-sub { font-size: 13px; color: #38BDF8; }
                .grid {
                    display: grid;
                    grid-template-columns: 1fr 1fr;
                    gap: 10px;
                    margin-bottom: 20px;
                }
                .grid-btn {
                    background: #111833;
                    border: 1px solid #1E2D4A;
                    border-radius: 14px;
                    padding: 12px;
                    display: flex;
                    align-items: center;
                    gap: 10px;
                    color: #E2E8F0;
                    font-size: 12px;
                    font-weight: 600;
                    cursor: pointer;
                }
                .btn-icon {
                    width: 30px;
                    height: 30px;
                    border-radius: 8px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-size: 14px;
                }
                .ic-red { background: rgba(239, 68, 68, 0.2); color: #EF4444; }
                .ic-purple { background: rgba(168, 85, 247, 0.2); color: #AB55F7; }
                .ic-amber { background: rgba(245, 158, 11, 0.2); color: #F59E0B; }
                .ic-amber2 { background: rgba(234, 179, 8, 0.2); color: #EAB308; }
                .ic-emerald { background: rgba(16, 185, 129, 0.2); color: #10B981; }
                .ic-blue { background: rgba(59, 130, 246, 0.2); color: #3B82F6; }
                .output-box {
                    background: #111833;
                    border: 1px solid #1E2938;
                    border-radius: 14px;
                    padding: 16px;
                    margin-bottom: 80px;
                    font-size: 13px;
                    color: #CBD5E1;
                    display: none;
                    line-height: 1.5;
                    white-space: pre-wrap;
                }
                .input-bar {
                    position: fixed;
                    bottom: 60px;
                    left: 12px;
                    right: 12px;
                    background: #0F172A;
                    border: 1px solid #1E2938;
                    border-radius: 30px;
                    padding: 8px 14px;
                    display: flex;
                    align-items: center;
                    gap: 10px;
                }
                .input-bar input {
                    background: transparent;
                    border: none;
                    color: #FFF;
                    width: 100%;
                    outline: none;
                    font-size: 13px;
                }
                .action-icon { color: #64748B; font-size: 18px; cursor: pointer; }
                .send-circle {
                    width: 36px;
                    height: 36px;
                    background: linear-gradient(135deg, #00C6FF 0%, #0072FF 100%);
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    color: white;
                    cursor: pointer;
                    flex-shrink: 0;
                }
                .nav-bar {
                    position: fixed;
                    bottom: 0;
                    left: 0;
                    right: 0;
                    background: #070D1B;
                    border-top: 1px solid #1E2938;
                    display: flex;
                    justify-content: space-around;
                    padding: 8px 0;
                }
                .nav-item { text-align: center; font-size: 10px; color: #64748B; cursor: pointer; }
                .nav-item.active { color: #38BDF8; font-weight: bold; }
                .nav-item div { font-size: 16px; margin-bottom: 2px; }
            </style>
        </head>
        <body>
            <div class="header">
                <img src="https://raw.githubusercontent.com/shaikmahammad757-web/Sameer-AI/main/logo.png" class="app-logo-img" onerror="this.style.display='none'">
                <div>
                    <div class="app-title">AI Sameer</div>
                    <div class="app-sub">Your Personal AI Assistant</div>
                </div>
            </div>

            <div class="hero-card">
                <img src="https://raw.githubusercontent.com/shaikmahammad757-web/Sameer-AI/main/logo.png" class="hero-logo-img" onerror="this.style.display='none'">
                <div class="greeting-title">✨ Hello! I'm AI Sameer</div>
                <div class="greeting-sub">How can I help you today?</div>
            </div>

            <div class="output-box" id="outputBox"></div>

            <div class="grid" id="actionGrid">
                <div class="grid-btn" onclick="runAIAction('youtube')">
                    <div class="btn-icon ic-red">▶</div>
                    <div>Concept to<br>YouTube Script</div>
                </div>
                <div class="grid-btn" onclick="runAIAction('image')">
                    <div class="btn-icon ic-purple">🖼</div>
                    <div>Create<br>an image</div>
                </div>
                <div class="grid-btn" onclick="runAIAction('summary')">
                    <div class="btn-icon ic-amber">📄</div>
                    <div>Summarize<br>a document</div>
                </div>
                <div class="grid-btn" onclick="runAIAction('ideas')">
                    <div class="btn-icon ic-amber2">💡</div>
                    <div>Give me<br>ideas</div>
                </div>
                <div class="grid-btn" onclick="runAIAction('translate')">
                    <div class="btn-icon ic-emerald">abc</div>
                    <div>Translate<br>text</div>
                </div>
                <div class="grid-btn" onclick="runAIAction('search')">
                    <div class="btn-icon ic-blue">🔍</div>
                    <div>Search<br>the web</div>
                </div>
            </div>

            <div class="input-bar">
                <span class="action-icon">🖼️</span>
                <span class="action-icon">📎</span>
                <input type="text" id="userInput" placeholder="Ask AI Sameer anything...">
                <!-- మైక్ క్లిక్ చేస్తే నేటివ్ ఆండ్రాయిడ్ స్పీచ్ రికగ్నిషన్ ట్రిగ్గర్ అవుతుంది -->
                <span class="action-icon" id="micButton" onclick="AndroidApp.startVoiceInput()" title="Speak">🎙️</span>
                <div class="send-circle" onclick="sendQuery()">➔</div>
            </div>

            <div class="nav-bar">
                <div class="nav-item active" onclick="switchNav('home')"><div>🏠</div>Home</div>
                <div class="nav-item" onclick="switchNav('chat')"><div>💬</div>Chat</div>
                <div class="nav-item" onclick="switchNav('tools')"><div>🛠</div>Tools</div>
                <div class="nav-item" onclick="switchNav('history')"><div>🕒</div>History</div>
                <div class="nav-item" onclick="switchNav('profile')"><div>👤</div>Profile</div>
            </div>

            <script>
                function setVoiceResult(text) {
                    document.getElementById('userInput').value = text;
                }

                function showResult(title, content, showUpload) {
                    var box = document.getElementById('outputBox');
                    box.style.display = 'block';
                    var html = "<b>" + title + "</b><br><br>" + content;
                    if(showUpload) {
                        html += "<br><br><button onclick='uploadToYouTube()' style='background:#FF0000; color:white; border:none; padding:10px 16px; border-radius:8px; font-weight:bold; cursor:pointer;'>🚀 Upload to YouTube Studio</button>";
                    }
                    box.innerHTML = html;
                }

                function runAIAction(type) {
                    var val = document.getElementById('userInput').value.trim();
                    if(!val) val = "Riding & Technology Topic";

                    if(type === 'youtube') {
                        showResult("🎬 YouTube Script & Tags Result:", "1. Hook: Welcome back to SK MD Riding TV!\n2. Core Content: Explaining " + val + ".\n3. Outro: Subscribe for more tech videos!", true);
                    } else if(type === 'image') {
                        showResult("🖼️ AI Image Generation Result:", "Generated high-resolution graphic concept for: " + val);
                    } else if(type === 'summary') {
                        showResult("📄 Document Summary:", "Key takeaways and concise summary for: " + val + ".");
                    } else if(type === 'ideas') {
                        showResult("💡 Top Viral Ideas for " + val + ":\n1. Hidden Features\n2. Pro Level Guide\n3. Ultimate Review Setup", false);
                    } else if(type === 'translate') {
                        showResult("🔤 Translation Result:", "Translated \"" + val + "\" accurately into Telugu and English variants.", false);
                    } else if(type === 'search') {
                        AndroidApp.openWebSearch(val);
                    }
                }

                function sendQuery() {
                    var val = document.getElementById('userInput').value.trim();
                    if(val) {
                        runAIAction('youtube');
                    } else {
                        alert("Please enter a topic or question first!");
                    }
                }

                function uploadToYouTube() {
                    AndroidApp.openYouTubeUpload();
                }

                function switchNav(tab) {
                    var box = document.getElementById('outputBox');
                    if(tab === 'home') {
                        box.style.display = 'none';
                    } else {
                        box.style.display = 'block';
                        box.innerHTML = "<b>" + tab.toUpperCase() + " Section:</b><br><br>Feature active and ready.";
                    }
                }
            </script>
        </body>
        </html>
        """.trimIndent()

        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        setContentView(webView)
    }

    class WebAppInterface(private val activity: MainActivity) {
        @JavascriptInterface
        fun startVoiceInput() {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "te-IN") // తెలుగు భాష మద్దతు
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
            }
            try {
                activity.startActivityForResult(intent, activity.SPEECH_REQUEST_CODE)
            } catch (e: Exception) {
                activity.runOnUiThread {
                    Toast.makeText(activity, "Speech recognition not supported on this device", Toast.LENGTH_SHORT).show()
                }
            }
        }

        @JavascriptInterface
        fun openYouTubeUpload() {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://studio.youtube.com"))
                activity.startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/upload"))
                activity.startActivity(intent)
            }
        }

        @JavascriptInterface
        fun openWebSearch(query: String) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$query"))
            activity.startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0) ?: ""
            if (spokenText.isNotEmpty()) {
                // మైక్ ద్వారా మాట్లాడిన మాటలను జావాస్క్రిప్ట్ ద్వారా ఇన్‌పుట్ బాక్స్‌లో పంపుతుంది
                webViewInstance?.post {
                    webViewInstance?.evaluateJavascript("setVoiceResult('$spokenText');", null)
                }
            }
        }
    }
}
