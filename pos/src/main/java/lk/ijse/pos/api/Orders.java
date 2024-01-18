package lk.ijse.pos.api;

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
import java.sql.Connection;
import java.sql.SQLException;

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
}