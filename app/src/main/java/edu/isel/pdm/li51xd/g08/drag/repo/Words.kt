package edu.isel.pdm.li51xd.g08.drag.repo

fun modelFromDto(dto: Array<WordnikWordDto>) = dto.toList().map { it.word }