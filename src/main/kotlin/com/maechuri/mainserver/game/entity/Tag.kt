package com.maechuri.mainserver.game.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table(name = "tag")
class Tag {

    @Id
    var id: Long? = null
    var name: String? = null

}