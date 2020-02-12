package com.bai.gifmaking

import android.app.Application

/**
 * FileName: BaiApplication
 * Author: bai
 * Date: 2019/6/12 16:10
 */
class BaiApplication : Application(){

    lateinit var mApplication:BaiApplication

    fun getApplication(): BaiApplication {
        return mApplication
    }

    override fun onCreate() {
        super.onCreate()

        mApplication = this

    }
}