package com.example

import com.example.formats.GsonMessage
import com.example.formats.YamlMessage
import com.example.formats.gsonMessageLens
import com.example.formats.yamlMessageLens
import com.example.models.JTEViewModel
import com.github.kittinunf.fuel.httpGet
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.http4k.cloudnative.env.Environment
import org.http4k.core.Body
import org.http4k.core.ContentType.Companion.TEXT_HTML
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.DebuggingFilters.PrintRequest
import org.http4k.filter.MicrometerMetrics
import org.http4k.filter.OpenTelemetryMetrics
import org.http4k.filter.OpenTelemetryTracing
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes

import org.http4k.server.asServer
import org.http4k.template.JTETemplates
import org.http4k.template.viewModel
import org.http4k.cloudnative.health.Completed
import org.http4k.cloudnative.health.Health
import org.http4k.cloudnative.health.ReadinessCheck
import org.http4k.format.Gson.asJsonObject
import org.http4k.server.Http4kServer
import org.http4k.server.ServerConfig
import org.http4k.server.SunHttp

// this is a micrometer registry used mostly for testing - substitute the correct implementation.
val registry = SimpleMeterRegistry()

val app: HttpHandler = routes(
    "/ping" bind GET to {
        Response(OK).body("pong")
    },

    "/formats/yaml" bind GET to {
        Response(OK).with(yamlMessageLens of YamlMessage("Barry", "Hello there!"))
    },

    "/formats/json/gson" bind GET to {
        Response(OK).with(gsonMessageLens of GsonMessage("Barry", "Hello there!"))
    },

    "/templates/jte" bind GET to {
        val renderer = JTETemplates().CachingClasspath()
        val view = Body.viewModel(renderer, TEXT_HTML).toLens()
        val viewModel = JTEViewModel("Hello there!")
        Response(OK).with(view of viewModel)
    },

    "/metrics" bind GET to {
        Response(OK).body("Example metrics route for http4kcloudnative")
    },

    "/opentelemetrymetrics" bind GET to {
        Response(OK).body("Example metrics route for http4kcloudnative")
    },

    "/config" bind GET to {
        Response(OK).body(Environment.ENV.asJsonObject().toString()/*.asJsonObject().toString()*/)
    }
)

private fun allIsWell(): Boolean {
    println("Performing health checks...")
    var ret = true
    // health checks https://www.http4k.org/guide/reference/cloud_native/
    //val endpoints = setOf("http://localhost:8001/liveness", "http://localhost:8001/readiness", "http://localhost:8001/configSHOULDFAIL", "http://localhost:8000")
    val endpoints = setOf("http://localhost:9000/ping", "http://localhost:9000/config")
    endpoints.forEach {
        println("Performing health check through the '$it' endpoint...")
        val (request, response, result) = it.httpGet().response()

        if (response.statusCode != 200) {
            ret = false
            println("request=$request")
            println("response=$response")
            println("result=$result")
        }
    }

    println("Performing health checks done...")
    return ret
}

fun main() {
    val printingApp: HttpHandler = PrintRequest()
            .then(ServerFilters.MicrometerMetrics.RequestCounter(registry))
            .then(ServerFilters.MicrometerMetrics.RequestTimer(registry))
            .then(ServerFilters.OpenTelemetryTracing())
            .then(ServerFilters.OpenTelemetryMetrics.RequestCounter())
            .then(ServerFilters.OpenTelemetryMetrics.RequestTimer()).then(app)

    val server = printingApp.asServer(SunHttp(9000)).start()
    println("Server started on " + server.port())

    if (!allIsWell()) {
        println("Health checks failed - Shutting down!")
        server.stop()
    }
}
