package com.tangmiyi.future.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Description: 网关
 * @author tangmiyi
 */
@SpringBootApplication
@MapperScan("com.tangmiyi.future.*.dao")
@EnableDiscoveryClient
@EnableScheduling
public class FutureGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(FutureGatewayApplication.class, args);
	}
}
