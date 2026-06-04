package com.example.androiddatingapp.data.api

import com.example.androiddatingapp.BuildConfig
import com.example.androiddatingapp.data.api.dto.ApiErrorBody
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private val gson = Gson()

    @Volatile
    private var authTokenProvider: () -> String? = { null }

    fun setAuthTokenProvider(provider: () -> String?) {
        authTokenProvider = provider
    }

    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        authTokenProvider()?.let { token ->
            requestBuilder.header("Authorization", "Bearer $token")
        }
        chain.proceed(requestBuilder.build())
    }

    /** localtunnel (loca.lt) показывает interstitial без этого заголовка. */
    private val tunnelInterceptor = Interceptor { chain ->
        val request = chain.request()
        val host = request.url.host
        if (host.endsWith("loca.lt")) {
            chain.proceed(
                request.newBuilder()
                    .header("bypass-tunnel-reminder", "true")
                    .build(),
            )
        } else {
            chain.proceed(request)
        }
    }

    private fun baseClientBuilder(): OkHttpClient.Builder =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .addInterceptor(tunnelInterceptor)

    /** API + Retrofit: с JWT. */
    val httpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        baseClientBuilder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    /**
     * MinIO presigned URL: без Authorization, иначе S3/MinIO может отклонить GET (видео/аватар не грузятся).
     */
    val mediaHttpClient: OkHttpClient by lazy {
        baseClientBuilder().build()
    }

    val api: DatingApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(DatingApiService::class.java)
    }

    fun parseErrorMessage(throwable: Throwable): String {
        if (throwable !is HttpException) {
            val msg = throwable.message.orEmpty()
            if (msg.contains("Unable to resolve host", ignoreCase = true) ||
                msg.contains("failed to connect", ignoreCase = true) ||
                msg.contains("timeout", ignoreCase = true)
            ) {
                return "Нет связи с сервером (${BuildConfig.API_BASE_URL}). " +
                    "Запустите бэкенд, start-tunnel.bat и start-tunnel-minio.bat."
            }
            return throwable.message ?: "Неизвестная ошибка"
        }
        val body = throwable.response()?.errorBody()?.string()
        if (!body.isNullOrBlank()) {
            try {
                val parsed = gson.fromJson(body, ApiErrorBody::class.java)
                parsed.error?.let { raw ->
                    return when {
                        raw.contains("City not found", ignoreCase = true) ->
                            "Город не найден на сервере. Выберите город из списка подсказок."
                        raw.contains("radiusKm", ignoreCase = true) ->
                            "Радиус слишком большой. Максимум 10 000 км."
                        raw.contains("minAge", ignoreCase = true) || raw.contains("maxAge", ignoreCase = true) ->
                            "Проверьте диапазон возраста (18–65 лет)."
                        else -> raw
                    }
                }
                parsed.errors?.let { errors ->
                    val first = errors.values.firstOrNull()?.toString()
                    if (!first.isNullOrBlank()) return first
                }
            } catch (_: Exception) {
                // ignore parse errors
            }
        }
        return when (throwable.code()) {
            401 -> "Требуется авторизация. Перезапустите приложение."
            404 -> "Сервис недоступен"
            else -> "Ошибка сервера (${throwable.code()})"
        }
    }
}
