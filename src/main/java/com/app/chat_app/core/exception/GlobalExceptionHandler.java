package com.app.chat_app.core.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.app.chat_app.core.exception.type.BusinessException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Tüm domain/business hatalarını ortak şekilde dışa veriyoruz.
     * UnauthenticatedException, UnauthorizedException, kendi yazacağın
     * NotFoundException vb. hepsi buradan geçer.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        HttpStatus status = ex.getStatus();
        ApiErrorResponse body = ApiErrorResponse.of(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        body.setCode(ex.getCode());

        // 5xx olanları log'layalım (gerçek bir hata sinyali); 4xx'leri sessiz geç.
        if (status.is5xxServerError()) {
            log.error("Business exception (5xx): {}", ex.getMessage(), ex);
        } else {
            log.debug("Business exception ({}): {}", status.value(), ex.getMessage());
        }

        return ResponseEntity.status(status).body(body);
    }

    /**
     * @Valid ile gelen body validation hataları.
     * Field-by-field hata mesajları döner.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                             HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                fieldErrors.put(fe.getField(), fe.getDefaultMessage())
        );

        ApiErrorResponse body = ApiErrorResponse.validationFailed(
                "Doğrulama hatası",
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Domain'den kaçan IllegalArgumentException'lar (entity factory validasyonları gibi).
     * 400 olarak çevir, hassas detay sızdırma.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                                  HttpServletRequest request) {
        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Domain'den kaçan IllegalStateException'lar (entity state ihlali gibi).
     * 409 Conflict daha doğru — istemcinin gönderdiği şey o anki state'le çelişiyor.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(IllegalStateException ex,
                                                               HttpServletRequest request) {
        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * Son çare: hiçbir spesifik handler yakalamadıysa.
     * Mesaj sızdırma — istemciye genel mesaj, server log'una stack trace.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnknown(Exception ex, HttpServletRequest request) {
        log.error("Beklenmeyen hata: {}", request.getRequestURI(), ex);

        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Beklenmeyen bir hata oluştu.",
                request.getRequestURI()
        );
        return ResponseEntity.internalServerError().body(body);
    }
}
