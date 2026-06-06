package com.nallanudi.app.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Lifecycle-aware Text-To-Speech manager.
 *
 * Exposes [isAvailable] and [isSpeaking] as StateFlows so the UI can
 * reactively update the pronunciation button state (Requirements 3.1–3.5).
 *
 * Call [shutdown] in ViewModel.onCleared() to release TTS resources.
 */
class TtsManager(context: Context) : TextToSpeech.OnInitListener {

    private val tts: TextToSpeech = TextToSpeech(context.applicationContext, this)

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false   // Requirement 3.4
            }

            @Deprecated("Deprecated in API 21")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _isSpeaking.value = false
            }
        })
    }

    /**
     * Called by Android TTS engine when initialisation completes.
     * Sets [isAvailable] based on success status (Requirement 3.2).
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.ENGLISH)
            _isAvailable.value = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
            if (!_isAvailable.value) {
                Log.e(TAG, "TTS language not supported or missing data.")
            }
        } else {
            _isAvailable.value = false
            Log.e(TAG, "TTS initialisation failed with status: $status")
        }
    }

    /**
     * Speak the given English text aloud (Requirement 3.1).
     * Does nothing if TTS is unavailable.
     */
    fun speak(text: String) {
        if (!_isAvailable.value) return
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    /**
     * Stop any ongoing speech.
     */
    fun stop() {
        if (tts.isSpeaking) {
            tts.stop()
            _isSpeaking.value = false
        }
    }

    /**
     * Release TTS resources. Call from ViewModel.onCleared().
     */
    fun shutdown() {
        tts.stop()
        tts.shutdown()
        _isSpeaking.value = false
    }

    companion object {
        private const val TAG = "TtsManager"
        private const val UTTERANCE_ID = "nalla_nudi_tts"
    }
}
