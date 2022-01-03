package com.projects.iutmessenger.models

class Group {
    var groupID: Long? = null
    var groupName: String? = null
    var imgUrl: String? = null
    var date: String? = null

    constructor()
    constructor(groupID: Long?, groupName: String?, imgUrl: String?, date: String?) {
        this.groupID = groupID
        this.groupName = groupName
        this.imgUrl = imgUrl
        this.date = date
    }

}