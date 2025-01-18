package com.taskease.collegeapp.Constants

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Toast

object Constant {

    fun toast(context: Context, message: String) {
        Toast.makeText(context,message,Toast.LENGTH_LONG).show()
    }

    fun applySlideInAnimation(view: View, position: Int) {
        view.alpha = 0f
        view.translationX = 300f // Start position (off-screen to the right)

        view.animate()
            .translationX(0f) // Slide to its final position
            .alpha(1f)        // Fade in
            .setDuration(300) // Duration for the animation
            .setStartDelay(position * 50L) // Stagger effect
            .start()
    }

    fun buttonAnimation(view : View)
    {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f, 1f)

        val animatorSet = AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 300
            interpolator = OvershootInterpolator()
        }
        animatorSet.start()
    }
}