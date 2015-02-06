<%--
  ~ Copyright (c) 2013. Knowledge Media Institute - The Open University
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<%@ include file="include.jsp" %>

<html>
<head>
    <link type="text/css" rel="stylesheet" href="<c:url value="
    /css/style.css"/>"/>
    <title>Apache Shiro Quickstart</title>
</head>
<body>

<h1>Apache Shiro Quickstart</h1>

<p>Hi <shiro:guest>Guest</shiro:guest><shiro:user><shiro:principal/></shiro:user>!
    ( <shiro:user><a href="<c:url value="logout.jsp"/>">Log out</a></shiro:user>
    <shiro:guest><a href="<c:url value="login.jsp"/>">Log in</a> (sample accounts provided)</shiro:guest> )
</p>

<p>Welcome to the Apache Shiro Quickstart sample application.
    This page represents the home page of any web application.</p>

<shiro:user><p>Visit your account page.</p></shiro:user>
<shiro:hasRole name="admin">
    <p>Visit the <a href="<c:url value="/registry" />">server administration page</a></p>
</shiro:hasRole>

<h2>Roles</h2>

<p>To show some taglibs, here are the roles you have and don't have. Log out and log back in under different user
    accounts to see different roles.</p>

<h3>Roles you have</h3>

<p>
    <shiro:hasRole name="admin">admin<br/></shiro:hasRole>
    <shiro:hasRole name="user">user<br/></shiro:hasRole>
</p>

<h3>Roles you DON'T have</h3>

<p>
    <shiro:lacksRole name="admin">admin<br/></shiro:lacksRole>
    <shiro:lacksRole name="user">user<br/></shiro:lacksRole>
</p>


</body>
</html>
