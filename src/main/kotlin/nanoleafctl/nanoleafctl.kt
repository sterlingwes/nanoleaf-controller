package nanoleafctl

import java.io.*
import java.net.URL
import java.nio.charset.Charset
import java.net.HttpURLConnection
import java.nio.charset.StandardCharsets

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

@kotlinx.serialization.ImplicitReflectionSerializer
fun main(args: Array<String>) {
  println("args: ${args.size}")
  args.forEach { println(">$it") }
  val controller = NanoleafController(args.get(0), args.get(1))
  println(controller.getPowerStatus())
}

@Serializable
data class Data(val value: Boolean = false)

class NanoleafController(ipAddress: String, apiToken: String) {
  val baseUrl = "http://$ipAddress:16021/api/v1/$apiToken"


  fun getPowerStatus(): Boolean {
    val responseString = URL("$baseUrl/state/on").readText()
    val jsonData = Json.parse(Data.serializer(), responseString)
    return jsonData.value
  }

  fun getToken() {
    post("$baseUrl/new")
  }

  fun turnOn() {
    put("/state", """{"on":{"value": true}}""")
  }

  fun turnOff() {
    put("/state", """{"on":{"value": false}}""")
  }

  fun post(serverURL: String): String {
    return httpMutate("POST", serverURL)
  }

  fun put(serverURL: String, message: String): String {
    return httpMutate("PUT", serverURL, message)
  }

  fun httpMutate(method: String, serverURL: String, message: String = ""): String {
    val url = URL("$baseUrl$serverURL")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = method
    connection.connectTimeout = 300000
    connection.connectTimeout = 300000
    connection.doOutput = true

    val postData: ByteArray = message.toByteArray(StandardCharsets.UTF_8)

    connection.setRequestProperty("charset", "utf-8")
    connection.setRequestProperty("content-length", postData.size.toString())
    connection.setRequestProperty("content-type", "application/json")

    val outputStream: DataOutputStream = DataOutputStream(connection.outputStream)
    outputStream.write(postData)
    outputStream.flush()

    println(connection.responseCode)
    return connection.inputStream.readTextAndClose()
  }
}

fun InputStream.readTextAndClose(charset: Charset = Charsets.UTF_8): String {
    return this.bufferedReader(charset).use { it.readText() }
}
