package com.bai.gifmaking.base

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.View
import com.bai.gifmaking.activity.PermissionsActivity
import com.bai.gifmaking.utils.PermissionsChecker


/**
 * FileName: BaseCompatActivity
 * Author: bai
 * Date: 2019/6/12 15:42
 */
abstract class BaseCompatActivity : AppCompatActivity(){

    private var permissionListTmp = arrayOf<String>(Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)// 所需的全部权限

    private val ResultCodeRequest = 0 // 请求码

    private var mPermissionsChecker: PermissionsChecker? = null // 权限检测器

    private var screenWidth = 0//屏幕宽

    private var screenHeight = 0//屏幕高


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initContentView(savedInstanceState)
        mPermissionsChecker = PermissionsChecker(this)
        screenWidth = resources.displayMetrics.widthPixels//屏幕宽
        screenHeight = resources.displayMetrics.heightPixels//屏幕高
    }

    /**
     * @Title: initView
     * @Description: (初始化布局文件的控件)
     * @param: savedInstanceState
     * @return: void 返回类型
     */
    protected abstract fun initContentView(savedInstanceState: Bundle?)

    /**
     * 每个页面OnResume检测权限
     */
    override fun onResume() {
        super.onResume()
        // 缺少权限时, 进入权限配置页面
        if (mPermissionsChecker!!.lacksPermissions(*permissionListTmp)) {
            PermissionsActivity.startActivityForResult(this, ResultCodeRequest, *permissionListTmp)
        }
    }

    /**
     * 权限结果处理，如果当前权限全部通过，就关闭权限处理页面
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 拒绝时, 关闭页面, 缺少主要权限, 无法运行
        if (requestCode == ResultCodeRequest && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            finish()
        }
    }

}