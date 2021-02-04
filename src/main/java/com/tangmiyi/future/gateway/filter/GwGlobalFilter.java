package com.tangmiyi.future.gateway.filter;

import com.tangmiyi.future.gateway.consts.GatewayConstant;
import com.tangmiyi.future.gateway.util.IPUtils;
import com.tangmiyi.future.gateway.util.JwtTokenUtil;
import com.tangmiyi.future.gateway.util.MD5Util;
import com.tangmiyi.future.gateway.util.SHASignUtil;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局过滤器
 */
@Component
@Slf4j
@RefreshScope
public class GwGlobalFilter implements GlobalFilter, Ordered {

    /**
     * sign过期时间毫秒
     */
    private final static long SIGN_TIME_OUT = 15 * 60 * 1000L;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 单机网关限流用一个ConcurrentHashMap来存储 bucket，
     * 如果是分布式集群限流的话，可以采用 Redis等分布式解决方案
     */
    private static final Map<String, Bucket> LOCAL_CACHE = new ConcurrentHashMap<>();

    /**
     * 桶的最大容量，即能装载 Token 的最大数量
     */
    @Value("${rate.limit.maxCapacity:500}")
    private Integer maxCapacity;

    /**
     * 每次 客户端 Token 补充量
     */
    @Value("${rate.limit.refillTokens:300}")
    private Integer refillTokens;

    @Value("${spring.profiles.active}")
    private String active;

    @Value("${jwt.token.head:Bearer}")
    private String tokenHead;

    @Value("${encryption.client.token:jfslkjflksjfsklfjksl@#()fjio13")
    private String encryptionToken;

    /**
     * 补充 Token 的时间间隔
     */
    private Duration refillDuration = Duration.ofMinutes(1);

    private Bucket createNewBucket(Integer refillCount) {
        Refill refill = Refill.of(refillCount, refillDuration);
        Bandwidth limit = Bandwidth.classic(maxCapacity, refill);
        return Bucket4j.builder().addLimit(limit).build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        MultiValueMap<String, String> paramters = request.getQueryParams();
        Integer refillCount = refillTokens;
        // 先限流
        // request.getRemoteAddress().getAddress().getHostAddress();
        String ip = IPUtils.getIpAddress(request);
        Bucket bucket = LOCAL_CACHE.computeIfAbsent(ip, k -> createNewBucket(refillCount));
        log.info("ip:{} ,availableTokens:{} ", ip, bucket.getAvailableTokens());
        if (!bucket.tryConsume(1)) {
            // 当可用的令牌书为0时，进行限流返回429状态码
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return response.setComplete();
        }
        // Route gatewayUrl = exchange.getRequiredAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        // URI uri = gatewayUrl.getUri();
        // 登录或swagger不做任何校验
        if(request.getPath().value().contains(GatewayConstant.MANAGER_PATH_LOGIN) ||
                request.getPath().value().contains(GatewayConstant.SWAGGERDO_PATH_DOC)){
            return chain.filter(exchange);
        }

        if (request.getPath().value().contains(GatewayConstant.REQUEST_PATH_ACTUATOR)
                || request.getPath().value().contains(GatewayConstant.REQUEST_PATH_OPEN)) {
            String sign = StringUtils.isNotBlank(headers.getFirst(GatewayConstant.REQUEST_PARAMETER_SIGN)) ? headers.getFirst(GatewayConstant.REQUEST_PARAMETER_SIGN) : paramters.getFirst(GatewayConstant.REQUEST_PARAMETER_SIGN);
            String stamp = StringUtils.isNotBlank(headers.getFirst(GatewayConstant.REQUEST_PARAMETER_STAMP)) ? headers.getFirst(GatewayConstant.REQUEST_PARAMETER_STAMP) : paramters.getFirst(GatewayConstant.REQUEST_PARAMETER_STAMP);
            String nonce = StringUtils.isNotBlank(headers.getFirst(GatewayConstant.REQUEST_PARAMETER_NONCE)) ? headers.getFirst(GatewayConstant.REQUEST_PARAMETER_NONCE) : paramters.getFirst(GatewayConstant.REQUEST_PARAMETER_NONCE);
            log.info(sign+"|"+stamp+"|"+nonce);
            if (StringUtils.isBlank(sign) || StringUtils.isBlank(stamp) || StringUtils.isBlank(nonce)) {
                log.info("sign,stamp,once must be not null");
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
            // 先检验时间
            Long bewteenTime = System.currentTimeMillis() - Long.parseLong(stamp);
            if (Math.abs(bewteenTime) > SIGN_TIME_OUT) {
                log.info("sign is overdue:{}", bewteenTime);
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
            String encrypted = SHASignUtil.sign(encryptionToken, stamp, nonce);
            log.info("encrypted: {} ; sign: {}", encrypted, sign);
            if (!StringUtils.upperCase(encrypted).equals(StringUtils.upperCase(sign))) {
                log.info("sign is failure");
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
        }
        // 做登录过滤校验
        else{
            // 是否登录
            boolean logined = false;
            String jwtToken = jwtTokenUtil.getTokenFromHttpHead(headers);
            // 兼容地址栏带token
            if(StringUtils.isBlank(jwtToken)){

                if(!CollectionUtils.isEmpty(paramters.get(GatewayConstant.ADDR_AUTHORIZATION))){
                    jwtToken = paramters.get(GatewayConstant.ADDR_AUTHORIZATION).get(0).substring(tokenHead.length());
                }
            }
            String applicationName = GatewayConstant.AUDIENCE_MANAGER;
            String env = GatewayConstant.ENV_DEV;
            if(active.contains(GatewayConstant.ENV_UAT)){
                env = GatewayConstant.ENV_UAT;
            }
            if(active.contains(GatewayConstant.ENV_PROD)){
                env = GatewayConstant.ENV_PROD;
            }
            if(StringUtils.isNotBlank(jwtToken)){
                Claims claims = jwtTokenUtil.verifyJWT(jwtToken,applicationName,env);
                // claims为空则未带jwtToken
                if(claims != null){
                    logined = true;
                }
            }
            if(!logined){
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    public static void main(String[] args) {
        String encryType = "SHA-1";
        String nonce = "123";
        System.out.println("nonce:"+nonce);
        String encryptionToken = "jfslkjflksjfsklfjksl@#()fjio13";
        String currentTime = System.currentTimeMillis()+"";
        System.out.println("stamp:"+currentTime);
        String encrypted;
        if(encryType.equals("SHA-1")) {
            encrypted = SHASignUtil.sign(encryptionToken,currentTime,nonce);
            System.out.println("sign:"+encrypted);
        }
        if(encryType.equals("MD5")) {
            encrypted = MD5Util.getMD5(currentTime + nonce  + encryptionToken);
            System.out.println(encrypted);
        }
    }
}