package com.sheep.treasuretool.data.model.entity

/**
 * 用户数据模型
 * @property id 用户ID， 用于WebSocket通信
 * @property username 用户名
 * @property nickname 昵称
 * @property avatar 头像URL
 * @property mobilePhone 手机号
 * @property password 密码/token
 * @property enabled 是否启用
 * @property registrationTime 注册时间
 */
data class User(
    val id: String,
    val username: String,
    val nickname: String,
    val avatar: String,
    val mobilePhone: String? = null,
    val password: String? = null,
    val enabled: Boolean? = false,
    val registrationTime: Long? = null
)