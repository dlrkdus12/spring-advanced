package org.example.expert.domain.common.aop;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.example.expert.config.JwtUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LogAop {

    private final HttpServletRequest request;
    private final JwtUtil jwtUtil;

    @Pointcut("execution(* org.example.expert.domain.comment.controller.CommentAdminController.*(..))")
    private void deleteComment() {}
    @Pointcut("execution(* org.example.expert.domain.user.controller.UserAdminController.*(..))")
    private void changeUserRole() {}

    @Around("deleteComment() || changeUserRole()")
    public Object logPrint(ProceedingJoinPoint joinPoint) throws Throwable {

        String userId = "Anonymous";

        // 요청 헤더에서 JWT 추출
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {

            String jwt = bearerToken.substring(7);

            try {
                // JWT 유효성 검사와 클레임 추출
                Claims claims = jwtUtil.extractClaims(jwt);
                if (claims != null) {
                    userId = claims.getSubject(); // 클레임에서 사용자 ID 추출
                }
            } catch (Exception e) {
                log.error("JWT 파싱 오류: {}", e.getMessage());
            }
        }

        String requestUrl = request.getRequestURI();

        try {
            return joinPoint.proceed();
        } finally {
            log.info("요청한 사용자 ID: {}", userId);
            log.info("API 요청 시각: {}", LocalDateTime.now());
            log.info("API 요청 URL: {}", requestUrl);
        }
    }
}
