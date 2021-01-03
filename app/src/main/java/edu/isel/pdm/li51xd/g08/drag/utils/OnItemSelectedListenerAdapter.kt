package edu.isel.pdm.li51xd.g08.drag.utils

import android.view.View
import android.widget.AdapterView

class OnItemSelectedListenerAdapter<T>(private val onItem: (T) -> Unit) : AdapterView.OnItemSelectedListener {
    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        val item = parent.getItemAtPosition(position) as T
        onItem(item)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) { }
}