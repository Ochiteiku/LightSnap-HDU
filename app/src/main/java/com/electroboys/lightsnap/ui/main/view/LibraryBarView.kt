package com.electroboys.lightsnap.ui.main.view

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.core.view.isVisible
import com.electroboys.lightsnap.R
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LibraryBarView @JvmOverloads constructor(
    private var context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr)  {
    private var searchEditText: EditText
    private var filter: Chip
    private var startSortMenu: ImageButton
    private var deleteAll: ImageButton
    private lateinit var sortPicker: Spinner

    var searchEditTextListener: ((String) -> Unit)? = null
    var filterListener: (() -> Unit)? = null
    var deleteAllListener: (() -> Unit)? = null
    var sortPickerListener: ((Int) -> Unit)? = null

    private var TodayDate = mutableMapOf<String, Long>(
        "StartDate" to MaterialDatePicker.todayInUtcMilliseconds(),
        "EndDate" to MaterialDatePicker.todayInUtcMilliseconds()
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.library_toolbar, this, true)

        searchEditText = findViewById(R.id.SearchEditText)
        searchEditText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
            override fun afterTextChanged(s: Editable?) {
                // 输入完成进行搜索
                searchEditTextListener?.invoke(s.toString())
            }
        })

        filter = findViewById(R.id.Filter)
        filter.text = formatDateRange(TodayDate)
        // 对话框的显示是异步发生的，直观上表现为click事件完成之后，才会弹出对话框进行选择
        // 这里需要将回调后的逻辑与点击事件进行分离
        filter.setOnClickListener{
            filterListener?.invoke()!!
        }

        // 开启下拉选框
        startSortMenu = findViewById(R.id.StartSortMenu)
        startSortMenu.setOnClickListener{
            sortPicker.isVisible = true
        }

        deleteAll = findViewById(R.id.btnDeleteAll)
        deleteAll.setOnClickListener{
            deleteAllListener?.invoke()
        }

        sortPicker = findViewById(R.id.SortPicker)
        // 设置排序的适配器(懒得拆分)
        val sortPickerAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.librarySortMenus)
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        sortPicker.adapter = sortPickerAdapter
        // 设置Spinner选择监听
        sortPicker.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ){
                sortPickerListener?.invoke(position)
                sortPicker.isVisible = false
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }
    }

    fun updateDateText(PickedDate: MutableMap<String, Long>){
        filter.text = formatDateRange(PickedDate)
    }

    private fun formatDateRange(currentDate: MutableMap<String, Long>): String{
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        return "${dateFormat.format(Date(currentDate["StartDate"]!!))}-${dateFormat.format(Date(currentDate["EndDate"]!!))}"
    }
}