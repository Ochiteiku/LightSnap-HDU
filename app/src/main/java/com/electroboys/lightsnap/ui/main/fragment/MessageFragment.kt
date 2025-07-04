package com.electroboys.lightsnap.ui.main.fragment

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.data.entity.Contact
import com.electroboys.lightsnap.domain.message.ChatAdapter
import com.electroboys.lightsnap.domain.message.ContactAndChatObject
import com.electroboys.lightsnap.utils.SecretUtil
import androidx.core.graphics.scale


class MessageFragment : Fragment(R.layout.fragment_message) {

    private val contacts = ContactAndChatObject.contacts
    private var selectedContact: Contact? = null
    private var selectedOption: TextView? = null
    private lateinit var messageListContainer: LinearLayout

    private lateinit var messageInput: EditText
    private lateinit var clipboard: ClipboardManager

    companion object {
        private var isMessageSecret = false
        fun getMessageSecret() = isMessageSecret
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emptyChatHint = view.findViewById<TextView>(R.id.emptyChatHint)
        messageListContainer = view.findViewById(R.id.messageListContainer)

        val contactViews = listOf(
            Triple(
                view.findViewById<ImageView>(R.id.contact1),
                view.findViewById<TextView>(R.id.contactName1),
                0
            ),
            Triple(
                view.findViewById<ImageView>(R.id.contact2),
                view.findViewById<TextView>(R.id.contactName2),
                1
            ),
            Triple(
                view.findViewById<ImageView>(R.id.contact3),
                view.findViewById<TextView>(R.id.contactName3),
                2
            ),
            Triple(
                view.findViewById<ImageView>(R.id.contact4),
                view.findViewById<TextView>(R.id.contactName4),
                3
            ),
            Triple(
                view.findViewById<ImageView>(R.id.contact5),
                view.findViewById<TextView>(R.id.contactName5),
                4
            )
        )

        contactViews.forEach { (imageView, textView, index) ->
            if (index < contacts.size) {
                val contact = contacts[index]

                // 设置头像
                imageView.apply {

                    setImageResource(contact.avatarRes)
                    contentDescription = contact.name
                    foreground =
                        ContextCompat.getDrawable(requireContext(), R.drawable.bg_contact_default)
                    clipToOutline = true

                    setOnClickListener {
                        selectContact(contact)
                        updateChatView(contact)
                    }
                }

                // 设置名字
                textView.apply {
                    text = contact.name
                    visibility = View.VISIBLE
                    setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.contact_default_text
                        )
                    )
                }
            } else {
                imageView.visibility = View.GONE
                textView.visibility = View.GONE
            }
        }

        val optionMessage = view.findViewById<TextView>(R.id.optionMessage)
        val optionDimension = view.findViewById<TextView>(R.id.optionDimension)
        val optionMark = view.findViewById<TextView>(R.id.optionMark)

        listOf(optionMessage, optionDimension, optionMark).forEach { option ->
            option.setOnClickListener {
                selectOption(it as TextView)
                updateMessageList()
            }
        }

        selectOption(optionMessage)
        updateMessageList()
    }

    private fun selectContact(contact: Contact) {
        selectedContact = contact

        // 使用与onViewCreated中相同的contactViews列表
        val contactViews = listOf(
            Triple(R.id.contact1, R.id.contactName1, 0),
            Triple(R.id.contact2, R.id.contactName2, 1),
            Triple(R.id.contact3, R.id.contactName3, 2),
            Triple(R.id.contact4, R.id.contactName4, 3),
            Triple(R.id.contact5, R.id.contactName5, 4)
        )

        contactViews.forEach { (imageViewId, textViewId, index) ->
            if (index < contacts.size) {
                val isSelected = contacts[index].name == contact.name

                view?.findViewById<ImageView>(imageViewId)?.foreground =
                    ContextCompat.getDrawable(
                        requireContext(),
                        if (isSelected) R.drawable.bg_contact_select else R.drawable.bg_contact_default
                    )

                view?.findViewById<TextView>(textViewId)?.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (isSelected) R.color.black else R.color.contact_default_text
                    )
                )
            }
        }
    }

    // 消息 - 未读 - 标记 bar
    private fun selectOption(selected: TextView) {
        listOf(
            requireView().findViewById<TextView>(R.id.optionMessage),
            requireView().findViewById<TextView>(R.id.optionDimension),
            requireView().findViewById<TextView>(R.id.optionMark)
        ).forEach { option ->
            option.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.msg_option_default_text
                )
            )
            option.background = null
        }

        selected.apply {
            setTextColor(ContextCompat.getColor(requireContext(), R.color.msg_option_selected_text))
            background =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_msg_option_selected)
        }

        selectedOption = selected
    }

    private fun updateMessageList() {
        messageListContainer.removeAllViews()

        val filteredContacts = when (selectedOption?.id) {
            R.id.optionMessage -> contacts
            R.id.optionDimension -> contacts.filter { it.unread }
            R.id.optionMark -> contacts.filter { it.marked }
            else -> contacts
        }

        filteredContacts.forEach { contact ->
            val messageItem = LayoutInflater.from(requireContext())
                .inflate(R.layout.msg_chat, messageListContainer, false)

            // 设置消息项选中状态
            messageItem.isSelected = contact == selectedContact

            messageItem.findViewById<ImageView>(R.id.messageAvatar).apply {

                setImageResource(contact.avatarRes)
                setOnClickListener {
                    selectContact(contact)
                    updateChatView(contact)
                    updateMessageList() // 刷新列表以更新所有项的选择状态
                }
            }

            messageItem.findViewById<TextView>(R.id.messageName).text = contact.name
            messageItem.findViewById<TextView>(R.id.messagePreview).text = contact.lastMessage

            if (contact.unread) {
                messageItem.findViewById<TextView>(R.id.messageName).setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.unread_message_name)
                )
            }

            messageItem.setOnClickListener {
                selectContact(contact)
                updateChatView(contact)
                updateMessageList()
            }

            messageListContainer.addView(messageItem)
        }
    }

    private fun setupPasteListeners() {
        // 监听长按输入框事件（软键盘粘贴）
        messageInput.setOnLongClickListener {
            handlePasteImage()
            true
        }
    }

    private fun handlePasteImage() {
        val item = clipboard.primaryClip?.getItemAt(0)
        item?.uri?.let { uri ->
            try {
                // 读取Uri为Bitmap
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream) ?: run {
                    Toast.makeText(requireContext(), "图片解码失败", Toast.LENGTH_SHORT).show()
                    return
                }
                inputStream?.close()

                // 动态缩放图片（使用输入框可用宽度）
                val scaledBitmap = scaleBitmapToFitWidth(bitmap)

                // 插入图片到输入框
                val imageSpan = ImageSpan(requireContext(), scaledBitmap)
                val spannableString = SpannableString(" ")
                spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                val selectionStart = messageInput.selectionStart
                val editable = messageInput.text
                editable.insert(selectionStart, spannableString)

                // 滚动到输入框底部（确保新插入的图片可见）
                messageInput.setSelection(editable.length)
                messageInput.scrollTo(0, messageInput.bottom)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "图片粘贴失败：${e.message}", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(requireContext(), "剪贴板无图片", Toast.LENGTH_SHORT).show()
        }
    }

    // 动态缩放图片适配输入框宽度（工具方法）
    private fun scaleBitmapToFitWidth(bitmap: Bitmap): Bitmap {
        // 计算输入框的可用宽度（总宽度 - 左右内边距）
        val targetWidth = messageInput.width - messageInput.paddingLeft - messageInput.paddingRight
        if (targetWidth <= 0) return bitmap  // 宽度未获取到（如输入框未加载完成），直接返回原图

        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        if (originalWidth <= targetWidth) return bitmap  // 原图宽度已小于可用宽度，无需缩放

        // 按比例缩放（保持宽高比）
        val scaleFactor = targetWidth.toFloat() / originalWidth
        val scaledHeight = (originalHeight * scaleFactor).toInt()
        return bitmap.scale(targetWidth, scaledHeight)
    }

    // 生命周期：移除监听器防止内存泄漏（需在 onDestroyView 中调用）
    override fun onDestroyView() {
        super.onDestroyView()
        messageInput.setOnKeyListener(null)
        messageInput.setOnLongClickListener(null)
    }

    // 会话
    @SuppressLint("SuspiciousIndentation")
    private fun updateChatView(contact: Contact) {
        val chatContainer = requireView().findViewById<FrameLayout>(R.id.chatContainer)
        chatContainer.removeAllViews()

        // 创建根布局
        val rootLayout = LinearLayout(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            orientation = LinearLayout.VERTICAL
        }

        // 添加工具栏
        val toolbar = LayoutInflater.from(requireContext())
            .inflate(R.layout.msg_chat_toolbar, rootLayout, false).apply {
                // 更新工具栏头像
                findViewById<ImageView>(R.id.toolbarAvatar).setImageResource(contact.avatarRes)
                // 更新工具栏标题
                findViewById<TextView>(R.id.toolbarTitle).text = contact.name
            }
        rootLayout.addView(toolbar)

        // 添加消息列表
        val messagesRecyclerView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ChatAdapter(
                contact.messages,
                contact.avatarRes // 传递联系人头像资源
            )
        }
        rootLayout.addView(messagesRecyclerView, LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f))

        // 添加输入框
        val inputContainer = LayoutInflater.from(requireContext())
            .inflate(R.layout.msg_chat_input, rootLayout, false)
        rootLayout.addView(inputContainer)

        // 绑定输入框（从 inputContainer 中获取 EditText）
        messageInput = inputContainer.findViewById(R.id.messageInput)
        clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // 设置输入框的粘贴监听器
        setupPasteListeners()

        // 设置发送按钮点击事件
//        inputContainer.findViewById<ImageView>(R.id.sendButton).apply {
//            setOnClickListener {
//                val inputText =
//                    inputContainer.findViewById<EditText>(R.id.messageInput).text.toString()
//                if (inputText.isNotBlank()) {
//                    // 处理发送逻辑
//                }
//            }
//        }
        val closeButton = toolbar.findViewById<ImageView>(R.id.closeMessageButton)
        closeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }


        val secretImage = toolbar.findViewById<ImageView>(R.id.secretMessageButton)
        secretImage.setOnClickListener {
            if (!isMessageSecret) {
                SecretUtil.setSecret(true)
                secretImage.setImageResource(R.drawable.ic_eye_closed)
            } else {
                SecretUtil.setSecret(false)
                secretImage.setImageResource(R.drawable.ic_eye_open)
            }
            isMessageSecret = !isMessageSecret
        }
        secretImage.setImageResource(if (isMessageSecret) R.drawable.ic_eye_closed else R.drawable.ic_eye_open)

        chatContainer.addView(rootLayout)
    }
}