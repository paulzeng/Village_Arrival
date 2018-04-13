/**
 * created by 小卷毛, 2018/03/05
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

@file:Suppress("NOTHING_TO_INLINE")

package com.ruanmeng.base

import android.app.Activity
import android.content.Intent
import android.support.annotation.IdRes
import android.view.View
import android.widget.Toast
import com.ruanmeng.utils.DialogHelper
import com.ruanmeng.utils.PreferencesUtils

/**
 * Activity 扩展类
 */
inline fun <reified T : View> Activity.find(@IdRes id: Int): T = findViewById(id)

inline fun <reified T : Activity> Activity.startActivity() = startActivity(Intent(this, T::class.java))

fun Activity.showToast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) = Toast.makeText(this, text, duration).show()

inline fun Activity.getString(key: String): String = PreferencesUtils.getString(this, key, "")

inline fun Activity.getString(key: String, defaultValue: String): String = PreferencesUtils.getString(this, key, defaultValue)

inline fun Activity.putString(key: String, vaule: String) = PreferencesUtils.putString(this, key, vaule)

inline fun Activity.getBoolean(key: String): Boolean = PreferencesUtils.getBoolean(this, key)

inline fun Activity.putBoolean(key: String, vaule: Boolean) = PreferencesUtils.putBoolean(this, key, vaule)

inline fun Activity.showLoadingDialog() = DialogHelper.showDialog(this)

inline fun Activity.cancelLoadingDialog() = DialogHelper.dismissDialog()