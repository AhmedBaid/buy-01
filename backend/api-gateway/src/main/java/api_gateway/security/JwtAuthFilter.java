package api_gateway.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLE = "X-User-Role";

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLE_SELLER = "SELLER";
    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final JwtProperties jwtProperties;

    public JwtAuthFilter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        ServerHttpRequest cleanRequest = request.mutate()
                .headers(headers -> {
                    headers.remove(HEADER_USER_ID);
                    headers.remove(HEADER_USER_ROLE);
                })
                .build();

        if ("OPTIONS".equals(methodName(request)) || isPublicPath(path)) {
            return chain.filter(exchange.mutate().request(cleanRequest).build());
        }

        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "Authentication required",
                    "Missing or malformed Authorization header");
        }

        Claims claims;
        try {
            claims = parseToken(authorization.substring(BEARER_PREFIX.length()));
        } catch (Exception e) {
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized",
                    "Invalid or expired token");
        }

        String userId = claims.getSubject();
        Object roleClaim = claims.get("role");
        String role = roleClaim == null ? "" : roleClaim.toString();

        if (requiresSellerRole(methodName(request), path) && !ROLE_SELLER.equals(role)) {
            return writeError(exchange, HttpStatus.FORBIDDEN, "Forbidden",
                    "Only sellers can manage products and media");
        }

        ServerHttpRequest authenticatedRequest = cleanRequest.mutate()
                .headers(headers -> {
                    headers.set(HEADER_USER_ID, userId);
                    headers.set(HEADER_USER_ROLE, role);
                })
                .build();

        return chain.filter(exchange.mutate().request(authenticatedRequest).build());
    }

    private boolean isPublicPath(String path) {
        return path.equals("/api/auth") || path.startsWith("/api/auth/");
    }

    private String methodName(ServerHttpRequest request) {
        HttpMethod method = request.getMethod();
        return method == null ? "" : method.name();
    }

    private boolean requiresSellerRole(String method, String path) {
        if (!WRITE_METHODS.contains(method)) {
            return false;
        }
        return matches(path, "/api/products") || matches(path, "/api/media");
    }

    private boolean matches(String path, String base) {
        return path.equals(base) || path.startsWith(base + "/");
    }

    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Mono<Void> writeError(ServerWebExchange exchange, HttpStatus status, String error, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"timestamp\":\"" + Instant.now() + "\"," +
                "\"status\":" + status.value() + "," +
                "\"error\":\"" + error + "\"," +
                "\"message\":\"" + message + "\"," +
                "\"path\":\"" + exchange.getRequest().getPath().value() + "\"}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
