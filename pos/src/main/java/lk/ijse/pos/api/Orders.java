package lk.ijse.pos.api;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lk.ijse.pos.db.DBProcess;
import lk.ijse.pos.dto.OrderinfoDTO;
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
import java.util.List;

@WebServlet(name = "orders",urlPatterns = "/orders")
public class Orders extends HttpServlet {
    final static Logger logger = LoggerFactory.getLogger(Orders.class);
    Connection connection;
    public void init(){
        logger.info("Init the Orders Servlet");
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
            logger.info("Start Order Servlet doPost method.");
            Jsonb jsonb = JsonbBuilder.create();
            var orderinfoDTO = jsonb.fromJson(req.getReader(), OrderinfoDTO.class);
            var dbProcess = new DBProcess();
            dbProcess.saveNewOrder(orderinfoDTO,connection);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if(req.getContentType() == null ||
            !req.getContentType().toLowerCase().startsWith("application/json")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }else{
            logger.info("Start Order Servlet doPut method.");
            Jsonb jsonb = JsonbBuilder.create();
            var orderinfoDTO = jsonb.fromJson(req.getReader(), OrderinfoDTO.class);
            var dbProcess = new DBProcess();
            dbProcess.updateOrder(orderinfoDTO, connection);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if(req.getContentType() == null ||
            !req.getContentType().toLowerCase().startsWith("application/json")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }else{
            logger.info("Start Order Servlet doDelete method.");
            Jsonb jsonb = JsonbBuilder.create();
            var orderinfoDTO = jsonb.fromJson(req.getReader(), OrderinfoDTO.class);
            var dbProcess = new DBProcess();
            dbProcess.deleteOrder(orderinfoDTO, connection);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("Start Order Servlet doGet method.");
        String order_id = req.getParameter("order_id");
        String search_term = req.getParameter("search_term");

        if (order_id == null || order_id.isEmpty() || search_term == null || search_term.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or empty parameter");
        } else if (order_id.equalsIgnoreCase("get_all") && search_term.equalsIgnoreCase("get_all")) {
            getAllOrders(resp);
        } else if (search_term.equalsIgnoreCase("use_only_id")){
            getOneOrder(order_id, resp);
        } else if (search_term.equalsIgnoreCase("get_next_id")){
            getNextOrderID(resp);
        } else {
            searchOrders(search_term, resp);
        }
    }

    private void getAllOrders(HttpServletResponse resp) throws IOException {
        logger.info("Start Order Servlet getAllOrders method.");
        var dbProcess = new DBProcess();
        List<OrderinfoDTO> orderinfoDTOList = dbProcess.getAllOrders(connection);

        if (orderinfoDTOList != null && !orderinfoDTOList.isEmpty()) {
            // Create a JSON array for all items
            JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
            for (OrderinfoDTO order : orderinfoDTOList) {
                JsonObject jsonObject = Json.createObjectBuilder()
                        .add("order_id", order.getOrder_id())
                        .add("date", String.valueOf(order.getDate()))
                        .add("customer_id", order.getCustomer_id())
                        .add("discount", order.getDiscount())
                        .add("total", order.getTotal())
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
        } else {
            logger.info("No orders found");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No orders found");
        }
    }

    private void getOneOrder(String order_id, HttpServletResponse resp) throws IOException {
        logger.info("Start Order Servlet getOneOrder method.");
        var dbProcess = new DBProcess();
        OrderinfoDTO order = dbProcess.getOrder(order_id, connection);

        if (order != null) {
            // Create a JSON object for a single item
            JsonObject jsonObject = Json.createObjectBuilder()
                    .add("order_id", order.getOrder_id())
                    .add("date", String.valueOf(order.getDate()))
                    .add("customer_id", order.getCustomer_id())
                    .add("discount", order.getDiscount())
                    .add("total", order.getTotal())
                    .build();
            StringWriter stringWriter = new StringWriter();
            try (JsonWriter jsonWriter = Json.createWriter(stringWriter)) {
                jsonWriter.writeObject(jsonObject);
            }
            var writer = resp.getWriter();
            writer.println(stringWriter.toString());
        } else {
            logger.info("Order not found");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void getNextOrderID(HttpServletResponse resp) throws IOException {
        logger.info("Start Order Servlet getNextOrderID method.");
        var dbProcess = new DBProcess();
        String next_order_id = dbProcess.getNextOrderID(connection);
        if(next_order_id!=null){
            JsonObject jsonObject = Json.createObjectBuilder().add("next_order_id", next_order_id).build();
            StringWriter stringWriter = new StringWriter();
            try (JsonWriter jsonWriter = Json.createWriter(stringWriter)) {
                jsonWriter.writeObject(jsonObject);
            }
            var writer = resp.getWriter();
            writer.println(stringWriter.toString());
        } else {
            logger.info("Next order id not generated.");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void searchOrders(String search_term, HttpServletResponse resp) throws IOException {
        logger.info("Start Order Servlet searchOrders method.");
        var dbProcess = new DBProcess();
        List<OrderinfoDTO> orderinfoDTOList = dbProcess.getSearchOrders(search_term, connection);

        if (orderinfoDTOList != null && !orderinfoDTOList.isEmpty()) {
            // Create a JSON array for all items
            JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
            for (OrderinfoDTO order : orderinfoDTOList) {
                JsonObject jsonObject = Json.createObjectBuilder()
                        .add("order_id", order.getOrder_id())
                        .add("date", String.valueOf(order.getDate()))
                        .add("customer_id", order.getCustomer_id())
                        .add("discount", order.getDiscount())
                        .add("total", order.getTotal())
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
        } else {
            logger.info("No orders found");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No orders found");
        }
    }
}