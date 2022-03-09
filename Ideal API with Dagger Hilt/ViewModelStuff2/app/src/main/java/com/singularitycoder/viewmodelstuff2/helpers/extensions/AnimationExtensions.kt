package com.singularitycoder.viewmodelstuff2.helpers.extensions

import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation

private fun View.revealAnimation(
    fromXDelta: Int = 0,
    toXDelta: Int = 0,
    fromYDelta: Int = 0,
    toYDelta: Int = 0,
    onAnimationEnd: () -> Unit = {},
    onAnimationStart: () -> Unit = {}
) {
    val animate = TranslateAnimation(fromXDelta.toFloat(), toXDelta.toFloat(), fromYDelta.toFloat(), toYDelta.toFloat()).apply {
        duration = 100
        fillAfter = true
        setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) = Unit
            override fun onAnimationEnd(animation: Animation?) = onAnimationEnd.invoke()
            override fun onAnimationStart(animation: Animation?) = onAnimationStart.invoke()
        })
    }
    this.startAnimation(animate)
}

fun View.revealLeftToRight() = revealAnimation(fromXDelta = this.width, onAnimationStart = { this@revealLeftToRight.visible() })

fun View.revealRightToLeft() = revealAnimation(toXDelta = this.width, onAnimationEnd = { this@revealRightToLeft.gone() })

fun View.revealBottomToTop() = revealAnimation(fromYDelta = this.height, onAnimationStart = { this@revealBottomToTop.visible() })

fun View.revealTopToBottom() = revealAnimation(toYDelta = this.height, onAnimationEnd = { this@revealTopToBottom.gone() })
