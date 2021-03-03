package prosayj.execises.projects.user.web.controller;

import prosayj.execises.web.mvc.controller.PageController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * 静态资源跳转类
 *
 * @author yangjian
 * @date 2021-03-02 上午 12:14
 * @since 1.0.0
 */
@Path("/to")
public class StaticJumpController implements PageController {
    @GET
    @POST
    @Path("/user/register")
    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        return "/WEB-INF/jsp/register.jsp";
    }
}
