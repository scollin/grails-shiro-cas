<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Protected Content</title>
</head>

<body>
    <p>This is protected content.</p>
    <p>Logged in as <shiro:principal/></p>
    <p><g:link controller="auth" action="signOut">Sign Out</g:link></p>
</body>
</html>