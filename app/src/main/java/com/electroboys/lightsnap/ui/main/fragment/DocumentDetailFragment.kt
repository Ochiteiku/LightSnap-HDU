package com.electroboys.lightsnap.ui.main.fragment

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment.STYLE_NO_TITLE
import androidx.fragment.app.Fragment
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.data.entity.Document

class DocumentDetailFragment : Fragment(R.layout.doc_document_detail) {
    companion object {
        fun newInstance(document: Document) = DocumentDetailFragment().apply {
            arguments = Bundle().apply { putParcelable("document", document) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view,savedInstanceState)
        val document = arguments?.getParcelable<Document>("document")!!

        view.findViewById<TextView>(R.id.detailTitle).text = document.title
        view.findViewById<TextView>(R.id.detailContent).text = document.content
        view.findViewById<TextView>(R.id.detailTime).text = "创建时间: ${document.time}"

        view.findViewById<ImageView>(R.id.closeButton).setOnClickListener {
            parentFragmentManager.popBackStack() // 退出全屏
        }

        enterFullscreen()
    }

    private fun enterFullscreen() {
        activity?.window?.apply {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    override fun onDestroyView() {
        exitFullscreen()
        super.onDestroyView()
    }

    private fun exitFullscreen() {
        activity?.window?.apply {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }
}