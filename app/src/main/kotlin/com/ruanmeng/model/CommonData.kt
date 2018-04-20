/**
 * created by 小卷毛, 2018/3/7 0007
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
 * 创建时间：2018-04-11 17:53
 */
data class CommonData(
        //地址列表
        var commonAddressId: String = "",
        var address: String = "",
        var nickName: String = "",
        var telephone: String = "",
        var lat: String = "",
        var lng: String = "",
        //附近订单
        var goodsOrderId: String = "",
        var goods: String = "",
        var status: String = "",
        var type: String = "",
        var receiptLat: String = "",
        var receiptLng: String = "",
        var receiptAddress: String = "",
        var buyAddress: String = ""
) : Serializable