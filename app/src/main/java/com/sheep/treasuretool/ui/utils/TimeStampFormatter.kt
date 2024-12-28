package com.sheep.treasuretool.ui.utils

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TimeStampFormatter {


    companion object {

        fun toSendTime(time: Long) : String {
            // 使用给定的秒级时间戳创建一个LocalDateTime实例
            val dateTime = LocalDateTime.ofEpochSecond(time, 0, ZoneId.systemDefault().rules.getOffset(LocalDateTime.now()))

            // 创建一个DateTimeFormatter实例来格式化日期时间
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

            // 使用formatter格式化LocalDateTime实例，并返回结果
            return dateTime.format(formatter)
        }

    }


}