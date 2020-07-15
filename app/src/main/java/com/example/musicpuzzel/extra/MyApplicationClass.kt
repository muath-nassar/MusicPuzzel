package com.example.musicpuzzel.extra

import android.app.Application
import android.util.Log
import com.example.musicpuzzel.model.SoundClip
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class MyApplicationClass : Application() {


    companion object {

       val allTracks = mutableListOf<SoundClip>()
        private var aSinglton: MyApplicationClass? = null
        fun getInstance(): MyApplicationClass {
            if (aSinglton == null) aSinglton = MyApplicationClass()
            return aSinglton!!
        }

    }

    override fun onCreate() {
        super.onCreate()



    }

}