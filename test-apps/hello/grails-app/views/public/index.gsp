<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Public Content</title>
</head>

<body>
    <p>This is public content.</p>
    <shiro:authenticated>
        <p>Logged in as <shiro:principal/></p>
        <p><g:link controller="auth" action="signOut">Sign Out</g:link></p>
    </shiro:authenticated>
    <shiro:notAuthenticated>
        <p>Not logged in.</p>
    </shiro:notAuthenticated>
</body>
</html>