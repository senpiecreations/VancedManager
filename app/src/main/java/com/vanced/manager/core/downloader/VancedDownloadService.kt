package com.vanced.manager.core.downloader

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.vanced.manager.core.installer.RootSplitInstallerService
import com.vanced.manager.core.installer.SplitInstaller
import com.vanced.manager.ui.fragments.HomeFragment
import com.vanced.manager.utils.InternetTools.getFileNameFromUrl
import java.lang.IllegalStateException

class VancedDownloadService: Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            downloadSplits()
        } catch (e: IllegalStateException) {
            Toast.makeText(this, "Unable to download Vanced", Toast.LENGTH_SHORT).show()
        }
        stopSelf()
        return START_STICKY
    }

    private fun downloadSplits(
        type: String = "arch"
    ) {
        val prefs = getSharedPreferences("installPrefs", Context.MODE_PRIVATE)
        prefs?.edit()?.putBoolean("isVancedDownloading", true)?.apply()
        val variant = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("vanced_variant", "nonroot")
        val lang = prefs?.getString("lang", "en")
        val theme = prefs?.getString("theme", "dark")
        val arch =
            when {
                Build.SUPPORTED_ABIS.contains("x86") -> "x86"
                Build.SUPPORTED_ABIS.contains("arm64-v8a") -> "arm64_v8a"
                else -> "armeabi_v7a"
            }
        val url =
            when (type) {
                "arch" -> "$baseUrl/$variant/Config/config.$arch.apk"
                "theme" -> "$baseUrl/$variant/Theme/$theme.apk"
                "lang" -> "$baseUrl/$variant/Language/split_config.$lang.apk"
                "enlang" -> "$baseUrl/$variant/Language/split_config.en.apk"
                else -> throw NotImplementedError("This type of APK is NOT valid. What the hell did you even do?")
            }

        PRDownloader.download(url, cacheDir.path, getFileNameFromUrl(url))
            .build()
            .setOnProgressListener { progress ->
                val intent = Intent(HomeFragment.VANCED_DOWNLOADING)
                val mProgress = progress.currentBytes * 100 / progress.totalBytes
                intent.action = HomeFragment.VANCED_DOWNLOADING
                intent.putExtra("vancedProgress", mProgress.toInt())
                intent.putExtra(
                    "fileName",
                    "Downloading ${getFileNameFromUrl(url)}..."
                )
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    when (type) {
                        "arch" -> downloadSplits("theme")
                        "theme" -> downloadSplits("lang")
                        "lang" -> {
                            if (lang == "en") {
                                prepareInstall(variant!!)
                            } else {
                                downloadSplits("enlang")
                            }
                        }
                        "enlang" -> prepareInstall(variant!!)
                    }
                }

                override fun onError(error: Error) {
                    val intent = Intent(HomeFragment.DOWNLOAD_ERROR)
                    intent.action = HomeFragment.DOWNLOAD_ERROR
                    intent.putExtra("DownloadError", error.toString())
                    LocalBroadcastManager.getInstance(this@VancedDownloadService)
                        .sendBroadcast(intent)
                }
            })
    }

    private fun prepareInstall(variant: String) {
        val intent = Intent(HomeFragment.VANCED_DOWNLOADED)
        intent.action = HomeFragment.VANCED_DOWNLOADED
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        if (variant == "root")
            launchRootInstaller()
        else
            launchInstaller()
    }

    private fun launchInstaller() {
        SplitInstaller.installSplitApk(this)
    }

    private fun launchRootInstaller() {
        startService(Intent(this, RootSplitInstallerService::class.java))
    }
    companion object {
        const val baseUrl = "https://vanced.app/api/v1/apks/v15.05.54"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}