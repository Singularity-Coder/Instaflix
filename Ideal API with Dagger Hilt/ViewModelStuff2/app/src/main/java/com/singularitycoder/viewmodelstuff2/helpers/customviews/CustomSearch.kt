package com.singularitycoder.viewmodelstuff2.helpers.customviews

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.*
import android.view.View.OnFocusChangeListener
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.databinding.LayoutCustomSearchBinding
import com.singularitycoder.viewmodelstuff2.helpers.extensions.*

// GESTURE LISTENER
// Pinch
// Double Tap
// Scrolls
// Long Press >500ms
// Flinch
// TOUCH LISTENER
// DRAG LISTENER - drag views on screen

// The advantage of a custom view over a reusable xml layout using include tag is that the view boiler plate can be nicely separated and reused

class CustomSearch :
    ConstraintLayout,
    View.OnTouchListener,
    GestureDetector.OnGestureListener,
    View.OnDragListener,
    GestureDetector.OnDoubleTapListener {

    private lateinit var gestureDetector: GestureDetector
    private lateinit var binding: LayoutCustomSearchBinding

    constructor(context: Context) : this(context, null, -1) {
        setUpView()
    }

    // This constructor is imp. Else view is not recognised - https://stackoverflow.com/questions/34469487/caused-by-java-lang-nosuchmethodexception-init-class-android-content-contex/53948502
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, -1) {
        setUpView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setUpView()
    }

    private fun setUpView() {
        binding = LayoutCustomSearchBinding.inflate(LayoutInflater.from(context))
        gestureDetector = GestureDetector(context, this)
        binding.root.layoutParams = ConstraintLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        addView(binding.root)

        binding.etSearch.apply {
            onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
                val searchDrawable = context.drawable(R.drawable.ic_round_search_24)?.changeColor(context, if (hasFocus) R.color.purple_500 else R.color.default_color)
                binding.etSearch.setCompoundDrawablesWithIntrinsicBounds(searchDrawable, null, null, null)
            }
            addTextChangedListener { it: Editable? ->
                if (it?.isBlank() == true) {
                    binding.cardVoiceSearch.visible()
                    binding.ibClearText.gone()
                } else {
                    binding.cardVoiceSearch.inVisible()
                    binding.ibClearText.visible()
                }
            }
        }

        binding.ibClearText.onSafeClick { clearTextAndHideKeyboard() }

        binding.etSearch.apply {
            setOnTouchListener(this@CustomSearch)
            setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) binding.etSearch.hideKeyboard()
            }
        }
    }

    private fun clearTextAndHideKeyboard() {
        binding.etSearch.apply {
            setText("")
            hideKeyboard() // This has to be before clearFocus else it wont work
            clearFocus()
        }
    }

    fun getSearchView(): EditText = binding.etSearch

    // https://github.com/mitchtabian/CodingWithMitchStore
    // https://www.youtube.com/watch?v=xHCsL5QOSv4
    // View.OnTouchListener - All gestures start by detecting a touch. So implement View.OnTouchListener interface first
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return if (v?.id == binding.etSearch.id) {
            gestureDetector.onTouchEvent(event)
            true
        } else false
    }

    // GestureDetector.OnGestureListener
    override fun onDown(e: MotionEvent?): Boolean {
        println("Down gesture")
        requestSearchFocus()
        return false
    }

    // GestureDetector.OnGestureListener
    override fun onShowPress(e: MotionEvent?) {
        println("Press gesture")
        requestSearchFocus()
    }

    // GestureDetector.OnGestureListener
    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        println("Single tap up gesture")
        requestSearchFocus()
        return false
    }

    // GestureDetector.OnGestureListener
    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        println("Scroll gesture")
        return false
    }

    // GestureDetector.OnGestureListener
    override fun onLongPress(e: MotionEvent?) {
        println("Long Press gesture")
        // On long press Copy text to clipboard
    }

    // GestureDetector.OnGestureListener
    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        println("Fling gesture")
        clearTextAndHideKeyboard()
        return false
    }

    // View.OnDragListener
    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        when (event?.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                println("onDrag: drag started.")
                return true
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                println("onDrag: drag entered.")
                return true
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
                println("onDrag: current point: ( " + event.x.toString() + " , " + event.y.toString() + " )")
                return true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                println("onDrag: exited.")
                return true
            }
            DragEvent.ACTION_DROP -> {
                println("onDrag: dropped.")
                return true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                println("onDrag: ended.")
                return true
            }
            else -> println("Unknown action type received by OnStartDragListener.")
        }
        return false
    }

    // GestureDetector.OnDoubleTapListener
    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        println("onSingleTapConfirmed: called")
        requestSearchFocus()
        return false
    }

    // GestureDetector.OnDoubleTapListener
    override fun onDoubleTap(e: MotionEvent?): Boolean {
        println("onDoubleTap: called")
        return false
    }

    // GestureDetector.OnDoubleTapListener
    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        println("onDoubleTapEvent: called")
        return false
    }

    private fun requestSearchFocus() {
        binding.etSearch.apply {
            performClick()
            requestFocus()
            showKeyboard()
        }
    }
}
