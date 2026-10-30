package com.example.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class SpeedDialRepository(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("lite_browser_prefs", Context.MODE_PRIVATE)

  companion object {
    const val TOTAL_ROWS = 20
    const val TOTAL_COLUMNS = 5
    const val TOTAL_SLOTS = TOTAL_ROWS * TOTAL_COLUMNS // 100 slots

    private const val KEY_SPEED_DIALS = "speed_dials_json"
    private const val KEY_INITIALIZED = "speed_dials_initialized"
  }

  fun loadSpeedDials(): List<SpeedDial> {
    if (!prefs.getBoolean(KEY_INITIALIZED, false)) {
      val initial = getDefaultSpeedDials()
      saveSpeedDials(initial)
      prefs.edit().putBoolean(KEY_INITIALIZED, true).apply()
      return initial
    }

    val jsonString = prefs.getString(KEY_SPEED_DIALS, null) ?: return getDefaultSpeedDials()
    return try {
      val jsonArray = JSONArray(jsonString)
      val list = mutableListOf<SpeedDial>()
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        list.add(
          SpeedDial(
            id = obj.getString("id"),
            title = obj.getString("title"),
            url = obj.getString("url"),
            position = i // Auto-align sequentially
          )
        )
      }
      list.mapIndexed { index, item -> item.copy(position = index) }
    } catch (e: Exception) {
      getDefaultSpeedDials()
    }
  }

  fun saveSpeedDials(list: List<SpeedDial>): List<SpeedDial> {
    // Auto-align positions sequentially without gaps
    val aligned = list.mapIndexed { index, item -> item.copy(position = index) }
    val jsonArray = JSONArray()
    for (item in aligned) {
      val obj = JSONObject().apply {
        put("id", item.id)
        put("title", item.title)
        put("url", item.url)
        put("position", item.position)
      }
      jsonArray.put(obj)
    }
    prefs.edit().putString(KEY_SPEED_DIALS, jsonArray.toString()).apply()
    return aligned
  }

  fun moveItem(list: List<SpeedDial>, fromIndex: Int, toIndex: Int): List<SpeedDial> {
    if (fromIndex !in list.indices || toIndex !in list.indices || fromIndex == toIndex) return list
    val mutable = list.toMutableList()
    val item = mutable.removeAt(fromIndex)
    mutable.add(toIndex, item)
    return saveSpeedDials(mutable)
  }

  private fun getDefaultSpeedDials(): List<SpeedDial> {
    val defaults = listOf(
      "Google" to "https://www.google.com",
      "YouTube" to "https://www.youtube.com",
      "Wikipedia" to "https://www.wikipedia.org",
      "Amazon" to "https://www.amazon.com",
      "Reddit" to "https://www.reddit.com",

      "X" to "https://x.com",
      "GitHub" to "https://github.com",
      "Yahoo" to "https://www.yahoo.com",
      "BBC News" to "https://www.bbc.com",
      "Facebook" to "https://www.facebook.com",

      "Instagram" to "https://www.instagram.com",
      "LinkedIn" to "https://www.linkedin.com",
      "Weather" to "https://weather.com",
      "Netflix" to "https://www.netflix.com",
      "Twitch" to "https://www.twitch.tv",

      "ChatGPT" to "https://chatgpt.com",
      "Quora" to "https://www.quora.com",
      "Pinterest" to "https://www.pinterest.com",
      "Spotify" to "https://www.spotify.com",
      "CNN" to "https://www.cnn.com"
    )

    return defaults.mapIndexed { index, pair ->
      SpeedDial(
        title = pair.first,
        url = pair.second,
        position = index
      )
    }
  }
}
