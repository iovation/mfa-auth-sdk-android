package com.launchkey.android.authenticator.sdk.ui.mock

import android.os.Handler
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.util.*

class MockHandler : Handler() {
    private var runnables: Queue<Runnable>? = null
    fun getNumberOfRunnables(): Int {
        return runnables!!.size
    }
    
    fun executeRunnablesInOrder() {
        while (!runnables!!.isEmpty()) {
            runnables!!.poll().run()
        }
    }
    
    fun executeNRunnables(n: Int) {
        for (i in 0 until n) {
            runnables!!.poll().run()
        }
    }
    
    companion object {
        fun createMockHandler(): MockHandler {
            val handler = Mockito.mock(
                MockHandler::class.java, Mockito.withSettings().lenient()
            )
            handler.runnables = LinkedList()
            Mockito.doCallRealMethod().`when`(handler).executeRunnablesInOrder()
            Mockito.doCallRealMethod().`when`(handler).executeNRunnables(ArgumentMatchers.anyInt())
            Mockito.doCallRealMethod().`when`(handler).getNumberOfRunnables()
            Mockito.`when`(
                handler.post(
                    ArgumentMatchers.any(
                        Runnable::class.java
                    )
                )
            ).thenAnswer { invocation ->
                (invocation.mock as MockHandler).runnables!!.add(invocation.getArgument<Any>(0) as Runnable)
                true
            }
            Mockito.`when`(
                handler.postDelayed(
                    ArgumentMatchers.any(
                        Runnable::class.java
                    ), ArgumentMatchers.anyLong()
                )
            ).thenAnswer { invocation ->
                (invocation.mock as MockHandler).runnables!!.add(invocation.getArgument<Any>(0) as Runnable)
                true
            }
            Mockito.doAnswer { invocation ->
                (invocation.mock as MockHandler).runnables!!.remove(invocation.getArgument<Any>(0))
                null
            }.`when`(handler).removeCallbacks(
                ArgumentMatchers.any(
                    Runnable::class.java
                )
            )
            return handler
        }
    }
}