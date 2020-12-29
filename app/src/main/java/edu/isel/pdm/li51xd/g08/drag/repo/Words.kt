package edu.isel.pdm.li51xd.g08.drag.repo

const val WORDS_KEY = "DRAG.Words"
fun modelFromDto(dto: Array<WordnikWordDto>) = dto.toList().map { it.word }