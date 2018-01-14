package org.daisy.stevin.netty.sample.server.restful;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Created by shaoyang.qi on 2018/1/14
 */
@Component
@Provider
public class JsonContextResolver implements ContextResolver<ObjectMapper> {

    final ObjectMapper mapper = (new ObjectMapper()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}