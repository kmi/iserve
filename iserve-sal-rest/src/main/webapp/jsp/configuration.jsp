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

<%@ include file="./include.jsp" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
    <link type="text/css" rel="stylesheet" href="<c:url value="/css/style.css"/>"/>
</head>
<body>

<h1>iServe Administration</h1>

<p>You are currently logged as root from <%= request.getRemoteHost() %>
</p>

<h2>Registry Configuration</h2>

<p>The server is currently running iServe version: ${it.iserveVersion} .</p>

<c:if test="${it.proxyHostName != null}">
    <p>Proxy details: ${it.proxyHostName}, port: ${it.proxyPort} </p>
</c:if>
<p>Documents folder: ${it.documentsFolderUri}</p>

<p>Documents path: ${it.documentsPath}</p>

<p>Services path: ${it.servicesPath}</p>

<p>Services Repository URL: ${it.servicesRepositoryUrl}</p>

<p>Data repository SPARQL URI: ${it.dataSparqlUri}</p>

<p>Data repository SPARQL Update URI: ${it.dataSparqlUpdateUri}</p>

<p>Data repository SPARQL Service URI: ${it.dataSparqlServiceUri}</p>

<h2>Elda Configuration</h2>

<p>See <a href="<c:url value="/api-config"/>">Elda's configuration</a>.</p>

<p><a href="<c:url value="admin.jsp"/>">Return to the administration page.</a></p>

<p><a href="<c:url value="index.jsp"/>">Return to the home page.</a></p>

<p><a href="<c:url value="logout.jsp"/>">Log out.</a></p>

</body>
</html>