package com.ruanmeng.utils

/**
 * 姓名替换，保留姓氏
 * 如果身姓名为空 或者 null ,返回null ；否则，返回替换后的字符串；
 */
fun String.nameReplaceWithStar(): String = when {
    isNullOrEmpty() -> ""
    else -> replaceAction("(?<=[\\u4e00-\\u9fa5]{" + (if (length > 3) "2" else "1") + "})[\\u4e00-\\u9fa5](?=[\\u4e00-\\u9fa5]{0})")
}

/**
 * 手机号号替换，保留前三位和后四位
 * 如果身手机号为空 或者 null ,返回null ；否则，返回替换后的字符串；
 */
fun String.phoneReplaceWithStar(): String = when {
    isNullOrEmpty() -> ""
    length < 7 -> this
    else -> replaceAction("(?<=\\d{3})\\d(?=\\d{4})")
}

/**
 * 身份证号替换，保留前四位和后四位
 * 如果身份证号为空 或者 null ,返回null ；否则，返回替换后的字符串；
 */
fun String.idCardReplaceWithStar(): String = when {
    isNullOrEmpty() -> ""
    length < 8 -> this
    else -> replaceAction("(?<=\\d{4})\\d(?=\\d{4})")
}

/**
 * 银行卡替换，保留后四位
 * 如果银行卡号为空 或者 null ,返回null ；否则，返回替换后的字符串；
 */
fun String.bankCardReplaceWithStar(): String = when {
    isNullOrEmpty() -> ""
    length < 4 -> this
    else -> replaceAction("(?<=\\d{0})\\d(?=\\d{4})")
}

/**
 * 银行卡替换，保留后四位
 * 如果银行卡号为空 或者 null ,返回null ；否则，返回替换后的字符串；
 */
fun String.bankCardReplaceHeaderWithStar(): String = when {
    isNullOrEmpty() -> ""
    length < 10 -> this
    else -> replaceAction( "(?<=\\d{6})\\d(?=\\d{4})")
}

/**
 * 实际替换动作
 *
 * @param regular 正则
 */
fun String.replaceAction(regular: String): String = replace(regular.toRegex(), "*")