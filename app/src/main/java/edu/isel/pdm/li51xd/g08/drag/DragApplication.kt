package edu.isel.pdm.li51xd.g08.drag

import android.app.Application
import com.android.volley.toolbox.Volley
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import edu.isel.pdm.li51xd.g08.drag.repo.DragRepository

class DragApplication : Application() {
    val repo by lazy {
        DragRepository(Volley.newRequestQueue(this),
            jacksonObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false))
    }
}