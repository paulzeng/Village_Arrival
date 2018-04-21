/**
 * created by 小卷毛, 2018/04/09 0029
 * Copyright (c) 2018, 416143467@qq.com All Rights Reserved.
 * #                   *********                            #
 * #                  ************                          #
 * #                  *************                         #
 * #                 **  ***********                        #
 * #                ***  ****** *****                       #
 * #                *** *******   ****                      #
 * #               ***  ********** ****                     #
 * #              ****  *********** ****                    #
 * #            *****   ***********  *****                  #
 * #           ******   *** ********   *****                #
 * #           *****   ***   ********   ******              #
 * #          ******   ***  ***********   ******            #
 * #         ******   **** **************  ******           #
 * #        *******  ********************* *******          #
 * #        *******  ******************************         #
 * #       *******  ****** ***************** *******        #
 * #       *******  ****** ****** *********   ******        #
 * #       *******    **  ******   ******     ******        #
 * #       *******        ******    *****     *****         #
 * #        ******        *****     *****     ****          #
 * #         *****        ****      *****     ***           #
 * #          *****       ***        ***      *             #
 * #            **       ****        ****                   #
 */
package com.ruanmeng.utils

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.widget.TextView

/**
 * Android系统本身内置了一些通用的Interpolator(插值器)，
 * AccelerateDecelerateInterpolator   在动画开始与结束的地方速率改变比较慢，在中间的时候加速
 * AccelerateInterpolator             在动画开始的地方速率改变比较慢，然后开始加速
 * AnticipateInterpolator             开始的时候向后然后向前甩
 * AnticipateOvershootInterpolator    开始的时候向后然后向前甩一定值后返回最后的值
 * BounceInterpolator                 动画结束的时候弹起
 * CycleInterpolator                  动画循环播放特定的次数，速率改变沿着正弦曲线
 * DecelerateInterpolator             在动画开始的地方快然后慢
 * LinearInterpolator                 以常量速率改变（匀速）
 * OvershootInterpolator              向前甩一定值后再回到原来位置
 */

/**
 * 旋转动画，默认300ms
 */
inline fun <reified T : View> T.startRotateAnimator(from: Float, to: Float) {
    ObjectAnimator.ofFloat(this, "rotation", from, to).apply {
        duration = 300
        interpolator = DecelerateInterpolator()
        start()
    }
}

/**
 * 旋转动画，设置指定的时间，单位毫秒
 */
inline fun <reified T : View> T.startRotateAnimator(from: Float, to: Float, milliseconds: Long) {
    ObjectAnimator.ofFloat(this, "rotation", from, to).apply {
        duration = milliseconds
        interpolator = DecelerateInterpolator()
        start()
    }
}

/**
 * 数字文本加载动画，默认1000ms
 */
inline fun <reified T : TextView> T.startIncreaseAnimator(to: Int) {
    ValueAnimator.ofInt(0, to).apply {
        addUpdateListener { valueAnimator -> text = valueAnimator.animatedValue.toString() }
        duration = 1000
        interpolator = DecelerateInterpolator()
        start()
    }
}

/**
 * 数字文本加载动画，默认1000ms
 */
inline fun <reified T : TextView> T.startIncreaseAnimator(to: Float) {
    ValueAnimator.ofFloat(0f, to).apply {
        addUpdateListener { valueAnimator ->
            text = String.format("%.2f", valueAnimator.animatedValue)
        }
        duration = 1000
        interpolator = DecelerateInterpolator()
        start()
    }
}

/**
 * 数字文本加载动画，设置指定的时间，单位毫秒
 */
inline fun <reified T : TextView> T.startIncreaseAnimator(to: Int, milliseconds: Long) {
    ValueAnimator.ofInt(0, to).apply {
        addUpdateListener { valueAnimator -> text = valueAnimator.animatedValue.toString() }
        duration = milliseconds
        interpolator = DecelerateInterpolator()
        start()
    }
}

/**
 * 数字文本加载动画，设置指定的时间，单位毫秒
 */
inline fun <reified T : TextView> T.startIncreaseAnimator(to: Float, milliseconds: Long) {
    ValueAnimator.ofFloat(0f, to).apply {
        addUpdateListener { valueAnimator ->
            text = String.format("%.2f", valueAnimator.animatedValue)
        }
        duration = milliseconds
        interpolator = DecelerateInterpolator()
        start()
    }
}

/**
 * 显示动画，默认300ms
 */
inline fun <reified T : View> T.visibleAnimation() {
    if (visibility == View.GONE || visibility == View.INVISIBLE) {
        val appearAnimation = AlphaAnimation(0f, 1f)
        appearAnimation.duration = 300
        appearAnimation.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                visibility = View.VISIBLE
            }

            override fun onAnimationStart(animation: Animation) {}

        })
        startAnimation(appearAnimation)
    }
}

/**
 * 隐藏动画，默认300ms
 */
inline fun <reified T : View> T.goneAnimation() {
    if (visibility == View.VISIBLE) {
        val disappearAnimation = AlphaAnimation(1f, 0f)
        disappearAnimation.duration = 300
        disappearAnimation.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                visibility = View.GONE
            }

            override fun onAnimationStart(animation: Animation) {}

        })
        startAnimation(disappearAnimation)
    }
}
