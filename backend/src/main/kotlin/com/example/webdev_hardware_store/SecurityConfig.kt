package com.example.webdev_hardware_store
import com.example.webdev_hardware_store.service.JwtAuthenticationFilter
import com.example.webdev_hardware_store.config.CustomUserDetailsService
import com.example.webdev_hardware_store.config.JwtUtil
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfigurationSource

@Configuration
@EnableMethodSecurity
class SecurityConfig(
    private val jwtUtil: JwtUtil,
    private val userDetailsService: CustomUserDetailsService,
    private val jwtAuthFilter: JwtAuthenticationFilter,
    private val corsConfigurationSource: CorsConfigurationSource
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager

    // Отключаем авторегистрацию фильтра как servlet-фильтра — он управляется только через Security chain
    @Bean
    fun jwtFilterRegistration(): FilterRegistrationBean<JwtAuthenticationFilter> {
        val bean = FilterRegistrationBean(jwtAuthFilter)
        bean.isEnabled = false
        return bean
    }

    @Bean
    fun filterChain(http: org.springframework.security.config.annotation.web.builders.HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource) }
            // CSRF не нужен: токен хранится в localStorage и передаётся через
            // Authorization: Bearer — браузер не добавляет его автоматически,
            // поэтому cross-site запросы с чужого домена не проходят аутентификацию.
            .csrf { it.disable() }
            .headers { headers ->
                headers.contentSecurityPolicy {
                    it.policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data: blob:; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "font-src 'self' data:; " +
                        "connect-src 'self'; " +
                        "frame-ancestors 'none'"
                    )
                }
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it
                    .requestMatchers(HttpMethod.POST, "/auth/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/categories/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                    .requestMatchers("/error").permitAll()
                    .requestMatchers("/actuator/health", "/actuator/info", "/actuator/metrics").permitAll()
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, _ ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                }
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}