package com.mingri.core.threadpool.support.eager;

import com.mingri.core.threadpool.proxy.RejectedProxyUtil;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 快速消费线程池
 * 发现池内线程大于核心线程数，不放入阻塞队列，而是创建非核心线程进行消费任务
 */
public class EagerThreadPoolExecutor extends ThreadPoolExecutor {

    // 线程池任务数量
    private final AtomicInteger submittedTaskCount = new AtomicInteger(0);

    // 线程池拒绝次数
    private final AtomicLong rejectedNum;

    public EagerThreadPoolExecutor(int corePoolSize,
                                   int maximumPoolSize,
                                   long keepAliveTime,
                                   TimeUnit unit,
                                   TaskQueue<Runnable> workQueue,
                                   ThreadFactory threadFactory,
                                   RejectedExecutionHandler handler,
                                   AtomicLong rejectedNum) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory,
                RejectedProxyUtil.createProxy(handler, rejectedNum));
        this.rejectedNum = rejectedNum;
        // 把当前线程池实例（this）传给队列，让队列持有线程池的引用
        // 这样 TaskQueue 在执行 offer() 等方法时，可以通过 executor 字段访问线程池的状态
        // （如线程数、最大线程数等），实现更智能的任务调度。
        workQueue.setExecutor(this);
    }

    public int getSubmittedTaskCount() {
        return submittedTaskCount.get();
    }

    public long getRejectedNum() {
        return rejectedNum.get();
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        submittedTaskCount.decrementAndGet();
    }

    @Override
    public void execute(Runnable command) {
        submittedTaskCount.incrementAndGet();
        try {
            super.execute(command);
        } catch (RejectedExecutionException ex) {
            TaskQueue taskQueue = (TaskQueue) super.getQueue();
            try {
                if (!taskQueue.retryOffer(command, 0, TimeUnit.MILLISECONDS)) {
                    submittedTaskCount.decrementAndGet();
                    throw new RejectedExecutionException("Queue capacity is full.", ex);
                }
            } catch (InterruptedException iex) {
                submittedTaskCount.decrementAndGet();
                throw new RejectedExecutionException(iex);
            }
        } catch (Exception ex) {
            submittedTaskCount.decrementAndGet();
            throw ex;
        }
    }
}
