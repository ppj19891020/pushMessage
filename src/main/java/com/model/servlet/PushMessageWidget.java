package com.model.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.model.push.DefaultWriterAppender;

/**
 * 主动推送
 * @author PeiJie
 *
 */
/**
 * 注解WebServlet用来描述一个Servlet
 * 属性name描述Servlet的名字,可选
 * 属性urlPatterns定义访问的URL,或者使用属性value定义访问的URL.(定义访问的URL是必选属性)
 */

@WebServlet(name="push",urlPatterns="/push",asyncSupported=true)
public class PushMessageWidget extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		response.setHeader("Cache-Control", "private");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Connection", "Keep-Alive");
        response.setHeader("Proxy-Connection", "Keep-Alive");
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.println("<!-- Comet is a programming technique that enables web servers to send data to the client without having any need for the client to request it. -->\n");
        writer.flush();
        
        if (!request.isAsyncSupported()) {
            // log.info("the servlet is not supported Async");
            return;
        }
        request.startAsync(request, response);
        if (request.isAsyncStarted()) {
            final AsyncContext asyncContext = request.getAsyncContext();
            asyncContext.setTimeout(1L * 60L * 1000L * 100);// 60 00sec

            asyncContext.addListener(new AsyncListener() {
                public void onComplete(AsyncEvent event) throws IOException {
                    DefaultWriterAppender.ASYNC_CONTEXT_QUEUE.remove(asyncContext);
                }

                public void onTimeout(AsyncEvent event) throws IOException {
                    DefaultWriterAppender.ASYNC_CONTEXT_QUEUE.remove(asyncContext);
                }

                public void onError(AsyncEvent event) throws IOException {
                    DefaultWriterAppender.ASYNC_CONTEXT_QUEUE.remove(asyncContext);
                }

                public void onStartAsync(AsyncEvent event) throws IOException {
                }
            });
            DefaultWriterAppender.ASYNC_CONTEXT_QUEUE.add(asyncContext);
        } else {
            // log.error("the ruquest is not AsyncStarted !");
        }
		
	}
	
	

}
