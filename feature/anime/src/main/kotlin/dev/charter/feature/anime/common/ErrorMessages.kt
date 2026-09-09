package dev.charter.feature.anime.common

import dev.charter.core.common.error.AppError

/**
 * Actionable user-facing copy per error class — the message tells the user
 * the next action, never the failure's internals (docs/ui/product.md §2).
 */
fun AppError.userMessage(): String =
    when (this) {
        is AppError.Network -> "网络不可用，请检查连接后重试"
        is AppError.Http -> "服务器出错了（$code），请稍后重试"
        is AppError.Rejected -> "请求太频繁了，请稍后再试"
        is AppError.Serialization -> "数据格式异常，请稍后重试"
        is AppError.Storage -> "本地存储异常，请重启应用后重试"
        is AppError.Unknown -> "出了点问题，请重试"
    }
