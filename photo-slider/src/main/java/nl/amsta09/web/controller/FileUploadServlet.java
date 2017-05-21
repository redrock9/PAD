package nl.amsta09.web.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import nl.amsta09.data.SqlConnector;
import nl.amsta09.data.SqlConnector.ThemeNotFoundException;
import nl.amsta09.driver.MainApp;
import nl.amsta09.model.Audio;
import nl.amsta09.model.Media;
import nl.amsta09.model.Photo;
import nl.amsta09.model.Theme;
import nl.amsta09.web.SessionManager.Session;
import nl.amsta09.web.SessionManager.SessionNotFoundException;

/**
 *
 * @author Hugo Thunnissen
 *
 * Deze class maakt het voor de client mogelijk om een foto te uploaden naar
 * de server.
 * 
 */

@WebServlet("/uploadphoto")
@MultipartConfig
public class FileUploadServlet extends HttpServlet { 

	protected void doGet(HttpServletRequest request, HttpServletResponse response){
		if(request.getAttribute("sessionId") == null){
			request.setAttribute("sessionId",MainApp.getSessionManager().newSession().getId());
		}

		request.setAttribute("themeId", 1);

		try {
			request.getRequestDispatcher("/WEB-INF/addPhoto.jsp").forward(request, response);
		} catch (ServletException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	/**
	 * Deze methode ontvangt een bestand in een httprequest en plaatst deze in de daarvoor bestemde map
	 * alvorens het bestand toe te voegen aan de database.
	 *
	 * @param request
	 * @param response
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException{
		response.setContentType("text/html;charset=UTF-8");

		// De sessie van de gebruiker
		String sessionId = request.getParameter("sessionId");
		Session session;
		try {
			session = MainApp.getSessionManager().getSessionById(Integer.parseInt(sessionId));
		} catch (NumberFormatException | SessionNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}

		// Het media object en de attributen waar het mee geinstantieerd wordt
		boolean savingFileSucceeded = false;
		Part filePart = null;
		String fileName;
		String destinationdir = "Resources" + File.separator;
		Media media;

		// De in- en outputstream voor het lezen en schrijven van het bestand
		OutputStream out= null;
		InputStream fileContent = null;

		// Connectie met de database
		SqlConnector conn = new SqlConnector();

		// Check om wat voor bestand het gaat, instantieer vervolgens een object van het juiste type
		if(request.getPart("file") != null){
			 System.out.println("DEBUG: Je gebruikt nog de standaardwaarde *file* voor de filepart naam");
			 return;
		}
		else if(request.getPart("photo") != null){
			filePart = request.getPart("photo");
			destinationdir += "Foto" + File.separator;;
			fileName = getFilenameFromFilePart(filePart);
			media = new Photo(destinationdir + fileName, fileName, 1);
		}
		else if(request.getPart("sound") != null){
			filePart = request.getPart("sound");
			destinationdir += "Audio" + File.separator;;
			fileName = getFilenameFromFilePart(filePart);
			media = new Audio(destinationdir + fileName, fileName, 1);
		}
		else {
			System.out.println("DEBUG: geen filepart met een geldige naam gevonden.");
			return;
		}

		// Check of er niet al een bestand bestaat met dezelfde naam
		while (conn.mediaInDatabase(media)){
			fileName = "x" + fileName;
			media.setRelativePath(destinationdir + fileName);
		}

		// Schrijf het bestand naar de juiste map
		try {
			out = new FileOutputStream(new File(media.getRelativePath()));
			fileContent = filePart.getInputStream();
			int read = 0;
			final byte[] bytes = new byte[1024];
			while ((read = fileContent.read(bytes)) != -1){
				out.write(bytes, 0, read);
			}
			savingFileSucceeded = true;
		} catch (FileNotFoundException e ){
			e.printStackTrace();
		} finally {
			if(	out != null ) {
				out.close(); 
			}
			if( fileContent != null ) {
				fileContent.close();
			}
		}

		// Check of opslaan bestand gelukt is en voeg het toe aan de database en sessie
		if(savingFileSucceeded){
			try{
				conn.insertMedia(media);
				System.out.println("ik haal nu de id op");
				System.out.println("media id is:" + conn.getMediaIdFrom(media));
				media.setId(conn.getMediaIdFrom(media));
				System.out.println("media toevoegen aan session");
				session.getAddedMedia().add(media);
				System.out.println("sessie aan het updaten");
				MainApp.getSessionManager().updateSession(session);
			}
			catch(SQLException e){
				sendErrorMessage(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Het is niet gelukt om bestand " + media.getName() + " aan de database toe te voegen");
				e.printStackTrace();
			}
		}
		else {
			sendErrorMessage(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Het is niet gelukt om bestand " + media.getName() + " op te slaan, probeer het alstublieft opnieuw");
		}
	}

	
	/**
	 * Deze methode haalt de naam van het bestand uit de content-disposition header van
	 * het filePart object
	 * @param filePart
	 * @return fileName
	 */
	private static String getFilenameFromFilePart(Part filePart) {
		String fileName = null;
		String header = filePart.getHeader("content-disposition");
		Pattern filenamePattern = Pattern.compile("filename=\"(.*)\"");
		Matcher matcher = filenamePattern.matcher(header);
		if(matcher.find()){
			fileName = matcher.group(1);
		}
		else {
			fileName = "onbekend";
		}
		return fileName;
	}

	/**
	 * Stuur een HttpResponse met een error code en een bericht.
	 * @param response
	 * @param status
	 * @param message
	 */
	private static void sendErrorMessage(HttpServletResponse response, int status, String message) throws IOException {
		response.setStatus(status);
		response.getWriter().write(message);
		response.flushBuffer();
	}
}
