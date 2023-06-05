package ru.evsyukov.app.api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig implements WebMvcConfigurer {

    @Value("${nginx.host}")
    private String allowHost;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowHost)
                .allowedMethods("*")
                .allowCredentials(true); //говорим что примем куки. Сессию создает наш сервер и отдает в ответе на /login
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.cors().and().csrf().disable()
                .authorizeRequests()
                .antMatchers("/report/*", "/check/*").
                    permitAll()
                .antMatchers("/admin/*")
                .hasRole("ADMIN")
                .anyRequest()
                .authenticated()
                .and()
                    .formLogin()
                .successHandler((req, resp, auth) -> resp.setStatus(HttpServletResponse.SC_OK))
                .failureHandler((req, resp, auth) -> resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED))
                .permitAll()
                .and()
                .logout(LogoutConfigurer::permitAll).build();
    }

    //temp solution for development
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user =
                User.withDefaultPasswordEncoder()
                        .username("u")
                        .password("p")
                        .roles("ADMIN")
                        .build();

        return new InMemoryUserDetailsManager(user);
    }

}
