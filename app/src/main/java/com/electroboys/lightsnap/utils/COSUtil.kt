package com.electroboys.lightsnap.utils

import android.content.Context
import android.util.Log
import com.tencent.cos.xml.CosXmlService
import com.tencent.cos.xml.CosXmlServiceConfig
import com.tencent.cos.xml.exception.CosXmlClientException
import com.tencent.cos.xml.exception.CosXmlServiceException
import com.tencent.cos.xml.listener.CosXmlResultListener
import com.tencent.cos.xml.model.CosXmlRequest
import com.tencent.cos.xml.model.CosXmlResult
import com.tencent.cos.xml.model.bucket.HeadBucketRequest
import com.tencent.cos.xml.model.bucket.HeadBucketResult
import com.tencent.cos.xml.transfer.COSXMLDownloadTask
import com.tencent.cos.xml.transfer.TransferConfig
import com.tencent.cos.xml.transfer.TransferManager
import com.tencent.cos.xml.transfer.TransferStateListener
import com.tencent.qcloud.core.auth.QCloudCredentialProvider
import com.tencent.qcloud.core.auth.ShortTimeCredentialProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

object COSUtil {

    private var cosXmlService: CosXmlService? = null
    private var transferManager: TransferManager? = null
    private var bucket: String? = null


    //用于初始化腾讯云服务，并检查是否可用
    suspend fun initCOS(
        context: Context,
        secretId: String,
        secretKey: String,
        region: String,
        bucket: String
    ): Boolean {
        return try {
            val credentialProvider: QCloudCredentialProvider = ShortTimeCredentialProvider(
                secretId,
                secretKey,
                300 // 凭证有效期 5 分钟
            )

            val serviceConfig = CosXmlServiceConfig.Builder()
                .setRegion(region)
                .isHttps(true)
                .builder()

            cosXmlService = CosXmlService(context.applicationContext, serviceConfig, credentialProvider)

            val transferConfig = TransferConfig.Builder()
                .setDivisionForUpload(2 * 1024 * 1024)
                .setSliceSizeForUpload(1 * 1024 * 1024)
                .setForceSimpleUpload(false)
                .build()

            transferManager = TransferManager(cosXmlService, transferConfig)

            COSUtil.bucket = bucket

            // 使用协程方式异步检查 COS 桶可用性
            checkCosBucketAvailable()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    //检查桶是否可用，使用suspendCancellableCoroutine转化为挂起函数
    suspend fun checkCosBucketAvailable(): Boolean = suspendCancellableCoroutine { continuation ->
        val request = HeadBucketRequest(bucket)
        cosXmlService?.headBucketAsync(request, object : CosXmlResultListener {
            override fun onSuccess(request: CosXmlRequest, result: CosXmlResult) {
                val headResult = result as HeadBucketResult
                Log.i("COSUtil", "桶可用: ${headResult.httpCode}")
                continuation.resume(true)
            }

            override fun onFail(
                request: CosXmlRequest,
                clientException: CosXmlClientException?,
                serviceException: CosXmlServiceException?
            ) {
                clientException?.printStackTrace()
                serviceException?.printStackTrace()
                continuation.resume(false)
            }
        }) ?: continuation.resume(false) // 如果 cosXmlService 为 null，也返回 false
    }

    //上传文件，采用协程的方式，这样可以获取上传的结果
    suspend fun uploadFile(
        cosPath: String,
        localFile: File,
        uploadId: String? = null
    ): Boolean = suspendCancellableCoroutine { continuation ->
        val tm = transferManager ?: run {
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }

        val uploadTask = tm.upload(bucket, cosPath, localFile.absolutePath, uploadId)
        uploadTask.setTransferStateListener { state ->
            when (state) {
                com.tencent.cos.xml.transfer.TransferState.COMPLETED -> continuation.resume(true)
                com.tencent.cos.xml.transfer.TransferState.FAILED,
                com.tencent.cos.xml.transfer.TransferState.CANCELED -> continuation.resume(false)
                else -> Unit // 其他状态忽略
            }
        }
    }

    //下载文件
    fun downloadFile(
        context: Context,
        cosPath: String,
        saveDirPath: String,
        savedFileName: String,
        stateListener: TransferStateListener? = null,
    ): COSXMLDownloadTask? {
        val tm = transferManager ?: return null
        val downloadTask = tm.download(context, bucket, cosPath, saveDirPath, savedFileName)
        stateListener?.let { downloadTask.setTransferStateListener(it) }
        return downloadTask
    }
}
