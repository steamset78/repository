package server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


//@WebListener
public class ContextListener /*implements ServletContextListener*/ {

    /*//private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        System.out.println("Starting up with:" + Runtime.version());
        System.out.println();

        //scheduler = Executors.newSingleThreadScheduledExecutor();
        //scheduler.scheduleAtFixedRate(new ScrapingJob(), 0, 20, TimeUnit.SECONDS);
        //scheduler.schedule(new RecalculateRoutes(), 0, TimeUnit.SECONDS);

        new Thread(new RecalculateRoutes()).start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println("Shutting down!");
        //scheduler.shutdownNow();
    }*/
	public static void main(String[] args) {
		new Thread(new RecalculateRoutes()).start();
	}
}