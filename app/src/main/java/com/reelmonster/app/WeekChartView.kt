package com.reelmonster.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/** Simple bar chart for the last 7 days. Today is the last (pink) bar. */
class WeekChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var data: List<DayCount> = emptyList()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    fun setData(days: List<DayCount>) {
        data = days
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) return
        val d = resources.displayMetrics.density
        val w = width.toFloat()
        val h = height.toFloat()
        val labelH = 22f * d
        val topPad = 20f * d
        val slot = w / data.size
        val barW = slot * 0.55f
        val max = (data.maxOf { it.count }).coerceAtLeast(10)
        val chartH = h - labelH - topPad

        paint.textAlign = Paint.Align.CENTER
        data.forEachIndexed { i, day ->
            val cx = slot * i + slot / 2f
            val barH = (day.count.toFloat() / max * chartH).coerceAtLeast(6f * d)
            val isToday = i == data.lastIndex

            paint.style = Paint.Style.FILL
            paint.color = if (isToday) 0xFFF27BAA.toInt() else 0xFFD9C9FA.toInt()
            rect.set(cx - barW / 2, topPad + chartH - barH, cx + barW / 2, topPad + chartH)
            canvas.drawRoundRect(rect, 10f * d, 10f * d, paint)

            paint.color = 0xFF3B2A5E.toInt()
            paint.textSize = 11f * d
            paint.isFakeBoldText = true
            canvas.drawText(day.count.toString(), cx, topPad + chartH - barH - 5f * d, paint)

            paint.isFakeBoldText = isToday
            paint.color = if (isToday) 0xFF3B2A5E.toInt() else 0xFF8B7FA8.toInt()
            paint.textSize = 11f * d
            canvas.drawText(day.label, cx, h - 4f * d, paint)
        }
        paint.isFakeBoldText = false
    }
}
