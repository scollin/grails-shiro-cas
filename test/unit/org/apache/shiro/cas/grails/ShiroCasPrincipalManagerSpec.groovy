package org.apache.shiro.cas.grails

import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.cas.CasToken
import spock.lang.Specification

class ShiroCasPrincipalManagerSpec extends Specification {
    void "Principal management works"() {
        setup:
        def upToken = newNonCasToken(1)
        def casToken1 = newCasToken(2)
        def casToken2 = newCasToken(3)

        when: "no tokens have been seen"
        then: "no principals are known to CAS"
        [upToken, casToken1, casToken2, null].every {!ShiroCasPrincipalManager.isFromCas(it?.principal)}

        when: "null or non-CAS tokens are seen"
        ShiroCasPrincipalManager.rememberPrincipalForToken(null)
        ShiroCasPrincipalManager.rememberPrincipalForToken(upToken)

        then: "no principals are known to CAS"
        [upToken, casToken1, casToken2, null].every {!ShiroCasPrincipalManager.isFromCas(it?.principal)}

        when: "a CAS token is seen"
        ShiroCasPrincipalManager.rememberPrincipalForToken(casToken1)

        then: "only that principal is known to CAS"
        [upToken, casToken2, null].every {!ShiroCasPrincipalManager.isFromCas(it?.principal)}
        ShiroCasPrincipalManager.isFromCas(casToken1.principal)

        when: "a principal is forgotten"
        ShiroCasPrincipalManager.forgetPrincipal(casToken1.principal)

        then: "it is no longer known to CAS"
        [upToken, casToken1, casToken2, null].every {!ShiroCasPrincipalManager.isFromCas(it?.principal)}

        when: "principals that aren't known to CAS are forgotten"
        ShiroCasPrincipalManager.forgetPrincipal(upToken.principal)
        ShiroCasPrincipalManager.forgetPrincipal(casToken1.principal)
        ShiroCasPrincipalManager.forgetPrincipal(null)

        then: "it doesn't do anything"
    }

    private static AuthenticationToken newNonCasToken(int num) {
        return new UsernamePasswordToken("user" + num, "password" + num)
    }

    private static AuthenticationToken newCasToken(int num) {
        def token = new CasToken("ticket" + num)
        token.userId = "user" + num
        return token
    }
}
