<%@ page contentType="text/html;Charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="" tagdir="/WEB-INF/tags"%>

<%-- 

Author: Hugo Thunnissen, Marco Bergsma

Via deze view kunnen de thema's beheerd worden

--%>

<html lang="en">


	<div id="left-selection-bar-wrapper">
		<section id="left-selection-bar">
			<header> 
				<h3>Thema selectie</h3>
			</header>
			<%-- --------------------------- LIJST MET THEMA'S -------------------------------- --%>
			${themes}
			<button class="bottom-button" type="button" onclick="showPopup('theme-creation-popup');">Nieuw thema</button>
		</section>
	</div>
	<div id="left-selection-bar-clearance"></div>

	<section id="middle-section">
		<header>
			<h3 id="photo-section-header-text" > Foto's in ${theme}</h3>
		</header><%--
		--%><section id="photo-section"><form class="floating-image" action="/photoselection" method="get">
					<div  class="photo-container">
						<img class="photo" height="150" src="${context}/webroot/icons/plus.jpg">
					</div>
					<button>Voeg een foto toe</button>
				</form><%--
				--%>${photos}
		</section>
		<%-- ---------------------- LIJST MET FOTO'S --------------------- --%>
	</section>

	<%--<section id="right-section">
		<%-- -------- MUZIEK -------
		${music}
	</section>--%>

	<script> selectedThemeId = '${selectedThemeId}'; </script>
	<form id="theme-creation-popup" class="hidden-popup" method="POST" action="/addtheme">
		<header>
			<h3>Maak een thema aan</h3>
		</header>
		<p>Geef het thema een naam:</p>
		<input name="theme-name" id="theme-name" placeholder="Thema-naam" type="text"/>
		<br><br>
		<button type="button" value="Maak thema" name="maak-thema" id="maak-thema" onclick="addTheme();" >Maak thema</button>
		<button name="annuleer" type="button" id="annuleer" onclick="hidePopup('theme-creation-popup');">Annuleer</button>
	</form>
</html>

