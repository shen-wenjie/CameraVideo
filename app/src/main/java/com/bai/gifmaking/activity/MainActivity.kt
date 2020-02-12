package com.bai.gifmaking.activity
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.PixelFormat
import android.hardware.Camera
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import android.view.Window
import android.widget.Toast
import com.bai.gifmaking.R
import com.bai.gifmaking.base.BaseCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*

/**
 * FileName: ResultVideoPlay
 * Author: bai
 * Date: 2019/6/11 15:55
 * https://blog.csdn.net/u011133887/article/details/83654724
 */
class MainActivity : BaseCompatActivity(),SurfaceHolder.Callback,View.OnTouchListener, View.OnClickListener {

    private val videoPathTmp:String? = Environment.getExternalStorageDirectory().path+"/bai.3gp"

    private var mediaRecorder: MediaRecorder?= null //录制视频的类

    private var surfaceHolder: SurfaceHolder?= null //SurfaceHolder

    private var mCameraId:Int=Camera.CameraInfo.CAMERA_FACING_BACK//摄像头Id

    private var mCamera:Camera?=null//摄像头

    private var mTotalProgress: Int = 0//总进度条长度

    private var mCurrentProgress: Int = 0 //当前进度

    private val threadProgress=  Thread(ProgressRunnable()) //进度条走动线程

    //Timer
    private var mTimer: Timer? = null

    // タイマー用の時間のための変数
    private var mTimerSec = 0.0

    private var mHandler = Handler()

    var arr = arrayListOf<Long>()

    var count = 0

    /**
     * 初始化View的方法
     */
    override fun initContentView(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        // 设置横屏显示
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_main)

        init()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        surfaceHolder = holder!!
        mCamera!!.startPreview()
        mCamera!!.cancelAutoFocus()
        // 关键代码 该操作必须在开启预览之后进行（最后调用），
        // 否则会黑屏，并提示该操作的下一步出错
        // 只有执行该步骤后才可以使用MediaRecorder进行录制
        // 否则会报 MediaRecorder(13280): start failed: -19
        mCamera!!.unlock()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        surfaceHolder =null
        mediaRecorder = null
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        surfaceHolder = holder!!


        try {
            //使用后置摄像头
            //var camerId: String = manager.getCameraIdList()[0]
            mCamera = Camera.open(mCameraId)
            //旋转摄像头90度
            mCamera!!.setDisplayOrientation(90)
            mCamera!!.setPreviewDisplay(surfaceHolder)//将摄像头预览画面填充到SurfaceView
            val parameters = mCamera!!.parameters
            parameters!!.pictureFormat = PixelFormat.JPEG
            parameters.setPreviewSize(640,480)
            parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE//1连续对焦
            mCamera!!.parameters = parameters
        } catch (e: RuntimeException) {
            //Camera.open() 在摄像头服务无法连接时可能会抛出 RuntimeException
            Toast.makeText(this,"カメラ起動失敗！",Toast.LENGTH_LONG).show()
            finish()
        }
        initRecorder()
    }

    /**
     * 初始化view
     */
    private fun init(){
        val holder = surfaceview.holder
        holder.addCallback(this)
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        initVariable()
        //roundProLl.setOnTouchListener(this)
        button1.setOnClickListener(this)
    }

    override fun onClick(v: View?) = if(button1.text=="撮影")
    {
        button1.text="停止"
        if (mTimer == null){
            mTimer = Timer()
            mTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    mTimerSec += 0.1
                    mHandler.post {
                        timer.text = String.format("%.1f", mTimerSec)

                    }
                }

            }, 1000, 1000)

        }

        //threadProgress .start()
        startRecordVideo()

    }else {
        if (mTimer != null){
            mTimer!!.cancel()
            mTimer = null
        }
        button1.text="撮影"
        releaseVideoResource()
        //roundProLl.visibility = View.GONE
        startActivity(Intent(this, ResultVideoPlay::class.java).putExtra("videoPath", videoPathTmp))
        finish()
    }


    /**
     * 初始化录制视频的参数值
     */
    private fun initRecorder(){
        mediaRecorder  = MediaRecorder()
        mediaRecorder!!.reset()
        mediaRecorder!!.setCamera(mCamera) //分配摄像头
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
        // 设置录制视频源为Camera(相机)
        mediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA)
        // 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        // 设置录制的视频编码h263 h264 MPEG_4_SP
        mediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        // 编码器 注意，如果使用AMR_NB将会导致IOS无法播放
        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        // 设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错
        mediaRecorder!!.setVideoSize(640, 480)
        mediaRecorder!!.setVideoEncodingBitRate(5*1024*1024)

        // 设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错
        mediaRecorder!!.setVideoFrameRate(60)
        mediaRecorder!!.setPreviewDisplay(surfaceHolder!!.surface)
        // 设置视频文件输出的路径
        mediaRecorder!!.setOutputFile(videoPathTmp)
        //改变保存后的视频文件播放时是否横屏(不加这句，视频文件播放的时候角度是反的)
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT){
            //输出视频旋转270度
            mediaRecorder!!.setOrientationHint(270)
        }else if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
            //输出视频旋转90度
            mediaRecorder!!.setOrientationHint(90)
        }

        //去掉滴的一声提示音
        val audioManager:AudioManager =getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true)
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC,true)
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0)
        audioManager.setStreamVolume(AudioManager.STREAM_DTMF, 0, 0)
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0)
        audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0)

        try {
            // 准备录制
            mediaRecorder!!.prepare()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 释放视频资源
     */
    private fun releaseVideoResource(){
        if (mediaRecorder != null) {
            mediaRecorder!!.setOnErrorListener(null)
            mediaRecorder!!.setOnInfoListener(null)
            mediaRecorder!!.setPreviewDisplay(null)
            try {
                // 停止录制
                mediaRecorder!!.stop()
                // 释放资源
                mediaRecorder!!.release()
                mediaRecorder = null
            } catch (e:IllegalStateException) {
                e.printStackTrace()
            } catch (e:RuntimeException) {
                e.printStackTrace()
            } catch (e:Exception) {
                e.printStackTrace()
            }
        }
    }
    /**
     * 开始录制视频
     */
    private fun startRecordVideo(){
        try {
            // 开始录制
            mediaRecorder!!.start()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 按下抬起的事件监听
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if(v== roundProLl){
            if (event!!.action == MotionEvent.ACTION_DOWN){
                threadProgress .start()
                startRecordVideo()
            }else if(event.action == MotionEvent.ACTION_UP){
                releaseVideoResource()
                roundProLl.visibility = View.GONE
                startActivity(Intent(this,ResultVideoPlay::class.java).putExtra("videoPath",videoPathTmp))
                finish()
            }
            return true
        }
        return false
    }

    /**
     * 初始化进度条长度
     */
    private fun initVariable() {
        mTotalProgress = 100
        mCurrentProgress = 0
    }

    /**
     * 线程控制进度条走动
     */
    internal inner class ProgressRunnable : Runnable {
        override fun run() {
            while (mCurrentProgress < mTotalProgress && mediaRecorder!=null) {
                mCurrentProgress += 1
                roundProgressBar!!.progress = mCurrentProgress
                try {
                    Thread.sleep(100)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
