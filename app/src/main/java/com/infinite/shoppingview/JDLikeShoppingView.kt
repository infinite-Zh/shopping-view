package com.infinite.shoppingview

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
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
class JDLikeShoppingView : View {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    private val mLinePaint: Paint by lazy {
        Paint().apply {
            color = Color.GRAY
            strokeWidth = 5f
            style = Paint.Style.STROKE
        }
    }

    private var mPath = Path()


    private lateinit var mBitmap: Bitmap
    private lateinit var mBitmapRect: Rect

    private val mControlPoints = mutableListOf<PointF>()


    fun addToShoppingCar(sourceView: ImageView, targetView: View) {
        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        (context as AppCompatActivity).window.findViewById<FrameLayout>(android.R.id.content)
            .addView(this, lp)

        // 获取content的坐标
        val rootView =
            (context as AppCompatActivity).window.findViewById<FrameLayout>(android.R.id.content)
        val contentLocation = intArrayOf(0, 0)
        rootView.getLocationInWindow(contentLocation)

        //获取要移动的view的左边
        val sourceLocation = intArrayOf(0, 0)
        sourceView.getLocationInWindow(sourceLocation)

        //获取目标view的坐标
        val targetLocation = intArrayOf(0, 0)
        targetView.getLocationInWindow(targetLocation)


        mBitmap = Bitmap.createBitmap(sourceView.width, sourceView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mBitmap)
        sourceView.draw(canvas)
        mBitmapRect = Rect(
            sourceLocation[0],
            sourceLocation[1],
            sourceLocation[0] + sourceView.width,
            sourceLocation[1] + sourceView.height
        )

        invalidate()

        val dataPointAx = sourceLocation[0] + sourceView.measuredWidth / 2
        // 减去content的y坐标值，获取到相对于content的y坐标
        val dataPointAy = sourceLocation[1] - contentLocation[1] + sourceView.measuredHeight / 2

        val dataPointBx = sourceLocation[0] + sourceView.measuredWidth
        val dataPointBy = sourceLocation[1] - contentLocation[1]

        mControlPoints.add(PointF(dataPointAx.toFloat(), dataPointAy.toFloat()))
        mControlPoints.add(PointF(dataPointBx.toFloat(), dataPointAy.toFloat()))
        mControlPoints.add(PointF(dataPointBx.toFloat(), dataPointBy.toFloat()))

        firstStep(mControlPoints, object : OnEnd {
            override fun end() {
                mControlPoints.clear()
                mControlPoints.add(PointF(dataPointBx.toFloat(), dataPointBy.toFloat()))
                mControlPoints.add(PointF(targetLocation[0].toFloat(), dataPointAy.toFloat()))
                mControlPoints.add(PointF(targetLocation[0].toFloat(), targetLocation[1].toFloat()))

                secondStep(mControlPoints)
            }
        })


    }


    private var mScale = 1f
    private fun firstStep(points: MutableList<PointF>, onEnd: OnEnd) {

        val pathAnim = ObjectAnimator.ofObject(BezierEvaluator(points[1]), points[0], points[2])
        pathAnim.duration = 500
        pathAnim.interpolator = AccelerateInterpolator()
        pathAnim.addUpdateListener {
            val p = it.animatedValue as PointF

            mBitmapRect.left = (p.x - mBitmap.width.times(mScale / 2)).toInt()
            mBitmapRect.top = (p.y - mBitmap.height.times(mScale / 2)).toInt()

            mPath.lineTo(p.x, p.y)
            invalidate()
        }
        val scaleAnim = ObjectAnimator.ofFloat(1f, 0.5f)
        scaleAnim.duration = 500
        scaleAnim.interpolator = OvershootInterpolator(8f)
        scaleAnim.addUpdateListener {
            mScale = it.animatedValue as Float
            Log.e("scale", "$mScale")
        }

        pathAnim.doOnEnd {
            onEnd.end()
        }
        pathAnim.start()
        scaleAnim.start()
    }

    private fun secondStep(points: MutableList<PointF>) {
        val pathAnim = ObjectAnimator.ofObject(BezierEvaluator(points[1]), points[0], points[2])
        pathAnim.duration = 500
        pathAnim.startDelay = 200
        pathAnim.interpolator = AccelerateInterpolator()
        pathAnim.addUpdateListener {
            val p = it.animatedValue as PointF

            mBitmapRect.left = (p.x - mBitmap.width.times(mScale / 2)).toInt()
            mBitmapRect.top = (p.y - mBitmap.height.times(mScale / 2)).toInt()

            mPath.lineTo(p.x, p.y)
            invalidate()
        }
        val scaleAnim = ObjectAnimator.ofFloat(0.5f, 0.3f)
        scaleAnim.duration = 500
        scaleAnim.startDelay = 200
        scaleAnim.addUpdateListener {
            mScale = it.animatedValue as Float
            Log.e("scale", "$mScale")
        }

        pathAnim.doOnEnd {
            (context as AppCompatActivity).window.findViewById<FrameLayout>(android.R.id.content)
                .removeView(this)
            mEndListener?.end()
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

    interface OnEnd {
        fun end()
    }

    private var mEndListener: OnEnd? = null

    fun setOnEndListener(l: OnEnd) {
        mEndListener = l
    }
}