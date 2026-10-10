package com.example.aisameer

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private val RECORD_AUDIO_REQUEST_CODE = 101
    private val SPEECH_REQUEST_CODE = 102
    private var tts: TextToSpeech? = null
    private lateinit var chatContainer: LinearLayout
    private lateinit var inputField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_REQUEST_CODE)
        }

        // మెయిన్ లేఅవుట్ (డార్క్ థీమ్ - గెమిని స్టైల్)
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF050B18.toInt())
            setPadding(24, 24, 24, 24)
        }

        // హెడర్
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 24
            }
        }

        val titleView = TextView(this).apply {
            text = "AI Sameer"
            textSize = 20f
            setTextColor(0xFF38BDF8.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val newChatBtn = Button(this).apply {
            text = "New Chat"
            textSize = 12f
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFF1E293B.toInt())
            setOnClickListener {
                chatContainer.removeAllViews()
                addMessage("AI Sameer", "New Chat Started. How can I help you today?")
            }
        }

        headerLayout.addView(titleView)
        headerLayout.addView(newChatBtn)
        rootLayout.addView(headerLayout)

        // చాట్ కంటైనర్ (స్క్రోలబుల్)
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f).apply {
                bottomMargin = 16
            }
        }

        chatContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scrollView.addView(chatContainer)
        rootLayout.addView(scrollView)

        // వెల్‌కమ్ మెసేజ్
        addMessage("AI Sameer", "Hello! I am AI Sameer. Your Personal AI Assistant. Ask anything or use voice input.")

        // టూల్స్ గ్రిడ్ బటన్స్
        val gridLayout = GridLayout(this).apply {
            columnCount = 2
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 16
            }
        }

        val tools = arrayOf("▶ YouTube Script", "🖼 Create Image", "🎥 Text to Video", "📄 Summarize")
        for (tool in tools) {
            val btn = Button(this).apply {
                text = tool
                textSize = 12f
                setTextColor(0xFFE2E8F0.toInt())
                setBackgroundColor(0xFF111833.toInt())
                setPadding(16, 16, 16, 16)
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(8, 8, 8, 8)
                }
                setOnClickListener {
                    addMessage("Feature", "$tool activated successfully.")
                    speak("$tool activated")
                }
            }
            gridLayout.addView(btn)
        }
        rootLayout.addView(gridLayout)

        // బాటమ్ ఇన్‌పుట్ బార్
        val inputBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(0xFF0F172A.toInt())
            setPadding(16, 8, 16, 8)
        }

        inputField = EditText(this).apply {
            hint = "Ask AI Sameer..."
            setHintTextColor(0xFF64748B.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 14f
            background = null
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val micBtn = Button(this).apply {
            text = "🎙️"
            textSize = 14f
            setBackgroundColor(0xFF1E293B.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            setOnClickListener { startVoiceInput() }
        }

        val sendBtn = Button(this).apply {
            text = "➔"
            textSize = 14f
            setBackgroundColor(0xFF00C6FF.toInt())
            setTextColor(0xFF000000.toInt())
            setOnClickListener { handleSend() }
        }

        inputBar.addView(inputField)
        inputBar.addView(micBtn)
        inputBar.addView(sendBtn)
        rootLayout.addView(inputBar)

        setContentView(rootLayout)
    }

    private fun addMessage(sender: String, message: String) {
        val msgBox = TextView(this).apply {
            text = "$sender:\n$message"
            textSize = 13f
            setTextColor(0xFFCBD5E1.toInt())
            setBackgroundColor(0xFF111833.toInt())
            setPadding(20, 16, 20, 16)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 10
            }
        }
        chatContainer.addView(msgBox)
    }

    private fun handleSend() {
        val query = inputField.text.toString().trim()
        if (query.isNotEmpty()) {
            inputField.setText("")
            addMessage("You", query)
            val response = "మీరు అడిగిన ప్రశ్నకి తక్షణ సమాధానం ఇక్కడ అందించబడింది."
            addMessage("AI Sameer", response)
            speak(response)
        }
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "te-IN")
        }
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
        } catch (e: Exception) {
            Toast.makeText(this, "Voice input not supported", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("te", "IN")
        }
    }

    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0) ?: ""
            if (spokenText.isNotEmpty()) {
                inputField.setText(spokenText)
                handleSend()
            }
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
