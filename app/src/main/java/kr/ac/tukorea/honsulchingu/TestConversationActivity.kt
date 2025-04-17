package kr.ac.tukorea.honsulchingu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.ac.tukorea.honsulchingu.databinding.ActivityTestConversationBinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class TestConversationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val binding = ActivityTestConversationBinding.inflate(layoutInflater)

        setContentView(binding.root)


        binding.sendButton.setOnClickListener {
            Thread {
                try {
                    val url = URL("http://13.56.140.108:8000/conversation_model") // ← 여기에 EC2 퍼블릭 IP 넣어줘!

                    val connection = url.openConnection() as HttpURLConnection

                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    connection.doOutput = true


                    val input_user = binding.inputMessage.text.toString()

                    val jsonInput = JSONObject()

                    runOnUiThread {
                        binding.chatTextView.append("\n나: $input_user")
                    }
                    jsonInput.put("input_user", input_user)


                    val outputStream: OutputStream = connection.outputStream

                    outputStream.write(jsonInput.toString().toByteArray(Charsets.UTF_8))
                    outputStream.flush()
                    outputStream.close()


                    val responseBuilder = StringBuilder()

                    val reader = BufferedReader(InputStreamReader(connection.inputStream))

                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        responseBuilder.append(line)
                    }
                    reader.close()


                    val responseJson = JSONObject(responseBuilder.toString())

                    val response = responseJson.getString("response")

                    runOnUiThread {
                        binding.chatTextView.append("\nAI: ${response}")
                        binding.inputMessage.setText("") // 입력창 비우기
                    }
                }
                catch (e: Exception) {
                    runOnUiThread {
                        binding.chatTextView.append("\n⚠️ 오류 발생: ${e.message}")
                    }
                    e.printStackTrace()
                }
            }.start()
        }
    }
}
