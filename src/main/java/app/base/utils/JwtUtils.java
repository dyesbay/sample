package app.base.utils;

import app.base.constants.GErrors;
import app.base.exceptions.GUnauthorized;
import app.base.objects.GPair;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtils {

    private JwtUtils() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtils.class);

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String SECRET = "FAFwqfr12";

    private static String generateToken(String subject, Date expired, String secret, GPair... pairs) {
        Map<String, Object> claims = new HashMap<>();
        if (pairs != null) {
            for (GPair pair : pairs) {
                claims.put(pair.getKey(), pair.getValue());
            }
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setExpiration(expired)
                .signWith(SignatureAlgorithm.HS512, Base64.getEncoder().encode(secret.getBytes()))
                .compact();
    }

    public static String generateUserToken(String subject, Date expired, GPair... pairs) {
        return generateToken(subject, expired, SECRET, pairs);
    }

    private static Claims getClaims(String token, String secret) throws GUnauthorized {
        if (!ObjectUtils.isBlank(token)) {
            try {
                return Jwts.parser()
                        .setSigningKey(Base64.getEncoder().encode(secret.getBytes()))
                        .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                        .getBody();
            } catch (ExpiredJwtException ex) {
                throw new GUnauthorized(GErrors.TOKEN_EXPIRED, new GPair("token", token));
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage());
            }
        }
        throw new GUnauthorized(GErrors.BAD_CREDENTIALS, new GPair("token", token));
    }

    public static Claims getUserClaims(String token) throws GUnauthorized {
        return getClaims(token, SECRET);
    }

}
