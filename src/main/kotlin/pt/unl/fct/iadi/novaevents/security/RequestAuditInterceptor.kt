package pt.unl.fct.iadi.novaevents.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class RequestAuditInterceptor : HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(RequestAuditInterceptor::class.java)

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        val authentication = SecurityContextHolder.getContext().authentication
        val principal = if (authentication != null && authentication.isAuthenticated && authentication.name != "anonymousUser") {
            authentication.name
        } else {
            "anonymous"
        }

        logger.info("[{}] {} {} [{}]", principal, request.method, request.requestURI, response.status)
    }
}

