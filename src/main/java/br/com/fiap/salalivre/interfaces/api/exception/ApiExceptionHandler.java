package br.com.fiap.salalivre.interfaces.api.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.fiap.salalivre.domain.exception.ConflitoDeHorarioException;
import br.com.fiap.salalivre.domain.exception.EntidadeNaoEncontradaException;
import br.com.fiap.salalivre.domain.exception.PeriodoInvalidoException;
import br.com.fiap.salalivre.domain.exception.PermissaoNegadaException;
import br.com.fiap.salalivre.domain.exception.RegraDeNegocioException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ResponseEntity<ApiErrorResponse> handleEntidadeNaoEncontrada(EntidadeNaoEncontradaException ex,
                                                                        HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(ConflitoDeHorarioException.class)
    public ResponseEntity<ApiErrorResponse> handleConflitoHorario(ConflitoDeHorarioException ex,
                                                                  HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(PermissaoNegadaException.class)
    public ResponseEntity<ApiErrorResponse> handlePermissaoNegada(PermissaoNegadaException ex,
                                                                  HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(PeriodoInvalidoException.class)
    public ResponseEntity<ApiErrorResponse> handlePeriodoInvalido(PeriodoInvalidoException ex,
                                                                  HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ApiErrorResponse> handleRegraDeNegocio(RegraDeNegocioException ex,
                                                                 HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidacao(MethodArgumentNotValidException ex,
                                                            HttpServletRequest request) {
        Map<String, String> detalhes = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            detalhes.put(erro.getField(), erro.getDefaultMessage());
        }
        return buildResponse(HttpStatus.BAD_REQUEST, "Campos invalidos.", request.getRequestURI(), detalhes);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                      HttpServletRequest request) {
        Map<String, String> detalhes = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violacao ->
                detalhes.put(violacao.getPropertyPath().toString(), violacao.getMessage()));
        return buildResponse(HttpStatus.BAD_REQUEST, "Parametros invalidos.", request.getRequestURI(), detalhes);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMensagemInvalida(HttpMessageNotReadableException ex,
                                                                   HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Corpo da requisicao invalido.", request.getRequestURI(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeral(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado.", request.getRequestURI(), null);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status,
                                                           String message,
                                                           String path,
                                                           Object details) {
        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                details
        );
        return ResponseEntity.status(status).body(response);
    }
}
