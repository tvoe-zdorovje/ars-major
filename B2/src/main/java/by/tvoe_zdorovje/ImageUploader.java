package by.tvoe_zdorovje;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static by.tvoe_zdorovje.StorageManager.getPassword;

public class ImageUploader extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger("General");
    private String token = generateToken();

    private static final Consumer<String> validatePassword = (password) -> {
        LOGGER.info("Verification password");
        boolean passIsValid = BCrypt.checkpw(password, getPassword());
        if (!passIsValid) {
            throw new SecurityException("Wrong password!");
        }
        LOGGER.info("Verification OK");
    };

    /**
     * Uploads images to storage.
     * Uploading: if {adminMode} param is "false" - validates password and generate token, else checks token and start uploading.
     * Sends number of uploaded image if http code is 500 or 200;
     *
     * @see ImageUploader#uploadImages(HttpServletRequest)
     * @see ImageProcessor#process(InputStream, boolean, boolean)
     * @see StorageManager#uploadResources(String, byte[])
     **/
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String origin = new StringBuilder()
                    .append(req.getScheme())
                    .append("://")
                    .append(req.getServerName().replace("backend.", ""))
                    .append(req.getContextPath())
                    .toString();
            resp.setHeader("Access-Control-Allow-Origin", origin);

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
            } else if ("true".equals(adminParam)) {
                if (!token.equals(req.getParameter("token"))) {
                    throw new SecurityException("Invalid token.");
                }
                try (PrintWriter writer = resp.getWriter()) {
                    final int uploadImages = uploadImages(req);

                    if (uploadImages != 0) {
                        invalidateCacheRequest(origin, req.getParameter("theme"));
                    }

                    resp.resetBuffer();
                    resp.setContentType("text/plain");

                    if (uploadImages <= 0) {
                        resp.setStatus(500);
                    }

                    writer.write(String.valueOf(uploadImages));
                    resp.flushBuffer();
                }
            } else {
                throw new IllegalStateException("Bad request: admin=" + adminParam);
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

    private void invalidateCacheRequest(String origin, String theme) throws IOException, InterruptedException {
        LOGGER.info("Invalidate cache request.");
        String uri = origin + "/resources/?theme=" + theme;

        final HttpClient client = HttpClient.newHttpClient();
        final HttpRequest request = HttpRequest.newBuilder(URI.create(uri)).POST(HttpRequest.BodyPublishers.noBody()).build();

        final HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        LOGGER.info("Invalidate cache response: "+response.statusCode());
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
