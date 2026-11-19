package com.reelmonster.app

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var repo: ReelRepository
    private lateinit var monster: MonsterView
    private lateinit var tvCount: TextView
    private lateinit var tvMessage: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvLevel: TextView
    private lateinit var tvLevelName: TextView
    private lateinit var chart: WeekChartView
    private lateinit var floatingSwitch: SwitchCompat

    private var syncing = false
    private var lastCount = -1

    // Re-draw whenever the count changes, including taps on the floating monster.
    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> render() }

    private val overlayLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Settings.canDrawOverlays(this)) {
                enableMonster()
            } else {
                Toast.makeText(this, R.string.overlay_denied, Toast.LENGTH_LONG).show()
                syncSwitch()
            }
        }

    private val notificationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* optional */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        repo = ReelRepository(this)

        monster = findViewById(R.id.monsterPreview)
        tvCount = findViewById(R.id.tvCount)
        tvMessage = findViewById(R.id.tvMessage)
        tvTime = findViewById(R.id.tvTime)
        tvLevel = findViewById(R.id.tvLevel)
        tvLevelName = findViewById(R.id.tvLevelName)
        chart = findViewById(R.id.weekChart)
        floatingSwitch = findViewById(R.id.switchFloating)

        findViewById<MaterialButton>(R.id.btnAdd).setOnClickListener { addReel() }
        monster.setOnClickListener { addReel() }
        findViewById<MaterialButton>(R.id.btnUndo).setOnClickListener { repo.decrement() }
        findViewById<MaterialButton>(R.id.btnReset).setOnClickListener { confirmReset() }

        floatingSwitch.setOnCheckedChangeListener { _, checked ->
            if (syncing) return@setOnCheckedChangeListener
            if (checked) requestEnable() else disableMonster()
        }
    }

    override fun onResume() {
        super.onResume()
        repo.register(prefListener)
        // If the user turned the monster on earlier, make sure it is running again.
        if (repo.overlayEnabled) {
            if (Settings.canDrawOverlays(this)) startMonsterService() else repo.overlayEnabled = false
        }
        syncSwitch()
        render()
    }

    override fun onPause() {
        repo.unregister(prefListener)
        super.onPause()
    }

    private fun addReel() {
        repo.increment()
        monster.pop()
    }

    private fun render() {
        val count = repo.getCount()
        val stage = MonsterStage.stageOf(count)
        tvCount.text = count.toString()
        tvTime.text = MonsterStage.timeLabel(count)
        tvLevel.text = "Level ${stage + 1}"
        tvLevelName.text = MonsterStage.name(stage)
        monster.setStage(stage)
        if (count != lastCount) {
            tvMessage.text = MonsterStage.message(count)
            lastCount = count
        }
        chart.setData(repo.last7Days())
    }

    private fun confirmReset() {
        AlertDialog.Builder(this)
            .setTitle(R.string.reset_title)
            .setMessage(R.string.reset_body)
            .setPositiveButton(R.string.reset_confirm) { _, _ -> repo.resetToday() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun syncSwitch() {
        syncing = true
        floatingSwitch.isChecked = repo.overlayEnabled
        syncing = false
    }

    private fun requestEnable() {
        if (Settings.canDrawOverlays(this)) {
            enableMonster()
            return
        }
        syncing = true
        floatingSwitch.isChecked = false
        syncing = false
        AlertDialog.Builder(this)
            .setTitle(R.string.overlay_title)
            .setMessage(R.string.overlay_body)
            .setPositiveButton(R.string.overlay_go) { _, _ ->
                overlayLauncher.launch(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                )
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun enableMonster() {
        repo.overlayEnabled = true
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        startMonsterService()
        syncSwitch()
    }

    private fun disableMonster() {
        repo.overlayEnabled = false
        stopService(Intent(this, FloatingMonsterService::class.java))
        syncSwitch()
    }

    private fun startMonsterService() {
        ContextCompat.startForegroundService(this, Intent(this, FloatingMonsterService::class.java))
    }
}
