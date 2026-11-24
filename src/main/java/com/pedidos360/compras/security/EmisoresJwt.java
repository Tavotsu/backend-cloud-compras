package com.pedidos360.compras.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

// Valida tokens de Entra ID (Microsoft) y de Cognito (AWS)
@Component
public class EmisoresJwt {

    private final Map<String, AuthenticationManager> managers;

    public EmisoresJwt(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String entraIssuer,
            @Value("${spring.security.oauth2.resourceserver.jwt.audiences}") String entraAudience,
            @Value("${app.security.cognito.issuer-uri}") String cognitoIssuer,
            @Value("${app.security.cognito.client-id}") String cognitoClientId) {
        managers = Map.of(
                entraIssuer, manager(entraIssuer, entraAudience, "roles", "preferred_username"),
                cognitoIssuer, manager(cognitoIssuer, cognitoClientId, "cognito:groups", "email"));
    }

    // Elige el validador según el emisor (iss) del token
    public AuthenticationManagerResolver<HttpServletRequest> resolver() {
        return new JwtIssuerAuthenticationManagerResolver(managers::get);
    }

    private static AuthenticationManager manager(String issuer, String audience, String claimRoles, String claimCorreo) {
        // El emisor se consulta en la primera petición: si no responde al arrancar,
        // el servicio levanta igual y lo vuelve a intentar con el siguiente token
        JwtDecoder decoder = new DecodificadorPerezoso(() -> {
            NimbusJwtDecoder nimbus = JwtDecoders.fromIssuerLocation(issuer);
            nimbus.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                    JwtValidators.createDefaultWithIssuer(issuer),
                    new JwtClaimValidator<List<String>>("aud", aud -> aud != null && aud.contains(audience))));
            return nimbus;
        });

        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName(claimRoles);
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(roles);
        converter.setPrincipalClaimName(claimCorreo);

        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(decoder);
        provider.setJwtAuthenticationConverter(converter);
        return provider::authenticate;
    }

    // Crea el decodificador real cuando llega el primer token y luego lo reutiliza
    private static final class DecodificadorPerezoso implements JwtDecoder {

        private final Supplier<JwtDecoder> fabrica;
        private volatile JwtDecoder delegado;

        DecodificadorPerezoso(Supplier<JwtDecoder> fabrica) {
            this.fabrica = fabrica;
        }

        @Override
        public Jwt decode(String token) throws JwtException {
            JwtDecoder actual = delegado;
            if (actual == null) {
                synchronized (this) {
                    if (delegado == null) {
                        try {
                            delegado = fabrica.get();
                        } catch (RuntimeException e) {
                            throw new JwtException("No se pudo obtener la configuración del emisor del token", e);
                        }
                    }
                    actual = delegado;
                }
            }
            return actual.decode(token);
        }
    }
}
