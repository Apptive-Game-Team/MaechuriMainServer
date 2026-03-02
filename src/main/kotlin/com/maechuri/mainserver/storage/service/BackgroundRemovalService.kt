package com.maechuri.mainserver.storage.service

interface BackgroundRemovalService {
    suspend fun removeBackground(imageBytes: ByteArray): ByteArray
}
