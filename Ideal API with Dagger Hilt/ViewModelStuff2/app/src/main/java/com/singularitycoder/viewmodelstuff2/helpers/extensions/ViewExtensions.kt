package com.singularitycoder.viewmodelstuff2.helpers.extensions

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
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
    val bundle = Bundle().apply { putString(IntentKey.ANIME_ID, animeId) }
    val fragment = AnimeDetailFragment().apply { arguments = bundle }
    showScreen(fragment = fragment, tag = FragmentsTags.ANIME_DETAIL.value, isAdd = true)
}

// https://stackoverflow.com/questions/5608720/android-preventing-double-click-on-a-button
fun View.onSafeClick(
    delayAfterClick: Long = 1.seconds(),
    onSafeClick: (View?) -> Unit
) {
    val onSafeClickListener = OnSafeClickListener(delayAfterClick, onSafeClick)
    setOnClickListener(onSafeClickListener)
}

class OnSafeClickListener(
    private val delayAfterClick: Long,
    private val onSafeClick: (View?) -> Unit
) : View.OnClickListener {
    private var lastClickTime = 0L

    override fun onClick(v: View?) {
        val elapsedRealtime = SystemClock.elapsedRealtime()
        if (elapsedRealtime - lastClickTime < delayAfterClick) return
        lastClickTime = elapsedRealtime
        onSafeClick(v)
    }
}
