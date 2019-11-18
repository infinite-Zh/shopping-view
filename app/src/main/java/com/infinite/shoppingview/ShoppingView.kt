package com.infinite.shoppingview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.view.*

/**
 * @author bug小能手
 * Created on 2019/11/18.
 */
class ShoppingView : View {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    private val mPaint: Paint by lazy {
        Paint().apply {
            color = Color.RED
            strokeWidth = 10f
            style = Paint.Style.STROKE
        }
    }
    private val mPointPaint: Paint by lazy {
        Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL_AND_STROKE
            textSize=50f
        }
    }
    private val mLinePaint: Paint by lazy {
        Paint().apply {
            color = Color.GRAY
            strokeWidth = 5f
            style = Paint.Style.STROKE
        }
    }
    private val mBezierPath: Path by lazy {
        Path().apply {
        }
    }
    private val mLinePath: Path by lazy {
        Path().apply {
        }
    }

    companion object{
        const val STATUS_INIT=0x01
        const val STATUS_ANIMATION=0x01 shl 1
    }

    private lateinit var mBitmap: Bitmap
    private lateinit var mBitmapRect: Rect

    private val mControlPoints = mutableListOf<PointF>()

    private var mStatus= STATUS_INIT


    fun addToShoppingCar(targetView:ImageView,car:View){
        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        (context as AppCompatActivity).findViewById<FrameLayout>(android.R.id.content).addView(this, lp)

        mBitmap= Bitmap.createBitmap(targetView.width,targetView.height,Bitmap.Config.ARGB_8888)
        val canvas=Canvas(mBitmap)
        targetView.draw(canvas)
        mBitmapRect=Rect(targetView.left,targetView.top,targetView.right,targetView.bottom)
        mStatus= STATUS_INIT
        invalidate()

        mStatus= STATUS_ANIMATION

        drawPath = true

//        val points= mutableListOf<PointF>()

        mControlPoints.add(PointF(targetView.getCenterX(),targetView.getCenterY()))
        mControlPoints.add(PointF(targetView.right.toFloat(),targetView.getCenterY()))
        mControlPoints.add(PointF(targetView.target.right.toFloat(),targetView.top.toFloat()))
//        firstStep(mControlPoints)
    }

    private fun View.getCenterX():Float{
        return (this.left+this.right)/2.toFloat()
    }
    private fun View.getCenterY():Float{
        return (this.top+this.bottom)/2.toFloat()
    }
    fun reset() {
        mBezierPath.reset()
        mControlPoints.clear()
        drawPath = false
        invalidate()
    }

    fun firstStep(points:MutableList<PointF>) {

        drawPath = true

        val valAnim = ValueAnimator.ofFloat(0.001f, 1f)
        valAnim.duration = 500
        valAnim.addUpdateListener {
            val i = it.animatedValue as Float
            val p = PointF(
                calculateBezier(points.size - 1, 0, i, true),
                calculateBezier(points.size - 1, 0, i, false)
            )
            if (i == 1 / 1000f) {
                mBezierPath.moveTo(p.x, p.y)
            } else {
                mBezierPath.lineTo(p.x, p.y)
            }

            mBitmapRect.left=p.x.toInt()-mBitmap.width/2
            mBitmapRect.top=p.y.toInt()+mBitmap.height/2

            invalidate()
        }
        valAnim.start()
    }

    fun secongStep(points:MutableList<PointF>){
        drawPath = true

        val valAnim = ValueAnimator.ofFloat(0.001f, 1f)
        valAnim.duration = 3 * 1000
        valAnim.addUpdateListener {
            val i = it.animatedValue as Float
            val p = PointF(
                calculateBezier(points.size - 1, 0, i, true),
                calculateBezier(points.size - 1, 0, i, false)
            )
            if (i == 1 / 1000f) {
                mBezierPath.moveTo(p.x, p.y)
            } else {
                mBezierPath.lineTo(p.x, p.y)
            }

            invalidate()
        }
        valAnim.start()
    }

    /**
     * p(i,j)=
     *
     * */
    private fun calculateBezier(order: Int, j: Int, t: Float, calculateX: Boolean): Float {
        return if (order == 1) {
            if (calculateX) {
                (1 - t).times(mControlPoints[j].x) + t.times(mControlPoints[j + 1].x)
            } else {
                (1 - t).times(mControlPoints[j].y) + t.times(mControlPoints[j + 1].y)
            }
        } else {
            (1 - t).times(calculateBezier(order - 1, j, t, calculateX)) + t.times(
                calculateBezier(
                    order - 1,
                    j + 1,
                    t,
                    calculateX
                )
            )
        }
    }

    private var drawPath = false
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        when(mStatus){
            STATUS_INIT -> {
            canvas?.drawBitmap(mBitmap,mBitmapRect.left.toFloat(),mBitmapRect.top.toFloat(),mLinePaint)
        }
            STATUS_ANIMATION->{
                canvas?.drawBitmap(mBitmap,mBitmapRect.left.toFloat(),mBitmapRect.top.toFloat(),mLinePaint)

            }
        }
        canvas?.apply {
            if (drawPath) {
                drawPath(mBezierPath, mPaint)
            }
            mLinePath
                .reset()
            mControlPoints.forEachIndexed { index, p ->
                drawCircle(p.x, p.y, 8f, mPointPaint)
                if (index == 0) {
                    mLinePath.moveTo(p.x, p.y)
                } else {
                    mLinePath.lineTo(p.x, p.y)
                }
            }
            drawPath(mLinePath, mLinePaint)
        }

    }
}