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
import java.io.PrintWriter;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static by.tvoe_zdorovje.StorageManager.getPassword;

public class General extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger("General");
    private String token = generateToken();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.info("[GET] Forward ars_major.jsp");
        gc();
        req.getRequestDispatcher("/WEB-INF/ars_major.jsp").forward(req, resp);
    }

    private static final Consumer<String> validatePassword = (password) -> {
        LOGGER.info("Verification password");
        boolean passIsValid = BCrypt.checkpw(password, getPassword());
        if (!passIsValid) {
            throw new SecurityException("Wrong password!");
        }
        LOGGER.info("Verification OK");
    };

    /**
     * Sends e-mail or uploads images to storage.
     * Uploading: if {adminMode} param is "false" - validates password and generate token, else checks token and start uploading.
     * Sends number of uploaded image if http code is 500 or 200;
     *
     * @see General#uploadImages(HttpServletRequest)
     * @see ImageProcessor#process(InputStream, boolean, boolean)
     * @see StorageManager#uploadResources(String, byte[])
     * Sending e-mail: read body and sends.
     * @see General#readBody(HttpServletRequest, Map, List)
     * @see Postman#sendEmail(Map, List)
     **/
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            gc();
            String adminParam = req.getParameter("admin");

            LOGGER.info("[POST] " + (adminParam == null ? "Send email" : "upload image. AdminMode = " + adminParam));
            resp.setCharacterEncoding("UTF-8");

            if ("false".equals(adminParam)) {
                validatePassword.accept(req.getReader().readLine());
                token = generateToken();
                try (PrintWriter writer = resp.getWriter()) {
                    writer.write(token);
                }
                return;
            }

            if ("true".equals(adminParam)) {
                if (!token.equals(req.getParameter("token"))) {
                    throw new SecurityException("Invalid token.");
                }
                try (PrintWriter writer = resp.getWriter()) {
                    final int uploadImages = uploadImages(req);

                    resp.resetBuffer();
                    resp.setContentType("text/plain");

                    if (uploadImages <= 0) {
                        resp.setStatus(500);
                    }

                    writer.write(String.valueOf(uploadImages));
                    resp.flushBuffer();
                }
            } else {
                Map<String, String> fields = new HashMap<>();
                List<JSONObject> files = new ArrayList<>();
                readBody(req, fields, files);
                gc();
                Postman.sendEmail(fields, files);
            }
        } catch (SecurityException e) {
            LOGGER.warning(e.getMessage());
            resp.sendError(403, e.getMessage());
        } catch (IllegalStateException e) {
            LOGGER.log(Level.WARNING, "", e);
            resp.sendError(400, e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Something went wrong.", e);
            resp.sendError(520, "Что-то пошло не так...");
        } finally {

            gc();
        }
    }

    /**
     * Reads request body and create JSONObject list of files.
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
     * Reads the body, processes the images and uploads them.
     *
     * @return number of uploaded images
     * @throws IllegalStateException - if value of the {theme} param is not valid or no files found.
     * @throws SecurityException     - if token is not valid.
     * @see ImageProcessor#process(InputStream, boolean, boolean)
     * @see StorageManager#uploadResources(String, byte[])
     **/
    private int uploadImages(HttpServletRequest req) throws IOException, FileUploadException {
        LOGGER.info("Start upload images. Read body... ");

        boolean hasError = false;

        String theme = req.getParameter("theme");
        switch (theme) {
            case "art-painting":
            case "dec-plaster":
            case "bas-relief":
                break;
            default:
                throw new IllegalStateException("Bad request: unexpected value of theme parameter: " + theme);
        }

        int numberOfUploadedImages = 0;
        ImageProcessor processor = new ImageProcessor();

        FileItemIterator iterator = new ServletFileUpload().getItemIterator(req);
        while (iterator.hasNext()) {
            FileItemStream item = iterator.next();

            if (!item.isFormField()) {
                if ("files".equals(item.getFieldName())) {
                    try (InputStream inputStream = item.openStream()) {

                        LOGGER.info("Process image: " + item.getName());
                        byte[] processedImage = processor.process(inputStream, true, false);
                        numberOfUploadedImages += StorageManager.uploadResources(theme, processedImage);

                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Something went wrong.", e);
                        hasError = true;
                    }
                }
            }
            gc();
        }
        LOGGER.info("Images upload completed: " + numberOfUploadedImages);
        return hasError ? -numberOfUploadedImages : numberOfUploadedImages;
    }

    private String generateToken() {
        LOGGER.info("Generate token.");
        byte[] bytes = new byte[5];
        new Random().nextBytes(bytes);
        return BCrypt.hashpw(new String(bytes), BCrypt.gensalt());
    }

    private void gc() {
        Runtime runtime = Runtime.getRuntime();
        for (int i = 0; i < 5; i++) {
            runtime.gc();
        }
    }
}
