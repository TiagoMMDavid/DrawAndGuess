package edu.isel.pdm.li51xd.g08.drag.repo

import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.JsonRequest
import com.fasterxml.jackson.databind.ObjectMapper

data class WordnikWordDto(val id: Int, val word: String)

class GetRandomWordsRequest(
        url: String,
        private val mapper: ObjectMapper,
        success: Response.Listener<Array<WordnikWordDto>>,
        error: Response.ErrorListener
) : JsonRequest<Array<WordnikWordDto>>(Method.GET, url, "", success, error) {

    override fun parseNetworkResponse(response: NetworkResponse): Response<Array<WordnikWordDto>> {
        val wordsDto = mapper.readValue(String(response.data), Array<WordnikWordDto>::class.java)
        return Response.success(wordsDto, null)
    }
}
