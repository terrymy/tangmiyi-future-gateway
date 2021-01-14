package com.tangmiyi.future.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

/**
 * 解决跨域问题
 */
@Configuration
public class CorsOverConfig {

	private static final String MAX_AGE = "18000L";

	@Bean
	public WebFilter corsFilter() {
		return (org.springframework.web.server.ServerWebExchange ctx, WebFilterChain chain) -> {
			org.springframework.http.server.reactive.ServerHttpRequest request = ctx.getRequest();
			if (CorsUtils.isCorsRequest(request)) {
				HttpHeaders requestHeaders = request.getHeaders();
				org.springframework.http.server.reactive.ServerHttpResponse response = ctx.getResponse();
				HttpMethod requestMethod = requestHeaders.getAccessControlRequestMethod();
				HttpHeaders headers = response.getHeaders();
				headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, requestHeaders.getOrigin());
				headers.addAll(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, requestHeaders
						.getAccessControlRequestHeaders());
				if (requestMethod != null) {
					headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, requestMethod.name());
				}
				headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
				headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
				headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE);
				if (request.getMethod() == HttpMethod.OPTIONS) {
					response.setStatusCode(HttpStatus.OK);
					return reactor.core.publisher.Mono.empty();
				}

			}
			return chain.filter(ctx);
		};
	}

//	@Bean
//	public ServerCodecConfigurer serverCodecConfigurer() {
//		return new DefaultServerCodecConfigurer();
//	}
//
//	@Bean
//	public DiscoveryLocatorProperties discoveryLocatorProperties() {
//		return new DiscoveryLocatorProperties();
//	}
}
