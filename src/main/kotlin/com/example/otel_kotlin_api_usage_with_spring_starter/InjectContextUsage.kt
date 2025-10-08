package com.example.otel_kotlin_api_usage_with_spring_starter

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.context.propagation.TextMapPropagator
import io.opentelemetry.context.propagation.TextMapSetter
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class InjectContextUsage {
    companion object {
        private val TEXT_MAP_SETTER: TextMapSetter<HttpRequest.Builder> = HttpRequestSetter()

        fun injectContextUsage() {
            // w3cトレースコンテキストとw3cバゲージを伝播するContextPropagatorsインスタンスを作成
            val propagators = ContextPropagators.create(
                TextMapPropagator.composite(
                    W3CTraceContextPropagator.getInstance(),
                    W3CBaggagePropagator.getInstance()
                )
            )

            // HttpRequestビルダーを作成
            val httpClient = HttpClient.newBuilder().build()
            val requestBuilder = HttpRequest.newBuilder()
                .uri(URI("http://127.0.0.1:8080/resource"))
                .GET()

            // ContextPropagatorsインスタンスがあるとき、現在のコンテキストをHTTPリクエストキャリアに注入
            propagators.textMapPropagator.inject(Context.current(), requestBuilder, TEXT_MAP_SETTER)

            // 注入されたコンテキストでリクエストを送信
            httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.discarding())
        }
    }

    /** [HttpRequest.Builder]キャリアを持つ[TextMapSetter]。 */
    private class HttpRequestSetter : TextMapSetter<HttpRequest.Builder> {
        override fun set(carrier: HttpRequest.Builder?, key: String, value: String) {
            carrier?.setHeader(key, value)
        }
    }
}
