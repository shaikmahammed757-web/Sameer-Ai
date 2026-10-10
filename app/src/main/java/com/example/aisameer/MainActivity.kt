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
                    background: #090E17;
                    color: #FFFFFF;
                    font-family: sans-serif;
                    padding: 16px;
                    padding-bottom: 120px;
                }
                .header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 20px;
                }
                .header-left {
                    display: flex;
                    align-items: center;
                    gap: 12px;
                }
                .menu-icon { font-size: 22px; cursor: pointer; color: #94A3B8; font-weight: bold; }
                .app-title { font-size: 18px; font-weight: 600; color: #E2E8F0; }
                
                .header-actions { display: flex; gap: 12px; align-items: center; }
                .icon-btn {
                    color: #94A3B8;
                    font-size: 18px;
                    cursor: pointer;
                    background: transparent;
                    border: none;
                }
                
                /* సైడ్ హిస్టరీ డ్రాయర్ */
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
                    text-align: center;
                    margin: 40px 0 30px 0;
                }
                .greeting-title { font-size: 24px; font-weight: 700; background: linear-gradient(90deg, #4285F4, #9B72CB, #D96570); -webkit-background-clip: text; -webkit-text-fill-color: transparent; margin-bottom: 6px; }
                
                /* ప్లస్ బటన్ పాపప్ మోడల్ */
                .modal {
                    position: fixed;
                    bottom: 0;
                    left: 0;
                    right: 0;
                    background: #111827;
                    border-top: 1px solid #1F2937;
                    border-radius: 24px 24px 0 0;
                    padding: 20px;
                    z-index: 2000;
                    display: none;
                }
                .modal.open { display: block; }
                .modal-grid {
                    display: grid;
                    grid-template-columns: repeat(3, 1fr);
                    gap: 12px;
                    margin-bottom: 16px;
                }
                .modal-card {
                    background: #1F2937;
                    border: 1px solid #374151;
                    border-radius: 16px;
                    padding: 16px 10px;
                    text-align: center;
                    cursor: pointer;
                    font-size: 12px;
                    color: #E5E7EB;
                }
                .modal-card div:first-child { font-size: 20px; margin-bottom: 6px; }
                .modal-item {
                    padding: 12px 0;
                    border-bottom: 1px solid #1F2937;
                    font-size: 13px;
                    cursor: pointer;
                    color: #E5E7EB;
                    display: flex;
                    align-items: center;
                    gap: 12px;
                }

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
                .sub-box {
                    background: linear-gradient(135deg, #1E1B4B 0%, #312E81 100%);
                    border: 1px solid #6366F1;
                    padding: 14px;
                    border-radius: 14px;
                    text-align: center;
                    margin-top: 10px;
                }
                .upgrade-btn {
                    background: linear-gradient(135deg, #6366F1, #EC4899);
                    color: white;
                    border: none;
                    padding: 8px 16px;
                    border-radius: 16px;
                    font-weight: bold;
                    font-size: 11px;
                    cursor: pointer;
                    margin-top: 8px;
                }

                .input-bar {
                    position: fixed;
                    bottom: 20px;
                    left: 16px;
                    right: 16px;
                    background: #111827;
                    border: 1px solid #374151;
                    border-radius: 30px;
                    padding: 8px 14px;
                    display: flex;
                    align-items: center;
                    gap: 12px;
                }
                .input-bar input { background: transparent; border: none; color: #FFF; width: 100%; outline: none; font-size: 14px; }
                .plus-btn { font-size: 22px; color: #94A3B8; cursor: pointer; font-weight: bold; background: none; border: none; }
                .mic-btn { font-size: 18px; color: #94A3B8; cursor: pointer; background: none; border: none; }
                .send-circle {
                    width: 36px;
                    height: 36px;
                    background: #3B82F6;
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    color: white;
                    cursor: pointer;
                    flex-shrink: 0;
                }
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

            <!-- ప్లస్ బటన్ పాపప్ మోడల్ -->
            <div class="modal" id="actionModal">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px;">
                    <b style="font-size: 14px; color: #E5E7EB;">Add to chat</b>
                    <span onclick="toggleModal()" style="cursor: pointer; color: #94A3B8; font-size: 16px;">✕</span>
                </div>
                <div class="modal-grid">
                    <div class="modal-card" onclick="runAIAction('photos')">
                        <div>🖼️</div>
                        <div>Photos</div>
                    </div>
                    <div class="modal-card" onclick="runAIAction('camera')">
                        <div>📷</div>
                        <div>Camera</div>
                    </div>
                    <div class="modal-card" onclick="runAIAction('avatar')">
                        <div>✨</div>
                        <div>Avatar</div>
                    </div>
                </div>
                <div class="modal-item" onclick="runAIAction('image')">🖼️ <div><b>Create an Image</b><br><span style="font-size: 11px; color: #94A3B8;">Generate custom visuals</span></div></div>
                <div class="modal-item" onclick="runAIAction('txt2vid')">🎥 <div><b>Text to Video</b><br><span style="font-size: 11px; color: #94A3B8;">4 daily free limit</span></div></div>
                <div class="modal-item" onclick="runAIAction('youtube')">▶ <div><b>YouTube Script</b><br><span style="font-size: 11px; color: #94A3B8;">Concept to script</span></div></div>
                <div class="modal-item" onclick="runAIAction('summary')">📄 <div><b>Summarize Document</b><br><span style="font-size: 11px; color: #94A3B8;">Key insights</span></div></div>
                <div class="modal-item" onclick="runAIAction('music')">🎵 <div><b>Music</b><br><span style="font-size: 11px; color: #94A3B8;">Make audio tracks</span></div></div>
                <div class="modal-item" onclick="runAIAction('canvas')">📝 <div><b>Canvas</b><br><span style="font-size: 11px; color: #94A3B8;">Code, write or slides</span></div></div>
                <div class="modal-item" onclick="runAIAction('research')">🔍 <div><b>Deep Research</b><br><span style="font-size: 11px; color: #94A3B8;">Get detailed reports</span></div></div>
            </div>

            <div class="header">
                <div class="header-left">
                    <div class="menu-icon" onclick="toggleSidebar()" title="Menu">≡</div>
                    <div class="app-title">AI Sameer</div>
                </div>
                <div class="header-actions">
                    <button class="icon-btn" onclick="clearAll()" title="New Chat">✏️</button>
                </div>
            </div>

            <div class="hero-card" id="heroCard">
                <div class="greeting-title">Hello, Sameer</div>
            </div>

            <div class="chat-container" id="chatContainer"></div>

            <input type="file" id="fileInput" accept="image/*" style="display:none" onchange="handleFileSelect(event)">

            <div class="input-bar">
                <button class="plus-btn" onclick="toggleModal()" title="Add Options">＋</button>
                <input type="text" id="userInput" placeholder="Ask AI Sameer...">
                <button class="mic-btn" onclick="AndroidApp.startVoiceInput()" title="Speak">🎙️</button>
                <div class="send-circle" onclick="sendQuery()">➔</div>
            </div>

            <script>
                var chatHistoryList = [];
                var dailyVideoCount = 0;

                function toggleSidebar() {
                    document.getElementById('sidebar').classList.toggle('open');
                }

                function toggleModal() {
                    document.getElementById('actionModal').classList.toggle('open');
                }

                function clearAll() {
                    document.getElementById('userInput').value = '';
                    document.getElementById('chatContainer').innerHTML = '';
                    document.getElementById('heroCard').style.display = 'block';
                }

                function setVoiceResult(text) {
                    document.getElementById('userInput').value = text;
                    sendQuery();
                }

                function handleFileSelect(event) {
                    var file = event.target.files[0];
                    if (file) {
                        appendChat("Photo: " + file.name, "ఈ ఫోటో మీ ప్రాజెక్ట్ కోసం విజయవంతంగా లోడ్ చేయబడింది.");
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

                function appendChat(userText, aiResponse, isLimit) {
                    document.getElementById('heroCard').style.display = 'none';
                    document.getElementById('actionModal').classList.remove('open');
                    
                    addHistory(userText);

                    var container = document.getElementById('chatContainer');
                    
                    var userBubble = document.createElement('div');
                    userBubble.className = 'chat-bubble-user';
                    userBubble.innerText = userText;
                    container.appendChild(userBubble);
                    
                    var aiBubble = document.createElement('div');
                    aiBubble.className = 'chat-bubble-ai';
                    
                    aiBubble.innerHTML = "<b>AI Sameer:</b><br><br>" + aiResponse;
                    
                    if(isLimit) {
                        aiBubble.innerHTML += "<div class='sub-box'><b>🔒 Limit Reached (4/4 Videos)</b><br>ఉచిత రోజువారీ లిమిట్ ముగిసింది. అన్‌లిమిటెడ్ వీడియోల కోసం ప్రో సబ్‌స్క్రిప్షన్ తీసుకోండి!<br><button class='upgrade-btn' onclick='alert(\"Upgrade to Pro for ₹199/month\")'>🚀 Upgrade to Pro</button></div>";
                    } else {
                        aiBubble.innerHTML += "<br><button class='tts-speaker-btn' onclick='AndroidApp.speakText(\"AI Sameer answered\")'>🔊 Listen</button>";
                        AndroidApp.speakText(aiResponse);
                    }
                    
                    container.appendChild(aiBubble);
               
