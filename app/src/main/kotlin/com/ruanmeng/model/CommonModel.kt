/**
 * created by 小卷毛, 2018/4/19 0019
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
package com.ruanmeng.model

import java.io.Serializable

/**
 * 项目名称：Village_Arrival
 * 创建人：小卷毛
 * 创建时间：2018-04-19 16:45
 */
data class CommonModel(
        var cyCtn: String = "",
        var msgCtn: String = "",
        var nowCtn: String = "",
        var msg: String = "",
        var msgcode: String = "",
        var orders: List<CommonData> ?= ArrayList(),
        var sliders: List<CommonData> ?= ArrayList(),
        var modules: List<CommonData> ?= ArrayList(),
        var nnews: List<CommonData> ?= ArrayList(),
        var cooperationList: List<CommonData> ?= ArrayList()
): Serializable