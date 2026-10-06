package api_gateway.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

class JwtAuthFilterTest {

    private static final String SECRET = "test-secret-key-for-jwt-filter-tests-0123456789";

    private JwtAuthFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(new JwtProperties(SECRET));
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    private String token(String subject, String role, long expirationOffsetMs) {
        return Jwts.builder()
                .setSubject(subject)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationOffsetMs))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    private ServerWebExchange exchange(HttpMethod method, String path, Consumer<HttpHeaders> headers) {
        MockServerHttpRequest.BaseBuilder<?> builder = MockServerHttpRequest.method(method, path);
        if (headers != null) {
            HttpHeaders custom = new HttpHeaders();
            headers.accept(custom);
            builder.headers(custom);
        }
        return MockServerWebExchange.from(builder.build());
    }

    @Test
    void publicAuthPathWithoutTokenIsAllowed() {
        ServerWebExchange exchange = exchange(HttpMethod.POST, "/api/auth/login", null);

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void protectedPathWithoutTokenReturns401() {
        ServerWebExchange exchange = exchange(HttpMethod.GET, "/api/users/profile", null);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    void nonBearerAuthorizationHeaderReturns401() {
        ServerWebExchange exchange = exchange(HttpMethod.GET, "/api/users/profile",
                headers -> headers.set(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz"));

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    void garbageTokenReturns401() {
        ServerWebExchange exchange = exchange(HttpMethod.GET, "/api/users/profile",
                headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"));

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    void expiredTokenReturns401() {
        String expired = token("user-1", "SELLER", -3600_000L);
        ServerWebExchange exchange = exchange(HttpMethod.GET, "/api/users/profile",
                headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + expired));

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    void clientCreatingProductReturns403() {
        String clientToken = token("user-2", "CLIENT", 3600_000L);
        ServerWebExchange exchange = exchange(HttpMethod.POST, "/api/products",
                headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken));

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(chain, never()).filter(any());
    }

    @Test
    void clientUpdatingMediaReturns403() {
        String clientToken = token("user-2", "CLIENT", 3600_000L);
        ServerWebExchange exchange = exchange(HttpMethod.PUT, "/api/media/123",
                headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken));

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(chain, never()).filter(any());
    }

    @Test
    void sellerCreatingProductIsForwardedWithIdentityHeaders() {
        String sellerToken = token("seller-1", "SELLER", 3600_000L);
        ServerWebExchange exchange = exchange(HttpMethod.POST, "/api/products",
                headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + sellerToken));

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void authenticatedClientCanReadProducts() {
        String clientToken = token("user-2", "CLIENT", 3600_000L);
        ServerWebExchange exchange = exchange(HttpMethod.GET, "/api/products/all",
                headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken));

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
    }

    @Test
    void forwardedIdentityHeadersContainSubjectAndRole() {
        String sellerToken = token("seller-9", "SELLER", 3600_000L);
        ServerWebExchange exchange = exchange(HttpMethod.GET, "/api/users/me",
                headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + sellerToken));

        ServerWebExchange[] forwarded = new ServerWebExchange[1];
        when(chain.filter(any())).thenAnswer(invocation -> {
            forwarded[0] = invocation.getArgument(0);
            return Mono.empty();
        });

        filter.filter(exchange, chain).block();

        assertThat(forwarded[0]).isNotNull();
        HttpHeaders forwardedHeaders = forwarded[0].getRequest().getHeaders();
        assertThat(forwardedHeaders.getFirst(JwtAuthFilter.HEADER_USER_ID)).isEqualTo("seller-9");
        assertThat(forwardedHeaders.getFirst(JwtAuthFilter.HEADER_USER_ROLE)).isEqualTo("SELLER");
    }

    @Test
    void spoofedIdentityHeadersAreStrippedAndOverwritten() {
        String clientToken = token("user-7", "CLIENT", 3600_000L);
        ServerWebExchange exchange = exchange(HttpMethod.GET, "/api/users/me", headers -> {
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken);
            headers.set(JwtAuthFilter.HEADER_USER_ID, "someone-else");
            headers.set(JwtAuthFilter.HEADER_USER_ROLE, "SELLER");
        });

        ServerWebExchange[] forwarded = new ServerWebExchange[1];
        when(chain.filter(any())).thenAnswer(invocation -> {
            forwarded[0] = invocation.getArgument(0);
            return Mono.empty();
        });

        filter.filter(exchange, chain).block();

        assertThat(forwarded[0]).isNotNull();
        HttpHeaders forwardedHeaders = forwarded[0].getRequest().getHeaders();
        assertThat(forwardedHeaders.getFirst(JwtAuthFilter.HEADER_USER_ID)).isEqualTo("user-7");
        assertThat(forwardedHeaders.getFirst(JwtAuthFilter.HEADER_USER_ROLE)).isEqualTo("CLIENT");
    }

    @Test
    void optionsPreflightIsAllowedWithoutToken() {
        ServerWebExchange exchange = exchange(HttpMethod.OPTIONS, "/api/products", null);

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
    }
}
