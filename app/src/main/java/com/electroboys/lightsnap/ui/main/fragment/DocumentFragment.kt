package com.electroboys.lightsnap.ui.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.data.entity.Document
import com.electroboys.lightsnap.domain.document.DocumentObject

class DocumentFragment : Fragment(R.layout.fragment_document) {

    private var selectedDocument: Document? = null
    private lateinit var documentListContainer: LinearLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        documentListContainer = view.findViewById(R.id.documentListContainer)
        // 加载文档列表
        updateDocumentList()

        // 默认显示第一个文档
//        if (DocumentObject.documents.isNotEmpty()) {
//            selectDocument(DocumentObject.documents[0])
//            updateDocumentDetail(DocumentObject.documents[0])
//        }
    }

    private fun updateDocumentList() {
        documentListContainer.removeAllViews()

        // 按置顶状态和时间排序
        val sortedDocuments = DocumentObject.documents.sortedWith(
            compareByDescending { it.time }
        )

        sortedDocuments.forEach { document ->
            // 创建文档列表项
            val docItem = LayoutInflater.from(requireContext())
                .inflate(R.layout.doc_item, documentListContainer, false).apply {

                    findViewById<TextView>(R.id.documentTitle).text = document.title
                    findViewById<TextView>(R.id.documentPreview).text = document.content
                    findViewById<TextView>(R.id.documentTime).text = document.time

//                    background = if (document == selectedDocument) {
//                        ContextCompat.getDrawable(requireContext(), R.drawable.bg_nav_selected)
//                    } else {
//                        ContextCompat.getDrawable(
//                            requireContext(),
//                            R.drawable.bg_msg_option_selected
//                        )
//                    }

                    setOnClickListener {
                        selectDocument(document)
//                        updateDocumentDetail(document)
                        showFullscreenDetail(document)
                    }
                }

            documentListContainer.addView(docItem)
        }
    }

    private fun selectDocument(document: Document) {
        selectedDocument = document
        updateDocumentList() // 刷新列表以更新选中状态
    }

//    private fun updateDocumentDetail(document: Document) {
//        val detailContainer = requireView().findViewById<FrameLayout>(R.id.documentContainer)
//        detailContainer.removeAllViews()
//        // 创建详情视图
//        val detailView = LayoutInflater.from(requireContext())
//            .inflate(R.layout.doc_document_detail, detailContainer, false).apply {
//                findViewById<TextView>(R.id.detailTitle).text = document.title
//                findViewById<TextView>(R.id.detailContent).text = document.content
//                findViewById<TextView>(R.id.detailTime).text = "创建时间: ${document.time}"
//            }
//        detailContainer.addView(detailView)
//    }

    private fun showFullscreenDetail(document: Document) {
        val detailFragment = DocumentDetailFragment.newInstance(document)

        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.main, detailFragment)
            .addToBackStack(null)
            .commit()
    }

}