package com.infinite.shoppingview

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.scale
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlin.math.nextDown
import kotlin.math.pow

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
        mControlPoints.add(PointF(car.getCenterX(),targetView.getCenterY()))
        mControlPoints.add(PointF(car.getCenterX(),car.getCenterY()))
        firstStep(mControlPoints)
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

    private var mScale=1f
    fun firstStep(points:MutableList<PointF>) {

        drawPath = true

        val pathAnim = ObjectAnimator.ofObject(BezierEvaluator(points[1]),points[0],points[2])
        pathAnim.duration = 500
        pathAnim.addUpdateListener {
            val p = it.animatedValue as PointF

            mBitmapRect.left=p.x.toInt()-mBitmap.width/2
            mBitmapRect.top=p.y.toInt()-mBitmap.height/2

            invalidate()
        }
        val scaleAnim=ObjectAnimator.ofFloat(1f,0.5f)
        scaleAnim.duration=500
        scaleAnim.addUpdateListener {
            mScale=it.animatedValue as Float
            Log.e("scale",mScale.toString())
        }
//        val animSet=AnimatorSet()
//        animSet.duration=500
//        animSet.playTogether(pathAnim,scaleAnim)
        pathAnim.start()
        scaleAnim.start()
    }

    class BezierEvaluator constructor(controlPointF: PointF) :TypeEvaluator<PointF>{

        private val control=controlPointF
        override fun evaluate(time: Float, start: PointF, end: PointF): PointF {
            val result=PointF()
            val timeLeft=1-time
            result.x = timeLeft.pow(2).times(start.x)+timeLeft.times(control.x).times(2).times(time)+time.pow(2).times(end.x)
            result.y = timeLeft.pow(2).times(start.y)+timeLeft.times(control.y).times(2).times(time)+time.pow(2).times(end.y)

            return result
        }
    }

    fun secongStep(points:MutableList<PointF>){
    }


    private var drawPath = false
    private val scaleMatrix=Matrix()
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        when(mStatus){
            STATUS_INIT -> {
//                scaleMatrix.preScale(mScale,mScale)
//                canvas?.setMatrix(scaleMatrix)
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