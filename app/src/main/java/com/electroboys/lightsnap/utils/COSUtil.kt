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
import com.tencent.cos.xml.transfer.COSXMLUploadTask
import com.tencent.cos.xml.transfer.TransferConfig
import com.tencent.cos.xml.transfer.TransferManager
import com.tencent.cos.xml.transfer.TransferStateListener
import com.tencent.qcloud.core.auth.QCloudCredentialProvider
import com.tencent.qcloud.core.auth.ShortTimeCredentialProvider
import java.io.File

object COSUtil {

    private var cosXmlService: CosXmlService? = null
    private var transferManager: TransferManager? = null
    private var bucket: String? = null

    fun initCOS(context: Context, secretId: String, secretKey: String, region: String, bucket: String): Boolean {
        return try {
            val credentialProvider: QCloudCredentialProvider = ShortTimeCredentialProvider(
                secretId,
                secretKey,
                300
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
            // 验证与 COS 桶的连接
            if (!checkCosBucketAvailable()) {
                Log.e("TEST1", "initCOS: 123", )
                return false
            }

            true // 初始化成功
        } catch (e: Exception) {
            e.printStackTrace()
            false // 初始化失败
        }
    }

    fun checkCosBucketAvailable(): Boolean {
        val headBucketRequest = HeadBucketRequest(bucket)

        cosXmlService?.headBucketAsync(headBucketRequest, object : CosXmlResultListener {
            override fun onSuccess(request: CosXmlRequest, result: CosXmlResult) {
                val headBucketResult = result as HeadBucketResult
                // 你可以在这里处理 headBucketResult 里的其他信息
                Log.e("TEST1", "initCOS: ${headBucketResult.httpCode}")
                true
            }

            override fun onFail(
                request: CosXmlRequest,
                clientException: CosXmlClientException?,
                serviceException: CosXmlServiceException?
            ) {
                clientException?.printStackTrace()
                serviceException?.printStackTrace()
                val message = clientException?.message ?: serviceException?.message ?: "未知错误"
                false
            }
        })
        return true
    }




    fun uploadFile(
        cosPath: String,
        localFile: File,
        uploadId: String? = null,
        stateListener: TransferStateListener? = null
    ): COSXMLUploadTask? {
        val tm = transferManager ?: return null
        val uploadTask = tm.upload(bucket, cosPath, localFile.absolutePath, uploadId)
        stateListener?.let { uploadTask.setTransferStateListener(it) }
        return uploadTask
    }

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