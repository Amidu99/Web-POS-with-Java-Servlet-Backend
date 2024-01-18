package lk.ijse.pos.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.sql.DataSource;
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
}