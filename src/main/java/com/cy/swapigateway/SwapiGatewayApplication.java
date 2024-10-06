package com.cy.swapigateway;

import com.cy.swapigateway.filter.CustomGlobalFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class SwapiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwapiGatewayApplication.class, args);
    }

//    @Bean
//    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
//        return builder.routes()
//                .route("tobaidu", r -> r.path("/baidu")
//                        .uri("https://www.baidu.com/"))
//                .route("toGithub", r -> r.path("/")
//                        .uri("http://yupi.icu/"))
//                .build();
//    }

//    @Bean
//    public GlobalFilter customFilter() {
//        return new CustomGlobalFilter();
//    }

}
