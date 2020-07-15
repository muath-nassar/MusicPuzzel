package com.example.musicpuzzel

import android.app.ProgressDialog
import android.content.Context
import android.media.MediaPlayer
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicpuzzel.extra.MyApplicationClass.Companion.allTracks
import com.example.musicpuzzel.model.CharWithPosition
import com.example.musicpuzzel.model.SoundClip
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.letter.view.*
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    private lateinit var chars: MutableList<Char>
    private lateinit var nameChars: MutableList<CharWithPosition>

    //--------------------------------
    lateinit var soundDownloadThread: Thread
    private lateinit var alertDialog: ProgressDialog

    //---------------------------------------------
    private var isPlaying = false
    private var mp: MediaPlayer? = null
    private lateinit var gridLayoutChoose: GridLayoutManager
    private lateinit var gridLayoutResult: GridLayoutManager
    private lateinit var choseAdapter: ChoseAdapter
    private lateinit var resultAdapter: ResultAdapter
    lateinit var track: SoundClip
    private var indexOfTrack = 1


    //---------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        gridLayoutChoose = object : GridLayoutManager(this, 9) {
            override fun canScrollHorizontally(): Boolean {
                return false
            }

            override fun canScrollVertically(): Boolean {
                return false
            }
        }


        track = allTracks[0]
        chars = track.reArrange()
        choseAdapter = ChoseAdapter(this, chars)
        rvChoose.layoutManager = gridLayoutChoose
        rvChoose.adapter = choseAdapter
        nameChars = track.getNameChars()
        resultAdapter = ResultAdapter(this, nameChars)

        gridLayoutResult = object : GridLayoutManager(this, nameChars.size) {
            override fun canScrollHorizontally(): Boolean {
                return false
            }

            override fun canScrollVertically(): Boolean {
                return false
            }
        }
        gridLayoutResult.reverseLayout = true

        rvAnswerSquares.layoutManager = gridLayoutResult
        rvAnswerSquares.adapter = resultAdapter
//*********************


    }


    override fun onResume() {
        super.onResume()
        downloadSound(track.sound!!)
        btnPlay.setOnClickListener {
            runOnPlayButton()
        }


    }

//**********************************************************************************************

    //adapter for choosing
    inner class ChoseAdapter(var context: Context, var data: MutableList<Char>) :
        RecyclerView.Adapter<ChoseAdapter.MyViewHolder>() {
        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvChar = itemView.tvChar
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView = LayoutInflater.from(context).inflate(R.layout.letter, parent, false)
            return MyViewHolder(itemView)
        }

        override fun getItemCount(): Int {
            return this.data.size
        }


        override fun onBindViewHolder(holder: ChoseAdapter.MyViewHolder, position: Int) {
            val letter = data[position].toString()
            holder.tvChar.text = letter
            holder.itemView.setOnClickListener {
                holder.itemView.visibility = View.INVISIBLE
                holder.itemView.isClickable = false
                val emptyPosition = resultAdapter.getLowestEmptyButton()

                resultAdapter.data[emptyPosition].char = data[position]
                resultAdapter.notifyChange(emptyPosition, position)

                if (resultAdapter.isFull()) {

                    if (resultAdapter.checkSolution(track.getNameWithoutSpace())) {
                        onRightAnswer()
                    } else {
                        onWrongAnswer()
                    }
                }
            }
        }

        fun activate(id: Int) {
            Log.e("mmm", "position in activate function body =$id")
            val holder = rvChoose.findViewHolderForLayoutPosition(id)
            holder!!.itemView.isClickable = true
            holder.itemView.visibility = View.VISIBLE
        }
    }

    //**********************************************************************************************
    //adapter for selected letters
    inner class ResultAdapter(var context: Context, var data: MutableList<CharWithPosition>) :
        RecyclerView.Adapter<ResultAdapter.MViewHolder>() {
        init {
            for (i in 0 until data.size) {
                data[i].char = ' '
            }
        }

        inner class MViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            //val tvNumber = itemView.tvNumber  ****** for demonestration
            val tvChar = itemView.tvChar
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MViewHolder {
            val itemView =
                LayoutInflater.from(context).inflate(R.layout.letter_empty, parent, false)
            return MViewHolder(itemView)
        }

        override fun getItemCount(): Int {
            return this.data.size
        }


        override fun onBindViewHolder(holder: MViewHolder, position: Int) {
            holder.tvChar.text = data[position].char.toString()
            holder.itemView.setBackgroundResource(R.drawable.result_button_shap)
            holder.itemView.setOnClickListener {

                holder.tvChar.text = " "
                data[position].char = ' '
                notifyDataSetChanged()

                choseAdapter.activate(data[position].choosedPosition)

            }
        }

        fun getLowestEmptyButton(): Int {
            var x = 0
            for (i in data) {
                if (i.char == ' ') {
                    return x
                } else {
                    x++
                }
            }
            return 0
        }

        fun isFull(): Boolean {
            for (i in data) {
                if (i.char == ' ') return false
            }
            return true

        }

        fun checkSolution(solution: String): Boolean {
            var textToCheck = ""
            for (i in data) {
                textToCheck += i.char
            }
            Log.e("mmm", "text to check = | ${textToCheck} | and solution is | $solution |")
            return solution == textToCheck
        }

        fun notifyChange(itemPosition: Int, setPosition: Int) {
            data[itemPosition].choosedPosition = setPosition
            notifyDataSetChanged()
        }
    }
//******************************************************************************************


    private fun onRightAnswer() {
        Toast.makeText(this, "correct answer", Toast.LENGTH_LONG).show()
        newRound()
    }

    private fun onWrongAnswer() {
        Toast.makeText(this, "wrong answer", Toast.LENGTH_LONG).show()
    }

    private fun downloadSound(url: String) {
        alertDialog = ProgressDialog(this)
        alertDialog.setMessage("انتظر من فضلك! يجري تنزيل الصوت")
        alertDialog.show()

        if (mp == null) {
            mp = MediaPlayer()
        } else {
            mp!!.stop()
            mp!!.release()
            mp = MediaPlayer()
        }
        soundDownloadThread = Thread {
            mp!!.setDataSource(url)
            mp!!.prepare()
        }
        soundDownloadThread.start()
        soundDownloadThread.join()
        alertDialog.dismiss()


    }

    private fun runOnPlayButton() {
        if (mp != null) {
            if (mp!!.isPlaying) {
                mp!!.pause()
                isPlaying = false
                btnPlay.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)

            } else {
                playMusic()

            }
        }
    }

    private fun playMusic() {
        progress_horizontal.max = mp!!.duration
        mp!!.start()
        isPlaying = true
        btnPlay.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24)
        Thread {
            mp!!.setOnCompletionListener {
                btnPlay.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
            }

            while (isPlaying) {
                runOnUiThread {
                    progress_horizontal.progress = mp!!.currentPosition
                }
                Thread.sleep(30)
            }

        }.start()
    }


private fun newRound(){
    val maxLength = allTracks.size
    if (indexOfTrack<maxLength){
        mp!!.stop()
        track = allTracks[indexOfTrack]
        nameChars = track.getNameChars()
        chars = track.reArrange()
        resultAdapter = ResultAdapter(this,nameChars)
        choseAdapter = ChoseAdapter(this,chars)
        rvAnswerSquares.adapter = resultAdapter
        rvChoose.adapter = choseAdapter
        downloadSound(track.sound!!)
        runOnPlayButton()
        indexOfTrack++
    }else{
        Toast.makeText(this,"ممتاز لقد أتممت جميع المراحل",Toast.LENGTH_SHORT).show()
    }

}
}