package com.example.aitrader

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val BASE_URL = "https://finnhub.io/api/v1/"
    // IMPORTANT: Replace this placeholder with your actual Finnhub API Key
    // You can get one for free at https://finnhub.io/dashboard
    const val API_KEY = "dajgcb1r01qhhp593b1gdajgcb1r01qhhp593b20"

    // Google Gemini AI Configuration
    // Get your key at https://aistudio.google.com/app/apikey
    const val GEMINI_API_KEY = "AQ.Ab8RN6Kq23U2QyBpCNa-MxfgaQUDWOqBeqY8OEvkrLc-3X9Ukw"
    const val GEMINI_MODEL_NAME = "gemini-flash-latest" 
    private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val geminiApi: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(GEMINI_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }
}
