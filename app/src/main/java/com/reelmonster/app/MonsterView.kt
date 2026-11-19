package com.reelmonster.app

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator

/** The original Reel Monster character, drawn with Canvas so it scales to any size. */
class MonsterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var stage = 0
    private var bob = 0.5f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val rect = RectF()

    private val bodyColors = intArrayOf(
        0xFFB9A2F5.toInt(), 0xFFC58EF0.toInt(), 0xFFA98BE0.toInt(), 0xFF8F7FD1.toInt(), 0xFF7C6BB8.toInt()
    )
    private val ink = 0xFF3B2A5E.toInt()
    private val pink = 0xFFF27BAA.toInt()

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 1800
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.REVERSE
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener {
            bob = it.animatedValue as Float
            invalidate()
        }
    }

    fun setStage(newStage: Int) {
        val s = newStage.coerceIn(0, 4)
        if (s != stage) {
            stage = s
            invalidate()
        }
    }

    /** Squish-and-bounce when tapped. */
    fun pop() {
        animate().cancel()
        animate().scaleX(1.15f).scaleY(0.85f).setDuration(70).withEndAction {
            animate().scaleX(1f).scaleY(1f).setDuration(220)
                .setInterpolator(OvershootInterpolator(3f)).start()
        }.start()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator.start()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        animate().cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        val oy = (bob - 0.5f) * h * 0.06f

        // Horns
        paint.style = Paint.Style.FILL
        paint.color = pink
        path.reset()
        path.moveTo(w * 0.24f, h * 0.30f + oy); path.lineTo(w * 0.18f, h * 0.08f + oy); path.lineTo(w * 0.42f, h * 0.22f + oy); path.close()
        canvas.drawPath(path, paint)
        path.reset()
        path.moveTo(w * 0.76f, h * 0.30f + oy); path.lineTo(w * 0.82f, h * 0.08f + oy); path.lineTo(w * 0.58f, h * 0.22f + oy); path.close()
        canvas.drawPath(path, paint)

        // Body
        paint.color = bodyColors[stage]
        rect.set(w * 0.10f, h * 0.18f + oy, w * 0.90f, h * 0.94f + oy)
        canvas.drawRoundRect(rect, w * 0.32f, w * 0.32f, paint)

        // Belly
        paint.color = 0x55FFFFFF
        rect.set(w * 0.28f, h * 0.60f + oy, w * 0.72f, h * 0.90f + oy)
        canvas.drawOval(rect, paint)

        // Cheeks
        paint.color = 0x66FF9EC4
        canvas.drawCircle(w * 0.22f, h * 0.60f + oy, w * 0.05f, paint)
        canvas.drawCircle(w * 0.78f, h * 0.60f + oy, w * 0.05f, paint)

        val ex = w * 0.20f
        val ey = h * 0.46f + oy
        val er = w * 0.10f
        drawEye(canvas, w * 0.5f - ex, ey, er, left = true)
        drawEye(canvas, w * 0.5f + ex, ey, er, left = false)
        drawMouth(canvas, w, h, oy)
        drawExtras(canvas, w, h, oy)
    }

    private fun drawEye(canvas: Canvas, cx: Float, cy: Float, r: Float, left: Boolean) {
        paint.style = Paint.Style.FILL
        when (stage) {
            4 -> { // exhausted: tired lines plus dark circles
                paint.color = 0x553B2A5E
                rect.set(cx - r, cy + r * 0.2f, cx + r, cy + r * 1.1f)
                canvas.drawOval(rect, paint)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = r * 0.35f
                paint.strokeCap = Paint.Cap.ROUND
                paint.color = ink
                canvas.drawLine(cx - r, cy, cx + r, cy + r * 0.3f * (if (left) 1 else -1), paint)
            }
            else -> {
                val big = if (stage == 2 && !left) 1.25f else 1f
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawCircle(cx, cy, r * big, paint)
                paint.color = ink
                val look = if (stage == 2) -r * 0.25f else 0f
                canvas.drawCircle(cx + look, cy + r * 0.1f, r * 0.5f * big, paint)
                if (stage <= 1) { // sparkle
                    paint.color = 0xFFFFFFFF.toInt()
                    canvas.drawCircle(cx + r * 0.15f, cy - r * 0.15f, r * 0.18f, paint)
                }
                if (stage == 3) { // heavy eyelid
                    paint.color = bodyColors[stage]
                    rect.set(cx - r * 1.1f, cy - r * 1.1f, cx + r * 1.1f, cy + r * 0.15f)
                    canvas.drawRect(rect, paint)
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = r * 0.25f
                    paint.color = ink
                    canvas.drawLine(cx - r, cy + r * 0.15f, cx + r, cy + r * 0.15f, paint)
                }
                if (stage == 2) { // raised brow on the right eye
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = r * 0.25f
                    paint.strokeCap = Paint.Cap.ROUND
                    paint.color = ink
                    if (!left) canvas.drawLine(cx - r, cy - r * 1.6f, cx + r, cy - r * 1.9f, paint)
                }
            }
        }
        paint.style = Paint.Style.FILL
    }

    private fun drawMouth(canvas: Canvas, w: Float, h: Float, oy: Float) {
        val cx = w * 0.5f
        val my = h * 0.68f + oy
        paint.color = ink
        paint.strokeCap = Paint.Cap.ROUND
        when (stage) {
            0 -> { // happy smile
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = w * 0.03f
                rect.set(cx - w * 0.1f, my - w * 0.08f, cx + w * 0.1f, my + w * 0.08f)
                canvas.drawArc(rect, 20f, 140f, false, paint)
            }
            1 -> { // excited open mouth
                paint.style = Paint.Style.FILL
                rect.set(cx - w * 0.09f, my - w * 0.03f, cx + w * 0.09f, my + w * 0.11f)
                canvas.drawOval(rect, paint)
                paint.color = pink
                rect.set(cx - w * 0.05f, my + w * 0.05f, cx + w * 0.05f, my + w * 0.11f)
                canvas.drawOval(rect, paint)
            }
            2 -> { // confused wiggle
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = w * 0.03f
                path.reset()
                path.moveTo(cx - w * 0.1f, my + w * 0.02f)
                path.quadTo(cx - w * 0.05f, my - w * 0.03f, cx, my + w * 0.02f)
                path.quadTo(cx + w * 0.05f, my + w * 0.07f, cx + w * 0.1f, my + w * 0.02f)
                canvas.drawPath(path, paint)
            }
            3 -> { // sleepy "o"
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = w * 0.03f
                canvas.drawCircle(cx, my + w * 0.02f, w * 0.04f, paint)
            }
            else -> { // frown
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = w * 0.03f
                rect.set(cx - w * 0.09f, my, cx + w * 0.09f, my + w * 0.14f)
                canvas.drawArc(rect, 200f, 140f, false, paint)
            }
        }
        paint.style = Paint.Style.FILL
    }

    private fun drawExtras(canvas: Canvas, w: Float, h: Float, oy: Float) {
        paint.style = Paint.Style.FILL
        if (stage == 3) { // Zzz
            paint.color = ink
            paint.textSize = w * 0.16f
            paint.isFakeBoldText = true
            canvas.drawText("z", w * 0.82f, h * 0.20f + oy, paint)
            paint.textSize = w * 0.11f
            canvas.drawText("z", w * 0.92f - w * 0.03f, h * 0.10f + oy, paint)
            paint.isFakeBoldText = false
        }
        if (stage == 4) { // sweat drop
            paint.color = 0xFF9ADCFF.toInt()
            path.reset()
            val sx = w * 0.86f
            val sy = h * 0.34f + oy
            path.moveTo(sx, sy - w * 0.06f)
            path.quadTo(sx + w * 0.05f, sy + w * 0.02f, sx, sy + w * 0.05f)
            path.quadTo(sx - w * 0.05f, sy + w * 0.02f, sx, sy - w * 0.06f)
            canvas.drawPath(path, paint)
        }
    }
}
