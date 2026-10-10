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
                body {
                    background: linear-gradient(180deg, #050B18 0%, #0A1228 100%);
                    color: #FFFFFF;
                    font-family: sans-serif;
                    padding: 16px;
                    padding-bottom: 140px;
                }
                .header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 16px;
                }
                .header-left {
                    display: flex;
                    align-items: center;
                    gap: 10px;
                }
                .app-logo-img {
                    width: 38px;
                    height: 38px;
                    border-radius: 10px;
                    object-fit: cover;
                    border: 1.5px solid #00C6FF;
                    flex-shrink: 0;
                }
                .hero-logo-img {
                    width: 60px;
                    height: 60px;
                    border-radius: 16px;
                    object-fit: cover;
                    margin: 0 auto 10px auto;
                    display: block;
                    border: 2px solid #AB55F7;
                    box-shadow: 0 0 15px rgba(168, 85, 247, 0.6);
                }
                .menu-icon { font-size: 22px; cursor: pointer; color: #38BDF8; font-weight: bold; }
                .app-title { font-size: 17px; font-weight: 800; background: linear-gradient(90deg, #00D2FF, #AB55F7); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
                .app-sub { font-size: 11px; color: #94A3B8; }
                
                .header-actions { display: flex; gap: 8px; align-items: center; }
                .icon-btn {
                    background: #111833;
                    border: 1px solid #1E2D4A;
                    color: #38BDF8;
                    width: 36px;
                    height: 36px;
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-size: 15px;
                    cursor: pointer;
                }
                
                .sidebar {
                    position: fixed;
                    top: 0;
                    left: -300px;
                    width: 300px;
                    height: 100%;
                    background: #0B132B;
                    border-right: 1px solid #1E2938;
                    z-index: 1000;
                    transition: 0.3s ease;
                    padding: 20px;
                    overflow-y: auto;
                    box-shadow: 5px 0 15px rgba(0,0,0,0.5);
                }
                .sidebar.open { left: 0; }
                .sidebar-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 20px;
                    font-size: 16px;
                    font-weight: bold;
                    color: #38BDF8;
                }
                .close-sidebar { font-size: 18px; cursor: pointer; color: #94A3B8; }
                .history-item {
                    background: #111833;
                    border: 1px solid #1E2D4A;
                    padding: 10px;
                    border-radius: 10px;
                    margin-bottom: 8px;
                    font-size: 12px;
                    color: #CBD5E1;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }
                .history-text { cursor: pointer; flex-grow: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-right: 8px; }
                .delete-btn {
                    background: rgba(239, 68, 68, 0.2);
                    color: #EF4444;
                    border: none;
                    width: 24px;
                    height: 24px;
                    border-radius: 50%;
                    cursor: pointer;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-size: 11px;
                }

                .hero-card {
                    background: radial-gradient(circle at center, #1E294B 0%, #0F172A 100%);
                    border: 1px solid #1E2938;
                    border-radius: 18px;
                    padding: 14px;
                    text-align: center;
                    margin-bottom: 14px;
                }
                .greeting-title { font-size: 15px; font-weight: 700; color: #F8FAFC; margin-bottom: 2px; }
                .greeting-sub { font-size: 11px; color: #38BDF8; }
                
                .tools-container { margin-bottom: 20px; }
                .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
                .grid-btn {
                    background: #111833;
                    border: 1px solid #1E2D4A;
                    border-radius: 14px;
                    padding: 12px;
                    display: flex;
                    align-items: center;
                    gap: 10px;
                    color: #E2E8F0;
                    font-size: 11px;
                    font-weight: 600;
                    cursor: pointer;
                }
                .btn-icon { width: 28px; height: 28px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 13px; flex-shrink: 0; }
                .ic-red { background: rgba(239, 68, 68, 0.2); color: #EF4444; }
                .ic-purple { background: rgba(168, 85, 247, 0.2); color: #AB55F7; }
                .ic-amber { background: rgba(245, 158, 11, 0.2); color: #F59E0B; }
                .ic-amber2 { background: rgba(234, 179, 8, 0.2); color: #EAB308; }
                .ic-emerald { background: rgba(16, 185, 129, 0.2); color: #10B981; }
                .ic-blue { background: rgba(59, 130, 246, 0.2); color: #3B82F6; }
                
                .chat-container { display: flex; flex-direction: column; gap: 12px; margin-bottom: 100px; }
                .chat-bubble-user {
                    background: #1E3A8A;
                    color: #E2E8F0;
                    padding: 12px 16px;
                    border-radius: 16px 16px 4px 16px;
                    max-width: 85%;
                    align-self: flex-end;
                    font-size: 13px;
                    line-height: 1.5;
                }
                .chat-bubble-ai {
                    background: #111833;
                    border: 1px solid #1E2938;
                    color: #CBD5E1;
                    padding: 14px 16px;
                    border-radius: 16px 16px 16px 4px;
                    max-width: 90%;
                    align-self: flex-start;
                    font-size: 13px;
                    line-height: 1.6;
                }
                .tts-speaker-btn {
                    background: rgba(0, 198, 255, 0.2);
                    color: #00C6FF;
                    border: 1px solid #00C6FF;
                    padding: 6px 12px;
                    border-radius: 8px;
                    font-size: 12px;
                    font-weight: bold;
                    cursor: pointer;
                    margin-top: 8px;
                    display: inline-flex;
                    align-items: center;
                    gap: 6px;
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
                .input-bar input { background: transparent; border: none; color: #FFF; width: 100%; outline: none; font-size: 13px; }
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
            <div class="sidebar" id="sidebar">
                <div class="sidebar-header">
                    <span>💬 Chat History</span>
                    <span class="close-sidebar" onclick="toggleSidebar()">✕</span>
                </div>
                <div id="historyList">
                    <div style="font-size: 12px; color: #64748B;">No recent chats</div>
                </div>
            </div>

            <div class="header">
                <div class="header-left">
                    <div class="menu-icon" onclick="toggleSidebar()" title="Menu">≡</div>
                    <img src="https://raw.githubusercontent.com/shaikmahammad757-web/Sameer-AI/main/logo.png" class="app-logo-img" onerror="this.style.display='none'">
                    <div>
                        <div class="app-title">AI Sameer</div>
                        <div class="app-sub">Your Personal AI Assistant</div>
                    </div>
                </div>
                <div class="header-actions">
                    <div class="icon-btn" onclick="toggleSidebar()" title="History">🕒</div>
                    <div class="icon-btn" onclick="clearAll()" title="New Chat">✏️</div>
                </div>
            </div>

            <div class="hero-card" id="heroCard">
                <img src="https://raw.githubusercontent.com/shaikmahammad757-web/Sameer-AI/main/logo.png" class="hero-logo-img" onerror="this.style.display='none'">
                <div class="greeting-title">✨ Hello! I'm AI Sameer</div>
                <div class="greeting-sub">How can I help you today?</div>
            </div>

            <div class="chat-container" id="chatContainer"></div>

            <div class="tools-container" id="toolsContainer">
                <div class="grid" id="actionGrid">
                    <div class="grid-btn" onclick="runAIAction('youtube')">
                        <div class="btn-icon ic-red">▶</div>
                        <div>Write a YouTube script</div>
                    </div>
                    <div class="grid-btn" onclick="runAIAction('image')">
                        <div class="btn-icon ic-purple">🖼</div>
                        <div>Create an image</div>
                    </div>
                    <div class="grid-btn" onclick="runAIAction('summary')">
                        <div class="btn-icon ic-amber">📄</div>
                        <div>Summarize a document</div>
                    </div>
                    <div class="grid-btn" onclick="runAIAction('ideas')">
                        <div class="btn-icon ic-amber2">💡</div>
                        <div>Give me ideas</div>
                    </div>
                    <div class="grid-btn" onclick="runAIAction('translate')">
                        <div class="btn-icon ic-emerald">abc</div>
                        <div>Translate text</div>
                    </div>
                    <div class="grid-btn" onclick="runAIAction('search')">
                        <div class="btn-icon ic-blue">🔍</div>
                        <div>Search the web</div>
                    </div>
                </div>
            </div>

            <input type="file" id="fileInput" accept="image/*" style="display:none" onchange="handleFileSelect(event)">

            <div class="input-bar">
                <span class="action-icon" onclick="document.getElementById('fileInput').click()" title="Upload">🖼️</span>
                <span class="action-icon" onclick="document.getElementById('fileInput').click()" title="Attach">📎</span>
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
                var chatHistoryList = [];

                function toggleSidebar() {
                    document.getElementById('sidebar').classList.toggle('open');
                }

                function clearAll() {
                    document.getElementById('userInput').value = '';
                    document.getElementById('chatContainer').innerHTML = '';
                    document.getElementById('heroCard').style.display = 'block';
                    document.getElementById('toolsContainer').style.display = 'block';
                }

                function setVoiceResult(text) {
                    document.getElementById('userInput').value = text;
                    sendQuery();
                }

                function handleFileSelect(event) {
                    var file = event.target.files[0];
                    if (file) {
                        appendChat("Image: " + file.name, "ఈ ఫోటో మీ ప్రాజెక్ట్ కోసం విజయవంతంగా లోడ్ చేయబడింది.");
                        document.getElementById('userInput').value = '';
                    }
                }

                function updateHistoryUI() {
                    var listDiv = document.getElementById('historyList');
                    if (chatHistoryList.length === 0) {
                        listDiv.innerHTML = "<div style='font-size: 12px; color: #64748B;'>No recent chats</div>";
                        return;
                    }
                    var html = "";
                    for(var i=0; i<chatHistoryList.length; i++) {
                        html += "<div class='history-item'>";
                        html += "<span class='history-text' onclick='loadHistoryItem(\"" + chatHistoryList[i] + "\")'>💬 " + chatHistoryList[i] + "</span>";
                        html += "<button class='delete-btn' onclick='deleteHistoryItem(" + i + ")' title='Delete'>✕</button>";
                        html += "</div>";
                    }
                    listDiv.innerHTML = html;
                }

                function addHistory(query) {
                    chatHistoryList.unshift(query);
                    updateHistoryUI();
                }

                function deleteHistoryItem(index) {
                    chatHistoryList.splice(index, 1);
                    updateHistoryUI();
                }

                function loadHistoryItem(query) {
                    toggleSidebar();
                    document.getElementById('userInput').value = query;
                    sendQuery();
                }

                function appendChat(userText, aiResponse) {
                    document.getElementById('heroCard').style.display = 'none';
                    document.getElementById('toolsContainer').style.display = 'none';
                    
                    addHistory(userText);

                    var container = document.getElementById('chatContainer');
                    
                    var userBubble = document.createElement('div');
                    userBubble.className = 'chat-bubble-user';
           
