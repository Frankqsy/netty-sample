package org.daisy.stevin.netty.sample.server.springmvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class IndexController {
    @RequestMapping("/")
    public String index() {
        System.out.println("index");
        return "index";
    }
    @RequestMapping(value = "/foo", produces = "text/html; charset=utf-8")
    public @ResponseBody String getShopInJSON(HttpServletRequest request) {
        return "hello,foo";
    }

    /**
     * 1. 使用RequestMapping注解来映射请求的URL
     * 
     * 2. 返回值会通过视图解析器解析为实际的物理视图, 对于InternalResourceViewResolver视图解析器，会做如下解析
     * 
     * 通过prefix+returnVal+suffix 这样的方式得到实际的物理视图，然后会转发操作"/views/success.jsp"
     * 
     * @return
     */
    @RequestMapping("/helloworld")
    public String hello() {
        System.out.println("hello world");
        return "success";
    }
}
