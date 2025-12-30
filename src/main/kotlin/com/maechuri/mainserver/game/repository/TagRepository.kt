package com.maechuri.mainserver.game.repository

import com.maechuri.mainserver.game.entity.Tag
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface TagRepository : R2dbcRepository<Tag, Long> {
}