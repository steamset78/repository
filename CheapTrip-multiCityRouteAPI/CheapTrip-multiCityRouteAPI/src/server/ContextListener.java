package server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.concurrent.ScheduledExecutorService;

public class ContextListener implements ServletContextListener {

    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        System.out.println("Starting up with:" + Runtime.version());
        System.out.println();

        //scheduler = Executors.newSingleThreadScheduledExecutor();
        //scheduler.scheduleAtFixedRate(new ScrapingJob(), 0, 20, TimeUnit.SECONDS);
        //scheduler.schedule(new RecalculateRoutes(), 0, TimeUnit.SECONDS);

        //new Thread(new RecalculateRoutes()).start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println("Shutting down!");
        //scheduler.shutdownNow();
    }
}