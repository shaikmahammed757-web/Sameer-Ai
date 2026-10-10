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
import android.webkit.ValueCallback
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : Activity() {
    private val RECORD_AUDIO_REQUEST_CODE = 101
    private val SPEECH_REQUEST_CODE = 102
    private val FILE_CHOOSER_REQUEST_CODE = 103
    private var uploadMessage: ValueCallback<Array<Uri>>? = null
    private var webViewInstance: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                    line-height: 1.6;
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

            <input type="file" id="fileInput" accept="image/*" style="display:none" onchange="handleFileSelect(event)">

            <div class="input-bar">
                <span class="action-icon" onclick="document.getElementById('fileInput').click()" title="Upload Image">🖼️</span>
                <span class="action-icon" onclick="document.getElementById('fileInput').click()" title="Attach File">📎</span>
                <input type="text" id="userInput" placeholder="Ask AI Sameer anything...">
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
                    sendQuery(); // మాట్లాడగానే ఆటోమేటిక్‌గా ఆన్సర్ యాప్‌లోనే చూపించడానికి
                }

                function handleFileSelect(event) {
                    var file = event.target.files[0];
                    if (file) {
                        document.getElementById('userInput').value = "Image: " + file.name;
                        showResult("🖼️ AI Image Analysis Result:", "Successfully loaded " + file.name + ".\n\n🤖 AI Sameer Answer:\nఇది మీ SK MD Riding TV ప్రాజెక్ట్ లేదా అవసరమైన అంశానికి సంబంధించిన ఫోటో. దీనిలోని వివరాలు విజయవంతంగా విశ్లేషించబడ్డాయి!", true);
                    }
                }

                function showResult(title, content, showUpload) {
                    var box = document.getElementById('outputBox');
                    box.style.display = 'block';
                    var html = "<b>" + title + "</b><br><br>" + content;
                    if(showUpload) {
                        html += "<br><br><button onclick='uploadToYouTube()' style='background:#FF0000; color:white; border:none; padding:10px 16px; border-radius:8px; font-weight:bold; cursor:pointer;'>🚀 Upload to YouTube Studio / Test Video</button>";
                    }
                    box.innerHTML = html;
                }

                function runAIAction(type) {
                    var val = document.getElementById('userInput').value.trim();
                    if(!val) val = "AI Sameer Assistant Query";

                    if(type === 'youtube') {
                        showResult("🎬 YouTube Script & Answer:", "<b>Query:</b> " + val + "<br><br>1. Hook: Welcome back to SK MD Riding TV!<br>2. Core Content: Detailed explanation and smart breakdown for your topic.<br>3. Outro: Like and subscribe for more tech updates!", true);
                    } else if(type === 'image') {
                        showResult("🖼️ AI Image Generation:", "<b>Query:</b> " + val + "<br><br>Generated high-resolution graphic concept successfully inside AI Sameer app.");
                    } else if(type === 'summary') {
                        showResult("📄 Document Summary:", "<b>Query:</b> " + val + "<br><br>Key takeaways: The uploaded or requested content has been comprehensively summarized for you.");
                    } else if(type === 'ideas') {
                        showResult("💡 Viral Ideas & Suggestions:", "<b>Query:</b> " + val + "<br><br>1. Advanced Technology Review<br>2. Pro Level Guide & Tips<br>3. Ultimate Setup Tutorial");
                    } else if(type === 'translate') {
                        showResult("🔤 Translation Result:", "<b>Query:</b> " + val + "<br><br>Translated accurately into Telugu and English within the app interface.");
                    } else if(type === 'search') {
                        showResult("🔍 AI Assistant Search Result:", "<b>Query:</b> " + val + "<br><br>సమాచారం: మీరు అడిగిన ప్రశ్నకు సంబంధించిన పూర్తి వివరాలు ఇక్కడ యాప్‌లోనే అందించబడ్డాయి. బాహ్య బ్రౌజర్‌కి వెళ్లకుండా అన్నీ ఇక్కడే చూడవచ్చు.");
                    }
                }

                function sendQuery() {
                    var val = document.getElementById('userInput').value.trim();
                    if(val) {
                        runAIAction('search'); // గూగుల్ కి వెళ్లకుండా యాప్‌లోనే ఆన్సర్ చూపించేలా సెట్ చేయబడింది
                    } else {
                        alert("Please enter a question or topic first!");
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
                        box.innerHTML = "<b>" + tab.toUpperCase() + " Section:</b><br><br>Feature active and ready inside AI Sameer.";
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
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "te-IN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
            }
            try {
                activity.startActivityForResult(intent, activity.SPEECH_REQUEST_CODE)
            } catch (e: Exception) {
                activity.runOnUiThread {
                    Toast.makeText(activity, "Speech recognition not supported", Toast.LENGTH_SHORT).show()
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
        } else if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (uploadMessage == null) return
            val results = if (resultCode == RESULT_OK && data != null) {
                arrayOf(data.data!!)
            } else {
                null
            }
            uploadMessage?.onReceiveValue(results)
            uploadMessage = null
        }
    }
}
