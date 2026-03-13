package com.example.sheloans.utils

import android.util.Base64
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

object MpesaClient {
    private const val BASE_URL = "https://sandbox.safaricom.co.ke/"
    

    private const val CONSUMER_KEY = "vLM1or8IMeUyZsPiNAnj8uZG39GZkDOT1aPkAubYGGEW0bLI"
    private const val CONSUMER_SECRET = "nZohslOBuzke7Pe52bEtUvozIhBjvqemIBTXkB7gOKStf9GkBhaXYv768cLTkzqm"
    

    const val BUSINESS_SHORT_CODE = "174379"
    

    const val PASSKEY = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919" //

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val instance: MpesaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(MpesaApiService::class.java)
    }

    fun getAuthHeader(): String {
        val keys = "$CONSUMER_KEY:$CONSUMER_SECRET"
        return "Basic " + Base64.encodeToString(keys.toByteArray(), Base64.NO_WRAP)
    }

    fun getTimestamp(): String {
        return SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
    }

    fun getPassword(shortCode: String, passkey: String, timestamp: String): String {
        val str = shortCode + passkey + timestamp
        return Base64.encodeToString(str.toByteArray(), Base64.NO_WRAP)
    }
}
