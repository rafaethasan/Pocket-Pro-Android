package com.softx.app.data

import android.content.Context
import android.webkit.CookieManager
import com.softx.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url
import java.net.URI
import java.util.concurrent.TimeUnit

interface PocketNativeApiService {
    @POST("api/pocket/native/auth/login")
    suspend fun login(@Body request: PocketNativeLoginRequest): PocketNativeAuthResponse

    @POST("api/pocket/native/auth/register")
    suspend fun register(@Body request: PocketNativeRegisterRequest): PocketNativeAuthResponse

    @POST("api/pocket/native/auth/guest")
    suspend fun quickStart(@Body request: PocketNativeGuestRequest): PocketNativeAuthResponse

    @GET("api/pocket/native/auth/me")
    suspend fun me(): PocketNativeAuthResponse

    @POST("api/pocket/native/auth/logout")
    suspend fun logout(): PocketNativeAuthResponse

    @GET("api/pocket/native/dashboard")
    suspend fun dashboard(): PocketNativeDashboardResponse

    @GET("api/pocket/native/records")
    suspend fun records(
        @retrofit2.http.Query("month") monthValue: String,
    ): PocketNativeRecordsResponse

    @GET("api/pocket/native/accounts")
    suspend fun accounts(): PocketNativeAccountsResponse

    @GET("api/pocket/native/recurring")
    suspend fun recurring(): PocketNativeRecurringResponse

    @GET("api/pocket/native/budget")
    suspend fun budget(): PocketNativeBudgetResponse

    @POST("api/pocket/native/budget/save")
    suspend fun saveBudget(
        @Body request: PocketNativeBudgetSaveRequest,
    ): PocketNativeBudgetResponse

    @GET("api/pocket/native/goals")
    suspend fun goals(): PocketNativeGoalsResponse

    @POST("api/pocket/native/goals/save")
    suspend fun saveGoal(
        @Body request: PocketNativeGoalSaveRequest,
    ): PocketNativeGoalsResponse

    @GET("api/pocket/native/categories")
    suspend fun categories(
        @retrofit2.http.Query("kind") kind: String,
    ): PocketNativeCategoryManagerResponse

    @POST("api/pocket/native/categories/save")
    suspend fun saveCategory(
        @Body request: PocketNativeCategorySaveRequest,
    ): PocketNativeCategoryManagerResponse

    @POST("api/pocket/native/categories/delete")
    suspend fun deleteCategory(
        @Body request: PocketNativeCategoryDeleteRequest,
    ): PocketNativeCategoryManagerResponse

    @POST("api/pocket/native/categories/move")
    suspend fun moveCategory(
        @Body request: PocketNativeCategoryMoveRequest,
    ): PocketNativeCategoryManagerResponse

    @POST("api/pocket/native/goals/{goalId}/saved")
    suspend fun addGoalSaved(
        @retrofit2.http.Path("goalId") goalId: Int,
        @Body request: PocketNativeGoalSavedRequest,
    ): PocketNativeGoalsResponse

    @GET("api/pocket/native/transaction/form")
    suspend fun transactionForm(
        @Query("mode") mode: String,
        @Query("edit_kind") editKind: String = "",
        @Query("edit_id") editId: Int = 0,
    ): PocketNativeTransactionFormResponse

    @POST("api/pocket/native/transaction")
    suspend fun saveTransaction(
        @Body request: PocketNativeTransactionSaveRequest,
    ): PocketNativeTransactionSaveResponse

    @FormUrlEncoded
    @POST
    suspend fun saveTransactionByUrl(
        @Url url: String,
        @FieldMap fields: Map<String, String>,
    ): Response<ResponseBody>

    @POST("api/pocket/native/records/delete")
    suspend fun deleteRecord(
        @Body request: PocketNativeRecordDeleteRequest,
    ): PocketNativeTransactionSaveResponse

    @POST
    suspend fun deleteRecordByUrl(
        @Url url: String,
    ): Response<ResponseBody>

    @GET
    suspend fun downloadPdf(
        @Url url: String,
    ): ResponseBody
}

object PocketNativeApiFactory {
    @Volatile
    private var cookieJar: PocketNativeCookieJar? = null

    @Volatile
    private var service: PocketNativeApiService? = null

    fun service(context: Context): PocketNativeApiService {
        service?.let { return it }
        synchronized(this) {
            service?.let { return it }
            val appContext = context.applicationContext
            val logging = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BASIC
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }
            val jar = PocketNativeCookieJar(appContext)
            val client = OkHttpClient.Builder()
                .cookieJar(jar)
                .connectTimeout(25, TimeUnit.SECONDS)
                .readTimeout(25, TimeUnit.SECONDS)
                .writeTimeout(25, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            cookieJar = jar
            service = Retrofit.Builder()
                .baseUrl(BuildConfig.POCKET_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PocketNativeApiService::class.java)
            return service!!
        }
    }

    fun clearSession() {
        cookieJar?.clear()
        runCatching {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
        }
    }

    fun syncSessionToWebView(context: Context) {
        service(context)
        val manager = CookieManager.getInstance()
        manager.setAcceptCookie(true)
        val cookies = cookieJar?.snapshot().orEmpty()
        cookies.forEach { cookie ->
            val targetDomain = cookie.domain.trim()
            if (targetDomain.isBlank()) {
                return@forEach
            }
            val baseUrl = if (cookie.secure) "https://$targetDomain" else "http://$targetDomain"
            val cookieValue = buildString {
                append("${cookie.name}=${cookie.value}")
                append("; Path=${cookie.path}")
                append("; Domain=$targetDomain")
                if (cookie.secure) append("; Secure")
                if (cookie.httpOnly) append("; HttpOnly")
            }
            manager.setCookie(baseUrl, cookieValue)
        }
        manager.flush()
    }

    fun syncSessionFromWebView(context: Context) {
        service(context)
        val manager = CookieManager.getInstance()
        val host = runCatching { URI(BuildConfig.POCKET_BASE_URL).host.orEmpty() }.getOrDefault("")
        if (host.isBlank()) {
            return
        }
        val rawCookies = manager.getCookie(BuildConfig.POCKET_BASE_URL).orEmpty()
        if (rawCookies.isBlank()) {
            return
        }
        val cookiePairs = rawCookies
            .split(";")
            .mapNotNull { item ->
                val clean = item.trim()
                if (clean.isBlank() || !clean.contains("=")) {
                    return@mapNotNull null
                }
                val separator = clean.indexOf("=")
                val name = clean.substring(0, separator).trim()
                val value = clean.substring(separator + 1).trim()
                if (name.isBlank()) null else name to value
            }
            .toMap()
        if (cookiePairs.isEmpty()) {
            return
        }
        cookieJar?.replaceCookiesForHost(host = host, cookies = cookiePairs, secure = BuildConfig.POCKET_BASE_URL.startsWith("https://"))
    }
}
