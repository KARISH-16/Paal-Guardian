package com.example.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object SupabaseConfig {
    const val PROJECT_URL = "https://czscwcjcxzemdzdqjcrq.supabase.co"
    const val REST_URL = "https://czscwcjcxzemdzdqjcrq.supabase.co/rest/v1"
    const val PUBLISHABLE_KEY = "sb_publishable_nKc247WKDQO9qv2QefW5Uw_3URi3ZOT"
}

data class AuthUser(
    val id: String,
    val email: String,
    val name: String,
    val phone: String,
    val role: String,
    val accessToken: String
)

sealed class AuthResult {
    data class Success(val user: AuthUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class SupabaseAuthService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    fun normalizeEmail(input: String): String {
        val trimmed = input.trim()
        return if (trimmed.contains("@")) {
            trimmed.lowercase()
        } else {
            val cleanPhone = trimmed.replace("+", "").replace(" ", "").replace("-", "")
            "farmer_$cleanPhone@paalguardian.app"
        }
    }

    suspend fun register(
        inputIdentifier: String,
        password: String,
        name: String,
        phone: String,
        role: String // "farmer" or "collection_center"
    ): AuthResult = withContext(Dispatchers.IO) {
        val email = normalizeEmail(inputIdentifier)
        val url = "${SupabaseConfig.PROJECT_URL}/auth/v1/signup"

        val metadata = JSONObject().apply {
            put("name", name)
            put("phone", phone)
            put("role", role)
        }

        val jsonBody = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("data", metadata)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toString().toRequestBody(jsonMedia))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseString = response.body?.string() ?: ""
                Log.d("SupabaseAuth", "Signup status=${response.code} body=$responseString")

                if (response.isSuccessful) {
                    val json = JSONObject(responseString)
                    val id = json.optJSONObject("user")?.optString("id")
                        ?: json.optString("id", "")
                    val token = json.optString("access_token", SupabaseConfig.PUBLISHABLE_KEY)
                    val userMeta = json.optJSONObject("user")?.optJSONObject("user_metadata")
                        ?: json.optJSONObject("user_metadata")
                    val resName = userMeta?.optString("name", name) ?: name
                    val resPhone = userMeta?.optString("phone", phone) ?: phone
                    val resRole = userMeta?.optString("role", role) ?: role

                    AuthResult.Success(
                        AuthUser(
                            id = if (id.isNotEmpty()) id else java.util.UUID.randomUUID().toString(),
                            email = email,
                            name = resName,
                            phone = resPhone,
                            role = resRole,
                            accessToken = token
                        )
                    )
                } else {
                    val errJson = try { JSONObject(responseString) } catch (_: Exception) { null }
                    val msg = errJson?.optString("msg")
                        ?: errJson?.optString("message")
                        ?: errJson?.optString("error_description")
                        ?: "Registration failed with status ${response.code}"
                    AuthResult.Error(msg)
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseAuth", "Signup network error", e)
            AuthResult.Error(e.localizedMessage ?: "Network connection error")
        }
    }

    suspend fun login(
        inputIdentifier: String,
        password: String
    ): AuthResult = withContext(Dispatchers.IO) {
        val email = normalizeEmail(inputIdentifier)
        val url = "${SupabaseConfig.PROJECT_URL}/auth/v1/token?grant_type=password"

        val jsonBody = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toString().toRequestBody(jsonMedia))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseString = response.body?.string() ?: ""
                Log.d("SupabaseAuth", "Login status=${response.code} body=$responseString")

                if (response.isSuccessful) {
                    val json = JSONObject(responseString)
                    val token = json.getString("access_token")
                    val userObj = json.optJSONObject("user")
                    val id = userObj?.optString("id") ?: java.util.UUID.randomUUID().toString()
                    val meta = userObj?.optJSONObject("user_metadata")
                    val name = meta?.optString("name") ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
                    val phone = meta?.optString("phone") ?: inputIdentifier
                    val role = meta?.optString("role") ?: "farmer"

                    AuthResult.Success(
                        AuthUser(
                            id = id,
                            email = email,
                            name = name,
                            phone = phone,
                            role = role,
                            accessToken = token
                        )
                    )
                } else {
                    val errJson = try { JSONObject(responseString) } catch (_: Exception) { null }
                    val msg = errJson?.optString("error_description")
                        ?: errJson?.optString("msg")
                        ?: errJson?.optString("message")
                        ?: "Invalid email/phone or password"
                    AuthResult.Error(msg)
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseAuth", "Login network error", e)
            AuthResult.Error(e.localizedMessage ?: "Network connection error")
        }
    }
}
