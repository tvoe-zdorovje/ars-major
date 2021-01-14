package by.tvoe_zdorovje;

import com.mailjet.client.Base64;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static by.tvoe_zdorovje.StorageManager.getPassword;

public class General extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger("General");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.info("[GET] Forward ars_major.jsp");
        req.getRequestDispatcher("/WEB-INF/ars_major.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.info("[POST]");
        resp.setCharacterEncoding("UTF-8");
        try {
            Consumer<String> validatePassword = (password) -> {
                LOGGER.info("Verification...");
                boolean passIsValid = BCrypt.checkpw(password, getPassword());
                if (!passIsValid) {
                    throw new SecurityException("Wrong password!");
                }
                LOGGER.info("Verification OK");
            };

            String adminParam = req.getParameter("admin");
            if ("false".equals(adminParam)) {
                    validatePassword.accept(req.getReader().readLine());
                    return;
            }
            Map<String, String> fields = new HashMap<>();
            List<JSONObject> files = new ArrayList<>();
            readBody(req, fields, files);

            if ("true".equals(adminParam)) {
                validatePassword.accept(fields.getOrDefault("password", ""));
                LOGGER.info("Upload resources.");
                StorageManager.uploadResources(fields.get("theme"), files);
            } else {
                LOGGER.info("Send email.");
                Postman.sendEmail(fields, files);
            }
        } catch (SecurityException e) {
            LOGGER.warning(e.getMessage());
            resp.sendError(403, e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[POST] code = 500. ", e);
            resp.sendError(500, "Что-то пошло не так...");
        }
    }

    // https://stackoverflow.com/questions/50791064/cant-read-multipart-data-from-httpservletrequest-in-servlet-3-0
    private void readBody(HttpServletRequest req, Map<String, String> fields, List<JSONObject> files) throws IOException, FileUploadException {
        LOGGER.info("Read body...");
        ServletFileUpload upload = new ServletFileUpload();
        FileItemIterator iterator = upload.getItemIterator(req);
        while (iterator.hasNext()) {
            FileItemStream item = iterator.next();
            try (InputStream stream = item.openStream()) {
                if (item.isFormField()) {
                    fields.put(item.getFieldName(), new String(stream.readAllBytes()));
                } else {
                    if (stream.available() > 0) {
                        JSONObject file = new JSONObject()
                                .put("ContentType", item.getContentType())
                                .put("Filename", item.getName())
                                .put("Base64Content", Base64.encode(stream.readAllBytes()));
                        files.add(file);
                    }
                }
            }
        }
    }
}
