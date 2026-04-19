package pt.unl.fct.iadi.novaevents.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (SecurityContextHolder.getContext().authentication == null) {
            val token = request.cookies
                ?.firstOrNull { it.name == "jwt" }
                ?.value

            if (!token.isNullOrBlank()) {
                runCatching {
                    val claims = jwtService.parseClaims(token)
                    val username = claims.subject
                    @Suppress("UNCHECKED_CAST")
                    val roles = claims["roles"] as? List<String> ?: emptyList()
                    val authorities = roles.map { SimpleGrantedAuthority(it) }

                    val authentication = UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                    )
                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        }

        filterChain.doFilter(request, response)
    }
}

