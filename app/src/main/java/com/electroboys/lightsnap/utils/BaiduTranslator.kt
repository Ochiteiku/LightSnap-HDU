package com.electroboys.lightsnap.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.electroboys.lightsnap.R

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

object BaiduTranslator {
    // 百度翻译API的APP_ID，用于标识调用的来源
    private const val APP_ID = "20250513002356112"

    // 百度翻译API的SECRET_KEY，用于生成签名
    private const val SECRET_KEY = "_mp8Xsp_9f9FrCVAdoO8"

    // 百度翻译API的URL，用于发送翻译请求
    private const val API_URL = "https://fanyi-api.baidu.com/api/trans/vip/translate"

    private const val TAG = "BaiduTranslator"

    // 使用OkHttpClient进行网络请求，通过by lazy实现延迟初始化
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS) // 设置连接超时时间为10秒
            .readTimeout(10, TimeUnit.SECONDS) // 设置读取超时时间为10秒
            .build()
    }

    /**
     * 翻译文本的方法
     * @param text 需要翻译的文本内容
     * @param from 源语言，默认为"auto"（自动检测）
     * @param to 目标语言，默认为"zh"（中文）
     * @return 翻译后的文本
     */
    suspend fun translate(
        context: Activity,
        text: String,

        callback: TranslationCallback? = null
    ) = withContext(Dispatchers.IO) {
        // 生成随机盐值，用于签名
        val salt = System.currentTimeMillis().toString()
        // 生成签名
        val sign = generateSign(text, salt)

        // 构建请求体，包含需要翻译的文本、源语言、目标语言、APP_ID、盐值和签名
        val formBody = FormBody.Builder()
            .add("q", text)
            .add("from", "auto")
            .add("to", "zh")
            .add("appid", APP_ID)
            .add("salt", salt)
            .add("sign", sign)
            .build()

        // 构建HTTP请求
        val request = Request.Builder()
            .url(API_URL) // 设置请求的URL
            .post(formBody) // 设置请求方法为POST，并附带请求体
            .build()

        // 执行请求并处理响应
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Translation failed") // 如果响应不成功，抛出异常
            val json = response.body?.string() // 解析响应体中的翻译结果
            Log.d(TAG, "parseResult: $json")
            json?.let {
                // 解析JSON字符串
                Log.d(TAG, "parseResult: $it")
                // 解析JSON对象
                val jsonObject = JSONObject(it)
                val errorCode = jsonObject.optString("error_code")
                if (errorCode != null&&errorCode.isNotEmpty()) {
                    // 如果有错误码，说明翻译失败，抛出异常
                    val errorMsg = jsonObject.optString("error_msg")
                    callback?.onFailure("Translation failed: $errorMsg") // 调用回调函数，通知翻译失败
                } else {
                    callback?.onSuccess(it) // 调用回调函数，通知翻译结果
                    val transResult = jsonObject.getJSONArray("trans_result")
                    val result = StringBuilder()
                    for (i in 0 until transResult.length()) {
                        val src = transResult.getJSONObject(i).getString("dst")
                        if (src.isNotEmpty())
                        {
                            result.append(src).append("\n")
                        }
                    }
                    callback?.onSuccess(result.toString()) // 调用回调函数，通知翻译结果
                    context.runOnUiThread {
                        showTranslationDialog(context,text, result.toString()) // 显示翻译结果对话框
                    }
                }

            }

        }
    }

    /**
     * 生成签名的方法
     * @param text 需要翻译的文本内容
     * @param salt 随机盐值
     * @return 生成的签名字符串
     */
    private fun generateSign(text: String, salt: String): String {
        // 拼接字符串，格式为：APP_ID + text + salt + SECRET_KEY
        val input = APP_ID + text + salt + SECRET_KEY
        // 使用MD5算法计算字符串的哈希值，并转换为十六进制字符串作为签名
        return MessageDigest.getInstance("MD5")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }



    private fun showTranslationDialog(
        context: Context,
        fromText: String,
        translatedText: String,

        ) {
        // 使用自定义样式
        val dialog = Dialog(context, R.style.CenterBottomSheet)
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_trans_result, null)
        dialog.setContentView(view)

        // 设置默认展开高度
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as Dialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
                behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
            }
        }

        val tvContent = view.findViewById<TextView>(R.id.tv_content)
        val tvTransContent = view.findViewById<TextView>(R.id.tv_trans_content)

        tvContent.text = fromText
        tvTransContent.text = translatedText

        view.findViewById<View>(R.id.tv_tips_copy).setOnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("translatedText", translatedText)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "翻译结果已复制到剪贴板", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        view.findViewById<View>(R.id.tv_tips_confirm).setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.apply {
            setGravity(Gravity.CENTER)
        }
        dialog.show()


    }

    interface TranslationCallback {
        fun onSuccess(translatedText: String)
        fun onFailure(error: String)
    }
}

