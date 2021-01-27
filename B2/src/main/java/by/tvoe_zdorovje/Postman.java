package by.tvoe_zdorovje;

import com.mailjet.client.*;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Emailv31;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONArray;
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

import static by.tvoe_zdorovje.StorageManager.*;

public class Postman extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger("Postman");

    private static final MailjetClient client = new MailjetClient(getMailjetKey(), getMailjetValue(), new ClientOptions("v3.1"));

    private static final String HTML_BODY_TEMPLATE =
            "<h3>Новое сообщение с сайта arsmajor.by</h3><br><br><h4>Имя: <b>name</b><br>Телефон: <b>phone</b><br>Сообщение:</h4><br>message";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String origin = new StringBuilder()
                .append(req.getScheme())
                .append("://")
                .append(req.getServerName().replace("backend.", ""))
                .append(req.getContextPath())
                .toString();
        resp.setHeader("Access-Control-Allow-Origin", origin);
        try {
            Map<String, String> fields = new HashMap<>();
            List<JSONObject> files = new ArrayList<>();
            readBody(req, fields, files);
            gc();
            Postman.sendEmail(fields, files);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Something went wrong.", e);
            resp.sendError(520, "Что-то пошло не так...");
        } finally {
            gc();
        }
    }

    /**
     * Reads request body and create JSONObject list of files and Map of fields.
     *
     * @see <a href='https://stackoverflow.com/questions/50791064/cant-read-multipart-data-from-httpservletrequest-in-servlet-3-0'></a>
     **/
    private void readBody(HttpServletRequest req, Map<String, String> fields, List<JSONObject> files) throws IOException, FileUploadException {
        LOGGER.info("Read body [e-mail]");
        ServletFileUpload upload = new ServletFileUpload();
        FileItemIterator iterator = upload.getItemIterator(req);
        while (iterator.hasNext()) {
            FileItemStream item = iterator.next();
            try (InputStream stream = item.openStream()) {
                if (item.isFormField()) {
                    String fieldName = item.getFieldName();
                    fields.put(fieldName, new String(stream.readAllBytes()));
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
            gc();
        }
    }

    /**
     * Sends e-mail via MailJet API
     * @see <a href='https://dev.mailjet.com/email/guides/send-api-v31/#send-with-attached-files'></a>
     **/
    public static void sendEmail(Map<String, String> fields, List<JSONObject> files) throws MailjetSocketTimeoutException, MailjetException {
        LOGGER.info("Send e-mail.");

        MailjetRequest request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(buildMessage(fields, files)));
        for (int i = 0; i < 5; i++) {
            System.gc();
        }
        MailjetResponse response = client.post(request);

        int status = response.getStatus();
        if (status < 200 || status > 299) throw new MailjetException(response.getData().toString());
    }

    /**
     * Builds JSONObject message from fields & files.
     **/
    private static JSONObject buildMessage(Map<String, String> fields, List<JSONObject> files) {
        LOGGER.info("Build body.");

        String HTML = HTML_BODY_TEMPLATE;

        for (Map.Entry<String, String> entry : fields.entrySet()) {
            HTML = HTML.replace(entry.getKey(), entry.getValue());
        }

        JSONObject message = new JSONObject()
                .put(Emailv31.Message.FROM, new JSONObject()
                        .put("Email", getMailjetFrom())
                        .put("Name", "arsmajor.by"))
                .put(Emailv31.Message.TO, new JSONArray()
                        .put(new JSONObject()
                                .put("Email", getMailjetTo())
                                .put("Name", "Roma")))
                .put(Emailv31.Message.SUBJECT, "[arsmajor.by] Новое сообщение")
                .put(Emailv31.Message.HTMLPART, HTML);

        if (!fields.isEmpty()) {
            JSONArray attachments = new JSONArray();
            files.forEach(attachments::put);
            message.put(Emailv31.Message.ATTACHMENTS, attachments);
        }

        return message;
    }

    private void gc() {
        Runtime runtime = Runtime.getRuntime();
        for (int i = 0; i < 3; i++) {
            runtime.gc();
        }
    }
}
