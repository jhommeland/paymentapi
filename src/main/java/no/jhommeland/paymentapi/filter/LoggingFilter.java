package no.jhommeland.paymentapi.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        logger.info("Request : Method={}, Path={}", request.getMethod(), request.getRequestURI());
        filterChain.doFilter(request, response);
        long endTime = System.currentTimeMillis();
        logger.info("Response: Method={}, Path={}, ResponseCode={}, RequestTime={}ms", request.getMethod(), request.getRequestURI(), response.getStatus(), endTime - startTime);
    }
}
