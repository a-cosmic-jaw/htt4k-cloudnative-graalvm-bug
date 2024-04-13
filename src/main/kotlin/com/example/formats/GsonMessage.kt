package com.example.formats

import org.http4k.core.Body
import org.http4k.format.Gson.auto

data class GsonMessage(val subject: String, val message: String)

val gsonMessageLens = Body.auto<GsonMessage>().toLens()
