package org.daisy.stevin.netty.sample.server.soa.impl;

import org.daisy.stevin.netty.sample.server.soa.facade.HelloService;

/**
 * Created by shaoyang.qi on 2017/6/4.
 */
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "Hello " + name;
    }
}
