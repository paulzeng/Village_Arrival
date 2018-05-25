package com.ruanmeng.village_arrival

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AlertDialog
import com.luck.picture.lib.tools.PictureFileUtils
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.*
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.share.Const
import com.ruanmeng.utils.GlideCacheUtil
import com.ruanmeng.utils.OkGoUpdateHttpUtil
import com.ruanmeng.utils.Tools
import com.vector.update_app.UpdateAppBean
import com.vector.update_app.UpdateAppManager
import com.vector.update_app_kotlin.check
import com.vector.update_app_kotlin.updateApp
import kotlinx.android.synthetic.main.activity_setting.*
import org.json.JSONObject

class SettingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        init_title("设置")
    }

    override fun init_title() {
        super.init_title()
        setting_version.setRightString("v" + Tools.getVersion(baseContext))
        setting_cache.setRightString(GlideCacheUtil.getInstance().getCacheSize(this@SettingActivity))

        setting_feedback.setOnClickListener { startActivity<FeedbackActivity>() }
        setting_about.setOnClickListener {
            // startActivity<AboutActivity>()
            val intent = Intent(baseContext, WebActivity::class.java)
            intent.putExtra("title", "关于我们")
            startActivity(intent)
        }
        setting_problem.setOnClickListener { startActivity<ProblemActivity>() }
        setting_online.setOnClickListener {
            val intent = Intent(baseContext, WebActivity::class.java)
            intent.putExtra("title", "在线咨询")
            startActivity(intent)
        }
        setting_version.setOnClickListener { getData() }
        setting_cache.setOnClickListener {
            AlertDialog.Builder(this)
                    .setTitle("清空缓存")
                    .setMessage("确定要清空缓存吗？")
                    .setPositiveButton("清空") { dialog, _ ->
                        dialog.dismiss()

                        GlideCacheUtil.getInstance().clearImageAllCache(baseContext)
                        PictureFileUtils.deleteCacheDirFile(baseContext)
                        setting_cache.setRightString("0B")
                    }
                    .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
        }
        bt_quit.setOnClickListener {
            AlertDialog.Builder(baseContext)
                    .setTitle("退出登录")
                    .setMessage("确定要退出当前账号吗？")
                    .setPositiveButton("退出") { _, _ ->
                        val intent = Intent(baseContext, LoginActivity::class.java)
                        intent.putExtra("offLine", true)
                        startActivity(intent)
                    }
                    .setNegativeButton("取消") { _, _ -> }
                    .create()
                    .show()
        }
    }

    override fun getData() {
        OkGo.post<String>(BaseHttp.get_versioninfo)
                .tag(this@SettingActivity)
                .execute(object : StringDialogCallback(baseContext) {

                    override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                        val obj = JSONObject(response.body())
                        val versionNew = obj.optString("versionNo").replace(".", "").toInt()
                        val versionOld = Tools.getVerCode(baseContext)
                        val url = "http://a.app.qq.com/o/simple.jsp?pkgname=com.ruanmeng.village_arrival"
                        val content = obj.optString("content")
                        val forces = obj.optString("forces") != "1"

                        if (versionNew > versionOld) {
                            dialog("版本更新", "是否升级到v${obj.optString("versionNo")}版本？\n\n$content") {
                                positiveButton("升级") { }
                                cancelable(forces)
                                onKey { _, _ -> return@onKey forces }
                                if (forces) negativeButton("暂不升级") { }
                                show()
                                // 必须要先调show()方法，后面的getButton才有效
                                getPositiveButton().setOnClickListener {
                                    if (forces) dismiss()
                                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                }
                            }
                        } else showToast("当前已是最新版本！")
                    }

                })
    }

    /**
     * 版本更新
     */
    @Suppress("unused")
    private fun checkUpdate() {
        //下载路径
        val path = Environment.getExternalStorageDirectory().absolutePath + Const.SAVE_FILE
        //自定义参数
        val params = HashMap<String, String>()
        params["_api_key"] = "bdcb07efb995304f749c50ade9a8f7ad"
        params["appKey"] = "343e3003e32d6d1531a244bce7b74430"

        updateApp(BaseHttp.version, OkGoUpdateHttpUtil()) {
            isPost = true     //设置请求方式，默认get
            setParams(params) //添加自定义参数
            targetPath = path //设置apk下载路径
        }.check {
            onBefore { showLoadingDialog() }
            parseJson {
                val obj = JSONObject(it).optJSONObject("data")
                val versionNew = obj.optString("buildVersionNo").toInt()
                val versionOld = Tools.getVerCode(baseContext)

                UpdateAppBean()
                        //（必须）是否更新Yes,No
                        .setUpdate(if (versionNew > versionOld) "Yes" else "No")
                        //（必须）新版本号，
                        .setNewVersion(obj.optString("buildVersionNo"))
                        //（必须）下载地址
                        // .setApkFileUrl(obj.optString("url"))
                        .setApkFileUrl(Const.URL_DOWNLOAD)
                        //（必须）更新内容
                        .setUpdateLog(obj.optString("buildUpdateDescription"))
                        //是否强制更新，可以不设置
                        .setConstraint(false)
            }
            hasNewApp { updateApp, updateAppManager -> showDownloadDialog(updateApp, updateAppManager) }
            noNewApp { showToast("当前已是最新版本！") }
            onAfter { cancelLoadingDialog() }
        }
    }

    /**
     * 自定义对话框
     */
    private fun showDownloadDialog(updateApp: UpdateAppBean, updateAppManager: UpdateAppManager) {
        dialog("版本更新", "是否升级到${updateApp.newVersion}版本？\n\n${updateApp.updateLog}") {
            positiveButton("升级") { updateAppManager.download() }
            negativeButton("暂不升级") { }
            show()
        }
    }
}
