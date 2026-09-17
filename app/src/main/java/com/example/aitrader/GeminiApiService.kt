package com.example.aitrader

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Header

interface GeminiApiService {
    @POST("v1beta/models/gemini-flash-latest:generateContent")
    suspend fun generateContent(
        @Header("X-goog-api-key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
