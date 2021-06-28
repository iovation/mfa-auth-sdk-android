package com.launchkey.android.authenticator.sdk.ui.mock;

import android.os.Handler;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.LinkedList;
import java.util.Queue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class MockHandler extends Handler {

    private Queue<Runnable> runnables;

    public int getNumberOfRunnables() {
        return runnables.size();
    }

    public void executeRunnablesInOrder() {
        while (!runnables.isEmpty()) {
            runnables.poll().run();
        }
    }

    public void executeNRunnables(int n) {
        for (int i = 0; i < n; ++i) {
            runnables.poll().run();
        }
    }

    public static MockHandler createMockHandler() {
        MockHandler handler = Mockito.mock(MockHandler.class, withSettings().lenient());
        handler.runnables = new LinkedList<>();
        Mockito.doCallRealMethod().when(handler).executeRunnablesInOrder();
        Mockito.doCallRealMethod().when(handler).executeNRunnables(anyInt());
        Mockito.doCallRealMethod().when(handler).getNumberOfRunnables();
        when(handler.post(any(Runnable.class))).thenAnswer(
                new Answer() {
                    public Object answer(InvocationOnMock invocation) {
                        ((MockHandler)invocation.getMock()).runnables.add((Runnable) invocation.getArgument(0));
                        return true;
                    }
                });
        when(handler.postDelayed(any(Runnable.class), anyLong())).thenAnswer(
                new Answer() {
                    public Object answer(InvocationOnMock invocation) {
                        ((MockHandler)invocation.getMock()).runnables.add((Runnable) invocation.getArgument(0));
                        return true;
                    }
                });
        doAnswer(
                new Answer() {
                    public Object answer(InvocationOnMock invocation) {
                        ((MockHandler)invocation.getMock()).runnables.remove(invocation.getArgument(0));
                        return null;
                    }
                }).when(handler).removeCallbacks(any(Runnable.class));

        return handler;
    }
}
