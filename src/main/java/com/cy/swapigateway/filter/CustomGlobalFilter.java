package com.cy.swapigateway.filter;

import com.cy.swapigateway.utils.SignGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 请求过滤
 */
@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    private String accessKeyInDb = "CWL2bTNn8co6SScwBLr8T79bmLwyPwEf";
    private String secretKeyInDb = "TTLqtqlvuX4lcJLMu+xxkgWcffex+hP4pzUQgODwljU=";
    private static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1.用户发送请求到API网关
        // 2.请求日志
        ServerHttpRequest request = exchange.getRequest();
        log.info("请求唯一标识："+request.getId());
        log.info("请求路径："+request.getPath());
        log.info("请求方法："+request.getMethod());
        log.info("请求参数："+request.getQueryParams());
        String sourceString = request.getRemoteAddress().getHostString();
        log.info("请求来源地址："+sourceString);
        log.info("请求来源地址："+request.getRemoteAddress());
        // 3.（黑白名单）
        // 拿到响应对象
        ServerHttpResponse response = exchange.getResponse();
        if (!IP_WHITE_LIST.contains(sourceString)) {
            // 设置响应状态码403 FORBIDDEN（禁止访问）
            response.setStatusCode(HttpStatus.FORBIDDEN);
            // 返回处理完成的response对象
            return response.setComplete();
        }
        // 4.用户鉴权（判断 ak、sk 是否合法）
        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        String nonce = headers.getFirst("nonce");
        String timestamp = headers.getFirst("timestamp");
        String sign = headers.getFirst("sign");
        String body = headers.getFirst("body");
        body = new String(body.getBytes(StandardCharsets.ISO_8859_1),StandardCharsets.UTF_8);
        // todo 实际从数据库中查用户数据
        if (!accessKeyInDb.equals(accessKey)) {
            return handleNoAuth(response);
        }
        // 直接检验随机数是否大于10000
        if (Long.parseLong(nonce) > 10000) {
            return handleNoAuth(response);
        }
        // 校验与当前时间不超过5分钟
        if ((System.currentTimeMillis() / 1000) - Long.parseLong(timestamp) > 60*5L) {
            return handleNoAuth(response);
        }
        // todo 实际从数据库中查出secretKey
        String generateSign = SignGenerator.generateSign(body, secretKeyInDb);
        if (!sign.equals(generateSign)) {
            return handleNoAuth(response);
        }
        // 5.请求的模拟接口是否存在？
        // 6.请求转发，调用模拟接口
        Mono<Void> filter = chain.filter(exchange);
        // 7.响应日志
        log.info("响应："+response.getStatusCode());
        // 8.调用成功，接口调用次数 +1
        if (response.getStatusCode() == HttpStatus.OK) {

        } else {
            // 9.调用失败，返回一个规范的错误
            handleInvokeError(response);
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }

    public Mono<Void> handleNoAuth(ServerHttpResponse response) {
        // 设置响应状态码403 FORBIDDEN（禁止访问）
        response.setStatusCode(HttpStatus.FORBIDDEN);
        // 返回处理完成的response对象
        return response.setComplete();
    }

    public Mono<Void> handleInvokeError(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }
}