package org.daisy.stevin.netty.sample.server.springmvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class IndexController {
    @RequestMapping("/")
    public String index() {
        return "index.html";
    }

    @RequestMapping("/index")
    public String indexJsp() {
        return "index.jsp";
    }

    @RequestMapping(value = "/foo", produces = "text/html; charset=utf-8")
    public
    @ResponseBody
    String getShopInJSON(HttpServletRequest request) {
        return "hello,foo";
    }

    /**
     * 1. 使用RequestMapping注解来映射请求的URL
     * <p>
     * 2. 返回值会通过视图解析器解析为实际的物理视图, 对于InternalResourceViewResolver视图解析器，会做如下解析
     * <p>
     * 通过prefix+returnVal+suffix 这样的方式得到实际的物理视图，然后会转发操作"/views/success.jsp"
     *
     * @return
     */
    @RequestMapping("/hello")
    public String hello() {
        return "success.html";
    }

    @RequestMapping("/hello_world")
    public String helloWorld() {
        return "success.jsp";
    }
}
