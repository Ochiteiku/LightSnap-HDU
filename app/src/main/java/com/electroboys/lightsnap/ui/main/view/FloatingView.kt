package com.electroboys.lightsnap.ui.main.view


import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.data.entity.SettingsConstants
import com.electroboys.lightsnap.data.entity.SettingsConstants.floatBitmapKey
import com.electroboys.lightsnap.data.screenshot.BitmapCache
import kotlin.math.abs


class FloatingView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    RelativeLayout(context, attrs, defStyleAttr) {
    private lateinit var ivFloatingView: ImageView
    private var rotateAnimation: RotateAnimation? = null
    private var inputStartX = 0
    private var inputStartY = 0
    private var viewStartX = 0
    private var viewStartY = 0
    private var inMovingX = 0
    private var inMovingY = 0

    private var mFloatBallParams: WindowManager.LayoutParams? = null
    private var mWindowManager: WindowManager? = null
    private var mContext: Context? = null
    private var mScreenHeight = 0
    private var mScreenWidth = 0
    var isShow: Boolean = false
        private set
    private var mDp167 = 0
    private var mDp58 = 0
    private var mLoading = false
    private var mValueAnimator: ValueAnimator? = null
    private var moveVertical = false


    constructor(context: Context) : this(context, null) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
        init(context)
    }

    private fun init(context: Context) {
        mContext = context
        inflate(context, R.layout.floating_view, this)
        ivFloatingView = findViewById(R.id.iv_floating_view)
        findViewById<View?>(R.id.iv_back).setOnClickListener {
            dismissFloatView()
            if (floatBitmapKey != null) {
                BitmapCache.clearExcept(floatBitmapKey)//清除缓存
                SettingsConstants.floatBitmapKey = null//清除缓存
            }
        }
        if (floatBitmapKey != null) {
            ivFloatingView.setImageBitmap(BitmapCache.getBitmap(floatBitmapKey!!))
        }






        if (mContext != null) {
            initFloatBallParams(mContext!!)
            val displayMetrics = DisplayMetrics()
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                .getDefaultDisplay().getMetrics(displayMetrics)
            mScreenWidth = displayMetrics.widthPixels
            mScreenHeight = displayMetrics.heightPixels
            mDp167 = Dp2Px(mContext!!, 167f) as Int
            mDp58 = Dp2Px(mContext!!, 58f) as Int
        }
        //  slop = ViewConfigurationCompat.getScaledPagingTouchSlop(ViewConfiguration.get(context));
        slop = 3
    }


    /**
     * 获取悬浮球的布局参数
     */
    private fun initFloatBallParams(context: Context) {
        mFloatBallParams = WindowManager.LayoutParams()
        mFloatBallParams!!.flags = (mFloatBallParams!!.flags
                or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE //避免悬浮球被通知栏部分遮挡
                or WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        mFloatBallParams!!.dimAmount = 0.2f

        //      mFloatBallParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mFloatBallParams!!.height = WindowManager.LayoutParams.WRAP_CONTENT
        mFloatBallParams!!.width = WindowManager.LayoutParams.WRAP_CONTENT

        mFloatBallParams!!.gravity = Gravity.START or Gravity.TOP
        mFloatBallParams!!.format = PixelFormat.RGBA_8888
        // 设置整个窗口的透明度
        mFloatBallParams!!.alpha = 1.0f
        // 显示悬浮球在屏幕左上角
        mFloatBallParams!!.x = 0
        mFloatBallParams!!.y = 0
        mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
    }

    private var slop = 0
    private var isDrag = false

    init {
        init(context)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.getAction()) {
            MotionEvent.ACTION_DOWN -> {
                if (null != mValueAnimator && mValueAnimator!!.isRunning()) {
                    mValueAnimator!!.cancel()
                }

                setPressed(true)
                isDrag = false
                inputStartX = event.getRawX().toInt()
                inputStartY = event.getRawY().toInt()
                viewStartX = mFloatBallParams!!.x
                viewStartY = mFloatBallParams!!.y
            }

            MotionEvent.ACTION_MOVE -> {
                inMovingX = event.getRawX().toInt()
                inMovingY = event.getRawY().toInt()
                val MoveX = viewStartX + inMovingX - inputStartX
                val MoveY = viewStartY + inMovingY - inputStartY

                if (mScreenHeight <= 0 || mScreenWidth <= 0) {
                    isDrag = false
//                    break
                }

                if (abs((inMovingX - inputStartX).toDouble()) > slop
                    && abs((inMovingY - inputStartY).toDouble()) > slop
                ) {
                    isDrag = true

                    mFloatBallParams!!.x = MoveX
                    mFloatBallParams!!.y = MoveY
                    updateWindowManager()
                } else {
                    isDrag = false
                }
            }

            MotionEvent.ACTION_UP -> {
                if (isDrag) {
                    //恢复按压效果
                    setPressed(false)
                }
                //吸附贴边计算和动画
                welt()
            }

            else -> {}
        }
        return isDrag || super.onTouchEvent(event)
    }

    private val isLeftSide: Boolean
        get() = getX() == 0f

    private val isRightSide: Boolean
        get() = getX() == (mScreenWidth - getWidth()).toFloat()

    //吸附贴边计算和动画
    private fun welt() {
        var movedX = mFloatBallParams!!.x
        var movedY = mFloatBallParams!!.y

        moveVertical = false
        if (mFloatBallParams!!.y < getHeight() && mFloatBallParams!!.x >= slop && mFloatBallParams!!.x <= mScreenWidth - getWidth() - slop) {
            movedY = 0
        } else if (mFloatBallParams!!.y > mScreenHeight - getHeight() * 2 && mFloatBallParams!!.x >= slop && mFloatBallParams!!.x <= mScreenWidth - getWidth() - slop) {
            movedY = mScreenHeight - getHeight()
        } else {
            moveVertical = true
            if (mFloatBallParams!!.x < mScreenWidth / 2 - getWidth() / 2) {
                movedX = 0
            } else {
                movedX = mScreenWidth - getWidth()
            }
        }

        val duration: Int
        if (moveVertical) {
            mValueAnimator = ValueAnimator.ofInt(mFloatBallParams!!.x, movedX)
            duration = movedX - mFloatBallParams!!.x
        } else {
            mValueAnimator = ValueAnimator.ofInt(mFloatBallParams!!.y, movedY)
            duration = movedY - mFloatBallParams!!.y
        }
        mValueAnimator!!.setDuration(abs(duration.toDouble()).toLong())
        mValueAnimator!!.addUpdateListener(object : AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator) {
                val level = animation.getAnimatedValue() as Int
                if (moveVertical) {
                    mFloatBallParams!!.x = level
                } else {
                    mFloatBallParams!!.y = level
                }
                updateWindowManager()
            }
        })
        mValueAnimator!!.setInterpolator(AccelerateInterpolator())
        mValueAnimator!!.start()
    }

    override fun onDetachedFromWindow() {
        if (null != mValueAnimator && mValueAnimator!!.isRunning()) {
            mValueAnimator!!.cancel()
        } //进入下个页面的时候贴边动画暂停，下个页面attached时候会继续动画， 你手速快的话还能在中途接住球继续拖动

        super.onDetachedFromWindow()
    }


    /**
     * 显示悬浮
     */
    fun showFloat() {
        if (this.isShow) return
        this.isShow = true
        if (mFloatBallParamsX == -1 || mFloatBallParamsY == -1) {
            //首次打开时，初始化的位置
            mFloatBallParams!!.x = mScreenWidth - mDp58
            mFloatBallParams!!.y = Dp2Px(mContext!!, 55f)
            mFloatBallParamsX = mFloatBallParams!!.x
            mFloatBallParamsY = mFloatBallParams!!.y
        } else {
            mFloatBallParams!!.x = mFloatBallParamsX
            mFloatBallParams!!.y = mFloatBallParamsY
        }
        mWindowManager!!.addView(this, mFloatBallParams)
        //吸附贴边计算和动画
        welt()
    }

    /**
     * 移除该view
     */
    fun dismissFloatView() {
        try {
            if (rotateAnimation != null) {
                rotateAnimation!!.cancel()
            }
            if (this.isShow) { //有可能已经关闭了
                if (mWindowManager != null) {
                    mWindowManager!!.removeViewImmediate(this)
                }
            }
        } catch (E: Exception) {
        }
        this.isShow = false
    }

    //更新位置，并保存到手机内存
    fun updateWindowManager() {
        try {
            if (mWindowManager != null && mFloatBallParams != null) {
                mWindowManager!!.updateViewLayout(this, mFloatBallParams)
                mFloatBallParamsX = mFloatBallParams!!.x
                mFloatBallParamsY = mFloatBallParams!!.y
            }
        } catch (e: Exception) {
        }
    }


    companion object {
        private const val TAG = "FloatingView"
        private var mFloatBallParamsX = -1
        private var mFloatBallParamsY = -1
    }

    /**
     * dp  转 px
     *
     * @param context
     * @param dp
     * @return
     */
    fun Dp2Px(context: Context, dp: Float): Int {
        val scale = context.getResources().getDisplayMetrics().density
        return (dp * scale + 0.5f).toInt()
    }


}
