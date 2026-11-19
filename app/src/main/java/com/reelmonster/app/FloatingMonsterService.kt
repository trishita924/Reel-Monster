package com.reelmonster.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import kotlin.math.abs

/**
 * Draws the monster in a system overlay window. Drag to move, tap to count a reel.
 * It only draws the monster. It does not read the screen or any other app.
 */
class FloatingMonsterService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var repo: ReelRepository
    private val handler = Handler(Looper.getMainLooper())

    private var root: LinearLayout? = null
    private var bubble: TextView? = null
    private var monster: MonsterView? = null
    private var params: WindowManager.LayoutParams? = null

    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        refresh(showBubble = false)
    }
    private val hideBubble = Runnable { bubble?.visibility = View.GONE }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        repo = ReelRepository(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            repo.overlayEnabled = false
            stopSelf()
            return START_NOT_STICKY
        }
        startAsForeground()
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (root == null) {
            createOverlay()
            repo.register(prefListener)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        repo.unregister(prefListener)
        handler.removeCallbacksAndMessages(null)
        root?.let {
            try {
                windowManager.removeView(it)
            } catch (e: IllegalArgumentException) {
            }
        }
        root = null
        super.onDestroy()
    }

    private fun startAsForeground() {
        val channelId = "reel_monster_overlay"
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(channelId, getString(R.string.notif_channel), NotificationManager.IMPORTANCE_LOW)
        )
        val openIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopIntent = PendingIntent.getService(
            this, 1, Intent(this, FloatingMonsterService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notif_title))
            .setContentText(getString(R.string.notif_text))
            .setContentIntent(openIntent)
            .addAction(0, getString(R.string.notif_hide), stopIntent)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIF_ID, notification)
        }
    }

    private fun createOverlay() {
        val d = resources.displayMetrics.density

        val bubbleView = TextView(this).apply {
            setBackgroundResource(R.drawable.bg_bubble)
            setTextColor(Color.parseColor("#3B2A5E"))
            textSize = 12f
            gravity = Gravity.CENTER
            maxWidth = (190 * d).toInt()
            setPadding((12 * d).toInt(), (8 * d).toInt(), (12 * d).toInt(), (8 * d).toInt())
            visibility = View.GONE
        }
        val monsterView = MonsterView(this)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            addView(
                bubbleView,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
            addView(monsterView, LinearLayout.LayoutParams((72 * d).toInt(), (72 * d).toInt()))
        }

        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (16 * d).toInt()
            y = (220 * d).toInt()
        }

        monsterView.setOnTouchListener(dragListener())
        root = container
        bubble = bubbleView
        monster = monsterView
        params = lp
        windowManager.addView(container, lp)
        refresh(showBubble = false)
    }

    private fun dragListener(): View.OnTouchListener {
        val slop = ViewConfiguration.get(this).scaledTouchSlop
        var startX = 0
        var startY = 0
        var downX = 0f
        var downY = 0f
        var moved = false
        return View.OnTouchListener { _, e ->
            val lp = params ?: return@OnTouchListener false
            val r = root ?: return@OnTouchListener false
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = lp.x; startY = lp.y
                    downX = e.rawX; downY = e.rawY
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = e.rawX - downX
                    val dy = e.rawY - downY
                    if (abs(dx) > slop || abs(dy) > slop) moved = true
                    if (moved) {
                        lp.x = (startX + dx).toInt().coerceAtLeast(0)
                        lp.y = (startY + dy).toInt().coerceAtLeast(0)
                        windowManager.updateViewLayout(r, lp)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) onTapped()
                    true
                }
                else -> false
            }
        }
    }

    private fun onTapped() {
        repo.increment()
        monster?.pop()
        refresh(showBubble = true)
    }

    private fun refresh(showBubble: Boolean) {
        val m = monster ?: return
        val b = bubble ?: return
        val d = resources.displayMetrics.density
        val count = repo.getCount()
        val stage = MonsterStage.stageOf(count)
        m.setStage(stage)

        val size = (72 * d * MonsterStage.scale(stage)).toInt()
        val lp = m.layoutParams
        if (lp.width != size) {
            lp.width = size
            lp.height = size
            m.layoutParams = lp
        }
        if (showBubble) {
            b.text = MonsterStage.message(count)
            b.visibility = View.VISIBLE
            handler.removeCallbacks(hideBubble)
            handler.postDelayed(hideBubble, 3500)
        }
    }

    companion object {
        private const val NOTIF_ID = 1001
        const val ACTION_STOP = "com.reelmonster.app.ACTION_STOP"
    }
}
