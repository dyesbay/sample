package app.base.controllers;

import app.base.constants.GErrors;
import app.base.exceptions.*;
import app.base.models.GResponse;
import app.base.objects.GPair;
import app.base.objects.IGEnum;
import app.base.services.GContextService;
import app.base.utils.ExceptionUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.Path;
import java.util.*;

import static app.base.constants.GErrors.*;


@ControllerAdvice
@AllArgsConstructor
@NoArgsConstructor
public class GControllerAdvice {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    protected GContextService contexts;

    private void logHex(GException ex) {
        logger.error(ex.getDescription(), ex);
    }

    protected String getError(String code) {
        if (code.contains(" ")) {
            return code;
        }
        return GErrors.valueOf(code).getValue();
    }

    protected String getError(IGEnum code) {
        return code.getValue();
    }

    private Map<String, Object> getErrorMap(List<GPair> pairs) {
        if (pairs == null) return null;
        Map<String, Object> data = new HashMap<>();
        pairs.stream().filter(p -> p.getValue() != null).forEach(pair -> data.put(pair.getKey(), pair.getValue()));
        return data;
    }

    private List<GPair> getErrorFields(ConstraintViolationException ex) {
        Map<String, String> fields = new HashMap<>();
        ex.getConstraintViolations()
                .forEach(cv -> {
                    Iterator<Path.Node> nodes = cv.getPropertyPath().iterator();
                    StringBuilder key = new StringBuilder();
                    while (nodes.hasNext()) {
                        Path.Node node = nodes.next();
                        if ((ElementKind.PROPERTY.equals(node.getKind())
                                || ElementKind.PARAMETER.equals(node.getKind()))
                                && node.getName() != null) {
                            if (node.getIndex() != null) {
                                key.append("[").append(node.getIndex()).append("].");
                            }
                            key.append(node.getName());
                        }

                        if (ElementKind.BEAN.equals(node.getKind()) && node.getIndex() != null) {
                            key.append("[").append(node.getIndex()).append("]");
                        }
                    }
                    fields.put(key.toString(), getError(cv.getMessage()));
                });
        logger.error("ConstraintViolationException  {}: {}", FIELDS_INVALID_FORMAT, fields);
        return Collections.singletonList(new GPair("fields", fields));
    }

    private List<GPair> getErrorFields(BindException ex) {
        Map<String, String> fields = new HashMap<>();

        if (ex.getFieldErrors() != null && !ex.getFieldErrors().isEmpty()) {
            ex.getFieldErrors().stream().filter(Objects::nonNull).forEach(error ->
                    fields.put(error.getField(), getError(error.getDefaultMessage()))
            );
        } else if (ex.getFieldError() != null) {
            fields.put(ex.getFieldError().getField(), getError(ex.getFieldError().getCode()));
        }

        logger.error("BindException  {}: {}", FIELDS_INVALID_FORMAT, fields);
        return Collections.singletonList(new GPair("fields", fields));

    }

    private List<GPair> getErrorFields(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new HashMap<>();

        if (ex.getBindingResult() != null && ex.getBindingResult().getFieldErrors() != null) {
            ex.getBindingResult().getFieldErrors().stream().filter(Objects::nonNull).forEach(error ->
                    fields.put(error.getField(), getError(error.getDefaultMessage()))
            );
        }
        logger.error("MethodArgumentNotValidException {}: {}", FIELDS_INVALID_FORMAT, fields);
        return Collections.singletonList(new GPair("fields", fields));
    }

    protected List<GPair> getErrorFields(List<GPair> pairs) {
        Map<String, Object> fields = getErrorMap(pairs);
        logger.error("Exception  {}: {}", FIELDS_INVALID_FORMAT, fields);
        return Collections.singletonList(new GPair("fields", fields));
    }

    protected List<GPair> getErrorFields(GPair... pairs) {
        return getErrorFields(Arrays.asList(pairs));
    }

