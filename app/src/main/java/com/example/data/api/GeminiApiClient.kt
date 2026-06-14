package com.aistudio.sublimationerp.data.api

import com.aistudio.sublimationerp.BuildConfig

class GeminiApiClient {
    fun makeApiCall() {
        val key = BuildConfig.GEMINI_API_KEY
        if (key == "MY_GEMINI_API_KEY" || key.isBlank()) {
            throw IllegalStateException("Gemini API key not configured. Set GEMINI_API_KEY in your .env file.")
        }
        // Dummy function
    }
}
