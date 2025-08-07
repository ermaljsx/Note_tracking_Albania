package com.voiceai.voicenotes.data.stt

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SpeechToTextManager(private val app: Application) : RecognitionListener {
    private var recognizer: SpeechRecognizer? = null

    private val _transcripts = MutableSharedFlow<String>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val transcripts = _transcripts.asSharedFlow()

    private val _isListening = MutableSharedFlow<Boolean>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val isListening = _isListening.asSharedFlow()

    fun startListening(languageTag: String?) {
        stopListening()
        recognizer = SpeechRecognizer.createSpeechRecognizer(app).also { sr ->
            sr.setRecognitionListener(this)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                if (!languageTag.isNullOrBlank()) putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
            }
            _isListening.tryEmit(true)
            sr.startListening(intent)
        }
    }

    fun stopListening() {
        recognizer?.stopListening()
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
        _isListening.tryEmit(false)
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() { _isListening.tryEmit(false) }
    override fun onError(error: Int) { _isListening.tryEmit(false) }
    override fun onResults(results: Bundle?) {
        val texts = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = texts?.firstOrNull() ?: return
        _transcripts.tryEmit(text)
    }
    override fun onPartialResults(partialResults: Bundle?) {
        val texts = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = texts?.firstOrNull() ?: return
        _transcripts.tryEmit(text)
    }
    override fun onEvent(eventType: Int, params: Bundle?) {}
}