    protected GResponse getResponse(IGEnum code, String message, List<GPair> pairs) {
        contexts.setErrorCode(code.name());
        return GResponse.builder()
                .code(code.name())
                .message(message != null ? message : getError(code))
                .data(getErrorMap(pairs))
                .build();
    }

    protected GResponse getResponse(IGEnum code, List<GPair> pairs) {
        return getResponse(code, getError(code), pairs);
    }

    protected GResponse getResponse(IGEnum code, GPair... pairs) {
        return getResponse(code, Arrays.asList(pairs));
    }

    protected GResponse getResponse(IGEnum code, String message) {
        return getResponse(code, message, Collections.emptyList());
    }

    protected GResponse getResponse(IGEnum code) {
        return getResponse(code, Collections.emptyList());
    }

    protected GResponse getResponse(GException ex) {
        List<GPair> pairs = ex.getPairs() != null ? ex.getPairs() : new ArrayList<>();
        return getResponse(ex.getCode(), ex.getText(), pairs);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public GResponse handleException(HttpRequestMethodNotSupportedException ex) {
        return getResponse(NOT_FOUND, new GPair("method", ex.getMethod()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public GResponse handleException(MissingServletRequestParameterException ex) {
        return getResponse(FIELD_REQUIRED, getErrorFields(new GPair(ex.getParameterName(), getError(FIELD_REQUIRED))));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ServletRequestBindingException.class)
    public GResponse handleException(ServletRequestBindingException ex) {
        return getResponse(FIELD_REQUIRED, new GPair("exception", ex.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public GResponse handleException(HttpMessageNotReadableException ex) {
        return getResponse(FIELDS_INVALID_FORMAT, new GPair("exception", ex.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public GResponse handleException(ConstraintViolationException ex) {
        return getResponse(FIELDS_INVALID_FORMAT, getErrorFields(ex));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public GResponse handleException(MethodArgumentNotValidException ex) {
        try {
            return getResponse(FIELDS_INVALID_FORMAT, getErrorFields(ex));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(FIELDS_INVALID_FORMAT);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public GResponse handleException(BindException ex) {
        try {
            String message = FIELDS_INVALID_FORMAT.getValue();
            if (ex.getGlobalError() != null) {
                message = ex.getGlobalError().getDefaultMessage();
            }
            return getResponse(FIELDS_INVALID_FORMAT, message, getErrorFields(ex));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(FIELDS_INVALID_FORMAT);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public GResponse handleException(MethodArgumentTypeMismatchException ex) {
        try {
            return getResponse(FIELDS_INVALID_FORMAT, getErrorFields(new GPair(ex.getName(), FIELDS_INVALID_FORMAT.getValue())));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getResponse(FIELDS_INVALID_FORMAT);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(GAlreadyExists.class)
    public GResponse handleException(GAlreadyExists ex) {
        logHex(ex);
        return getResponse(ex);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(GBadRequest.class)
    public GResponse handleException(GBadRequest ex) {
        logHex(ex);
        return getResponse(ex);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(GUnauthorized.class)
    public GResponse handleException(GUnauthorized ex) {
        logHex(ex);
        return getResponse(ex);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(GNotFound.class)
    public GResponse handleException(GNotFound ex) {
        logHex(ex);
        return getResponse(ex);
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(GNotAllowed.class)
    public GResponse handleException(GNotAllowed ex) {
        logHex(ex);
        return getResponse(ex);
    }


    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(GSystemError.class)
    public GResponse handleException(GSystemError ex) {
        logHex(ex);
        return getResponse(ex);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(GException.class)
    public GResponse handleException(GException ex) {
        logHex(ex);
        return getResponse(ex);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(IllegalStateException.class)
    public GResponse handleException(IllegalStateException ex) {
        logger.error(ex.getMessage());
        return getResponse(INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public GResponse handleException(Exception ex) {
        logger.error(ExceptionUtils.getFullLog(ex));
        return getResponse(INTERNAL_SERVER_ERROR);
    }

}
