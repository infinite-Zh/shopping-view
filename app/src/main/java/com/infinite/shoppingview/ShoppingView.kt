package com.infinite.shoppingview

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.graphics.scale
import kotlin.math.pow

/**
 * @author bug小能手
 * Created on 2019/11/18.
 */
class ShoppingView : View {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    private val mLinePaint: Paint by lazy {
        Paint().apply {
            color = Color.GRAY
            strokeWidth = 5f
            style = Paint.Style.STROKE
        }
    }

    companion object {
        const val STATUS_INIT = 0x01
        const val STATUS_ANIMATION = 0x01 shl 1
    }

    private lateinit var mBitmap: Bitmap
    private lateinit var mBitmapRect: Rect

    private val mControlPoints = mutableListOf<PointF>()

    private var mStatus = STATUS_INIT


    fun addToShoppingCar(sourceView: ImageView, targetView: View) {
        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

//        (context as AppCompatActivity).window.addContentView(this, lp)
        (context as AppCompatActivity).window.findViewById<FrameLayout>(android.R.id.content).addView(this,lp)

        // 获取content的坐标
        val rootView =
            (context as AppCompatActivity).window.findViewById<FrameLayout>(android.R.id.content)
        val contentLocation = intArrayOf(0, 0)
        rootView.getLocationInWindow(contentLocation)

        //获取要移动的view的左边
        val sourceLocation = intArrayOf(0, 0)
        sourceView.getLocationInWindow(sourceLocation)

        //获取目标view的坐标
        val locationCar = intArrayOf(0, 0)
        targetView.getLocationInWindow(locationCar)


        mBitmap = Bitmap.createBitmap(sourceView.width, sourceView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mBitmap)
        sourceView.draw(canvas)
        mBitmapRect = Rect(
            sourceLocation[0],
            sourceLocation[1],
            sourceLocation[0] + sourceView.width,
            sourceLocation[1] + sourceView.height
        )
        mStatus = STATUS_INIT
        invalidate()

        mStatus = STATUS_ANIMATION



        val dataPointAx = sourceLocation[0]
        // 减去content的y坐标值，获取到相对于content的y坐标
        val dataPointAy = sourceLocation[1] - contentLocation[1]

        val dataPointBx = locationCar[0]
        val dataPointBy = locationCar[1] - contentLocation[1]

        mControlPoints.add(PointF(dataPointAx.toFloat(), dataPointAy.toFloat()))
        mControlPoints.add(PointF(dataPointBx.toFloat(), dataPointAy.toFloat()))
        mControlPoints.add(PointF(dataPointBx.toFloat(), dataPointBy.toFloat()))

        firstStep(mControlPoints)
    }


    private var mScale = 1f
    private fun firstStep(points: MutableList<PointF>) {

        val pathAnim = ObjectAnimator.ofObject(BezierEvaluator(points[1]), points[0], points[2])
        pathAnim.duration = 500
        pathAnim.interpolator = AccelerateInterpolator()
        pathAnim.addUpdateListener {
            val p = it.animatedValue as PointF

            mBitmapRect.left = p.x.toInt()
            mBitmapRect.top = p.y.toInt()
            invalidate()
        }
        val scaleAnim = ObjectAnimator.ofFloat(1f, 0.5f)
        scaleAnim.duration = 500
        scaleAnim.addUpdateListener {
            mScale = it.animatedValue as Float
        }

        pathAnim.doOnEnd {
            mEndListener?.end()
            (context as AppCompatActivity).window.findViewById<FrameLayout>(android.R.id.content).removeView(this)
        }
        pathAnim.start()
        scaleAnim.start()
    }

    class BezierEvaluator constructor(controlPointF: PointF) : TypeEvaluator<PointF> {

        private val control = controlPointF
        override fun evaluate(time: Float, start: PointF, end: PointF): PointF {
            val result = PointF()
            val timeLeft = 1 - time
            result.x =
                timeLeft.pow(2).times(start.x) + timeLeft.times(control.x).times(2).times(time) + time.pow(
                    2
                ).times(end.x)
            result.y =
                timeLeft.pow(2).times(start.y) + timeLeft.times(control.y).times(2).times(time) + time.pow(
                    2
                ).times(end.y)

            return result
        }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        when (mStatus) {
            STATUS_INIT -> {
                canvas?.drawBitmap(
                    mBitmap,
                    mBitmapRect.left.toFloat(),
                    mBitmapRect.top.toFloat(),
                    mLinePaint
                )
            }
            STATUS_ANIMATION -> {
                val b = mBitmap.scale(
                    (mBitmap.width * mScale).toInt(),
                    (mBitmap.height * mScale).toInt()
                )
                canvas?.drawBitmap(
                    b,
                    mBitmapRect.left.toFloat(),
                    mBitmapRect.top.toFloat(),
                    mLinePaint
                )

            }
        }

    }

    interface OnEnd {
        fun end()
    }

    private var mEndListener: OnEnd? = null

    fun setOnEndListener(l: OnEnd) {
        mEndListener = l
    }
}