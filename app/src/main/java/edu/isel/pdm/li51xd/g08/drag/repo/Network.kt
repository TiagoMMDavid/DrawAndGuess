package edu.isel.pdm.li51xd.g08.drag.repo

import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.JsonRequest
import com.fasterxml.jackson.databind.ObjectMapper

data class WordDTO(val id: Int, val word: String)

class GetRandomWordsRequest(
    url: String,
    private val mapper: ObjectMapper,
    success: Response.Listener<Array<WordDTO>>,
    error: Response.ErrorListener
) : JsonRequest<Array<WordDTO>>(Method.GET, url, "", success, error) {

    override fun parseNetworkResponse(response: NetworkResponse): Response<Array<WordDTO>> {
        val wordsDto = mapper.readValue(String(response.data), Array<WordDTO>::class.java)
        return Response.success(wordsDto, null)
    }
}
