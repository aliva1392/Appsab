package com.example.data.api

import com.example.BuildConfig

class GeminiApiClient {
    init {
        val key = BuildConfig.GEMINI_API_KEY
        if (key == "MY_GEMINI_API_KEY" || key.isBlank()) {
            throw IllegalStateException("Gemini API key not configured. Set GEMINI_API_KEY in your .env file.")
        }
    }

    fun makeApiCall() {
        // Dummy function
    }
}
