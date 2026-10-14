package com.example.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

class JarvisSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted

    private val _isWakeWordListening = MutableStateFlow(false)
    val isWakeWordListening: StateFlow<Boolean> = _isWakeWordListening

    private var continuousRecognizer: SpeechRecognizer? = null
    private var onSpeechResultCallback: ((String) -> Unit)? = null
    private var onWakeWordTriggeredCallback: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            val bnLocale = Locale("bn", "BD")
            val result = tts?.setLanguage(bnLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to general Bengali or default locale
                tts?.setLanguage(Locale("bn"))
            }

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
        }
    }

    fun setMuted(muted: Boolean) {
        _isMuted.value = muted
        if (muted) {
            stopSpeaking()
        }
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        if (_isMuted.value || text.isBlank() || !isTtsInitialized) {
            onComplete?.invoke()
            return
        }

        stopSpeaking()
        val utteranceId = "JARVIS_${System.currentTimeMillis()}"
        val params = Bundle()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        _isSpeaking.value = true
    }

    fun stopSpeaking() {
        if (_isSpeaking.value) {
            tts?.stop()
            _isSpeaking.value = false
        }
    }

    fun setListeningState(listening: Boolean) {
        _isListening.value = listening
    }

    fun createSpeechIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("bn-BD", "bn", "en-US"))
            putExtra(RecognizerIntent.EXTRA_PROMPT, "JARVIS শুনছে... বাংলায় বলুন")
        }
    }

    fun startContinuousWakeWordListening(
        onWakeWordTriggered: () -> Unit,
        onSpeechCommandResult: (String) -> Unit
    ) {
        this.onWakeWordTriggeredCallback = onWakeWordTriggered
        this.onSpeechResultCallback = onSpeechCommandResult
        _isWakeWordListening.value = true

        mainHandler.post {
            try {
                if (SpeechRecognizer.isRecognitionAvailable(context)) {
                    continuousRecognizer?.destroy()
                    continuousRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(object : RecognitionListener {
                            override fun onReadyForSpeech(params: Bundle?) {}
                            override fun onBeginningOfSpeech() {}
                            override fun onRmsChanged(rmsdB: Float) {}
                            override fun onBufferReceived(buffer: ByteArray?) {}
                            override fun onEndOfSpeech() {}

                            override fun onError(error: Int) {
                                // Restart listening loop if wake word mode is still on
                                if (_isWakeWordListening.value) {
                                    mainHandler.postDelayed({
                                        restartContinuousListening()
                                    }, 1200)
                                }
                            }

                            override fun onResults(results: Bundle?) {
                                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                if (!matches.isNullOrEmpty()) {
                                    val heard = matches[0].lowercase(Locale.ROOT)
                                    if (heard.contains("jarvis") || heard.contains("জার্ভিস") || heard.contains("hey jarvis") || heard.contains("হেই")) {
                                        onWakeWordTriggeredCallback?.invoke()
                                    } else {
                                        onSpeechResultCallback?.invoke(matches[0])
                                    }
                                }
                                if (_isWakeWordListening.value) {
                                    mainHandler.postDelayed({
                                        restartContinuousListening()
                                    }, 800)
                                }
                            }

                            override fun onPartialResults(partialResults: Bundle?) {
                                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                if (!matches.isNullOrEmpty()) {
                                    val heard = matches[0].lowercase(Locale.ROOT)
                                    if (heard.contains("jarvis") || heard.contains("জার্ভিস") || heard.contains("hey jarvis")) {
                                        onWakeWordTriggeredCallback?.invoke()
                                    }
                                }
                            }

                            override fun onEvent(eventType: Int, params: Bundle?) {}
                        })
                    }
                    restartContinuousListening()
                }
            } catch (_: Exception) {}
        }
    }

    private fun restartContinuousListening() {
        if (!_isWakeWordListening.value) return
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            continuousRecognizer?.startListening(intent)
        } catch (_: Exception) {}
    }

    fun stopContinuousWakeWordListening() {
        _isWakeWordListening.value = false
        mainHandler.post {
            try {
                continuousRecognizer?.stopListening()
                continuousRecognizer?.destroy()
                continuousRecognizer = null
            } catch (_: Exception) {}
        }
    }

    fun shutdown() {
        stopSpeaking()
        stopContinuousWakeWordListening()
        tts?.shutdown()
        tts = null
    }
}
