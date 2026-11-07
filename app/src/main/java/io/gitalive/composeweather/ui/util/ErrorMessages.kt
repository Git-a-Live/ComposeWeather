package io.gitalive.composeweather.ui.util

import retrofit2.HttpException
import java.io.IOException

/** 把底层异常翻译成用户可读文案。 */
fun Throwable.toUserMessage(): String = when (this) {
    is IOException -> "网络不可用，已尝试展示本地缓存"
    is HttpException -> "服务返回异常（HTTP ${code()}）"
    else -> message?.takeIf { it.isNotBlank() } ?: "加载失败，请稍后重试"
}
