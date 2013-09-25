package com.epimorphics.lda.jmx;

import com.epimorphics.lda.cache.Cache;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class CacheControl implements ServletContextListener {

    public void contextDestroyed(ServletContextEvent s) {
    }

    public void contextInitialized(ServletContextEvent s) {
        JMXSupport.register("com.epimorphics.lda.jmx:type=cache", new Control());
    }

    public interface ControlMBean {

        public void clearAll();

    }

    public static class Control implements ControlMBean {

        public void clearAll() {
            Cache.Registry.clearAll();
        }
    }

}
