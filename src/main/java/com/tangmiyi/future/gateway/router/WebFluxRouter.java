package com.tangmiyi.future.gateway.router;

import com.tangmiyi.future.gateway.handler.WebFluxHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class WebFluxRouter {

    /**
     * 功能性端点方式
     * @param webFluxHandler
     * @return
     */
    @Bean
    public RouterFunction<ServerResponse> routeCity(WebFluxHandler webFluxHandler) {
        return RouterFunctions
                .route(RequestPredicates.GET("/webFlux/hello")
                                .and(RequestPredicates.accept(MediaType.TEXT_PLAIN)),
                        webFluxHandler::hello);
    }
}
