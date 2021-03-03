package prosayj.execises.projects.user.web.controller;

import prosayj.execises.projects.user.domain.User;
import prosayj.execises.projects.user.repository.DatabaseUserRepository;
import prosayj.execises.projects.user.service.UserService;
import prosayj.execises.projects.user.service.impl.UserServiceImpl;
import prosayj.execises.projects.user.sql.DBConnectionManager;
import prosayj.execises.web.mvc.controller.PageController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * UserController
 *
 * @author yangjian
 * @date 2021-03-02 上午 12:34
 * @since 1.0.0
 */
@Path("/user")
public class UserController implements PageController {
    private UserService userService;

    {
        userService = new UserServiceImpl(new DatabaseUserRepository(new DBConnectionManager()));
    }


    /**
     * // /hello/world -> HelloWorldController
     *
     * @param request  HTTP 请求
     * @param response HTTP 相应
     * @return String
     * @throws Throwable Throwable
     */
    @POST
    @Path("/regist")
    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        User save = new User();
        save.setName(request.getParameter("name"));
        save.setEmail(request.getParameter("email"));
        save.setPhoneNumber(request.getParameter("phoneNumber"));
        save.setPassword(request.getParameter("password"));
        return userService.register(save) ? "/WEB-INF/jsp/success.jsp" : "/WEB-INF/jsp/failure.jsp";
    }
}
