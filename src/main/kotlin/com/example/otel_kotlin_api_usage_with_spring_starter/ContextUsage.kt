package com.example.otel_kotlin_api_usage_with_spring_starter

import io.opentelemetry.context.Context
import io.opentelemetry.context.ContextKey
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ContextUsage {
    companion object {
        @JvmStatic
        @Throws(Exception::class)
        fun contextUsage() {
            // 例のコンテキストキーを定義
            val exampleContextKey: ContextKey<String> = ContextKey.named("example-context-key")

            // コンテキストは追加するまでキーを含まない
            // Context.current() は現在のコンテキストにアクセス
            // 出力 => current context value: null
            println("current context value: " + Context.current().get(exampleContextKey))

            // コンテキストにエントリを追加
            val context = Context.current().with(exampleContextKey, "value")

            // ローカルコンテキスト変数には追加された値が含まれる
            // 出力 => context value: value
            println("context value: " + context.get(exampleContextKey))
            // 現在のコンテキストはまだ値を含まない
            // 出力 => current context value: null
            println("current context value: " + Context.current().get(exampleContextKey))

            // context.makeCurrent() を呼び出すと、スコープが閉じられるまで
            // Context.current() がコンテキストに設定され、その後 Context.current() は
            // context.makeCurrent() が呼び出される前の状態に復元される。
            // 結果として得られる Scope は AutoCloseable を実装し、通常は
            // try-with-resources ブロックで使用される。Scope.close() の呼び出しに失敗すると
            // エラーとなり、メモリリークやその他の問題を引き起こす可能性がある。
            context.makeCurrent().use { scope ->
                // 現在のコンテキストに追加された値が含まれる
                // 出力 => context value: value
                println("context value: " + Context.current().get(exampleContextKey))
            }

            // ローカルコンテキスト変数には追加された値がまだ含まれる
            // 出力 => context value: value
            println("context value: " + context.get(exampleContextKey))
            // 現在のコンテキストにはもう値が含まれない
            // 出力 => current context value: null
            println("current context value: " + Context.current().get(exampleContextKey))

            val executorService = Executors.newSingleThreadExecutor()
            val scheduledExecutorService = Executors.newScheduledThreadPool(1)

            // コンテキストインスタンスはアプリケーションコード内で明示的に渡すことができるが、
            // Context.makeCurrent() を呼び出し、Context.current() を介してアクセスする
            // 暗黙のコンテキストを使用する方が便利である。
            // コンテキストは暗黙のコンテキスト伝播のための多数のユーティリティを提供する。
            // これらのユーティリティは Scheduler、ExecutorService、ScheduledExecutorService、
            // Runnable、Callable、Consumer、Supplier、Function などのユーティリティクラスを
            // ラップし、実行前に Context.makeCurrent() を呼び出すように動作を変更する。
            context.wrap(::callable).call()
            context.wrap(::runnable).run()
            context.wrap(executorService).submit(::runnable)
            context.wrap(scheduledExecutorService).schedule(::runnable, 1, TimeUnit.SECONDS)
            context.wrapConsumer<Any>(::consumer).accept(Any())
            context.wrapConsumer<Any, Any>(::biConsumer).accept(Any(), Any())
            context.wrapFunction<Any, Any>(::function).apply(Any())
            context.wrapSupplier<Any>(::supplier).get()
        }

        /** 例の [java.util.concurrent.Callable]。 */
        private fun callable(): Any {
            return Any()
        }

        /** 例の [Runnable]。 */
        private fun runnable() {}

        /** 例の [java.util.function.Consumer]。 */
        private fun consumer(obj: Any) {}

        /** 例の [java.util.function.BiConsumer]。 */
        private fun biConsumer(object1: Any, object2: Any) {}

        /** 例の [java.util.function.Function]。 */
        private fun function(obj: Any): Any {
            return obj
        }

        /** 例の [java.util.function.Supplier]。 */
        private fun supplier(): Any {
            return Any()
        }
    }
}
