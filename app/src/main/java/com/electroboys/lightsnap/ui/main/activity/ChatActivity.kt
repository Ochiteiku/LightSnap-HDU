package com.electroboys.lightsnap.ui.main.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.electroboys.lightsnap.R

class ChatActivity : AppCompatActivity() {

    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageView
    private lateinit var clipboard: ClipboardManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.msg_chat_input)

        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        // 监听 Ctrl+V 键盘事件
        messageInput.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN &&
                keyCode == KeyEvent.KEYCODE_V &&
                event.isCtrlPressed) {

                handlePasteImage()

                // 拦截系统默认的 Ctrl+V 行为，防止粘贴旧文本
                true
            }
            false
        }
        messageInput.setOnLongClickListener {
            handlePasteImage()  // 长按输入框时触发粘贴逻辑
            true
        }
    }


    fun handlePasteImage() {
        val item = clipboard.primaryClip?.getItemAt(0)
        item?.uri?.let { uri ->
            // 读取Uri为Bitmap
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // 将Bitmap转换为ImageSpan
            val imageSpan = ImageSpan(this, bitmap)
            // 创建包含图片的SpannableString
            val spannableString = SpannableString(" ")  // 占位符，长度为1
            spannableString.setSpan(
                imageSpan,
                0, 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // 将SpannableString插入到EditText的光标位置
            val selectionStart = messageInput.selectionStart
            val editable = messageInput.text
            editable.insert(selectionStart, spannableString)
        } ?: run {
            Toast.makeText(this, "剪贴板无图片", Toast.LENGTH_SHORT).show()
        }
    }

}