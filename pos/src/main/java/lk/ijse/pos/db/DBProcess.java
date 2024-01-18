package lk.ijse.pos.db;

import lk.ijse.pos.dto.CustomerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.SQLException;

public class DBProcess {
    final static Logger logger = LoggerFactory.getLogger(DBProcess.class);
    private static final String SAVE_CUSTOMER_DATA = "INSERT INTO customer (customer_id, name, address, salary) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_CUSTOMER_DATA = "UPDATE customer SET name=?, address=?, salary=? WHERE customer_id=?";
    private static final String DELETE_CUSTOMER_DATA = "DELETE FROM customer WHERE customer_id=?";

    public void saveNewCustomer(CustomerDTO customer, Connection connection) {
        logger.info("Start saveNewCustomer method.");
        try {
            var ps = connection.prepareStatement(SAVE_CUSTOMER_DATA);
            ps.setString(1, customer.getCustomer_id());
            ps.setString(2, customer.getName());
            ps.setString(3, customer.getAddress());
            ps.setDouble(4, customer.getSalary());

            if (ps.executeUpdate() != 0) {
                logger.info("Customer saved.");
            } else {
                logger.error("Failed to save the Customer.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateCustomer(CustomerDTO customer, Connection connection) {
        logger.info("Start updateCustomer method.");
        try {
            var ps = connection.prepareStatement(UPDATE_CUSTOMER_DATA);
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getAddress());
            ps.setDouble(3, customer.getSalary());
            ps.setString(4, customer.getCustomer_id());

            if (ps.executeUpdate() != 0) {
                logger.info("Customer updated.");
            } else {
                logger.error("Failed to update the Customer.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteCustomer(CustomerDTO customer, Connection connection) {
        try {
            var ps = connection.prepareStatement(DELETE_CUSTOMER_DATA);
            ps.setString(1, customer.getCustomer_id());

            if (ps.executeUpdate() != 0) {
                logger.info("Customer deleted.");
            } else {
                logger.error("Failed to delete the Customer.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}