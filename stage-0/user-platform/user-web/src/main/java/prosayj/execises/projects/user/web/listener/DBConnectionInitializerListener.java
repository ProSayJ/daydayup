package prosayj.execises.projects.user.web.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * DBConnectionInitializerListener
 *
 * @author yangjian
 * @date 2021-02-28 上午 12:08
 * @since 1.0.0
 */
@WebListener
public class DBConnectionInitializerListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
