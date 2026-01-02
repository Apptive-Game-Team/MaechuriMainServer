package com.maechuri.mainserver.game.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.Objects

@Table(name = "tag")
class Tag(
    @Id
    var id: Long? = null,
    var name: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Tag) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return Objects.hash(id)
    }
}