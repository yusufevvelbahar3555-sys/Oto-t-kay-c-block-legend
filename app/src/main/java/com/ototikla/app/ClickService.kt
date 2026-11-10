package com.ototikla.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Color
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class ClickService : AccessibilityService() {
    private lateinit var wm: WindowManager
    private var panel: LinearLayout? = null
    private var target: TextView? = null
    private lateinit var pParams: WindowManager.LayoutParams
    private lateinit var tParams: WindowManager.LayoutParams
    private lateinit var startBtn: Button
    private val handler = Handler(Looper.getMainLooper())
    private var running = false
    private var interval = 50L

    private val tick = object : Runnable {
        override fun run() {
            if (!running) return
            tap()
            handler.postDelayed(this, interval)
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun params(w: Int, h: Int, x: Int, y: Int) = WindowManager.LayoutParams(
        w, h, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ).apply { gravity = Gravity.TOP or Gravity.START; this.x = x; this.y = y }

    override fun onServiceConnected() {
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val size = dp(48)

        tParams = params(size, size, dp(150), dp(300))
        target = TextView(this).apply {
            text = "+"; textSize = 22f; gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.argb(150, 220, 0, 0))
                setStroke(dp(2), Color.WHITE)
            }
        }
        drag(target!!, target!!, tParams)
        wm.addView(target, tParams)

        pParams = params(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, dp(20), dp(100))
        val handle = TextView(this).apply {
            text = " ✥ "; textSize = 22f; setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        startBtn = Button(this).apply {
            text = "BAŞLAT"
            setOnClickListener { if (running) stop() else start() }
        }
        val closeBtn = Button(this).apply {
            text = "✕"
            setOnClickListener { stop(); disableSelf() }
        }
        panel = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.argb(200, 30, 30, 30))
            addView(handle); addView(startBtn); addView(closeBtn)
        }
        drag(handle, panel!!, pParams)
        wm.addView(panel, pParams)
    }

    private fun drag(touchView: View, moveView: View, p: WindowManager.LayoutParams) {
        var sx = 0f; var sy = 0f; var ox = 0; var oy = 0
        touchView.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> { sx = e.rawX; sy = e.rawY; ox = p.x; oy = p.y }
                MotionEvent.ACTION_MOVE -> {
                    p.x = ox + (e.rawX - sx).toInt()
                    p.y = oy + (e.rawY - sy).toInt()
                    wm.updateViewLayout(moveView, p)
                }
            }
            true
        }
    }

    private fun start() {
        interval = getSharedPreferences("ayar", MODE_PRIVATE).getLong("aralik", 50L).coerceAtLeast(10L)
        running = true
        startBtn.text = "DURDUR"
        tParams.flags = tParams.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        wm.updateViewLayout(target, tParams)
        handler.post(tick)
    }

    private fun stop() {
        running = false
        handler.removeCallbacks(tick)
        if (::startBtn.isInitialized) startBtn.text = "BAŞLAT"
        target?.let {
            tParams.flags = tParams.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
            try { wm.updateViewLayout(it, tParams) } catch (_: Exception) {}
        }
    }

    private fun tap() {
        val t = target ?: return
        val loc = IntArray(2)
        t.getLocationOnScreen(loc)
        val x = loc[0] + t.width / 2f
        val y = loc[1] + t.height / 2f
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 1))
            .build()
        dispatchGesture(gesture, null, null)
    }

    private fun removeViews() {
        stop()
        try { panel?.let { wm.removeView(it) } } catch (_: Exception) {}
        try { target?.let { wm.removeView(it) } } catch (_: Exception) {}
        panel = null; target = null
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean { removeViews(); return super.onUnbind(intent) }
    override fun onDestroy() { removeViews(); super.onDestroy() }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() { stop() }
}
