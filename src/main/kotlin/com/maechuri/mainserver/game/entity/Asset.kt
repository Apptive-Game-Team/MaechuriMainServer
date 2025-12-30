package com.maechuri.mainserver.game.entity

import lombok.NoArgsConstructor
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table(name = "asset")
@NoArgsConstructor
class Asset {

    @Id
    private var id: Long? = null
    private var name: String? = null
    private var metaFileUrl: String? = null
}