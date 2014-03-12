/**
 * 
 */
package com.model.push;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.AsyncContext;

/**
 * @author Gao Youbo
 * @since 2013-4-23
 * @description
 * @TODO 向一个Queue<AsyncContext>中每个Context的Writer进行输出
 */
public class AsyncContextQueueWriter extends Writer {
    /**
     * AsyncContext队列
     */
    private Queue<AsyncContext> queue;

    /**
     * 消息队列
     */
    private static final BlockingQueue<String> MESSAGE_QUEUE = new LinkedBlockingQueue<String>();

    /**
     * 发�?消息到异步线程，�?��输出到http response�?     * 
     * @param cbuf
     * @param off
     * @param len
     * @throws IOException
     */
    private void sendMessage(char[] cbuf, int off, int len) throws IOException {
        sendMessage(new String(cbuf, off, len));
    }

    /**
     * 发�?消息到异步线程，�?��输出到http response�?     * 
     * @param str
     * @throws IOException
     */
    private void sendMessage(String str) throws IOException {
        try {
            MESSAGE_QUEUE.put(str);
        } catch (Exception ex) {
            IOException t = new IOException();
            t.initCause(ex);
            throw t;
        }
    }

    /**
     * 异步线程，当消息队列中被放入数据，将释放take方法的阻塞，将数据发送到http response流上
     */
    private Runnable notifierRunnable = new Runnable() {
        public void run() {
            boolean done = false;
            while (!done) {
                String script = null;
                try {
                    script = MESSAGE_QUEUE.take();
                    for (AsyncContext ac : queue) {
                        try {
                            PrintWriter acWriter = ac.getResponse().getWriter();
                            acWriter.println(script);
                            acWriter.flush();
                        } catch (IOException ex) {
                            System.out.println(ex);
                            queue.remove(ac);
                        }
                    }
                } catch (InterruptedException iex) {
                    done = true;
                    System.out.println(iex);
                }
            }
        }
    };

    /**
     * 保持�?��默认的writer，输出至控制台，这个writer是同步输出，其他输出到response流的writer是异步输�?     */
    private static final Writer DEFAULT_WRITER = new OutputStreamWriter(System.out);

    /**
     * 构�?AsyncContextQueueWriter
     * 
     * @param queue
     */
    AsyncContextQueueWriter(Queue<AsyncContext> queue) {
        this.queue = queue;
        Thread notifierThread = new Thread(notifierRunnable);
        notifierThread.start();
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        DEFAULT_WRITER.write(cbuf, off, len);
        sendMessage(cbuf, off, len);
    }

    @Override
    public void write(String str) throws IOException {
        // TODO Auto-generated method stub
        DEFAULT_WRITER.write(str);
        sendMessage(str);
    }

    @Override
    public void flush() throws IOException {
        DEFAULT_WRITER.flush();
    }

    @Override
    public void close() throws IOException {
        DEFAULT_WRITER.close();
        for (AsyncContext ac : queue) {
            ac.getResponse().getWriter().close();
        }
    }
}
