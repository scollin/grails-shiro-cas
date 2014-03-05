@package.line@import org.apache.shiro.authc.AccountException
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.cas.CasAuthenticationException
import org.apache.shiro.cas.CasToken
import org.apache.shiro.cas.grails.ConfigUtils
import org.jasig.cas.client.authentication.AttributePrincipal
import org.jasig.cas.client.validation.TicketValidationException
import org.jasig.cas.client.validation.TicketValidator

/**
 * Simple realm that authenticates users against an CAS server.
 */
class @realm.name@ {
    static authTokenClass = org.apache.shiro.cas.CasToken

    TicketValidator casTicketValidator

    def authenticate(authToken) {
        def ticket = preValidate(authToken)
        log.info "Attempting to authenticate ${authToken.principal} in CAS realm..."
        try {
            def casAssertion = ticketValidator.validate(ticket, ConfigUtils.serviceUrl)
            def casPrincipal = casAssertion.principal
            def username = casPrincipal.name
            if (log.infoEnabled) {
                log.info("Validated ticket ${ticket} as ${username}")
                log.info("With attributes: ${casPrincipal.attributes}")
            }
            updateAuthToken(authToken, casPrincipal)
            ConfigUtils.putPrincipal(authToken)
            return new SimpleAuthenticationInfo(createApplicationPrincipal(casPrincipal), ticket, getClass().simpleName)
        } catch (TicketValidationException ex) {
            log.error("Unable to validate ticket ${ticket}", ex)
            throw new CasAuthenticationException("Unable to validate ticket ${ticket}", ex)
        }
    }

    private static Object createApplicationPrincipal(AttributePrincipal casPrincipal) {
        return casPrincipal.name // TODO: if needed, add application-specific principal logic
    }

    private static String preValidate(CasToken authToken) {
        if (authToken == null) {
            throw new AccountException("This realm requires a non-null token.")
        }
        def ticket = authToken.credentials as String
        if (!ticket) {
            throw new AccountException("No ticket found within token")
        }
        return ticket
    }

    private static void updateAuthToken(CasToken authToken, AttributePrincipal casPrincipal) {
        def attributes = casPrincipal.attributes
        authToken.setUserId(casPrincipal.name)
        def isRemembered = (attributes["longTermAuthenticationRequestTokenUsed"] as String)?.toBoolean()
        if (isRemembered) {
            authToken.setRememberMe(true)
        }
    }
}
