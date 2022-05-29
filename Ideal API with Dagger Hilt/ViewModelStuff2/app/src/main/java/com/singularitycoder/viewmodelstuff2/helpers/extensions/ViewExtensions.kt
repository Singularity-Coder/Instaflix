package com.singularitycoder.viewmodelstuff2.helpers.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.view.animation.AlphaAnimation
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.anime.view.AnimeDetailFragment
import com.singularitycoder.viewmodelstuff2.helpers.constants.FragmentsTags
import com.singularitycoder.viewmodelstuff2.helpers.constants.IntentKey
import com.singularitycoder.viewmodelstuff2.helpers.utils.timeNow
import java.net.URL


fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.inVisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.enable() {
    this.isEnabled = true
}

fun View.disable() {
    this.isEnabled = false
}

fun Context.color(@ColorRes colorRes: Int) = ContextCompat.getColor(this, colorRes)

fun Context.drawable(@DrawableRes drawableRes: Int): Drawable? = ContextCompat.getDrawable(this, drawableRes)

// Exceptions should be used for exceptional situations outside of the normal logic of a program. It might throw an exception in order to "give up" and go back to the caller (or to a catch{} block at the end of the method). ... It is not always obvious when an exception is appropriate.
fun String.toDrawable(): Drawable? {
    if (this.length < 7) return null
    if ("https://" !in this) return null
    if (this.substring(0..8) != "https://") return null
    // https://www.geeksforgeeks.org/check-if-an-url-is-valid-or-not-using-regular-expression/
    val regex = "((http|https)://)(www.)?" + "[a-zA-Z0-9@:%._+~#?&/=]" + "{2,256}\\.[a-z]" + "{2,6}\\b([-a-zA-Z0-9@:%" + "._+~#?&/=]*)"
//    if (!regex.toRegex().matches(this)) return null
    return Drawable.createFromStream(URL(this).openConnection().getInputStream(), "DRAWABLE_$timeNow")
}

fun Activity.setStatusBarColorTo(@ColorRes color: Int) {
    window.also {
        it.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        it.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        it.statusBarColor = ContextCompat.getColor(this, R.color.purple_700)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        it.setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
    }
}

fun Drawable.changeColor(
    context: Context,
    @ColorRes color: Int
): Drawable {
    val unwrappedDrawable = this
    val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable)
    DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(context, color))
    return this
}

// https://stackoverflow.com/questions/12728255/in-android-how-do-i-set-margins-in-dp-programmatically
fun View.setMargins(
    start: Int,
    top: Int,
    end: Int,
    bottom: Int
) {
    if (this.layoutParams !is ViewGroup.MarginLayoutParams) return
    val params = this.layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(start, top, end, bottom)
    this.requestLayout()
}

fun AppCompatActivity.showScreen(
    fragment: Fragment,
    tag: String,
    isAdd: Boolean = false
) {
    if (isAdd) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_to_left, R.anim.slide_to_right, R.anim.slide_to_left, R.anim.slide_to_right)
            .add(R.id.bottom_nav_view_container, fragment, tag)
            .addToBackStack(null)
            .commit()
    } else {
        supportFragmentManager.beginTransaction()
            .replace(R.id.bottom_nav_view_container, fragment, tag)
            .commit()
    }
}

fun AppCompatActivity.showAnimeDetailsOfThis(animeId: String) {
    println("Anime Id: $animeId")
    val bundle = Bundle().apply { putString(IntentKey.ID_OF_ANIME, animeId) }
    val fragment = AnimeDetailFragment().apply { arguments = bundle }
    showScreen(fragment = fragment, tag = FragmentsTags.ANIME_DETAIL.value, isAdd = true)
}

// https://stackoverflow.com/questions/5608720/android-preventing-double-click-on-a-button
fun View.onSafeClick(
    delayAfterClick: Long = 1.seconds(),
    onSafeClick: (Pair<View?, Boolean>) -> Unit
) {
    val onSafeClickListener = OnSafeClickListener(delayAfterClick, onSafeClick)
    setOnClickListener(onSafeClickListener)
}

class OnSafeClickListener(
    private val delayAfterClick: Long,
    private val onSafeClick: (Pair<View?, Boolean>) -> Unit
) : View.OnClickListener {
    private var lastClickTime = 0L
    private var isClicked = false

    override fun onClick(v: View?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            v?.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }
        val elapsedRealtime = SystemClock.elapsedRealtime()
        if (elapsedRealtime - lastClickTime < delayAfterClick) return
        lastClickTime = elapsedRealtime
        v?.startAnimation(AlphaAnimation(1F, 0.8F))
//        v?.setTouchEffect()
        isClicked = !isClicked
        onSafeClick(v to isClicked)
    }

    // https://stackoverflow.com/questions/7175873/how-to-set-button-click-effect-in-android
    // https://stackoverflow.com/questions/56716093/setcolorfilter-is-deprecated-on-api29
    @SuppressLint("ClickableViewAccessibility")
    fun View.setTouchEffect() {
        this.setOnTouchListener { v, event ->
            v ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(R.color.purple_100, BlendModeCompat.SRC_ATOP)
                    v.invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    v.background.clearColorFilter()
                    v.invalidate()
                }
            }
            false
        }
    }
}

// https://stackoverflow.com/questions/16954109/reduce-the-size-of-a-bitmap-to-a-specified-size-in-android
fun Bitmap?.resizeTo(maxWidth: Int, maxHeight: Int): Bitmap? {
    this ?: return this
    var width = this.width
    var height = this.height
    val bitmapRatio = width.toFloat() / height.toFloat()
    if (bitmapRatio > 1) {
        width = maxWidth
        height = (width / bitmapRatio).toInt()
    } else {
        height = maxHeight
        width = (height * bitmapRatio).toInt()
    }
    return Bitmap.createScaledBitmap(this, width, height, true)
}

fun Bitmap?.getDominantColor(): Int {
    this ?: return -1
    val newBitmap = Bitmap.createScaledBitmap(this, 1, 1, true)
    val color = newBitmap.getPixel(0, 0)
    newBitmap.recycle()
    return color
}

fun SwipeRefreshLayout.setDefaultColors(context: Context) {
    setColorSchemeColors(
        context.color(R.color.purple_500),
        context.color(R.color.purple_700)
    )
}

fun AppCompatActivity.avoidScreenShots() {
    window?.setFlags(
        WindowManager.LayoutParams.FLAG_SECURE,
        WindowManager.LayoutParams.FLAG_SECURE
    )
}

fun AppCompatActivity.setTransparentStatusBar() {
    window.setFlags(
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
    )
}

// https://stackoverflow.com/questions/2868047/fullscreen-activity-in-android
fun AppCompatActivity.fullScreen() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.insetsController?.hide(WindowInsets.Type.statusBars())
    } else {
        @Suppress("DEPRECATION")
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }
}
