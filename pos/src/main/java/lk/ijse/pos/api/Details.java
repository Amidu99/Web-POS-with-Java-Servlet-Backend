package lk.ijse.pos.api;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
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
import java.io.StringWriter;
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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("Start Details Servlet doGet method.");
        String order_id = req.getParameter("order_id");

        if (order_id == null || order_id.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or empty 'order_id' parameter");
        } else {
            System.out.println("Start doGet all order details method..");
            var dbProcess = new DBProcess();
            List<OrderdetailsDTO> orderdetailsDTOList = dbProcess.getAllOrderDetails(order_id, connection);

            if (orderdetailsDTOList != null && !orderdetailsDTOList.isEmpty()) {
                // Create a JSON array for all items
                JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
                for (OrderdetailsDTO dto : orderdetailsDTOList) {
                    JsonObject jsonObject = Json.createObjectBuilder()
                            .add("order_id", dto.getOrder_id())
                            .add("item_code", dto.getItem_code())
                            .add("description", dto.getDescription())
                            .add("unit_price", dto.getUnit_price())
                            .add("get_qty", dto.getGet_qty())
                            .build();
                    jsonArrayBuilder.add(jsonObject);
                }
                // Convert the JSON array to a string
                StringWriter stringWriter = new StringWriter();
                try (JsonWriter jsonWriter = Json.createWriter(stringWriter)) {
                    jsonWriter.writeArray(jsonArrayBuilder.build());
                }
                // Write the JSON string to the response
                var writer = resp.getWriter();
                writer.println(stringWriter.toString());
            }
        }
    }
}