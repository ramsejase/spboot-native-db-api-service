package com.rj.ntiv.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.rj.ntiv.lib.config.ConnectionUrl;
import com.rj.ntiv.lib.config.MongoQualifier;

@Configuration(proxyBeanMethods = false)
public class AppRouter {
    
    @Bean
    @SuppressWarnings("null")
    RouterFunction<ServerResponse> displayConnections(
        @MongoQualifier(name = "${mongo.config[0].name}")
        @Autowired
        ConnectionUrl url1,

        @MongoQualifier(name = "coredb")
        @Autowired
        ConnectionUrl url2) { 

        System.out.println("------------API Router Initialized");
        return RouterFunctions.route()
        .GET(
            "/call",
            request -> ServerResponse.ok().bodyValue(
                "1) " + url1.val() 
                + "\n" 
                + "2) " + url2.val()

                )
        ).build();
    }
}