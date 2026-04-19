package pt.unl.fct.iadi.novaevents.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${app.jwt.secret}") private val jwtSecret: String,
    @Value("\${app.jwt.expiration-seconds:28800}") private val expirationSeconds: Long
) {

    fun generateToken(authentication: Authentication): String {
        val now = Instant.now()
        val expiry = now.plus(Duration.ofSeconds(expirationSeconds))

        val roles = authentication.authorities.map(GrantedAuthority::getAuthority)

        return Jwts.builder()
            .subject(authentication.name)
            .claim("roles", roles)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(signingKey())
            .compact()
    }

    fun parseClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(signingKey())
            .build()
            .parseSignedClaims(token)
            .payload

    private fun signingKey(): SecretKey =
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret))
}

