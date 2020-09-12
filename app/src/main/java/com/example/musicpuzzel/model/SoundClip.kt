package com.example.musicpuzzel.model

import android.os.Build
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
        return 18-text.length
      /*  when(text.length){
            in 0..5 -> return 7
            in 6..9 ->return 5
            else -> return 3
        }*/
    }

    private fun generateRandomeChars(): CharArray{
        val source = "ذدجحخهعغفقثصضطكمنتاألبيسشظزوةىرء"
        val charArray: CharArray
        if (false && Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            val x= Random().ints(getSaltNumber(name).toLong(), 0, source.length).toArray().map (source::get).joinToString("")
            charArray = x.toCharArray()
            return charArray
        }else{
            val arrayOfChars = source.toCharArray()
            val mList = mutableListOf<Char>()
            for (char in arrayOfChars){
                mList.add(char)
            }
            charArray = CharArray(getSaltNumber(name))
            for (i in 0 until getSaltNumber(name)){
                charArray[i] = mList.random()
            }
            return charArray
        }


    }
}