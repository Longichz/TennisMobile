package xyz.genscode.tennis.data

import android.annotation.SuppressLint
import android.content.Context

class Storage(var context : Context) {
    fun getRecord() : String{
        val settings = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return settings.getString("score", "0").toString()
    }

    @SuppressLint("CommitPrefEdits")
    fun setRecord(score : String){
        val settings = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val settingsEditor = settings.edit()
        settingsEditor.putString("score", score)
        settingsEditor.apply()
    }
}