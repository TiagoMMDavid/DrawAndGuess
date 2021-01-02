package edu.isel.pdm.li51xd.g08.drag.utils

import android.os.CountDownTimer

class CountDownTimerAdapter(millisInFuture: Long, countDownInterval: Long, private val onTickListener: (Long) -> Unit)
    : CountDownTimer(millisInFuture, countDownInterval) {

    override fun onTick(millisUntilFinished: Long) {
        onTickListener(millisUntilFinished)
    }

    override fun onFinish() {}
}