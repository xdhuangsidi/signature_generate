package org.junit.internal.runners.statements;

import java.lang.Thread.State;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestTimedOutException;

public class FailOnTimeout extends Statement {
    private final boolean lookForStuckThread;
    private final Statement originalStatement;
    private volatile ThreadGroup threadGroup;
    private final TimeUnit timeUnit;
    private final long timeout;

    public static class Builder {
        private boolean lookForStuckThread;
        private long timeout;
        private TimeUnit unit;

        private Builder() {
            this.lookForStuckThread = false;
            this.timeout = 0;
            this.unit = TimeUnit.SECONDS;
        }

        public Builder withTimeout(long timeout, TimeUnit unit) {
            if (timeout < 0) {
                throw new IllegalArgumentException("timeout must be non-negative");
            } else if (unit == null) {
                throw new NullPointerException("TimeUnit cannot be null");
            } else {
                this.timeout = timeout;
                this.unit = unit;
                return this;
            }
        }

        public Builder withLookingForStuckThread(boolean enable) {
            this.lookForStuckThread = enable;
            return this;
        }

        public FailOnTimeout build(Statement statement) {
            if (statement != null) {
                return new FailOnTimeout(this, statement);
            }
            throw new NullPointerException("statement cannot be null");
        }
    }

    private class CallableStatement implements Callable<Throwable> {
        private final CountDownLatch startLatch;

        private CallableStatement() {
            this.startLatch = new CountDownLatch(1);
        }

        public Throwable call() throws Exception {
            try {
                this.startLatch.countDown();
                FailOnTimeout.this.originalStatement.evaluate();
                return null;
            } catch (Exception e) {
                throw e;
            } catch (Throwable e2) {
                return e2;
            }
        }

        public void awaitStarted() throws InterruptedException {
            this.startLatch.await();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Deprecated
    public FailOnTimeout(Statement statement, long timeoutMillis) {
        this(builder().withTimeout(timeoutMillis, TimeUnit.MILLISECONDS), statement);
    }

    private FailOnTimeout(Builder builder, Statement statement) {
        this.threadGroup = null;
        this.originalStatement = statement;
        this.timeout = builder.timeout;
        this.timeUnit = builder.unit;
        this.lookForStuckThread = builder.lookForStuckThread;
    }

    public void evaluate() throws Throwable {
        CallableStatement callable = new CallableStatement();
        FutureTask<Throwable> task = new FutureTask(callable);
        this.threadGroup = new ThreadGroup("FailOnTimeoutGroup");
        Thread thread = new Thread(this.threadGroup, task, "Time-limited test");
        thread.setDaemon(true);
        thread.start();
        callable.awaitStarted();
        Throwable throwable = getResult(task, thread);
        if (throwable != null) {
            throw throwable;
        }
    }

    private Throwable getResult(FutureTask<Throwable> task, Thread thread) {
        try {
            if (this.timeout > 0) {
                return (Throwable) task.get(this.timeout, this.timeUnit);
            }
            return (Throwable) task.get();
        } catch (Throwable e) {
            return e;
        } catch (ExecutionException e2) {
            return e2.getCause();
        } catch (TimeoutException e3) {
            return createTimeoutException(thread);
        }
    }

    private Exception createTimeoutException(Thread thread) {
        StackTraceElement[] stackTrace = thread.getStackTrace();
        Thread stuckThread = this.lookForStuckThread ? getStuckThread(thread) : null;
        Exception currThreadException = new TestTimedOutException(this.timeout, this.timeUnit);
        if (stackTrace != null) {
            currThreadException.setStackTrace(stackTrace);
            thread.interrupt();
        }
        if (stuckThread == null) {
            return currThreadException;
        }
        new Exception("Appears to be stuck in thread " + stuckThread.getName()).setStackTrace(getStackTrace(stuckThread));
        return new MultipleFailureException(Arrays.asList(new Throwable[]{currThreadException, new Exception("Appears to be stuck in thread " + stuckThread.getName())}));
    }

    private StackTraceElement[] getStackTrace(Thread thread) {
        try {
            return thread.getStackTrace();
        } catch (SecurityException e) {
            return new StackTraceElement[0];
        }
    }

    private Thread getStuckThread(Thread mainThread) {
        if (this.threadGroup == null) {
            return null;
        }
        Thread[] threadsInGroup = getThreadArray(this.threadGroup);
        if (threadsInGroup == null) {
            return null;
        }
        Thread stuckThread = null;
        long maxCpuTime = 0;
        for (Thread thread : threadsInGroup) {
            if (thread.getState() == State.RUNNABLE) {
                long threadCpuTime = cpuTime(thread);
                if (stuckThread == null || threadCpuTime > maxCpuTime) {
                    stuckThread = thread;
                    maxCpuTime = threadCpuTime;
                }
            }
        }
        if (stuckThread == mainThread) {
            stuckThread = null;
        }
        return stuckThread;
    }

    private Thread[] getThreadArray(ThreadGroup group) {
        int enumSize = Math.max(group.activeCount() * 2, 100);
        int loopCount = 0;
        do {
            Thread[] threads = new Thread[enumSize];
            int enumCount = group.enumerate(threads);
            if (enumCount < enumSize) {
                return copyThreads(threads, enumCount);
            }
            enumSize += 100;
            loopCount++;
        } while (loopCount < 5);
        return null;
    }

    private Thread[] copyThreads(Thread[] threads, int count) {
        int length = Math.min(count, threads.length);
        Thread[] result = new Thread[length];
        for (int i = 0; i < length; i++) {
            result[i] = threads[i];
        }
        return result;
    }

    private long cpuTime(Thread thr) {
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        if (mxBean.isThreadCpuTimeSupported()) {
            try {
                return mxBean.getThreadCpuTime(thr.getId());
            } catch (UnsupportedOperationException e) {
            }
        }
        return 0;
    }
}
