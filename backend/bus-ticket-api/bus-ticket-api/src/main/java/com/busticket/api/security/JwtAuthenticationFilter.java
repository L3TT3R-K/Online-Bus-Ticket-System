package com.busticket.api.security;

import com.busticket.api.dto.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null || authorization.isBlank() || !jwtService.hasBearerToken(authorization)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Long maTK = jwtService.extractMaTK(authorization);
            filterChain.doFilter(new MaTkHeaderRequestWrapper(request, maTK), response);
        } catch (RuntimeException exception) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(response.getWriter(), new ApiResponse(false, exception.getMessage()));
        }
    }

    private static final class MaTkHeaderRequestWrapper extends HttpServletRequestWrapper {

        private final String maTK;

        private MaTkHeaderRequestWrapper(HttpServletRequest request, Long maTK) {
            super(request);
            this.maTK = String.valueOf(maTK);
        }

        @Override
        public String getHeader(String name) {
            if ("X-MaTK".equalsIgnoreCase(name)) {
                return maTK;
            }

            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if ("X-MaTK".equalsIgnoreCase(name)) {
                return Collections.enumeration(List.of(maTK));
            }

            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> headerNames = Collections.list(super.getHeaderNames());
            boolean hasMaTkHeader = headerNames.stream().anyMatch(header -> "X-MaTK".equalsIgnoreCase(header));

            if (!hasMaTkHeader) {
                headerNames.add("X-MaTK");
            }

            return Collections.enumeration(headerNames);
        }
    }
}
