package com.maechuri.mainserver.storage.service

import org.springframework.stereotype.Service
import java.awt.Color
import java.awt.Point
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.LinkedList
import javax.imageio.ImageIO

@Service
class WhiteToTransparentService : BackgroundRemovalService {

    companion object {
        private const val WHITE_THRESHOLD = 245 // R, G, B 값이 모두 이 값 이상이면 흰색으로 간주
    }

    override suspend fun removeBackground(imageBytes: ByteArray): ByteArray {
        val originalImage: BufferedImage = ImageIO.read(ByteArrayInputStream(imageBytes))
            ?: return imageBytes // 이미지를 읽을 수 없으면 원본 반환

        val width = originalImage.width
        val height = originalImage.height

        val toTransparent = Array(height) { BooleanArray(width) }
        val queue = LinkedList<Point>()

        // 1. 이미지 가장자리의 흰색 픽셀을 시작점으로 큐에 추가
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    if (isWhite(originalImage.getRGB(x, y)) && !toTransparent[y][x]) {
                        toTransparent[y][x] = true
                        queue.add(Point(x, y))
                    }
                }
            }
        }

        // 2. Flood Fill 알고리즘 실행
        val dx = intArrayOf(0, 0, 1, -1)
        val dy = intArrayOf(1, -1, 0, 0)

        while (queue.isNotEmpty()) {
            val point = queue.poll()
            for (i in 0..3) {
                val nx = point.x + dx[i]
                val ny = point.y + dy[i]

                if (nx in 0 until width && ny in 0 until height && !toTransparent[ny][nx] && isWhite(originalImage.getRGB(nx, ny))) {
                    toTransparent[ny][nx] = true
                    queue.add(Point(nx, ny))
                }
            }
        }

        // 3. 새로운 이미지 생성
        val newImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (toTransparent[y][x]) {
                    newImage.setRGB(x, y, 0x00FFFFFF) // 투명 픽셀로 설정
                } else {
                    newImage.setRGB(x, y, originalImage.getRGB(x, y))
                }
            }
        }

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(newImage, "png", outputStream) // 배경 투명도를 지원하는 PNG로 출력
        return outputStream.toByteArray()
    }

    private fun isWhite(rgb: Int): Boolean {
        val color = Color(rgb)
        return color.red >= WHITE_THRESHOLD && color.green >= WHITE_THRESHOLD && color.blue >= WHITE_THRESHOLD
    }
}
