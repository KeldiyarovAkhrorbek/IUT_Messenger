package com.projects.iutmessenger.models

class MessageToAdmin {
    var telegramNickname: String? = null
    var messageBody: String? = null
    var groupName: String? = null
    var date: String? = null
    var senderUID: String? = null
    var senderTOKEN: String? = null
    var done: Boolean? = false
    var done_by: String? = null


    constructor()

    constructor(
        telegramNickname: String?,
        messageBody: String?,
        groupName: String?,
        date: String?,
        senderUID: String?,
        senderTOKEN: String?,
        done: Boolean?,
        done_by: String?
    ) {
        this.telegramNickname = telegramNickname
        this.messageBody = messageBody
        this.groupName = groupName
        this.date = date
        this.senderUID = senderUID
        this.senderTOKEN = senderTOKEN
        this.done = done
        this.done_by = done_by
    }


}