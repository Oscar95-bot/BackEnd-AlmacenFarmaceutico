package com.almacen.farmaceutico.AlmacenFarmaceutico.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.Map;
import java.util.TreeMap;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ResponseEntity<?> negocio(ApiException ex) { return error(ex.getStatus(), ex.getMessage()); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new TreeMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("mensaje", "Revise los datos ingresados.", "campos", campos));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<?> formato(Exception ex) { return error(HttpStatus.BAD_REQUEST, "Formato de datos o parámetros inválido."); }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> integridad(Exception ex) { return error(HttpStatus.CONFLICT, "El registro está duplicado o tiene referencias incompatibles."); }

    @ExceptionHandler({CannotAcquireLockException.class, OptimisticLockingFailureException.class})
    ResponseEntity<?> concurrencia(Exception ex) { return error(HttpStatus.CONFLICT, "Otra operación está actualizando el inventario. Intente nuevamente."); }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<?> autenticacion(Exception ex) { return error(HttpStatus.UNAUTHORIZED, "Credenciales inválidas o usuario inactivo."); }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<?> permiso(Exception ex) { return error(HttpStatus.FORBIDDEN, "No tiene permiso para realizar esta operación."); }

    @ExceptionHandler({IllegalArgumentException.class, ArithmeticException.class})
    ResponseEntity<?> argumento(Exception ex) { return error(HttpStatus.BAD_REQUEST, "Cantidad o valor inválido."); }

    @ExceptionHandler(Exception.class)
    ResponseEntity<?> inesperado(Exception ex) {
        log.error("Error inesperado en la API", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo completar la operación.");
    }

    private ResponseEntity<?> error(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(Map.of("mensaje", mensaje, "estado", status.value()));
    }
}
