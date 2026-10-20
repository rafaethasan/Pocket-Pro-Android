package com.softx.app.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import org.json.JSONArray
import org.json.JSONObject
import java.security.KeyStore
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class PocketNativeCookieJar(
    context: Context,
) : CookieJar {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val store = ConcurrentHashMap<String, MutableList<Cookie>>()

    init {
        store.putAll(readPersistedCookies())
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val key = url.host
        val existing = store[key]
            ?.filterNot { cached ->
                cookies.any { incoming -> incoming.name == cached.name && incoming.matches(url) }
            }
            ?.toMutableList()
            ?: mutableListOf()
        existing.addAll(cookies)
        store[key] = existing
        persistCookies()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val key = url.host
        val now = System.currentTimeMillis()
        val validCookies = store[key]
            ?.filter { it.expiresAt >= now && it.matches(url) }
            ?.toMutableList()
            ?: mutableListOf()
        store[key] = validCookies
        persistCookies()
        return validCookies
    }

    fun clear() {
        store.clear()
        prefs.edit()
            .remove(PREF_KEY)
            .remove(ENCRYPTED_PREF_KEY)
            .apply()
    }

    fun snapshot(): List<Cookie> {
        return store.values.flatten()
    }

    fun replaceCookiesForHost(
        host: String,
        cookies: Map<String, String>,
        secure: Boolean = true,
    ) {
        val cleanHost = host.trim()
        if (cleanHost.isBlank()) {
            return
        }
        val nextCookies = cookies.entries
            .filter { it.key.isNotBlank() }
            .mapNotNull { (name, value) ->
                runCatching {
                    Cookie.Builder()
                        .name(name.trim())
                        .value(value)
                        .hostOnlyDomain(cleanHost)
                        .path("/")
                        .expiresAt(Date(System.currentTimeMillis() + 1000L * 60L * 60L * 24L * 30L).time)
                        .apply {
                            if (secure) secure()
                        }
                        .build()
                }.getOrNull()
            }
            .toMutableList()
        if (nextCookies.isEmpty()) {
            return
        }
        store[cleanHost] = nextCookies
        persistCookies()
    }

    private fun persistCookies() {
        val root = JSONObject()
        store.forEach { (host, cookies) ->
            val hostCookies = JSONArray()
            cookies.forEach { cookie ->
                hostCookies.put(
                    JSONObject().apply {
                        put("name", cookie.name)
                        put("value", cookie.value)
                        put("expiresAt", cookie.expiresAt)
                        put("domain", cookie.domain)
                        put("path", cookie.path)
                        put("secure", cookie.secure)
                        put("httpOnly", cookie.httpOnly)
                        put("hostOnly", cookie.hostOnly)
                    },
                )
            }
            root.put(host, hostCookies)
        }
        val encrypted = encrypt(root.toString()) ?: return
        prefs.edit()
            .putString(ENCRYPTED_PREF_KEY, encrypted)
            .remove(PREF_KEY)
            .apply()
    }

    private fun readPersistedCookies(): Map<String, MutableList<Cookie>> {
        val rawJson = readSecurePayload()
        if (rawJson.isBlank()) {
            return emptyMap()
        }
        return runCatching {
            val root = JSONObject(rawJson)
            root.keys().asSequence().associateWith { host ->
                val cookies = mutableListOf<Cookie>()
                val array = root.optJSONArray(host) ?: JSONArray()
                for (index in 0 until array.length()) {
                    val cookieObject = array.optJSONObject(index) ?: continue
                    val cookie = decodeCookie(cookieObject)
                    if (cookie != null) {
                        cookies.add(cookie)
                    }
                }
                cookies
            }
        }.getOrElse { emptyMap() }
    }

    /**
     * Existing installs stored this payload in plaintext. Migrate it once, then remove it.
     * If the Android Keystore is unavailable, do not retain a plaintext session.
     */
    private fun readSecurePayload(): String {
        val encrypted = prefs.getString(ENCRYPTED_PREF_KEY, "").orEmpty()
        if (encrypted.isNotBlank()) {
            return decrypt(encrypted).orEmpty()
        }

        val legacy = prefs.getString(PREF_KEY, "").orEmpty()
        if (legacy.isBlank()) {
            return ""
        }
        val migrated = encrypt(legacy)
        prefs.edit().apply {
            if (migrated != null) {
                putString(ENCRYPTED_PREF_KEY, migrated)
            }
            remove(PREF_KEY)
        }.apply()
        return legacy
    }

    private fun encrypt(payload: String): String? = runCatching {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val ciphertext = cipher.doFinal(payload.toByteArray(Charsets.UTF_8))
        val encodedIv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        val encodedCiphertext = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        "$encodedIv:$encodedCiphertext"
    }.getOrNull()

    private fun decrypt(payload: String): String? = runCatching {
        val parts = payload.split(":", limit = 2)
        require(parts.size == 2)
        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }.getOrNull()

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build(),
            )
        }.generateKey()
    }

    private fun decodeCookie(source: JSONObject): Cookie? {
        val name = source.optString("name", "").trim()
        val value = source.optString("value", "")
        val domain = source.optString("domain", "").trim()
        val path = source.optString("path", "/").ifBlank { "/" }
        val expiresAt = source.optLong("expiresAt", Long.MAX_VALUE)
        if (name.isBlank() || domain.isBlank()) {
            return null
        }

        return runCatching {
            val builder = Cookie.Builder()
                .name(name)
                .value(value)
                .path(path)
            if (source.optBoolean("hostOnly", false)) {
                builder.hostOnlyDomain(domain)
            } else {
                builder.domain(domain)
            }
            if (expiresAt > 0) {
                builder.expiresAt(expiresAt)
            }
            if (source.optBoolean("secure", false)) {
                builder.secure()
            }
            if (source.optBoolean("httpOnly", false)) {
                builder.httpOnly()
            }
            builder.build()
        }.getOrNull()
    }

    private companion object {
        const val PREFS_NAME = "pocket_native_session"
        const val PREF_KEY = "cookie_store"
        const val ENCRYPTED_PREF_KEY = "cookie_store_encrypted"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "pocket_pro_session_key_v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH_BITS = 128
    }
}
