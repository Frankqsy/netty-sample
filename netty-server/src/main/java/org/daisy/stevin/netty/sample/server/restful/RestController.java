package org.daisy.stevin.netty.sample.server.restful;

import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

@Controller
@Path("/rest")
public class RestController {
    @GET
    @Path(value = "/")
    @Produces("application/json")
    public String index() {
        System.out.println("call index");
        return "rest index";
    }

    @GET
    @Path(value = "/foo")
    @Produces("application/json")
    public String sayHello(@QueryParam("hello") String hello) {
        System.out.println("call sayHello,param:" + hello);
        return "hello,foo";
    }

}
