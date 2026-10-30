package com.example.data

import androidx.compose.ui.graphics.Color
import java.net.URI
import java.util.UUID

data class SpeedDial(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val url: String,
  val position: Int
) {
  val domain: String
    get() {
      return try {
        val cleanUrl = url.trim()
        val uri = if (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://")) {
          URI(cleanUrl)
        } else {
          URI("https://$cleanUrl")
        }
        val host = uri.host ?: cleanUrl
        host.removePrefix("www.")
      } catch (e: Exception) {
        val clean = url.trim()
          .removePrefix("https://")
          .removePrefix("http://")
          .removePrefix("www.")
        clean.split("/").firstOrNull()?.split("?")?.firstOrNull() ?: clean
      }
    }

  val faviconUrl: String
    get() = "https://www.google.com/s2/favicons?domain=$domain&sz=128"

  val brandColor: Color
    get() {
      val d = domain.lowercase()
      return when {
        d.contains("google") -> Color(0xFF4285F4)
        d.contains("youtube") -> Color(0xFFFF0000)
        d.contains("wikipedia") -> Color(0xFF333333)
        d.contains("amazon") -> Color(0xFFFF9900)
        d.contains("reddit") -> Color(0xFFFF4500)
        d.contains("twitter") || d == "x.com" -> Color(0xFF1DA1F2)
        d.contains("github") -> Color(0xFF24292E)
        d.contains("yahoo") -> Color(0xFF6001D2)
        d.contains("bbc") -> Color(0xFFBB1919)
        d.contains("facebook") -> Color(0xFF1877F2)
        d.contains("instagram") -> Color(0xFFE1306C)
        d.contains("linkedin") -> Color(0xFF0A66C2)
        d.contains("weather") -> Color(0xFF0288D1)
        d.contains("netflix") -> Color(0xFFE50914)
        d.contains("chatgpt") || d.contains("openai") -> Color(0xFF10A37F)
        d.contains("gemini") -> Color(0xFF1A73E8)
        d.contains("qwen") -> Color(0xFF615CED)
        d.contains("librechat") -> Color(0xFF2C8FFF)
        d.contains("quora") -> Color(0xFFB92B27)
        d.contains("pinterest") -> Color(0xFFBD081C)
        d.contains("spotify") -> Color(0xFF1DB954)
        d.contains("cnn") -> Color(0xFFCC0000)
        d.contains("bing") -> Color(0xFF008373)
        d.contains("duckduckgo") -> Color(0xFFDE5833)
        d.contains("microsoft") -> Color(0xFF00A4EF)
        d.contains("apple") -> Color(0xFF555555)
        d.contains("telegram") -> Color(0xFF26A5E4)
        d.contains("whatsapp") -> Color(0xFF25D366)
        d.contains("tiktok") -> Color(0xFFFE2C55)
        d.contains("ebay") -> Color(0xFFE53238)
        d.contains("imdb") -> Color(0xFFF5C518)
        d.contains("medium") -> Color(0xFF00AB6C)
        d.contains("stackoverflow") -> Color(0xFFF48024)
        else -> {
          val fallbackPalette = listOf(
            Color(0xFF1E88E5), // Blue
            Color(0xFFE53935), // Red
            Color(0xFF43A047), // Green
            Color(0xFFFB8C00), // Orange
            Color(0xFF8E24AA), // Purple
            Color(0xFF00ACC1), // Cyan
            Color(0xFF3949AB), // Indigo
            Color(0xFFD81B60), // Pink
            Color(0xFF00897B), // Teal
            Color(0xFF6D4C41), // Brown
            Color(0xFF546E7A)  // Blue-grey
          )
          val hash = kotlin.math.abs(d.hashCode())
          fallbackPalette[hash % fallbackPalette.size]
        }
      }
    }
}
