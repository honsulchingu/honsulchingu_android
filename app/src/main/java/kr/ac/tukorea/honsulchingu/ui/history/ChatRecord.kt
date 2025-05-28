package kr.ac.tukorea.honsulchingu.ui.history

import org.json.JSONArray
import org.json.JSONObject

data class ChatRecord(
    val name: String,
    val last_chat: String,
    val last_time: Long,
    val start_time: String,
    val tag: List<String>,
    val image: Int
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("name", name)
        json.put("last_chat", last_chat)
        json.put("last_time", last_time)
        json.put("start_time", start_time)
        json.put("tag", JSONArray(tag))
        json.put("image", image)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): ChatRecord {
            val tagList = mutableListOf<String>()
            val tagArray = json.getJSONArray("tag")
            for (i in 0 until tagArray.length()) tagList.add(tagArray.getString(i))
            return ChatRecord(
                json.getString("name"),
                json.getString("last_chat"),
                json.getLong("last_time"),
                json.getString("start_time"),
                tagList,
                json.getInt("image")
            )
        }
    }
}
