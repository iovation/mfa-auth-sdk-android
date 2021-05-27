package com.launchkey.android.authenticator.sdk.ui.internal.util;

import android.os.Handler;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.launchkey.android.authenticator.sdk.ui.fake.FakeAuthRequestManager;
import com.launchkey.android.authenticator.sdk.ui.fake.FakeAuthenticatorManager;
import com.launchkey.android.authenticator.sdk.ui.fake.FakeLifecycle;
import com.launchkey.android.authenticator.sdk.ui.test.BaseTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExpirationTimerTrackerTest extends BaseTest {
    public ExpirationTimerTrackerTest() {
        super(new FakeAuthenticatorManager(), new FakeAuthRequestManager(), null);
    }

    @Mock
    private TimingCounter.NowProvider mockNowProvider;

    @Mock
    private Observer<ExpirationTimerTracker.State> mockObserver;

    @Mock
    private LifecycleOwner mockLifecycleOwner;

    private FakeLifecycle mockLifecycle = new FakeLifecycle();

    private long counter = 0;

    @Before
    public void setup() {
        when(mockNowProvider.getNow()).thenAnswer(
                new Answer() {
                    public Long answer(InvocationOnMock invocation) {
                        return counter++;
                    }
                });
        when(mockLifecycleOwner.getLifecycle()).thenReturn(mockLifecycle);
    }

    @Test
    public void testOnUpdateIsCalled() throws Exception {
        final Handler handler = new Handler(getContext().getMainLooper());
        final ExpirationTimerTracker tracker = new ExpirationTimerTracker(0, 0, 10, mockNowProvider, handler, mockLifecycleOwner);
        handler.post(new Runnable() {
            @Override
            public void run() {
                tracker.getState().observe(mockLifecycleOwner, mockObserver);
                mockLifecycle.setState(Lifecycle.State.RESUMED);
                mockLifecycle.setEvent(Lifecycle.Event.ON_RESUME);
            }
        });
        Thread.sleep(1000);
        ArgumentCaptor<ExpirationTimerTracker.State> expState = ArgumentCaptor.forClass(ExpirationTimerTracker.State.class);
        verify(mockObserver, times(11)).onChanged(expState.capture());
        for (int i = 0; i < 9; ++i) {
            assertEquals(10-(i+1), ((ExpirationTimerTracker.State.Update)expState.getAllValues().get(i)).remainingMillis);
            assertEquals((i+1) / 10f, ((ExpirationTimerTracker.State.Update)expState.getAllValues().get(i)).progress, .0001);
        }
    }

    @Test
    public void testOnExpiredIsNotCalled() throws Exception {
        final Handler handler = new Handler(getContext().getMainLooper());
        final ExpirationTimerTracker tracker = new ExpirationTimerTracker(0, 0, 10, mockNowProvider, handler, mockLifecycleOwner, 50);
        final Observer<ExpirationTimerTracker.State> spyObserver = Mockito.spy(new Observer<ExpirationTimerTracker.State>() {
            int i = 0;
            @Override
            public void onChanged(ExpirationTimerTracker.State state) {
                ++i;
                if (i == 10) {
                    tracker.stop();
                }
            }
        });
        handler.post(new Runnable() {
            @Override
            public void run() {
                tracker.getState().observe(mockLifecycleOwner, spyObserver);
                mockLifecycle.setState(Lifecycle.State.RESUMED);
                mockLifecycle.setEvent(Lifecycle.Event.ON_RESUME);
            }
        });
        Thread.sleep(1000);
        ArgumentCaptor<ExpirationTimerTracker.State> expState = ArgumentCaptor.forClass(ExpirationTimerTracker.State.class);
        verify(spyObserver, times(10)).onChanged(expState.capture());
        for (final ExpirationTimerTracker.State state : expState.getAllValues()) {
            assertFalse(state instanceof ExpirationTimerTracker.State.Expired);
        }
    }

    @Test
    public void testOnExpiredIsCalled() throws Exception {
        final Handler handler = new Handler(getContext().getMainLooper());
        final ExpirationTimerTracker tracker = new ExpirationTimerTracker(0, 0, 10, mockNowProvider, handler, mockLifecycleOwner);
        handler.post(new Runnable() {
            @Override
            public void run() {
                tracker.getState().observe(mockLifecycleOwner, mockObserver);
                mockLifecycle.setState(Lifecycle.State.RESUMED);
                mockLifecycle.setEvent(Lifecycle.Event.ON_RESUME);
            }
        });
        Thread.sleep(1000);
        ArgumentCaptor<ExpirationTimerTracker.State> expState = ArgumentCaptor.forClass(ExpirationTimerTracker.State.class);
        verify(mockObserver, times(11)).onChanged(expState.capture());
        assertTrue(expState.getAllValues().get(expState.getAllValues().size()-1) instanceof ExpirationTimerTracker.State.Expired);
    }

    @Test
    public void testRunnableStopsPostingOnStop() throws Exception {
        final Handler handler = new Handler(getContext().getMainLooper());
        final ExpirationTimerTracker tracker = new ExpirationTimerTracker(0, 0, 10, mockNowProvider, handler, mockLifecycleOwner);
        final Observer<ExpirationTimerTracker.State> spyObserver = Mockito.spy(new Observer<ExpirationTimerTracker.State>() {
            @Override
            public void onChanged(ExpirationTimerTracker.State state) {
                tracker.stop();
            }
        });
        handler.post(new Runnable() {
            @Override
            public void run() {
                tracker.getState().observe(mockLifecycleOwner, spyObserver);
                mockLifecycle.setState(Lifecycle.State.RESUMED);
                mockLifecycle.setEvent(Lifecycle.Event.ON_RESUME);
            }
        });
        Thread.sleep(1000);
        ArgumentCaptor<ExpirationTimerTracker.State> expState = ArgumentCaptor.forClass(ExpirationTimerTracker.State.class);
        verify(spyObserver, times(1)).onChanged(expState.capture());
        assertTrue(expState.getValue() instanceof ExpirationTimerTracker.State.Update);
    }

    @Test
    public void testListenerStopsListeningOnRemove() throws Exception {
        final Handler handler = new Handler(getContext().getMainLooper());
        final ExpirationTimerTracker tracker = new ExpirationTimerTracker(0, 0, 10, mockNowProvider, handler, mockLifecycleOwner);
        handler.post(new Runnable() {
            @Override
            public void run() {
                tracker.getState().observe(mockLifecycleOwner, mockObserver);
            }
        });
        Thread.sleep(1000);
        verify(mockObserver, times(0)).onChanged(any(ExpirationTimerTracker.State.Update.class));
        verify(mockObserver, times(0)).onChanged(any(ExpirationTimerTracker.State.Expired.class));
    }
}
