package com.bai.gifmaking.activity
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.view.View
import com.bai.gifmaking.R
import com.bai.gifmaking.base.BaseCompatActivity
import kotlinx.android.synthetic.main.activity_result_gif.*

/**
 * FileName: ResultGif
 * Author: bai
 * Date: 2019/6/14 17:15
 *
 * 生成Gif的步骤，参考链接：https://blog.csdn.net/qugengting/article/details/79595649
 */
class ResultGif : BaseCompatActivity() {

    private var videoUrl1:String ?=null
    private var gifContent:String ?=null

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun initContentView(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_result_gif)
        if (intent.hasExtra("videoPath")){
            videoUrl1 = intent.getStringExtra("videoPath")
            gifContent = intent.getStringExtra("gifContent")

            resultGif.text = videoUrl1+"\n"+gifContent
        }
    }


}