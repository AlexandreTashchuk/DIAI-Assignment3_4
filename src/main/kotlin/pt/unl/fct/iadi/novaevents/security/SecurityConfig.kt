package pt.unl.fct.iadi.novaevents.security

import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.savedrequest.CookieRequestCache
import org.springframework.security.web.context.NullSecurityContextRepository
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.config.http.SessionCreationPolicy

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val jpaUserDetailsManager: JpaUserDetailsManager,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val jwtAuthSuccessHandler: JwtAuthSuccessHandler
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationProvider(passwordEncoder: PasswordEncoder): AuthenticationProvider {
        val provider = DaoAuthenticationProvider()
        provider.setUserDetailsService(jpaUserDetailsManager)
        provider.setPasswordEncoder(passwordEncoder)
        return provider
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { csrf ->
                csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .securityContext { context ->
                context.securityContextRepository(NullSecurityContextRepository())
            }
            .requestCache { cache -> cache.requestCache(CookieRequestCache()) }
            .authenticationProvider(authenticationProvider(passwordEncoder()))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/error/**").permitAll()
                    .requestMatchers("/login").permitAll()
                    .requestMatchers(HttpMethod.GET, "/clubs", "/clubs/*", "/events", "/clubs/*/events/*").permitAll()
                    .requestMatchers(HttpMethod.GET, "/clubs/*/events/new", "/clubs/*/events/*/edit").hasAnyRole("EDITOR", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/clubs/*/events").hasAnyRole("EDITOR", "ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/clubs/*/events/*").hasAnyRole("EDITOR", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/clubs/*/events/*/delete").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/clubs/*/events/*").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .formLogin { form ->
                form
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .successHandler(jwtAuthSuccessHandler)
                    .failureHandler { _, response, _ ->
                        response.sendRedirect("/login?error")
                    }
                    .permitAll()
            }
            .logout { logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessHandler { _, response, _ ->
                        response.addCookie(clearJwtCookie())
                        response.status = HttpServletResponse.SC_FOUND
                        response.sendRedirect("/login?logout")
                    }
                    .permitAll()
            }
            .exceptionHandling { ex ->
                ex.authenticationEntryPoint(LoginUrlAuthenticationEntryPoint("/login"))
            }

        return http.build()
    }

    private fun clearJwtCookie() = jakarta.servlet.http.Cookie("jwt", "").apply {
        isHttpOnly = true
        path = "/"
        maxAge = 0
    }
}

