package com.example.musicpuzzel

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicpuzzel.extra.MyApplicationClass.Companion.allTracks
import com.example.musicpuzzel.extra.OnHelpDialogInterface
import com.example.musicpuzzel.model.CharWithPosition
import com.example.musicpuzzel.extra.HelpCustomDialog
import com.example.musicpuzzel.extra.OnSuccessDialogInterface
import com.example.musicpuzzel.extra.SuccessDialog
import com.example.musicpuzzel.model.SoundClip
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.letter.view.*
import java.lang.Exception


class MainActivity : AppCompatActivity(), OnHelpDialogInterface,OnSuccessDialogInterface {
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var chars: MutableList<Char>
    private lateinit var nameChars: MutableList<CharWithPosition>
    private val selectedPositionsRandomly = mutableListOf<Int>()

    //--------------------------------
    lateinit var soundDownloadThread: Thread
    lateinit var successDialog: SuccessDialog
    lateinit var successThread: Thread

    //---------------------------------------------
    private var isPlaying = false
    private var audioManager: AudioManager? = null
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
        sharedPreferences = getSharedPreferences("myPref", Context.MODE_PRIVATE)
        funOpenForFirstTimeHandeling()
        tvType.setText(R.string.password)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        gridLayoutChoose = object : GridLayoutManager(this, 9) {
            override fun canScrollHorizontally(): Boolean {
                return false
            }

            override fun canScrollVertically(): Boolean {
                return false
            }
        }


        track = allTracks[0]
        tvDescription.text = track.description
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
        tvType.setOnClickListener {
            val dialog = HelpCustomDialog(this)
            dialog.showDialog()
        }
        btnHelp.setOnClickListener {
            val dialog = HelpCustomDialog(this)
            dialog.showDialog()
        }
        tvHelp.setOnClickListener {
            val dialog = HelpCustomDialog(this)
            dialog.showDialog()
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        mp!!.release()
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
                audioManager!!.playSoundEffect(SoundEffectConstants.CLICK, 1.0f);
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
        fun deactivate(char: Char): Int{
            var x = 0
            for (i in data){
                if (i == char){
                    val holder = rvChoose.findViewHolderForLayoutPosition(x)
                    holder!!.itemView.visibility = View.INVISIBLE
                    holder.itemView.isClickable = false
                    break
                }else {
                    x++
                }
            }
            return x
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
                audioManager!!.playSoundEffect(SoundEffectConstants.CLICK, 1.0f)
                if (holder.tvChar.text!=" "){
                    holder.tvChar.text = " "
                    data[position].char = ' '
                    notifyDataSetChangedOverride()
                    choseAdapter.activate(data[position].choosedPosition)
                }
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
            notifyDataSetChangedOverride()
        }
/*        fun revealLetter(position: Int,char: Char){
            val holder = rvAnswerSquares.findViewHolderForLayoutPosition(position)
            (holder as MViewHolder).tvChar.setText(char.toString())

           data[position].char = char
          //  notifyDataSetChanged()
            holder.itemView.isClickable = false
        }*/
        fun notifyDataSetChangedOverride(){
            notifyDataSetChanged()
            if (selectedPositionsRandomly.size>0){
                for (position in selectedPositionsRandomly){
                    val holder = rvAnswerSquares.findViewHolderForLayoutPosition(position)
                    holder!!.itemView.isClickable=false
                }
            }
        }

    }
//******************************************************************************************

    private fun onRightAnswer() {
        successDialog = SuccessDialog(this)
        successDialog.showDialog()
        tvStage.text = (tvStage.text.toString().toInt() + 1).toString()
        newRound()
    }

    private fun onWrongAnswer() {
        Toast.makeText(this, "wrong answer", Toast.LENGTH_LONG).show()
    }

    private fun downloadSound(url: String) {


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


    }

    private fun runOnPlayButton() {
        if (mp != null) {
            if (mp!!.isPlaying) {
                mp!!.pause()
                isPlaying = false
                btnPlay.setImageResource(R.drawable.btn_play)

            } else {
                playMusic()

            }
        }
    }

    private fun playMusic() {
        progress_horizontal.max = mp!!.duration
        mp!!.start()
        isPlaying = true
        btnPlay.setImageResource(R.drawable.ic_btn_pause)
        Thread {

            mp!!.setOnCompletionListener {
                btnPlay.setImageResource(R.drawable.btn_play)
                isPlaying=false
            }

            while (isPlaying) {
                runOnUiThread {
                    progress_horizontal.progress = mp!!.currentPosition
                }
                Thread.sleep(30)
            }

        }.start()
    }

    private fun newRound() {
        val maxLength = allTracks.size
        if (indexOfTrack < maxLength) {
            successThread = Thread{
                mp!!.stop()
                track = allTracks[indexOfTrack]
                downloadSound(track.sound!!)
                selectedPositionsRandomly.clear()
                nameChars = track.getNameChars()
                chars = track.reArrange()
                resultAdapter = ResultAdapter(this, nameChars)
                choseAdapter = ChoseAdapter(this, chars)

                runOnUiThread {
                    gridLayoutResult.spanCount = nameChars.size
                    tvType.setText(R.string.password)
                    tvDescription.visibility = View.GONE
                    tvDescription.text = track.description
                    rvAnswerSquares.adapter = resultAdapter
                    rvChoose.adapter = choseAdapter
                    successDialog.setBtnNextRoundVisibilityOn()
                    successDialog.disableProgressBar()
                    indexOfTrack++
                }
            }
            successThread.start()

        } else {
            successDialog.cancelDialog()
            Toast.makeText(this, "ممتاز لقد أتممت جميع المراحل", Toast.LENGTH_SHORT).show()

            mp!!.stop()
            isPlaying=false
        }
    }

    private fun funOpenForFirstTimeHandeling() {
        try {
            val isOpened = sharedPreferences.getBoolean("isOpenedBefore", false)
            if (!!isOpened) {
                sharedPreferences.edit().putBoolean("isOpenedBefore", true).apply()
                sharedPreferences.edit().putInt("credit", 500).apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun revealLetter() {
        val text = track.getNameWithoutSpace()
        if (selectedPositionsRandomly.size < text.length) {


            var x: Int
            do {
                x =  (Math.random()*text.length).toInt()
            } while (selectedPositionsRandomly.contains(x))
            val char = text[x]
            val chosePositionSelected = choseAdapter.deactivate(char)
            //resultAdapter.revealLetter(x,char)
            selectedPositionsRandomly.add(x)


           // val emptyPosition = resultAdapter.getLowestEmptyButton()

            resultAdapter.data[x].char = char
            resultAdapter.notifyChange(x, chosePositionSelected)

            if (resultAdapter.isFull()) {

                if (resultAdapter.checkSolution(track.getNameWithoutSpace())) {
                    onRightAnswer()
                } else {
                    onWrongAnswer()
                }
            }

        }
    }

    override fun showType() {
        tvType.text = track.type
    }

    override fun showDescription() {
        tvDescription.visibility = View.VISIBLE
        //****************

    }

    override fun onSuccessDialog() {
        runOnPlayButton()
    }


}