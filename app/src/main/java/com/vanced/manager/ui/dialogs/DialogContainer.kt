package com.vanced.manager.ui.dialogs

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import androidx.preference.PreferenceManager
import com.vanced.manager.R
import com.vanced.manager.ui.MainActivity
import com.vanced.manager.utils.MiuiHelper

object DialogContainer {

    fun showSecurityDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(context.resources.getString(R.string.welcome))
            .setMessage(context.resources.getString(R.string.security_context))
            .setPositiveButton(context.resources.getString(R.string.close)) { dialog, _ ->
                run {
                    dialog.dismiss()
                    if (MiuiHelper.isMiui()) {
                        showMiuiDialog(context)
                    }
                }
            }
            .create()
            .show()

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putBoolean("firstStart", false).apply()
    }

    private fun showMiuiDialog(context: Context) {
        basicAlertBuilder(
            context.getString(R.string.miui_one_title),
            context.getString(R.string.miui_one),
            context
        )
    }

    fun secondMiuiDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.miui_two_title))
            .setMessage(context.getString(R.string.miui_two))
            .setPositiveButton("Fine") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    //Easter Egg
    fun statementFalse(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Wait what?")
            .setMessage("So this statement is false huh? I'll go with True!")
            .setPositiveButton("wut?") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putBoolean("statement", true).apply()
    }

    private fun basicAlertBuilder(title: String, msg: String, context: Context) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton("close") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    fun installAlertBuilder(msg: String, context: Context) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.error))
            .setMessage(msg)
            .setPositiveButton(context.getString(R.string.close)) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    fun regularPackageInstalled(msg: String, activity: MainActivity) {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.success))
            .setMessage(msg)
            .setPositiveButton(activity.getString(R.string.close)) { _, _ -> activity.restartActivity() }
            .setCancelable(false)
            .create()
            .show()
    }

    fun launchVanced(activity: MainActivity) {
        val intent = Intent()
        intent.component =
            if (PreferenceManager.getDefaultSharedPreferences(activity).getString("vanced_variant", "nonroot") == "root")
                ComponentName("com.google.android.youtube", "com.google.android.youtube.HomeActivity")
            else
                ComponentName("com.vanced.android.youtube", "com.google.android.youtube.HomeActivity")
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.success))
            .setMessage(activity.getString(R.string.vanced_installed))
            .setPositiveButton(activity.getString(R.string.launch)) { _, _ ->
                run {
                    startActivity(activity, intent, null)
                    activity.finish()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                run {
                    dialog.dismiss()
                    activity.restartActivity()
                }
            }
            .setCancelable(false)
            .create()
            .show()
    }

}