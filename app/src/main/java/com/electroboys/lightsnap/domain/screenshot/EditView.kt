package com.electroboys.lightsnap.domain.screenshot

import android.content.Context
import android.util.AttributeSet
import android.view.View

class EditView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs)  {

    init {
        isClickable = false
        isFocusable = false
        isFocusableInTouchMode = false
    }


}