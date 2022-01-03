package com.projects.iutmessenger.models

class Student {
    var uid: String? = null
    var birthDate: String? = null
    var groupID: Long? = null
    var imageUrl: String? = null
    var name: String? = null
    var surname: String? = null
    var telegramNickName: String? = null
    var role: String? = "user"
    var token: String? = "token"
    var email: String? = null


    constructor()
    constructor(
        id: String?,
        birthDate: String?,
        groupID: Long?,
        imageUrl: String?,
        name: String?,
        surname: String?,
        telegramNickName: String?
    ) {
        this.uid = id
        this.birthDate = birthDate
        this.groupID = groupID
        this.imageUrl = imageUrl
        this.name = name
        this.surname = surname
        this.telegramNickName = telegramNickName
    }

    constructor(
        id: String?,
        birthDate: String?,
        groupID: Long?,
        imageUrl: String?,
        name: String?,
        surname: String?,
        telegramNickName: String?,
        role: String?
    ) {
        this.uid = id
        this.birthDate = birthDate
        this.groupID = groupID
        this.imageUrl = imageUrl
        this.name = name
        this.surname = surname
        this.telegramNickName = telegramNickName
        this.role = role
    }

    constructor(
        uid: String?,
        birthDate: String?,
        groupID: Long?,
        imageUrl: String?,
        name: String?,
        surname: String?,
        telegramNickName: String?,
        role: String?,
        token: String?,
        email: String?
    ) {
        this.uid = uid
        this.birthDate = birthDate
        this.groupID = groupID
        this.imageUrl = imageUrl
        this.name = name
        this.surname = surname
        this.telegramNickName = telegramNickName
        this.role = role
        this.token = token
        this.email = email
    }

    override fun toString(): String {
        return "Student(uid=$uid, birthDate=$birthDate, groupID=$groupID, imageUrl=$imageUrl, name=$name, surname=$surname, telegramNickName=$telegramNickName, role=$role, token=$token)"
    }


}