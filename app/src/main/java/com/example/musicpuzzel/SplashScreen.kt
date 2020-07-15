package com.example.musicpuzzel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.musicpuzzel.extra.MyApplicationClass.Companion.allTracks
import com.example.musicpuzzel.model.SoundClip
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        getAllTracks()
    }


    private fun getAllTracks() {
        var request: Task<QuerySnapshot>?
            val db = Firebase.firestore

            request = db.collection("tracks").orderBy("level").get()
            request. addOnSuccessListener { querySnapshot ->
                Log.e("mmm",querySnapshot.toString())
                for (document in querySnapshot) {
                    allTracks.add(
                        SoundClip(
                            document.getString("name")!!,
                            document.getString("sound"),
                            document.getString("type"),
                            document.getString("description")
                        )
                    )
                }
                if (allTracks.size>0){
                    val intent = Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            }
                .addOnFailureListener {
                        exception ->
                    Log.e("mmm","************************************************* \n"+  exception.message)
                }
        }





}