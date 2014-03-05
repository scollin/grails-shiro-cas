package org.apache.shiro.cas.grails

import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.cas.CasToken

class ShiroCasPrincipalManagerTests {
    void testPrincipalManagement() {
        def upToken = newNonCasToken(1)
        def casToken1 = newCasToken(2)
        def casToken2 = newCasToken(3)
        assert [upToken, casToken1, casToken2, null].every {!ShiroCasPrincipalManager.isFromCas(it?.principal)}
        ShiroCasPrincipalManager.rememberPrincipalForToken(null)
        ShiroCasPrincipalManager.rememberPrincipalForToken(upToken)
        assert [upToken, casToken1, casToken2, null].every {!ShiroCasPrincipalManager.isFromCas(it?.principal)}
        ShiroCasPrincipalManager.rememberPrincipalForToken(casToken1)
        assert [upToken, casToken2, null].every {!ShiroCasPrincipalManager.isFromCas(it?.principal)}
        assert ShiroCasPrincipalManager.isFromCas(casToken1.principal)
        ShiroCasPrincipalManager.forgetPrincipal(casToken1.principal)
        assert [upToken, casToken1, casToken2, null].every {!ShiroCasPrincipalManager.isFromCas(it?.principal)}
        ShiroCasPrincipalManager.forgetPrincipal(upToken.principal)
        ShiroCasPrincipalManager.forgetPrincipal(casToken1.principal)
        ShiroCasPrincipalManager.forgetPrincipal(null)
    }

    private AuthenticationToken newNonCasToken(int num) {
        return new UsernamePasswordToken("user" + num, "password" + num)
    }

    private AuthenticationToken newCasToken(int num) {
        def token = new CasToken("ticket" + num)
        token.userId = "user" + num
        return token
    }
}
