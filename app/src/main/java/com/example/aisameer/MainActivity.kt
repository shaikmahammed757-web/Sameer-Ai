package com.example.aisameer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()

        // ఆండ్రాయిడ్ నుండి జావాస్క్రిప్ట్‌కి కనెక్షన్ ఇస్తుంది
        webView.addJavascriptInterface(WebAppInterface(this), "AndroidApp")

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
                        padding-bottom: 120px;
                    }
                    .header {
                        display: flex;
                        align-items: center;
                        gap: 12px;
                        margin-bottom: 16px;
                    }
                    .star-logo {
                        width: 44px;
                        height: 44px;
                        background: radial-gradient(circle, rgba(56,189,248,0.2) 0%, rgba(0,0,0,0) 70%);
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-size: 28px;
                    }
                    .app-title { font-size: 20px; font-weight: 800; background: linear-gradient(90deg, #00D2FF, #A855F7); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
                    .app-sub { font-size: 11px; color: #94A3B8; }

                    .hero-card {
                        background: radial-gradient(circle at center, #1E294B 0%, #0F172A 100%);
                        border: 1px solid #1E293B;
                        border-radius: 24px;
                        padding: 20px;
                        text-align: center;
                        margin-bottom: 20px;
                    }
                    .robot-glow {
                        width: 90px;
                        height: 90px;
                        margin: 0 auto 12px auto;
                        background: radial-gradient(circle, rgba(56,189,248,0.3) 0%, rgba(0,0,0,0) 70%);
                        border-radius: 50%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-size: 50px;
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
                        background: #111B33;
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
                    .ic-purple { background: rgba(168, 85, 247, 0.2); color: #A855F7; }
                    .ic-amber { background: rgba(245, 158, 11, 0.2); color: #F59E0B; }
                    .ic-amber2 { background: rgba(234, 179, 8, 0.2); color: #EAB308; }
                    .ic-emerald { background: rgba(16, 185, 129, 0.2); color: #10B981; }
                    .ic-blue { background: rgba(59, 130, 246, 0.2); color: #3B82F6; }

                    .input-bar {
                        position: fixed;
                        bottom: 60px;
                        left: 12px;
                        right: 12px;
                        background: #0F172A;
                        border: 1px solid #1E293B;
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
                    .action-icon { color: #64748B; font-size: 18px; }
                    .send-circle {
                        width: 36px;
                        height: 36px;
                        background: linear-gradient(135deg, #00C6FF, #0072FF);
                        border-radius: 50%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        color: white;
                        cursor: pointer;
                    }

                    .nav-bar {
                        position: fixed;
                        bottom: 0;
                        left: 0;
                        right: 0;
                        background: #070D1B;
                        border-top: 1px solid #1E293B;
                        display: flex;
                        justify-content: space-around;
                        padding: 8px 0;
                    }
                    .nav-item { text-align: center; font-size: 10px; color: #64748B; }
                    .nav-item.active { color: #38BDF8; font-weight: bold; }
                    .nav-item div { font-size: 16px; margin-bottom: 2px; }
                </style>
            </head>
            <body>

                <div class="header">
                    <div class="star-logo">✨</div>
                    <div>
                        <div class="app-title">AI Sameer</div>
                        <div class="app-sub">Your Personal AI Assistant</div>
                    </div>
                </div>

                <div class="hero-card">
                    <div class="robot-glow">🤖</div>
                    <div class="greeting-title">✨ Hello! I'm AI Sameer</div>
                    <div class="greeting-sub">How can I help you today?</div>
                </div>

                <div class="grid">
                    <div class="grid-btn" onclick="openYouTubeUpload()">
                        <div class="btn-icon ic-red">▶</div>Write &<br>Upload YouTube
                    </div>
                    <div class="grid-btn">
                        <div class="btn-icon ic-purple">🖼</div>Create<br>an image
                    </div>
                    <div class="grid-btn">
                        <div class="btn-icon ic-amber">📄</div>Summarize<br>a document
                    </div>
                    <div class="grid-btn">
                        <div class="btn-icon ic-amber2">💡</div>Give me<br>ideas
                    </div>
                    <div class="grid-btn">
                        <div class="btn-icon ic-emerald">🔤</div>Translate<br>text
                    </div>
                    <div class="grid-btn">
                        <div class="btn-icon ic-blue">🔍</div>Search<br>the web
                    </div>
                </div>

                <div class="input-bar">
                    <span class="action-icon">🖼</span>
                    <span class="action-icon">📎</span>
                    <input type="text" id="userInput" placeholder="Ask anything or YouTube topic...">
                    <span class="action-icon">🎙</span>
                    <div class="send-circle" onclick="sendQuery()">➔</div>
                </div>

                <div class="nav-bar">
                    <div class="nav-item active"><div>🏠</div>Home</div>
                    <div class="nav-item"><div>💬</div>Chat</div>
                    <div class="nav-item"><div>🎛</div>Tools</div>
                    <div class="nav-item"><div>🕒</div>History</div>
                    <div class="nav-item"><div>👤</div>Profile</div>
                </div>

                <script>
                    function openYouTubeUpload() {
                        var topic = document.getElementById('userInput').value;
                        if(!topic) topic = "AI Sameer Video Suggestion";
                        // ఆండ్రాయిడ్ కోడ్‌కి పంపిస్తుంది
                        AndroidApp.openYouTube(topic);
                    }
                    function sendQuery() {
                        var val = document.getElementById('userInput').value;
                        if(val) {
                            alert("AI Sameer processing: " + val);
                        }
                    }
                </script>
            </body>
            </html>
        """.trimIndent()

        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        setContentView(webView)
    }

    // జావాస్క్రిప్ట్ నుండి ఇక్కడ కాల్ వస్తుంది, అప్పుడు YouTube యాప్ ఓపెన్ అవుతుంది
    class WebAppInterface(private val activity: Activity) {
        @JavascriptInterface
        fun openYouTube(title: String) {
            try {
                // యూజర్ టైప్ చేసిన టాపిక్‌తో YouTube ని ఓపెన్ చేయడానికి ఇంటెంట్
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"))
                intent.setPackage("com.google.android.youtube")
                activity.startActivity(intent)
            } catch (e: Exception) {
                // ఒకవేళ YouTube యాప్ లేకపోతే బ్రౌజర్‌లో ఓపెన్ అవుతుంది
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/upload"))
                activity.startActivity(intent)
            }
        }
    }
}
