package com.electroboys.lightsnap.utils

import android.content.Context
import org.json.JSONObject
import java.io.InputStream

object KeyUtil {

    private var configJson: JSONObject? = null

    // 初始化配置
    fun initialize(context: Context) {
        // 读取配置文件，如果无法读取则不进行初始化
        val jsonString = loadJSONFromAsset(context, "key.json")
        if (jsonString.isNullOrEmpty()) {
            // 可以选择在这里打印日志，提醒开发者文件读取失败
            configJson = JSONObject() // 如果文件为空，初始化为空对象
        } else {
            try {
                configJson = JSONObject(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
                // 如果 JSON 解析失败，可以在这里记录错误日志
                configJson = JSONObject() // 解析失败时初始化为空对象
            }
        }
    }

    // 从 assets 中加载 JSON 配置文件
    private fun loadJSONFromAsset(context: Context, fileName: String): String? {
        return try {
            val inputStream: InputStream = context.assets.open(fileName)
            inputStream.bufferedReader().use { it.readText() }
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
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
