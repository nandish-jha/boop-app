package com.prodash.reminders

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionService
import android.speech.RecognitionService.Callback
import android.speech.SpeechRecognizer
import android.speech.SpeechRecognizer.ERROR_CLIENT

/**
 * Required companion for [BoopVoiceInteractionService] so Android lists BOOP as a
 * selectable digital assistant. Forwards recognition to the platform SpeechRecognizer.
 */
class BoopRecognitionService : RecognitionService() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var activeCallback: Callback? = null

    override fun onStartListening(recognizerIntent: Intent, listener: Callback) {
        activeCallback = listener
        releaseRecognizer()
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            listener.error(ERROR_CLIENT)
            return
        }
        val recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer = recognizer
        recognizer.setRecognitionListener(object : android.speech.RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = listener.readyForSpeech(params)
            override fun onBeginningOfSpeech() = listener.beginningOfSpeech()
            override fun onRmsChanged(rmsdB: Float) = listener.rmsChanged(rmsdB)
            override fun onBufferReceived(buffer: ByteArray?) = listener.bufferReceived(buffer)
            override fun onEndOfSpeech() = listener.endOfSpeech()
            override fun onError(error: Int) {
                listener.error(error)
                releaseRecognizer()
            }
            override fun onResults(results: Bundle?) {
                listener.results(results ?: Bundle())
                releaseRecognizer()
            }
            override fun onPartialResults(partialResults: Bundle?) {
                listener.partialResults(partialResults ?: Bundle())
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        try {
            recognizer.startListening(recognizerIntent)
        } catch (_: Throwable) {
            listener.error(ERROR_CLIENT)
            releaseRecognizer()
        }
    }

    override fun onCancel(listener: Callback) {
        activeCallback = listener
        try {
            speechRecognizer?.cancel()
        } catch (_: Throwable) {
        }
        releaseRecognizer()
        listener.error(ERROR_CLIENT)
    }

    override fun onStopListening(listener: Callback) {
        activeCallback = listener
        try {
            speechRecognizer?.stopListening()
        } catch (_: Throwable) {
        }
    }

    override fun onDestroy() {
        releaseRecognizer()
        super.onDestroy()
    }

    private fun releaseRecognizer() {
        try {
            speechRecognizer?.destroy()
        } catch (_: Throwable) {
        }
        speechRecognizer = null
        activeCallback = null
    }
}
