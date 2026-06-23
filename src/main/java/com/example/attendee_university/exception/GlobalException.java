package com.example.attendee_university.exception;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalException {

    private ProblemDetail build(
            HttpStatus status, String title, String detail, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("about:blank"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        List<Map<String, String>> errors =
                e.getBindingResult().getFieldErrors().stream()
                        .map(fe -> Map.of("field", fe.getField(), "message", fe.getDefaultMessage()))
                        .collect(Collectors.toList());

        ProblemDetail problem =
                build(
                        HttpStatus.BAD_REQUEST,
                        "Validation Failed",
                        "One or more fields are invalid.",
                        request);
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException e, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Not Found", e.getMessage(), request);
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ProblemDetail handleConflict(DuplicateUserException e, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "Conflict", e.getMessage(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ProblemDetail handleBadRequest(BadRequestException e, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", e.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleInvalidJson(
            HttpMessageNotReadableException e, HttpServletRequest request) {

        Throwable cause = e.getMostSpecificCause();
        String message = cause.getMessage();

        if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidDefinitionException
                || cause instanceof com.fasterxml.jackson.databind.exc.MismatchedInputException) {
            String causeMsg = message != null ? message : "";

            if (causeMsg.contains("Cannot deserialize value of type")) {
                String field = "unknown";
                com.fasterxml.jackson.databind.exc.MismatchedInputException mie =
                        (cause instanceof com.fasterxml.jackson.databind.exc.MismatchedInputException m) ? m : null;
                if (mie != null && mie.getPath() != null && !mie.getPath().isEmpty()) {
                    field = mie.getPath().stream()
                            .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : "[" + ref.getIndex() + "]")
                            .collect(Collectors.joining("."));
                }

                String expected = "unknown";
                if (causeMsg.contains("`java.lang.String`"))  expected = "string";
                else if (causeMsg.contains("`java.lang.Integer`") || causeMsg.contains("`java.lang.Long`")) expected = "integer";
                else if (causeMsg.contains("`java.lang.Boolean`")) expected = "boolean";
                else if (causeMsg.contains("`java.util.UUID`"))    expected = "UUID";

                String detail = String.format("Field '%s' must be a %s.", field, expected);
                ProblemDetail problem = build(HttpStatus.BAD_REQUEST, "Invalid Field Type", detail, request);
                problem.setProperty("errors", List.of(Map.of("field", field, "message", detail)));
                return problem;
            }
        }
        if (message != null && message.contains("not one of the values accepted for Enum class")) {
            return build(HttpStatus.BAD_REQUEST, "Invalid Enum Value", "Invalid enum value provided.", request);
        }
        if (message != null && message.contains("UUID")) {
            return build(HttpStatus.BAD_REQUEST, "Malformed Request",
                    "One or more fields require a valid UUID format (e.g. 123e4567-e89b-12d3-a456-426614174000).", request);
        }
        return build(HttpStatus.BAD_REQUEST, "Malformed Request",
                "The request body could not be read. Please check your JSON format.", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleUnauthorized(AuthenticationException e, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Unauthorized", e.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleForbidden(AccessDeniedException e, HttpServletRequest request) {
        return build(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "You do not have permission to access this resource.",
                request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception e, HttpServletRequest request) {
        log.error("[General Error]: {}", e.getMessage());
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred.",
                request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(
            ConstraintViolationException e, HttpServletRequest request) {
        List<Map<String, String>> errors =
                e.getConstraintViolations().stream()
                        .map(
                                v -> {
                                    String path = v.getPropertyPath().toString();
                                    String field =
                                            path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                                    return Map.of("field", field, "message", v.getMessage());
                                })
                        .toList();

        ProblemDetail problem =
                build(
                        HttpStatus.BAD_REQUEST,
                        "Validation Failed",
                        "One or more fields are invalid.",
                        request);
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String requiredType =
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "valid value";
        String message =
                String.format("Invalid value '%s' for parameter '%s'.", e.getValue(), e.getName());
        return build(HttpStatus.BAD_REQUEST, "Bad Request", message, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(
            IllegalArgumentException e, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", e.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ProblemDetail handleForbiddenCustom(ForbiddenException e, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Forbidden", e.getMessage(), request);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail handleMethodValidation(
            HandlerMethodValidationException e, HttpServletRequest request) {
        try {
            List<Map<String, String>> errors =
                    e.getValueResults().stream()
                            .flatMap(
                                    result ->
                                            result.getResolvableErrors().stream()
                                                    .map(
                                                            error -> {
                                                                String field = result.getMethodParameter().getParameterName();
                                                                String message = error.getDefaultMessage();
                                                                return Map.of(
                                                                        "field", field != null ? field : "unknown",
                                                                        "message", message != null ? message : "Invalid value");
                                                            }))
                            .toList();
            ProblemDetail problem =
                    build(
                            HttpStatus.BAD_REQUEST,
                            "Validation Failed",
                            "One or more fields are invalid.",
                            request);
            problem.setProperty("errors", errors);
            return problem;
        } catch (Exception ex) {
            return build(
                    HttpStatus.BAD_REQUEST, "Validation Failed", "One or more fields are invalid.", request);
        }
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(
            NoResourceFoundException e, HttpServletRequest request) {
        return build(
                HttpStatus.NOT_FOUND, "Not Found", "Endpoint not found: " + e.getResourcePath(), request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        return build(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Method Not Allowed",
                "Method '" + e.getMethod() + "' is not supported for this endpoint.",
                request);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ProblemDetail handleNoHandlerFound(NoHandlerFoundException e, HttpServletRequest request) {
        return build(
                HttpStatus.NOT_FOUND, "Not Found", "Endpoint not found: " + e.getRequestURL(), request);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ProblemDetail handleResourceAccess(ResourceAccessException e, HttpServletRequest request) {
        log.error("[Network Error]: {}", e.getMessage());
        return build(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Service Unavailable",
                "Cannot connect to external service. Please try again later.",
                request);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ProblemDetail handleHttpClientError(
            HttpClientErrorException e, HttpServletRequest request) {
        log.error(
                "[External API Client Error]: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
        if (e.getStatusCode().value() == 401) {
            return build(
                    HttpStatus.BAD_REQUEST,
                    "Invalid Credentials",
                    "Invalid WeBill365 credentials. Please update your Client ID and Secret.",
                    request);
        }
        return build(
                HttpStatus.BAD_REQUEST,
                "External API Error",
                "External service returned an error: " + e.getStatusCode(),
                request);
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ProblemDetail handleHttpServerError(
            HttpServerErrorException e, HttpServletRequest request) {
        log.error(
                "[External API Server Error]: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
        return build(
                HttpStatus.SERVICE_UNAVAILABLE,
                "External Service Error",
                "WeBill365 server error. Please try again later.",
                request);
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<?> handleInternalAuth(
            InternalAuthenticationServiceException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(build(HttpStatus.UNAUTHORIZED, "Unauthorized", e.getMessage(), request));
    }
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParam(
            MissingServletRequestParameterException e, HttpServletRequest request) {
        return build(
                HttpStatus.BAD_REQUEST,
                "Missing Parameter",
                "Required parameter '" + e.getParameterName() + "' is missing.",
                request);
    }

    @ExceptionHandler(MissingRequestValueException.class)
    public ProblemDetail handleMissingRequestValue(
            MissingRequestValueException e, HttpServletRequest request) {
        return build(
                HttpStatus.BAD_REQUEST,
                "Missing Request Value",
                e.getMessage(),
                request);
    }

    @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class)
    public ProblemDetail handleDuplicateKey(
            org.springframework.dao.DuplicateKeyException e, HttpServletRequest request) {
        log.warn("[Duplicate Key]: {}", e.getMessage());
        return build(HttpStatus.CONFLICT, "Conflict", "Duplicate entry detected.", request);
    }

    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public ProblemDetail handleDataAccess(
            org.springframework.dao.DataAccessException e, HttpServletRequest request) {
        log.error("[DB Error]: {}", e.getMessage());
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Database Error",
                "An unexpected database error occurred.",
                request);
    }
}
