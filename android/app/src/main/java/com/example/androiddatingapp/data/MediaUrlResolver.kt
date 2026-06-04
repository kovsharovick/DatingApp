package com.example.androiddatingapp.data

import android.net.Uri
import com.example.androiddatingapp.BuildConfig

/**
 * Нормализует URL медиа с бэкенда для туннеля localtunnel и MinIO.
 */
object MediaUrlResolver {

    private const val MINIO_BUCKET = "dating-videos"

    private val minioTunnelUri: Uri? = runCatching {
        Uri.parse(BuildConfig.MINIO_PUBLIC_BASE_URL.trim())
    }.getOrNull()

    private val apiTunnelHost: String? = runCatching {
        Uri.parse(BuildConfig.API_BASE_URL.trim()).host
    }.getOrNull()

    fun resolve(url: String?): String {
        if (url.isNullOrBlank()) return ""

        val trimmed = url.trim()

        if (!trimmed.contains("://")) {
            return buildPublicObjectUrl(trimmed)
        }

        // Presigned нельзя менять host — ломается подпись AWS. Нужен minio.endpoint=туннель на бэке.
        if (isPresignedUrl(trimmed)) {
            return trimmed
        }

        val tunnel = minioTunnelUri ?: return trimmed
        return runCatching {
            val parsed = Uri.parse(trimmed)
            if (!shouldRewrite(parsed)) return trimmed
            parsed.buildUpon()
                .scheme(tunnel.scheme ?: "https")
                .encodedAuthority(tunnel.encodedAuthority)
                .build()
                .toString()
        }.getOrDefault(trimmed)
    }

    private fun buildPublicObjectUrl(objectKey: String): String {
        val base = BuildConfig.MINIO_PUBLIC_BASE_URL.trim().trimEnd('/')
        val key = objectKey.trimStart('/')
        val path = if (key.startsWith("$MINIO_BUCKET/")) key else "$MINIO_BUCKET/$key"
        return "$base/$path"
    }

    private fun isPresignedUrl(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("x-amz-signature=") ||
            lower.contains("x-amz-algorithm=") ||
            lower.contains("x-amz-credential=")
    }

    private fun isLocalOrPrivateHost(host: String): Boolean =
        host == "localhost" ||
            host == "127.0.0.1" ||
            host == "10.0.2.2" ||
            host == "minio" ||
            host.startsWith("192.168.")

    private fun shouldRewrite(uri: Uri): Boolean {
        val host = uri.host ?: return false
        if (host.endsWith("loca.lt")) return false
        if (apiTunnelHost != null && host == apiTunnelHost) return false
        if (isLocalOrPrivateHost(host)) return true
        return uri.port == 9000
    }
}
