package org.daisy.stevin.netty.sample.client.soa;

import org.daisy.stevin.netty.sample.server.soa.facade.HelloService;
import org.daisy.stevin.netty.soa.consumer.RpcConsumer;

/**
 * Created by shaoyang.qi on 2017/6/4.
 */
public class SoaClient {
    public static void main(String[] args) throws Exception {
        HelloService helloService = RpcConsumer.newProxy(HelloService.class);

        for (int i = 0; i < 10; i++) {
            String string = helloService.hello("Foo");

            System.out.println(string);

            Thread.sleep(1000);
        }

    }

}
