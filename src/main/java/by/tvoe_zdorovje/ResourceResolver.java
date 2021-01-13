package by.tvoe_zdorovje;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResourceResolver extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger("ResourceResolver");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        String resource = req.getServletPath() + req.getPathInfo();

        LOGGER.info("[GET] resources: " + resource);

        if (resource.endsWith("/")) { // is folder
            resp.setContentType("application/json");

            try (PrintWriter writer = resp.getWriter();) {
                String jsonResourceList = StorageManager.getResourceList(resource.substring(1));
                writer.write(jsonResourceList);
            } catch (Exception e) {
                resp.sendError(500, "Что-то пошло не так...");
                LOGGER.log(Level.WARNING, "[POST] code = 500. ", e);
            }
        } else {
            String location = StorageManager.BUCKET_URL + resource;
            LOGGER.info("Redirect to " + location);
            resp.sendRedirect(location);
        }
    }
}
