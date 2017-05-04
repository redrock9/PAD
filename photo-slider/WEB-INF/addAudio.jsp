<%@ page contentType="text/html;Charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="" tagdir="/WEB-INF/tags"%>

<html lang="en">
<head>
	<link href="${context}/webroot/style/mainstylesheet.css" rel="stylesheet" type="text/css" media="screen" />
	<link href="${context}/webroot/style/dropzone.css" rel="stylesheet" type="text/css" media="screen" />
	<meta charset="UTF-8">
	<title></title>
</head>

	<body>
		<%@include file="header.jsp"%>
		<h1>Voeg audio toe via deze pagina</h1>	
		<form method="POST" action="${context}/uploadaudio" enctype="multipart/form-data">
			Naam van het bestand:
			<input type="text" name="name" id="name"/>
			<br/>
			Kies een bestand:
			<input type="file" name="file" id="file" /> 
			<br/>
			<input type="submit" value="upload" name="upload" id="upload" />
		</form>

		<c:if test="${message != null}">
			<p>${message}</p>
		</c:if>
	</body>

</html>