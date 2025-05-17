package com.electroboys.lightsnap.domain.screenshot.scrollshot

import android.app.Activity
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import com.electroboys.lightsnap.ui.main.view.scrollshot.HighlightOverlayView
import com.electroboys.lightsnap.ui.main.view.scrollshot.BlockerView
import com.electroboys.lightsnap.ui.main.view.scrollshot.ExitButtonView
import kotlin.math.log

typealias ScrollableComponentCallback = (View) -> Unit

object ScrollableComponentSelector {

    fun highlightScrollableViews(
        activity: Activity,
        rootView: ViewGroup,
        onComponentSelected: ScrollableComponentCallback
    ) {
        val scrollableViews = ScrollShotHelper.findAllScrollableViews(rootView)

        if (scrollableViews.isEmpty()) {
            Toast.makeText(activity, "当前页面无可滚动内容", Toast.LENGTH_SHORT).show()
            return
        }

        val decorView = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
            ?: return

        Log.d("ScrollableComponentSelector", "开始添加高亮层")

        // 添加遮挡层
        val blockerView = BlockerView(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            alpha = 0.5f
        }
        decorView.addView(blockerView)

        // 高亮所有可滚动组件
        scrollableViews.forEach { view ->
            if (view.width <= 0 || view.height <= 0) {
                Log.w("ScrollableComponentSelector", "跳过无效组件")
                return@forEach
            }

            val overlay = HighlightOverlayView(activity).apply {
                layoutParams = FrameLayout.LayoutParams(view.width, view.height).apply {
                    leftMargin = getXRelativeToRoot(view)
                    topMargin = getYRelativeToRoot(view)
                }
                alpha = 0.5f
                setOnClickListener {
                    removeHighlightOverlays(decorView)
                    onComponentSelected.invoke(view)
                }
            }
            decorView.addView(overlay)
        }

        // 添加退出按钮
        val exitButtonView = ExitButtonView(activity).apply {
            layoutParams = FrameLayout.LayoutParams(160, 80).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                marginEnd = 40
                bottomMargin = 40
            }
            setOnClickListener {
                removeHighlightOverlays(decorView)
                onComponentSelected.invoke(it)
            }
        }
        decorView.addView(exitButtonView)

        Toast.makeText(activity, "选取高亮区域进行长截图", Toast.LENGTH_SHORT).show()
    }

    private fun getXRelativeToRoot(view: View): Int {
        val location = IntArray(2)
        view.getLocationInWindow(location)
        return location[0]
    }

    private fun getYRelativeToRoot(view: View): Int {
        val location = IntArray(2)
        view.getLocationInWindow(location)
        return location[1]
    }

    private fun removeHighlightOverlays(container: ViewGroup) {
        for (i in container.childCount - 1 downTo 0) {
            val child = container.getChildAt(i)
            if (child is HighlightOverlayView || child is BlockerView || child is ExitButtonView) {
                container.removeView(child)
            }
        }
    }
}
