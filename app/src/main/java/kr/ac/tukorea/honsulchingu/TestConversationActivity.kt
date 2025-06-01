//package kr.ac.tukorea.honsulchingu
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.media.AudioFormat
//import android.media.AudioRecord
//import android.media.MediaPlayer
//import android.media.MediaRecorder
//import android.os.Bundle
//import android.os.Environment
//import android.util.Log
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import kr.ac.tukorea.honsulchingu.databinding.ActivityTestConversationBinding
//import org.json.JSONObject
//import java.io.BufferedReader
//import java.io.DataOutputStream
//import java.io.File
//import java.io.InputStreamReader
//import java.io.OutputStream
//import java.io.RandomAccessFile
//import java.lang.Math.abs
//import java.net.HttpURLConnection
//import java.net.URL
//import java.time.LocalDateTime
//import java.time.format.DateTimeFormatter
//
//class TestConversationActivity : AppCompatActivity() {
//    @Volatile
//    private var isPressed = false
//    @Volatile
//    private var isPlaying = false
//    @Volatile
//    private var time = ""
//
//    private var fileCount = 0
//    private val sharedPreferences by lazy { getSharedPreferences("AppPreferences", MODE_PRIVATE) }
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//
//        val binding = ActivityTestConversationBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // 채팅
//        binding.sendButton.setOnClickListener {
//            Thread {
//                val url = URL("http://15.152.46.59:8000/conversation_model")
//
//                val connection = url.openConnection() as HttpURLConnection
//
//                connection.requestMethod = "POST"
//                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
//                connection.doOutput = true
//
//
//                val input_user = binding.inputMessage.text.toString()
//
//                val jsonInput = JSONObject()
//
//                runOnUiThread { binding.chatTextView.append("\n나: $input_user") }
//                jsonInput.put("id_user", "alps1248@gmail.com")
//                jsonInput.put("select_user", "민혁")
//                jsonInput.put("start_user", "2025. 05. 01. 18-00-00")
//                jsonInput.put("input_user", input_user)
//
//
//                val outputStream: OutputStream = connection.outputStream
//
//                outputStream.write(jsonInput.toString().toByteArray(Charsets.UTF_8))
//                outputStream.flush()
//                outputStream.close()
//
//
//                val reader = BufferedReader(InputStreamReader(connection.inputStream))
//
//                val responseBuilder = StringBuilder()
//
//                var line: String?
//
//                while (reader.readLine().also { line = it } != null) responseBuilder.append(line)
//
//                reader.close()
//
//
//                val responseJson = JSONObject(responseBuilder.toString())
//
//                val response = responseJson.getString("response")
//
//                runOnUiThread {
//                    binding.chatTextView.append("\nAI: ${response}")
//                    binding.inputMessage.setText("")
//                }
//            }.start()
//        }
//
//        // 녹음
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 0)
//        }
//
//
//        val externalDirs = getExternalFilesDirs(Environment.DIRECTORY_MUSIC)
//        val externalDir = externalDirs.firstOrNull()
//
//        fileCount = sharedPreferences.getInt("fileCount", 0)
//
//        binding.recordButton.setOnClickListener {
//            saveWAV(binding, externalDir!!)
//        }
//
//        // 재생
//        binding.playRecordingButton.setOnClickListener {
//            playWAV(binding, externalDir!!, binding.recordingNumberEditText.text.toString())
//        }
//
//        // 전송
//        binding.sendRecordingButton.setOnClickListener {
//            sendWAV(externalDir!!, binding.recordingNumberEditText.text.toString())
//        }
//    }
//
//    // 전송
//    private fun sendWAV(externalDir: File, inputName: String) {
//        val wavFile = File(externalDir, "${inputName}.wav")
//
//        val boundary = "===" + System.currentTimeMillis() + "==="
//        val lineEnd = "\r\n"
//        val twoHyphens = "--"
//
//        val url = URL("http://:8000/analyzer_model/")
//        val conn = url.openConnection() as HttpURLConnection
//        conn.requestMethod = "POST"
//        conn.doOutput = true
//        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
//
//        val outputStream = DataOutputStream(conn.outputStream)
//
//        outputStream.writeBytes(twoHyphens + boundary + lineEnd)
//        outputStream.writeBytes("Content-Disposition: form-data; name=\"wavFile\"; filename=\"${wavFile.name}\"$lineEnd")
//        outputStream.writeBytes("Content-Type: text/wav$lineEnd")
//        outputStream.writeBytes(lineEnd)
//
//        val wavBytes = wavFile.readBytes()
//        outputStream.write(wavBytes)
//        outputStream.writeBytes(lineEnd)
//
//        outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
//        outputStream.flush()
//        outputStream.close()
//
//        val responseCode = conn.responseCode
//        val response = conn.inputStream.bufferedReader().readText()
//
//        println("응답 코드: $responseCode")
//        println("서버 응답: $response")
//    }
//
//    // 재생
//    private fun playWAV(binding: ActivityTestConversationBinding, externalDir: File, inputName: String) {
//        if (isPressed) {
//            isPressed = false
//            binding.recordButton.text = "녹음 시작"
//        }
//
//
//        val wavFile = File(externalDir, "${inputName}.wav")
//
//        isPlaying = true
//
//        val mediaPlayer = MediaPlayer().apply {
//            setDataSource(wavFile.absolutePath)
//            prepare()
//            start()
//        }
//
//        mediaPlayer.setOnCompletionListener {
//            it.release()
//            isPlaying = false
//            Toast.makeText(this, "${wavFile.name} 재생 완료", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    // 녹음
//    private fun saveWAV(binding: ActivityTestConversationBinding, externalDir: File) {
//        isPressed = !isPressed
//
//        if (isPressed) {
//            if (isPlaying) {
//                runOnUiThread {
//                    Toast.makeText(this, "재생 중...", Toast.LENGTH_SHORT).show()
//                }
//                isPressed = false
//                return
//            }
//            binding.recordButton.text = "녹음 중..."
//
//
//            Thread {
//                val sampleRate = 16000
//                val bufferSize = AudioRecord.getMinBufferSize(
//                    sampleRate,
//                    AudioFormat.CHANNEL_IN_MONO,
//                    AudioFormat.ENCODING_PCM_16BIT
//                )
//
//                val silenceThreshold = 2000L
//                val byteRate = 16 * sampleRate / 8
//
//
//                while (isPressed) {
//                    var soundDetected = false
//
//                    val detectRecorder = AudioRecord(
//                        MediaRecorder.AudioSource.VOICE_RECOGNITION,
//                        sampleRate,
//                        AudioFormat.CHANNEL_IN_MONO,
//                        AudioFormat.ENCODING_PCM_16BIT,
//                        bufferSize
//                    )
//                    val detectData = ByteArray(bufferSize)
//
//                    detectRecorder.startRecording()
//
//                    while (isPressed && !soundDetected) {
//                        val read = detectRecorder.read(detectData, 0, bufferSize)
//                        var sum = 0.0
//                        for (i in 0 until read step 2) {
//                            val low = detectData[i].toInt() and 0xff
//                            val high = detectData[i + 1].toInt()
//                            val sample = (high shl 8) or low
//                            sum += abs(sample.toDouble())
//                        }
//                        val amplitude = sum / (read / 2)
//                        if (amplitude > 100) {
//                            soundDetected = true
//                        }
//                        Log.d("녹음", "${amplitude}")
//                    }
//
//                    detectRecorder.stop()
//                    detectRecorder.release()
//
//
//                    time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy. MM. dd. HH-mm-ss"))
//
//                    val wavFile = File(externalDir, "${time}.wav")
//                    val raf = RandomAccessFile(wavFile, "rw")
//
//                    val totalDataLen = 0
//
//                    raf.writeBytes("RIFF")
//                    raf.writeInt(Integer.reverseBytes(totalDataLen + 36))
//                    raf.writeBytes("WAVE")
//                    raf.writeBytes("fmt ")
//                    raf.writeInt(Integer.reverseBytes(16))
//                    raf.writeShort(java.lang.Short.reverseBytes(1.toShort()).toInt())
//                    raf.writeShort(java.lang.Short.reverseBytes(1.toShort()).toInt())
//                    raf.writeInt(Integer.reverseBytes(sampleRate))
//                    raf.writeInt(Integer.reverseBytes(byteRate))
//                    raf.writeShort(java.lang.Short.reverseBytes(2.toShort()).toInt())
//                    raf.writeShort(java.lang.Short.reverseBytes(16.toShort()).toInt())
//                    raf.writeBytes("data")
//                    raf.writeInt(0)
//
//                    val audioRecord = AudioRecord(
//                        MediaRecorder.AudioSource.VOICE_RECOGNITION,
//                        sampleRate,
//                        AudioFormat.CHANNEL_IN_MONO,
//                        AudioFormat.ENCODING_PCM_16BIT,
//                        bufferSize
//                    )
//
//
//                    val data = ByteArray(bufferSize)
//                    var totalDataWritten = 0
//                    var lastAudioTime = System.currentTimeMillis()
//
//                    audioRecord.startRecording()
//
//                    while (isPressed) {
//                        val read = audioRecord.read(data, 0, bufferSize)
//                        var sum = 0.0
//                        for (i in 0 until read step 2) {
//                            val low = data[i].toInt() and 0xff
//                            val high = data[i + 1].toInt()
//                            val sample = (high shl 8) or low
//                            sum += abs(sample.toDouble())
//                        }
//                        val amplitude = sum / (read / 2)
//                        Log.d("녹음", "                    ${amplitude}")
//
//                        if (amplitude > 100) {
//                            raf.write(data, 0, read)
//                            totalDataWritten += read
//                            lastAudioTime = System.currentTimeMillis()
//                        }
//
//                        if (System.currentTimeMillis() - lastAudioTime > silenceThreshold) break
//                    }
//
//                    audioRecord.stop()
//                    audioRecord.release()
//
//                    raf.seek(4)
//                    raf.writeInt(Integer.reverseBytes(36 + totalDataWritten))
//                    raf.seek(40)
//                    raf.writeInt(Integer.reverseBytes(totalDataWritten))
//                    raf.close()
//
//
//                    runOnUiThread {
//                        Toast.makeText(this, "${wavFile.name} 녹음 완료", Toast.LENGTH_SHORT).show()
//                    }
//                    fileCount++
//                    sharedPreferences.edit().putInt("fileCount", fileCount).apply()
//                }
//
//
//                runOnUiThread {
//                    binding.recordButton.text = "녹음 시작"
//                }
//            }.start()
//        }
//    }
//}
