package com.maechuri.mainserver.game.entity

import com.maechuri.mainserver.scenario.entity.AssetStatus
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table(name = "asset")
class Asset(
    @Id
    var id: Long? = null,
    var name: String,
    var prompt: String? = null,
    var rawUrl: String? = null,
    var resizedUrl: String? = null,
    var finalUrl: String? = null,
    var status: AssetStatus = AssetStatus.COMPLETED,
    var createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @Transient
    var assetTags: Set<AssetTag> = emptySet()
}
