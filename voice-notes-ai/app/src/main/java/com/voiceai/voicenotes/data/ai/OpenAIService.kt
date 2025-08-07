package com.voiceai.voicenotes.data.ai

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface OpenAIHttpApi {
    @POST("/v1/chat/completions")
    suspend fun chat(@Body body: ChatRequest): ChatResponse
}

class OpenAIRepository(apiKeyProvider: () -> String?) {
    private val apiKeyProviderInternal = apiKeyProvider

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(ApiKeyInterceptor(apiKeyProviderInternal))
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.openai.com")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(client)
        .build()

    private val api: OpenAIHttpApi = retrofit.create(OpenAIHttpApi::class.java)

    private val enrichedNoteAdapter = moshi.adapter(EnrichedNote::class.java)

    suspend fun enrich(transcript: String, localeTag: String?): EnrichedNote? {
        val system = """
            You are an assistant that cleans and structures voice notes in Albanian and English.
            Return ONLY JSON with fields: ai_note_text (string), tasks (array of { title, due_iso8601? }), suggestions (array of strings).
            - If tasks are present, extract them explicitly with concise titles. Parse relative dates into ISO-8601 in user's locale if possible.
            - If shopping list, keep items one per line in ai_note_text and include 1-3 healthy suggestions.
            - If idea/project, produce an ordered plan (Steps:) in ai_note_text and extract step items as tasks.
            - Keep the language of the original unless requested otherwise.
        """.trimIndent()

        val user = """
            Locale: ${localeTag ?: "auto"}
            Raw note:
            ${transcript}
        """.trimIndent()

        val req = ChatRequest(
            model = "gpt-4o-mini",
            messages = listOf(
                Message("system", system),
                Message("user", user)
            )
        )

        val resp = api.chat(req)
        val content = resp.choices.firstOrNull()?.message?.content ?: return null

        // Try parse JSON directly
        return try {
            enrichedNoteAdapter.fromJson(content)
        } catch (e: Exception) {
            // Fallback: wrap as ai text only
            EnrichedNote(aiNoteText = content)
        }
    }
}

private class ApiKeyInterceptor(private val apiKeyProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val key = apiKeyProvider()
        val newReq = if (!key.isNullOrBlank()) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $key")
                .build()
        } else request
        return chain.proceed(newReq)
    }
}