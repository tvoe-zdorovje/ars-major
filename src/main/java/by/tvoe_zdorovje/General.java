package by.tvoe_zdorovje;

import com.mailjet.client.Base64;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONObject;

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
import java.util.logging.Level;
import java.util.logging.Logger;

public class General extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger("General");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.info("[GET] Forward ars+major.jsp");
        req.getRequestDispatcher("/WEB-INF/ars_major.jsp").forward(req, resp);
    }

    // https://stackoverflow.com/questions/50791064/cant-read-multipart-data-from-httpservletrequest-in-servlet-3-0
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.info("[POST] send email request");
        resp.setCharacterEncoding("UTF-8");
        try {
            Map<String, String> fields = new HashMap<>();
            List<JSONObject> files = new ArrayList<>();

            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iterator = upload.getItemIterator(req);
            while (iterator.hasNext()) {
                FileItemStream item = iterator.next();
                try (InputStream stream = item.openStream()) {
                    if (item.isFormField()) {
                        fields.put(item.getFieldName(), new String(stream.readAllBytes()));
                    } else {
                        if (stream.available()>0) {
                            JSONObject file = new JSONObject()
                                    .put("ContentType", item.getContentType())
                                    .put("Filename", item.getName())
                                    .put("Base64Content", Base64.encode(stream.readAllBytes()));
                            files.add(file);
                        }
                    }
                }
            }

            Postman.sendEmail(fields, files);
        } catch (Exception e) {
            resp.sendError(500, "Что-то пошло не так...");
            LOGGER.log(Level.WARNING, "[POST] code = 500. ", e);
        }
    }
}
