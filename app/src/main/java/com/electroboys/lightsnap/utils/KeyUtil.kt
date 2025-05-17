package com.electroboys.lightsnap.utils

import android.content.Context
import org.json.JSONObject
import java.io.InputStream

object KeyUtil {

    private var configJson: JSONObject? = null

    // 初始化配置
    fun initialize(context: Context) {
        val jsonString = loadJSONFromAsset(context, "key.json")
        configJson = JSONObject(jsonString.orEmpty())
    }

    // 从 assets 中加载 JSON 配置文件
    private fun loadJSONFromAsset(context: Context, fileName: String): String? {
        val json: String?
        try {
            val inputStream: InputStream = context.assets.open(fileName)
            json = inputStream.bufferedReader().use { it.readText() }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    // 获取 DeepSeek API 密钥
    fun getDeepSeekApiKey(): String {
        return configJson?.optString("deepseek_api_key", "") ?: ""
    }

    // 获取其他配置信息
    fun getBaiduAppId(): String {
        return configJson?.optString("baidu_app_id", "") ?: ""
    }

    fun getBaiduApiKey(): String {
        return configJson?.optString("baidu_api_key", "") ?: ""
    }

    fun getQCloudSecretId(): String {
        return configJson?.optString("qcloud_secretId", "") ?: ""
    }

    fun getQCloudSecretKey(): String {
        return configJson?.optString("qcloud_secretKey", "") ?: ""
    }

    fun getQCloudRegion(): String {
        return configJson?.optString("qcloud_region", "") ?: ""
    }

    fun getQCloudBucket(): String {
        return configJson?.optString("qcloud_bucket", "") ?: ""
    }
}