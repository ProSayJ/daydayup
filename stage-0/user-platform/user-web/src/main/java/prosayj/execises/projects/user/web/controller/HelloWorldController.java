package prosayj.execises.projects.user.web.controller;

import prosayj.execises.web.mvc.controller.PageController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * 输出 “Hello,World” Controller
 *
 * @author yangjian
 * @since 1.0
 */
@Path("/hello")
public class HelloWorldController implements PageController {


    @GET
    @POST
    @Path("/world") // /hello/world -> HelloWorldController
    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        return "index.jsp";
    }
}
