package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.model.PomodoroPhase

class PomodoroNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        // 一時停止促進リマインダーは作業開始済み・停止済みならサービスを起こさない
        // （停止後に残ったアラームでサービスが復活して待機通知を出すのを防ぐ）
        if (action == PomodoroService.ACTION_PAUSE_REMINDER) {
            val state = PomodoroService.timerState.value
            if (!state.isPaused || state.isRunning) return
            if (state.phase == PomodoroPhase.COMPLETED) return
        }
        if (action == PomodoroService.ACTION_MUTE_PAUSE_REMINDER) {
            if (!PomodoroService.timerState.value.isPaused) return
        }
        val serviceIntent = Intent(context, PomodoroService::class.java).apply {
            this.action = action
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (_: Exception) {
            // バックグラウンドからのフォアグラウンド起動が許可されない場合でも、
            // 計測中でサービスが動いていれば通常の起動で届けられる
            try {
                context.startService(serviceIntent)
            } catch (_: Exception) {}
        }
    }
}
