package lk.ijse.pos.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lk.ijse.pos.db.DBProcess;
import lk.ijse.pos.dto.OrderdetailsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "details",urlPatterns = "/details")
public class Details extends HttpServlet {
    final static Logger logger = LoggerFactory.getLogger(Details.class);
    Connection connection;
    public void init(){
        logger.info("Init the Details Servlet");
        try {
            InitialContext ctx = new InitialContext();
            DataSource pool = (DataSource) ctx.lookup("java:comp/env/jdbc/webpos");
            System.out.println("DataSource pool: " + pool);
            this.connection = pool.getConnection();
        } catch (NamingException | SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if(req.getContentType() == null ||
            !req.getContentType().toLowerCase().startsWith("application/json")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }else{
            logger.info("Start Details Servlet doPost method.");
            Jsonb jsonb = JsonbBuilder.create();
            var dbProcess = new DBProcess();

            // Handle JSON array
            List<OrderdetailsDTO> dtoList= jsonb.fromJson(req.getReader(),new ArrayList<OrderdetailsDTO>(){
            }.getClass().getGenericSuperclass());
            dbProcess.saveOrderDetails(dtoList,connection);
            jsonb.toJson(dtoList,resp.getWriter());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("Start Details Servlet doDelete method.");
        String order_id = req.getParameter("order_id");

        if (order_id == null || order_id.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or empty 'order_id' parameter");
        } else {
            var dbProcess = new DBProcess();
            dbProcess.deleteOrderDetails(order_id, connection);
        }
    }
}