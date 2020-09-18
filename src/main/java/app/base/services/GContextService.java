package app.base.services;


import app.base.exceptions.GBadRequest;
import app.base.objects.GPair;
import app.base.utils.DateUtils;
import app.base.utils.EncryptionUtils;
import app.base.utils.ObjectUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static app.base.constants.GConstants.*;

@Service
public class GContextService {

    @Value("${spring.profiles.active}")
    private String profile;

    @Value("${info.app.name}")
    private String name;

    private static final List<String> KEYS = Arrays.asList(
            HEADER_SESSION,
            HEADER_USER);

    public String getProfile() {
        return profile;
    }

    public void setContext(HttpServletRequest request) {
        KEYS.forEach(key -> MDC.put(key, request.getHeader(key)));

        setPath("[" + request.getMethod() + "] " + request.getServletPath());
    }

    public void set(String name, String value) {
        MDC.put(name, value);
    }

    public void setLong(String name, Long value) {
        set(name, ObjectUtils.toStringOrNull(value));
    }

    public void setBoolean(String name, Boolean value) {
        set(name, ObjectUtils.toStringOrNull(value));
    }

    public void setUuid(String name, UUID value) {
        set(name, ObjectUtils.toStringOrNull(value));
    }

    public String get(String name) {
        return ObjectUtils.toStringOrNull(MDC.get(name));
    }

    public Long getLong(String name) {
        return ObjectUtils.parseLongOrNull(get(name));
    }

    public UUID getUuid(String name) {
        return ObjectUtils.parseUuidOrNull(get(name));
    }

    public Boolean getBoolean(String name) {
        return ObjectUtils.parseBooleanOrNull(get(name));
    }

    public void setUser(Long user) {
        setLong(HEADER_USER, user);
    }

    public String getUser() {
        return get(HEADER_USER);
    }

    public void setSession(Long session) {
        setLong(HEADER_SESSION, session);
    }

    public Long getSession() {
        return getLong(HEADER_SESSION);
    }

    public void setSessionExpired(Date date) {
        set(HEADER_SESSION_EXPIRED, DateUtils.formatSystemDateTime(date));
    }

    public Date getSessionExpired() {
        return app.base.utils.DateUtils.parseSystemDateTime(MDC.get(HEADER_SESSION_EXPIRED));
    }

    public void setDomain(String domain) {
        set(HEADER_DOMAIN, domain);
    }

    public String getDomain() {
        String domain = get(HEADER_DOMAIN);
        if (ObjectUtils.isBlank(domain)) return get("Host");
        return domain;
    }

    public String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            return "unknown";
        }
    }

    public void setPath(String path) {
        set(HEADER_PATH, path);
    }

    public String getPath() {
        return get(HEADER_PATH);
    }

    public GPair[] getUserTokenPairs() throws GBadRequest {
        List<GPair> pairs = new ArrayList<>();
        pairs.add(new GPair(HEADER_SESSION, EncryptionUtils.encrypt(ObjectUtils.toStringOrNull(getSession()))));
        pairs.add(new GPair(HEADER_USER, EncryptionUtils.encrypt(ObjectUtils.toStringOrNull(getUser()))));

        return pairs.toArray(new GPair[0]);
    }

    public void setErrorCode(String errorCode) {
        set(HEADER_ERROR_CODE, errorCode);
    }

    public String getErrorCode() {
        return get(HEADER_ERROR_CODE);
    }

    public String getContextInfo() {
        return "CONTEXT:" +
                "\n• profile: " + profile +
                "\n• destination: " + name +
                "\n• domain: " + getDomain() +
                "\n• hostname: " + getHostName() +
                "\n• path: " + getPath() +
                "\n• user: " + getUser() +
                "\n• userSession: " + getSession() +
                "\n";
    }

}
