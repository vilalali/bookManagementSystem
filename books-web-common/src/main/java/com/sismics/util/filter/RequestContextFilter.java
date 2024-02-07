package com.sismics.util.filter;

import com.sismics.books.core.constant.Constants;
import com.sismics.books.core.model.context.AppContext;
import com.sismics.books.core.util.DirectoryUtil;
import com.sismics.books.core.util.TransactionUtil;
import com.sismics.util.EnvironmentUtil;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.EMF;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;

/**
 * Filter used to process a couple things in the request context.
 * 
 * @author jtremeaux
 */
public class RequestContextFilter implements Filter {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(RequestContextFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Force the locale in order to not depend on the execution environment
        Locale.setDefault(new Locale(Constants.DEFAULT_LOCALE_ID));

        // Injects the webapp root
        String webappRoot = filterConfig.getServletContext().getRealPath("/");
        EnvironmentUtil.setWebappRoot(webappRoot);
        
        // Initialize the app directory
        File baseDataDirectory = null;
        try {
            baseDataDirectory = DirectoryUtil.getBaseDataDirectory();
        } catch (Exception e) {
            log.error("Error initializing base data directory", e);
        }
        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Using base data directory: {0}", baseDataDirectory.toString()));
        }
        
        // Initialize file logger
        RollingFileAppender fileAppender = new RollingFileAppender();
        fileAppender.setName("FILE");
        fileAppender.setFile(DirectoryUtil.getLogDirectory() + File.separator + "books.log");
        fileAppender.setLayout(new PatternLayout("%d{DATE} %p %l %m %n"));
        fileAppender.setThreshold(Level.INFO);
        fileAppender.setAppend(true);
        fileAppender.setMaxFileSize("5MB");
        fileAppender.setMaxBackupIndex(5);
        fileAppender.activateOptions();
        org.apache.log4j.Logger.getRootLogger().addAppender(fileAppender);
        
        // Initialize the application context
        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                AppContext.getInstance();
            }
        });
    }

    @Override
    public void destroy() {
        // NOP
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        EntityManager em = null;
        
        try {
            em = EMF.get().createEntityManager();
        } catch (Exception e) {
            throw new ServletException("Cannot create entity manager", e);
        }
        ThreadLocalContext context = ThreadLocalContext.get();
        context.setEntityManager(em);
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            ThreadLocalContext.cleanup();
            
            // IOException are thrown if the client closes the connection before completion
            if (!(e instanceof IOException)) {
                log.error("An exception occured, rolling back current transaction", e);

                // If an unprocessed error comes up from the application layers (Jersey...), rollback the transaction (should not happen)
                if (em.isOpen()) {
                    if (em.getTransaction() != null && em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    
                    try {
                        em.close();
                    } catch (Exception ce) {
                        log.error("Error closing entity manager", ce);
                    }
                }
                throw new ServletException(e);
            }
        }
        
        ThreadLocalContext.cleanup();

        // No error processing the request : commit / rollback the current transaction depending on the HTTP code
        if (em.isOpen()) {
            if (em.getTransaction() != null && em.getTransaction().isActive()) {
                HttpServletResponse r = (HttpServletResponse) response;
                int statusClass = r.getStatus() / 100;
                if (statusClass == 2 || statusClass == 3) {
                    try {
                        em.getTransaction().commit();
                    } catch (Exception e) {
                        log.error("Error during commit", e);
                        r.sendError(500);
                    }
                } else {
                    em.getTransaction().rollback();
                }
                
                try {
                    em.close();
                } catch (Exception e) {
                    log.error("Error closing entity manager", e);
                }
            }
        }
    }
}
