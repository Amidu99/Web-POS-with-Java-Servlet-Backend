package lk.ijse.pos.api;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lk.ijse.pos.db.DBProcess;
import lk.ijse.pos.dto.CustomerDTO;
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

@WebServlet(name = "customers",urlPatterns = "/customers")
public class Customers extends HttpServlet {
    final static Logger logger = LoggerFactory.getLogger(Customers.class);
    Connection connection;

    public void init() {
        logger.info("Init the Customer Servlet");
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
        if (req.getContentType() == null ||
            !req.getContentType().toLowerCase().startsWith("application/json")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            logger.info("Start Customer Servlet doPost method.");
            Jsonb jsonb = JsonbBuilder.create();
            var customerDTO = jsonb.fromJson(req.getReader(), CustomerDTO.class);
            var dbProcess = new DBProcess();
            dbProcess.saveNewCustomer(customerDTO, connection);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (req.getContentType() == null ||
            !req.getContentType().toLowerCase().startsWith("application/json")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            logger.info("Start Customer Servlet doPut method.");
            Jsonb jsonb = JsonbBuilder.create();
            var customerDTO = jsonb.fromJson(req.getReader(), CustomerDTO.class);
            var dbProcess = new DBProcess();
            dbProcess.updateCustomer(customerDTO, connection);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (req.getContentType() == null ||
            !req.getContentType().toLowerCase().startsWith("application/json")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            logger.info("Start Customer Servlet doDelete method.");
            Jsonb jsonb = JsonbBuilder.create();
            var customerDTO = jsonb.fromJson(req.getReader(), CustomerDTO.class);
            var dbProcess = new DBProcess();
            dbProcess.deleteCustomer(customerDTO, connection);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String customer_id = req.getParameter("customer_id");
        String search_term = req.getParameter("search_term");

        if (customer_id == null || customer_id.isEmpty() || search_term == null || search_term.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or empty parameter");
        } else if (customer_id.equalsIgnoreCase("get_all") && search_term.equalsIgnoreCase("get_all")) {
            getAllCustomers(resp);
        } else if (search_term.equalsIgnoreCase("use_only_id")) {
            getOneCustomer(customer_id, resp);
        } else if (search_term.equalsIgnoreCase("get_next_id")) {
            getNextCustomerID(resp);
        } else {
            searchCustomers(search_term, resp);
        }
    }

    private void getAllCustomers(HttpServletResponse resp) throws IOException {
        logger.info("Start Customer Servlet getAllCustomers method.");
        var dbProcess = new DBProcess();
        List<CustomerDTO> customerDTOList = dbProcess.getAllCustomers(connection);

        if (customerDTOList != null && !customerDTOList.isEmpty()) {
            // Create a JSON array for all customers
            JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
            for (CustomerDTO customer : customerDTOList) {
                JsonObject jsonObject = Json.createObjectBuilder()
                        .add("customer_id", customer.getCustomer_id())
                        .add("name", customer.getName())
                        .add("address", customer.getAddress())
                        .add("salary", customer.getSalary())
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
            logger.info("No customers found.");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No customers found");
        }
    }

    private void getOneCustomer(String customer_id, HttpServletResponse resp) throws IOException {
        logger.info("Start Customer Servlet getOneCustomer method.");
        var dbProcess = new DBProcess();
        CustomerDTO customer = dbProcess.getCustomer(customer_id, connection);

        if (customer != null) {
            // Create a JSON object for a single customer
            JsonObject jsonObject = Json.createObjectBuilder()
                    .add("customer_id", customer.getCustomer_id())
                    .add("name", customer.getName())
                    .add("address", customer.getAddress())
                    .add("salary", customer.getSalary())
                    .build();

            StringWriter stringWriter = new StringWriter();
            try (JsonWriter jsonWriter = Json.createWriter(stringWriter)) {
                jsonWriter.writeObject(jsonObject);
            }
            var writer = resp.getWriter();
            writer.println(stringWriter.toString());
        } else {
            logger.info("No customer found.");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void getNextCustomerID(HttpServletResponse resp) throws IOException {
        logger.info("Start Customer Servlet getNextCustomerID method.");
        var dbProcess = new DBProcess();
        String next_customer_id = dbProcess.getNextCustomerID(connection);
        if (next_customer_id != null) {
            JsonObject jsonObject = Json.createObjectBuilder().add("next_customer_id", next_customer_id).build();
            StringWriter stringWriter = new StringWriter();
            try (JsonWriter jsonWriter = Json.createWriter(stringWriter)) {
                jsonWriter.writeObject(jsonObject);
            }
            var writer = resp.getWriter();
            writer.println(stringWriter.toString());
        } else {
            logger.info("Next Customer ID not generated.");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void searchCustomers(String search_term, HttpServletResponse resp) throws IOException {
        logger.info("Start Customer Servlet searchCustomers method.");
        var dbProcess = new DBProcess();
        List<CustomerDTO> customerDTOList = dbProcess.getSearchCustomers(search_term, connection);

        if (customerDTOList != null && !customerDTOList.isEmpty()) {
            JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
            for (CustomerDTO customer : customerDTOList) {
                JsonObject jsonObject = Json.createObjectBuilder()
                        .add("customer_id", customer.getCustomer_id())
                        .add("name", customer.getName())
                        .add("address", customer.getAddress())
                        .add("salary", customer.getSalary())
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
            logger.info("No customers found.");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No customers found");
        }
    }
}