/**
 * created by 小卷毛, 2018/4/21 0021
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
package com.ruanmeng.base

import android.view.View
import com.jakewharton.rxbinding2.view.RxView
import java.util.concurrent.TimeUnit

inline fun <reified T : View> T.visible() {
    visibility = View.VISIBLE
}

inline fun <reified T : View> T.invisible() {
    visibility = View.INVISIBLE
}

inline fun <reified T : View> T.gone() {
    visibility = View.GONE
}

/**
 * 防抖动点击事件，时间单位秒（默认1s）
 */
inline fun <reified T : View> T.setOneClickListener(onClickListener: View.OnClickListener) {
    RxView.clicks(this).throttleFirst(1, TimeUnit.SECONDS)
            .subscribe {
                onClickListener.onClick(this)
            }
}

/**
 * 防抖动点击事件，时间单位秒
 */
inline fun <reified T : View> T.setOneClickListener(duration: Long, onClickListener: View.OnClickListener) {
    RxView.clicks(this).throttleFirst(duration, TimeUnit.SECONDS)
            .subscribe {
                onClickListener.onClick(this)
            }
}