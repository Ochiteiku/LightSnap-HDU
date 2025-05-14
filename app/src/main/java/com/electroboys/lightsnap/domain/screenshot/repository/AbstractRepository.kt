package com.electroboys.lightsnap.domain.screenshot.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AbstractRepository {
    suspend fun getSummary(content: String): String {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // 构造 messages 数组
        val messagesArray = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", "You are a helpful assistant.")
            })
            put(JSONObject().apply {
                put("role", "user")
                put("content", "请根据以下内容生成一段简洁摘要，只返回结果文本，不需要解释或格式修饰：\n$content")
            })
        }

        // 构造完整请求体
        val rootObject = JSONObject().apply {
            put("model", "deepseek-chat")
            put("messages", messagesArray)
            put("stream", false)
        }

        val json = rootObject.toString()
        val body = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://api.deepseek.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer sk-afa4e0109bf74082910c90d46c7b43ea")
            .post(body)
            .build()

        return withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                try {
                    val jsonObject = JSONObject(responseBody)
                    val summary = jsonObject
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                    summary.trim()
                } catch (e: Exception) {
                    e.printStackTrace()
                    println("解析摘要失败，原始内容：$responseBody")
                    "解析摘要失败，请检查返回格式"
                }
            } else {
                println("请求失败，状态码：${response.code}, 返回内容：$responseBody")
                "摘要失败: ${response.message} (${response.code})"
            }
        }
    }
}