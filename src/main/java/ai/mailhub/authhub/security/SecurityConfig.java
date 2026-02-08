package ai.mailhub.authhub.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity){

        return httpSecurity
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // only needed for browsers
                .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/actuator/**", "/test/**", "/oauth/**").permitAll()
                .pathMatchers("/h2/**").permitAll()
                .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(Customizer.withDefaults()))
                .build() ;

    }
}
