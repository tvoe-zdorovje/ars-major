package by.tvoe_zdorovje;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class ResourceResolver extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        String resource = req.getServletPath() + req.getPathInfo();

        if (resource.endsWith("/")) { // is folder
            resp.setContentType("application/json");

            try (PrintWriter writer = resp.getWriter();) {
                String jsonResourceList = StorageManager.getResourceList(resource.substring(1));
                writer.write(jsonResourceList);
            } catch (Exception e) {
                resp.sendError(500, "Что-то пошло не так...");
                e.printStackTrace();
            }
        } else {
        resp.sendRedirect(StorageManager.BUCKET_URL + resource);
        }
    }
}
