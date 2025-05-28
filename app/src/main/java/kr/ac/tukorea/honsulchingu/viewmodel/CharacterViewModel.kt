package kr.ac.tukorea.honsulchingu.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kr.ac.tukorea.honsulchingu.ui.character.ChatCharacter
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class CharacterViewModel : ViewModel() {

    private val _filteredCharacters = MutableLiveData<List<ChatCharacter>>()
    val filteredCharacters: LiveData<List<ChatCharacter>> get() = _filteredCharacters

    private val allCharacters = mutableListOf<ChatCharacter>()

    var greet_live = MutableLiveData<String>()
    var chatcount_live = MutableLiveData<Int>()
    var favoritecount_live = MutableLiveData<Int>()

    // 특정 타입의 캐릭터 업데이트
    fun updateCharacters(type: String) {
        _filteredCharacters.value = allCharacters.filter { it.type == type }
    }

    // 특정 앤드포인트의 URL 업데이트
    fun updateURL(endPoint: String): URL {
        val IPv4 = "13.208.186.203"
        return URL("http://$IPv4:8000$endPoint")
    }

    // 캐릭터 리스트 DB 로딩 후 업데이트
    fun loadCharacters(context: Context, onLoaded: () -> Unit) {
        Thread {
            val url = updateURL("/load_character")

            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                doOutput = true
            }


            val jsonInput = JSONObject()

            connection.outputStream.use { it.write(jsonInput.toString().toByteArray(Charsets.UTF_8)) }


            val responseString = connection.inputStream.bufferedReader().use { it.readText() }

            val responseJsonObject = JSONObject(responseString)

            val responseJsonArray = responseJsonObject.getJSONArray("character")

            val loadedCharacters = MutableList(responseJsonArray.length()) { i ->
                val item = responseJsonArray.getJSONObject(i)
                val name = item.getString("name")
                val greet = item.getString("greet")
                val tag = item.getString("tag").split(',').map { it.trim() }
                val description = item.getString("description")
                val image = context.resources.getIdentifier(item.getString("image"), "drawable", context.packageName)
                ChatCharacter(i, name.substringBefore('_'), name.substringAfter('_'), greet, tag, description, image)
            }


            allCharacters.clear()
            allCharacters.addAll(loadedCharacters)
            _filteredCharacters.postValue(allCharacters)
            Handler(Looper.getMainLooper()).post { onLoaded() }
        }.start()
    }
}
