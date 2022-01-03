package com.projects.iutmessenger.models

class ChatMessage {

    var from: String? = null
    var message: String? = null
    var date: String? = null
    var photoUrl: String? = null
    var userName: String? = null


    constructor()
    constructor(
        from: String?,
        message: String?,
        date: String?,
        photoUrl: String?,
        userName: String?
    ) {
        this.from = from
        this.message = message
        this.date = date
        this.photoUrl = photoUrl
        this.userName = userName
    }
}