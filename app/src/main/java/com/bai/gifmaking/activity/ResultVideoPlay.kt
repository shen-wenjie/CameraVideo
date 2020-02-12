package com.bai.gifmaking.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.bai.gifmaking.R
import kotlinx.android.synthetic.main.activity_result_video.*
import android.widget.Toast
import android.media.MediaPlayer
import android.view.View
import com.bai.gifmaking.base.BaseCompatActivity

/**
 * FileName: ResultVideoPlay
 * Author: bai
 * Date: 2019/6/11 15:55
 */
class ResultVideoPlay: BaseCompatActivity() {

    private var mContext: Context?= null
    private var videoUrl1:String ?=null

    override fun initContentView(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_result_video)

        mContext = this

        if (intent.hasExtra("videoPath")){
            videoUrl1 = intent.getStringExtra("videoPath")
        }

        val uri = Uri.parse(videoUrl1)

        //设置视频控制器
        //videoView.setMediaController(MediaController(this))

        //播放完成回调
        videoView.setOnCompletionListener(MyPlayerOnCompletionListener())

        //设置视频路径
        videoView.setVideoURI(uri)

        //开始播放视频
        videoView.start()

        ensureButton.setOnClickListener {
            val content:String = contentText.text.toString()
            startActivity(Intent(this, ResultGif::class.java).putExtra("videoPath",videoUrl1).putExtra("gifContent",content))
            finish()
        }
    }


    internal inner class MyPlayerOnCompletionListener : MediaPlayer.OnCompletionListener {

        override fun onCompletion(mp: MediaPlayer) {
            Toast.makeText(mContext, "播放完成了", Toast.LENGTH_SHORT).show()
        }
    }
}