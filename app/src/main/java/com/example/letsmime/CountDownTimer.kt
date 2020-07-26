package com.example.letsmime

import android.os.Handler
import android.os.Message
import android.os.SystemClock

abstract class CountDownTimer(
    /**
     * Millis since epoch when alarm should stop.
     */
    private val millisInFuture: Long,
    /**
     * The interval in millis that the user receives callbacks
     */
    private val countdownInterval: Long
) {

    private var stopTimeInFuture: Long = 0
    private var pauseTime: Long = 0
    private var cancelled = false
    private var paused = false

    /**
     * Cancel the countdown.
     *
     * Do not call it from inside CountDownTimer threads
     */
    fun cancel() {
        mHandler.removeMessages(MSG)
        cancelled = true
    }

    /**
     * Start the countdown.
     */
    @Synchronized
    fun start(): CountDownTimer {
        if (millisInFuture <= 0) {
            onFinish()
            return this
        }
        stopTimeInFuture = SystemClock.elapsedRealtime() + millisInFuture
        mHandler.sendMessage(mHandler.obtainMessage(MSG))
        cancelled = false
        paused = false
        return this
    }

    /**
     * Pause the countdown.
     */
    fun pause(): Long {
        pauseTime = stopTimeInFuture - SystemClock.elapsedRealtime()
        paused = true
        return pauseTime
    }

    /**
     * Resume the countdown.
     */
    fun resume(): Long {
        stopTimeInFuture = pauseTime + SystemClock.elapsedRealtime()
        paused = false
        mHandler.sendMessage(mHandler.obtainMessage(MSG))
        return pauseTime
    }

    /**
     * Callback fired on regular interval.
     * @param millisUntilFinished The amount of time until finished.
     */
    abstract fun onTick(millisUntilFinished: Long)

    /**
     * Callback fired when the time is up.
     */
    abstract fun onFinish()

    // handles counting down
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            synchronized(this@CountDownTimer) {
                if (!paused) {
                    val millisLeft =
                        stopTimeInFuture - SystemClock.elapsedRealtime()
                    if (millisLeft <= 0) {
                        onFinish()
                    } else if (millisLeft < countdownInterval) {
                        // no tick, just delay until done
                        sendMessageDelayed(obtainMessage(MSG), millisLeft)
                    } else {
                        val lastTickStart = SystemClock.elapsedRealtime()
                        onTick(millisLeft)

                        // take into account user's onTick taking time to execute
                        var delay =
                            lastTickStart + countdownInterval - SystemClock.elapsedRealtime()

                        // special case: user's onTick took more than interval to
                        // complete, skip to next interval
                        while (delay < 0) delay += countdownInterval
                        if (!cancelled) {
                            sendMessageDelayed(obtainMessage(MSG), delay)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val MSG = 1
    }

}