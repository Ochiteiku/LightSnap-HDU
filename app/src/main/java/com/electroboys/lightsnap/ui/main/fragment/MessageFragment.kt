package com.electroboys.lightsnap.ui.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.data.entity.Contact
import com.electroboys.lightsnap.domain.message.ChatAdapter
import com.electroboys.lightsnap.domain.message.ContactAndChatObject

class MessageFragment : Fragment(R.layout.fragment_message) {


    private val contacts = ContactAndChatObject.contacts

    private var selectedContact: Contact? = null
    private var selectedOption: TextView? = null
    private lateinit var messageListContainer: LinearLayout

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

    // 会话
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

        // 设置发送按钮点击事件
        inputContainer.findViewById<ImageView>(R.id.sendButton).apply {
            setOnClickListener {
                val inputText =
                    inputContainer.findViewById<EditText>(R.id.messageInput).text.toString()
                if (inputText.isNotBlank()) {
                    // TODO 处理发送逻辑
                }
            }
        }

        chatContainer.addView(rootLayout)
    }
}