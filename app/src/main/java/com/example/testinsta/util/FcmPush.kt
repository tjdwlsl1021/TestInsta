package com.example.testinsta.util

import com.example.testinsta.navigation.model.PushDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class FcmPush {
    var JSON = MediaType.parse("application/json; charset=utf-8")
    var url = "https://fcm.googleapis.com/fcm/send"

    // Firebase - 프로젝트 - 클라우드 메시징 - 서버키!!
    var serverKey =
        "AAAAXygj5NU:APA91bGQLeDRRxHpLddIs193_q5YL3xzgf7Kw3gwNQwBjneoTa6zxmYSDmpk68UOioZSr0WUm_MsrVsllj7dMtLCtjixzmWWGBhebaRWmQ_oqiZX3biArQZsrk9bCT5Sy6rbIuKQQqDa"
    var gson: Gson? = null
    var okHttpClient: OkHttpClient? = null

    companion object {
        var instance = FcmPush()
    }

    init {
        gson = Gson()
        okHttpClient = OkHttpClient()
    }

    fun sendMessage(destinationUid: String, title: String, message: String) {
        FirebaseFirestore.getInstance().collection("pushtokens").document(destinationUid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var token = task.result?.get("pushtoken").toString() // Token 아니고 token소문자!!

                    var pushDTO = PushDTO()
                    pushDTO.to = token
                    pushDTO.notification.title = title
                    pushDTO.notification.body = message

                    var body = RequestBody.create(JSON, gson?.toJson(pushDTO))
                    val request = Request.Builder()
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "key=$serverKey")
                        .url(url)
                        .post(body)
                        .build()

                    okHttpClient?.newCall(request)?.enqueue(object : Callback {
                        override fun onFailure(call: Call?, e: IOException?) {
                            e?.printStackTrace()
                        }

                        override fun onResponse(call: Call?, response: Response?) {
                            println(response?.body()?.string())
                        }

                    })
                }
            }
    }
}