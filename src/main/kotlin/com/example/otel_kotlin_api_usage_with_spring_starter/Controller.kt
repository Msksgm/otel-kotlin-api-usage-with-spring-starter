package com.example.otel_kotlin_api_usage_with_spring_starter

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.context.Context
import io.opentelemetry.context.ContextKey
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller {
    private val tracer = GlobalOpenTelemetry.getTracer("otel-kotlin-api-usage")
    private val userIdKey: ContextKey<String> = ContextKey.named("user-id")

    @GetMapping("/ping")
    fun ping(): String {
        return "pong"
    }

    @GetMapping("/context-demo")
    fun contextDemo(): Map<String, String> {
        val span = tracer.spanBuilder("context-demo").startSpan()

        try {
            span.makeCurrent().use { scope ->
                val context = Context.current().with(userIdKey, "user-123")
                // context を共有しないか、makeCurrent の外で呼び出すと null になる
                contextCallInMakeCurrent(userIdKey)

                // context を共有して、内部で makeCurrent を呼び出ときに値が取得できる
                contextStartUsage(context, userIdKey)

                context.makeCurrent().use { contextScope ->
                    // makeCurrent の中であれば current を引数に利用しなくても、Context.current() で値が取得できる
                    contextCallInMakeCurrent(userIdKey)
                    val currentUserId = Context.current().get(userIdKey)
                    Span.current().setAttribute("user.id", currentUserId ?: "unknown")

                    return mapOf(
                        "message" to "Context demonstration",
                        "userId" to (currentUserId ?: "not set"),
                        "traceId" to span.spanContext.traceId
                    )
                }
            }
        } finally {
            span.end()
        }
    }

    /**
     * contextCallInMakeCurrent
     *
     * makeCurrent の中で呼び出すと Context.current() で値が取得できる。
     * makeCurrent の外で呼び出すと Context.current() でnullを取得する
     *
     * @param userIdKey
     */
    fun contextCallInMakeCurrent(userIdKey: ContextKey<String>) {
        val currentUserId = Context.current().get(userIdKey)
        println("userIdKey in contextCallInMakeCurrent is $currentUserId")
    }

    /**
     * contextStartUsage
     *
     * Context.current().get() は makeCurrent の中で呼び出すと値が取得できる。
     * context.get() は makeCurrent の外で呼び出しても値が取得できる。ただし、context を共有（変数の引き回し）をした場合に限る。
     *
     * @param context
     * @param userIdKey
     */
    fun contextStartUsage(context: Context, userIdKey: ContextKey<String>) {
        context.makeCurrent().use { contextScope ->
            contextCallInMakeCurrent(userIdKey)
            val currentUserId = Context.current().get(userIdKey)
            println("currentUserId in contextStartUsage is $currentUserId")
        }
        val currentUserId = context.get(userIdKey)
        println("userIdKey in contextStartUsage is $currentUserId")
    }

    @GetMapping("/context-service-demo")
    fun contextServiceDemo(): String {
        ContextUsage.contextUsage()
        return "context-service-demo"
    }
}
