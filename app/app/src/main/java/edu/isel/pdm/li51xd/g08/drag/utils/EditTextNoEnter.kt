package edu.isel.pdm.li51xd.g08.drag.utils

import android.text.Editable
import android.text.TextWatcher

class EditTextNoEnter : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (s != null) {
            for (i in s.length - 1 downTo 0) {
                if (s[i] == '\n') {
                    s.delete(i, i + 1)
                    return
                }
            }
        }
    }
}