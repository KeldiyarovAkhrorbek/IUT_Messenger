package com.projects.iutmessenger.notification

data class PushNotification(
    val data: NotificationData,
    val to: String
)