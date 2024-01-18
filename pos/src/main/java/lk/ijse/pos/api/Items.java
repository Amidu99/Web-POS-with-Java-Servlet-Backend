package lk.ijse.pos.api;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lk.ijse.pos.db.DBProcess;
import lk.ijse.pos.dto.ItemDTO;
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

@WebServlet(name = "items",urlPatterns = "/items")
public class Items extends HttpServlet {
    final static Logger logger = LoggerFactory.getLogger(Items.class);
    Connection connection;
    public void init(){
        logger.info("Init the Items Servlet");
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
            logger.info("Start Item Servlet doPost method.");
            Jsonb jsonb = JsonbBuilder.create();
            var itemDTO = jsonb.fromJson(req.getReader(), ItemDTO.class);
            var dbProcess = new DBProcess();
            dbProcess.saveNewItem(itemDTO,connection);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if(req.getContentType() == null ||
            !req.getContentType().toLowerCase().startsWith("application/json")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }else{
            logger.info("Start Item Servlet doPut method.");
            Jsonb jsonb = JsonbBuilder.create();
            var itemDTO = jsonb.fromJson(req.getReader(), ItemDTO.class);
            var dbProcess = new DBProcess();
            dbProcess.updateItem(itemDTO, connection);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if(req.getContentType() == null ||
            !req.getContentType().toLowerCase().startsWith("application/json")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }else{
            logger.info("Start Item Servlet doDelete method.");
            Jsonb jsonb = JsonbBuilder.create();
            var itemDTO = jsonb.fromJson(req.getReader(), ItemDTO.class);
            var dbProcess = new DBProcess();
            dbProcess.deleteItem(itemDTO, connection);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("Start Item Servlet doGet method.");
        String item_code = req.getParameter("item_code");
        String search_term = req.getParameter("search_term");

        if (item_code == null || item_code.isEmpty() || search_term == null || search_term.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or empty parameter");
        } else if (item_code.equalsIgnoreCase("get_all") && search_term.equalsIgnoreCase("get_all")) {
            getAllItems(resp);
        } else if (search_term.equalsIgnoreCase("use_only_code")){
            getOneItem(item_code, resp);
        } else if (search_term.equalsIgnoreCase("get_next_code")){
            getNextItemCode(resp);
        } else {
            searchItems(search_term, resp);
        }
    }

    private void getAllItems(HttpServletResponse resp) throws IOException {
        logger.info("Start Item Servlet getAllItems method.");
        var dbProcess = new DBProcess();
        List<ItemDTO> itemDTOList = dbProcess.getAllItems(connection);

        if (itemDTOList != null && !itemDTOList.isEmpty()) {
            // Create a JSON array for all items
            JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
            for (ItemDTO item : itemDTOList) {
                JsonObject jsonObject = Json.createObjectBuilder()
                        .add("item_code", item.getItem_code())
                        .add("description", item.getDescription())
                        .add("unit_price", item.getUnit_price())
                        .add("qty_on_hand", item.getQty_on_hand())
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
            logger.info("No items found");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No items found");
        }
    }

    private void getOneItem(String item_code, HttpServletResponse resp) throws IOException {
        logger.info("Start Item Servlet getOneItem method.");
        var dbProcess = new DBProcess();
        ItemDTO item = dbProcess.getItem(item_code, connection);

        if (item != null) {
            // Create a JSON object for a single item
            JsonObject jsonObject = Json.createObjectBuilder()
                    .add("item_code", item.getItem_code())
                    .add("description", item.getDescription())
                    .add("unit_price", item.getUnit_price())
                    .add("qty_on_hand", item.getQty_on_hand())
                    .build();
            StringWriter stringWriter = new StringWriter();
            try (JsonWriter jsonWriter = Json.createWriter(stringWriter)) {
                jsonWriter.writeObject(jsonObject);
            }
            var writer = resp.getWriter();
            writer.println(stringWriter.toString());
        } else {
            logger.info("Item not found");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void getNextItemCode(HttpServletResponse resp) throws IOException {
        logger.info("Start Item Servlet getNextItemCode method.");
        var dbProcess = new DBProcess();
        String next_item_code = dbProcess.getNextItemCode(connection);
        if(next_item_code!=null){
            JsonObject jsonObject = Json.createObjectBuilder().add("next_item_code", next_item_code).build();
            StringWriter stringWriter = new StringWriter();
            try (JsonWriter jsonWriter = Json.createWriter(stringWriter)) {
                jsonWriter.writeObject(jsonObject);
            }
            var writer = resp.getWriter();
            writer.println(stringWriter.toString());
        } else {
            logger.info("Next item_code not generated.");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void searchItems(String search_term, HttpServletResponse resp) throws IOException {
        logger.info("Start Item Servlet searchItems method.");
        var dbProcess = new DBProcess();
        List<ItemDTO> itemDTOList = dbProcess.getSearchItems(search_term, connection);

        if (itemDTOList != null && !itemDTOList.isEmpty()) {
            // Create a JSON array for all items
            JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
            for (ItemDTO item : itemDTOList) {
                JsonObject jsonObject = Json.createObjectBuilder()
                        .add("item_code", item.getItem_code())
                        .add("description", item.getDescription())
                        .add("unit_price", item.getUnit_price())
                        .add("qty_on_hand", item.getQty_on_hand())
                        .build();
                jsonArrayBuilder.add(jsonObject);
            }
            StringWriter stringWriter = new StringWriter();
            try (JsonWriter jsonWriter = Json.createWriter(stringWriter)) {
                jsonWriter.writeArray(jsonArrayBuilder.build());
            }
            var writer = resp.getWriter();
            writer.println(stringWriter.toString());
        } else {
            logger.info("No items found");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No items found");
        }
    }
}