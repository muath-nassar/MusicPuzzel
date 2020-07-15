package com.example.musicpuzzel.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.FileDescriptor
import java.util.*

class SoundClip(val name: String, val sound: String?, val type: String?, val description: String?){
   public fun reArrange():  MutableList<Char> {
        val randomArr: MutableList<Char> = mutableListOf()
        val arr = name.toCharArray()
        for (char in arr){
            if (char != ' ') randomArr.add(char)
        }
       val generated = generateRandomeChars()
       for (i in generated){
           randomArr.add(i)
       }
        randomArr.shuffle()
       return randomArr
    }
    public fun getNameChars(): MutableList<CharWithPosition>{
        val array: MutableList<CharWithPosition> = mutableListOf()
        val arr = name.toCharArray()
        for (char in arr){
            if (char != ' ') array.add(CharWithPosition(char,-1))
        }
        return array
    }
     fun  getNameWithoutSpace(): String{
         var name = ""
         val arr = getNameChars()
         for (i in arr){
             name+=i.char
         }
         return name
     }
    private fun getSaltNumber(text:String): Int{
        when(text.length){
            in 0..5 -> return 7
            in 6..9 ->return 5
            else -> return 3
        }
    }

    private fun generateRandomeChars(): CharArray{
        val source = "ضصثقفغعهخحجدشسيبلاتنمكطئءؤرلاىةوزظذ"
        val x= Random().ints(getSaltNumber(name).toLong(), 0, source.length).toArray().map (source::get).joinToString("")
        return x.toCharArray()
    }
}