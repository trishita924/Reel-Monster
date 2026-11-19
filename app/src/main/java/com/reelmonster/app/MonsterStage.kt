package com.reelmonster.app

object MonsterStage {

    fun stageOf(count: Int): Int = when {
        count <= 5 -> 0
        count <= 15 -> 1
        count <= 30 -> 2
        count <= 50 -> 3
        else -> 4
    }

    private val names = arrayOf("Tiny and happy", "Excited", "Confused", "Huge and sleepy", "Exhausted")
    private val scales = floatArrayOf(1.0f, 1.15f, 1.3f, 1.5f, 1.7f)

    private val messages = arrayOf(
        arrayOf("Hi! I'm tiny and I believe in you.", "Just a few, right?", "Cute reels only. I'm watching."),
        arrayOf("One more? Seriously?", "Ooh, this one's good. Okay, one more.", "We're warming up!"),
        arrayOf("Wait, how long have we been here?", "Girl, we've been here for HOURS.", "Your thumb needs a vacation."),
        arrayOf("I'm so sleepy. Are you?", "I live here now.", "My eyes are closing. Yours should too."),
        arrayOf("PLEASE GO OUTSIDE.", "I have become one with the feed.", "Drink water. Look at a tree. Anything.")
    )

    fun name(stage: Int) = names[stage.coerceIn(0, 4)]
    fun scale(stage: Int) = scales[stage.coerceIn(0, 4)]
    fun message(count: Int): String = messages[stageOf(count)].random()

    fun timeLabel(count: Int): String {
        val minutes = count * 30 / 60 // estimate: 30 seconds per reel
        return if (minutes >= 60) "${minutes / 60}h ${minutes % 60}m" else "$minutes min"
    }
}
