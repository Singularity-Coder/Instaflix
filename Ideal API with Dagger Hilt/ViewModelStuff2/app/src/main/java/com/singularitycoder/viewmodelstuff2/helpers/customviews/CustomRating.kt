package com.singularitycoder.viewmodelstuff2.helpers.customviews

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.singularitycoder.viewmodelstuff2.R
import dagger.hilt.android.AndroidEntryPoint

// TODO
// 1. Each rating bar must be a face with hair
// 2. Hair should be fire animation - violet flames

// https://stackoverflow.com/questions/7007429/how-to-draw-a-triangle-a-star-a-square-or-a-heart-on-the-canvas
// https://www.vogella.com/tutorials/AndroidCustomViews/article.html
// https://codelabs.developers.google.com/codelabs/advanced-android-training-custom-view-from-scratch/index.html?index=..%2F..advanced-android-training#2
// https://medium.com/mobile-app-development-publication/building-custom-component-with-kotlin-fc082678b080
// https://www.raywenderlich.com/142-android-custom-view-tutorial
// https://medium.com/mobile-app-development-publication/custom-touchable-animated-view-in-kotlin-3ad599f85dbc
// https://medium.com/@quiro91/custom-view-mastering-onmeasure-a0a0bb11784d
// Custom and Compound Views
// View starts drawing in the onDraw() method which receives Canvas object. U can draw shapes, text or bitmap
// If you want to re draw the view then you should call invalidate() method which triggers onDraw() method of this view
// To draw stuff, we use Canvas API

// LayoutManager calls onMeasure() method of this view. LayoutManager determines the size of this view

// Lifecycle of android view
// onAttachedToWindow() - called when window is available
// onDetachedFromWindow() - called when view removed from parent. Used to stop animations, clean up resources used by view

// Lifecycle events - Animates -> Measure -> Layout -> Draw
// If this view is not render in the layout editor: Build -> Make Project - https://stackoverflow.com/questions/16592965/android-studio-layout-editor-cannot-render-custom-views

@AndroidEntryPoint
class CustomRating : View {

    var rating: Int = 0
        set(value) {
            field = value
            invalidate() // After setting the rating value we want the onDraw() method to be called again to redraw the rating view
        }

    private val path = Path() // This is like pencil that defined the boundaries
    private var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG) // This is literally paint brush with paint to fill in the sketch defined by the Path()

    private var viewWidth: Float = 0F
    private var viewHeight: Float = 0F

    private var activeColor: Int = Color.parseColor("#FF6200EE")
    private var inActiveColor: Int = Color.parseColor("#D1C4E9")

    constructor(context: Context?) : this(context, null, -1)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, -1)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        // Init defaults for the view
        attrs ?: return
        val styledAttrs = context?.obtainStyledAttributes(attrs, R.styleable.CustomRating) ?: return
        rating = styledAttrs.getInt(R.styleable.CustomRating_rating, rating) // We do this to set rating value directly with xml attribute
        styledAttrs.recycle()
    }

    init {
        val displayMetrics = Resources.getSystem().displayMetrics

        // Set default view properties
        paint.apply {
            style = Paint.Style.FILL
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }

        viewWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            60F,
            displayMetrics
        )

        viewHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            30F,
            displayMetrics
        )
    }

    // Set the view dimensions here
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec) // We will get the width defined in the xml
        val heightMode = MeasureSpec.getMode(heightMeasureSpec) // We will get the height defined in xml
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        viewWidth = widthSize.toFloat()
        viewHeight = (viewWidth / 5) * 0.8F

        setMeasuredDimension(viewWidth.toInt(), viewHeight.toInt())
    }

    // This draws the image with the specified dimensions
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // Fill the canvas with background color
        canvas?.drawColor(Color.WHITE)

        // Starting point
        path.moveTo(
            viewWidth / 2,
            viewHeight / 5
        )

        // Upper left path
        path.cubicTo(
            5 * viewWidth / 14,
            0F,
            0F,
            viewHeight / 15,
            viewWidth / 28,
            2 * viewHeight / 5
        )

        // Upper right path
        path.cubicTo(
            viewWidth,
            viewHeight / 15,
            9 * viewWidth / 14,
            0F,
            viewWidth / 2,
            viewHeight / 5
        )

        // Lower left path
        path.cubicTo(
            viewWidth / 14,
            2 * viewHeight / 3,
            3 * viewWidth / 7,
            5 * viewHeight / 6,
            viewWidth / 2,
            viewHeight
        )

        // Lower right path
        path.cubicTo(
            4 * viewWidth / 7,
            5 * viewHeight / 6,
            13 * viewWidth / 14,
            2 * viewHeight / 3,
            27 * viewWidth / 28,
            2 * viewHeight / 5
        )

//        paint.apply {
//            shader = null
//            color = activeColor
//            style = Paint.Style.FILL
//        }
//        canvas?.drawPath(path, paint)
    }
}
