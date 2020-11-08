package edu.isel.pdm.li51xd.g08.drag.utils

interface DrawingListener {
    fun onNewPoint(x: Float, y: Float, isInitial: Boolean)

    fun onSizeChange()
}