package lk.ijse.pos.db;

import lk.ijse.pos.dto.CustomerDTO;
import lk.ijse.pos.dto.ItemDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBProcess {
    final static Logger logger = LoggerFactory.getLogger(DBProcess.class);
    private static final String SAVE_CUSTOMER_DATA = "INSERT INTO customer (customer_id, name, address, salary) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_CUSTOMER_DATA = "UPDATE customer SET name=?, address=?, salary=? WHERE customer_id=?";
    private static final String DELETE_CUSTOMER_DATA = "DELETE FROM customer WHERE customer_id=?";
    private static final String GET_ALL_CUSTOMER_DATA = "SELECT * FROM customer ORDER BY customer_id";
    private static final String GET_CUSTOMER_DATA = "SELECT name, address, salary FROM customer WHERE customer_id=?";
    private static final String GET_LAST_CUSTOMER_ID = "SELECT customer_id FROM customer ORDER BY customer_id DESC LIMIT 1";
    private static final String GET_SEARCH_CUSTOMER_DATA = "SELECT * FROM customer WHERE customer_id LIKE ? OR name LIKE ? OR address LIKE ?";
    private static final String SAVE_ITEM_DATA = "INSERT INTO item (item_code, description, unit_price, qty_on_hand) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_ITEM_DATA = "UPDATE item SET description=?, unit_price=?, qty_on_hand=? WHERE item_code=?";

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
        logger.info("Start deleteCustomer method.");
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

    public List<CustomerDTO> getAllCustomers(Connection connection) {
        logger.info("Start getAllCustomers method.");
        try {
            var ps = connection.prepareStatement(GET_ALL_CUSTOMER_DATA);
            var resultSet = ps.executeQuery();
            List<CustomerDTO> customerDTOList = new ArrayList<>();
            while (resultSet.next()) {
                String customer_id = resultSet.getString(1);
                String name = resultSet.getString(2);
                String address = resultSet.getString(3);
                double salary = resultSet.getDouble(4);
                logger.info("customer_id = " + customer_id + " name = " + name + " address = " + address + " salary = " + salary);
                System.out.println("customer_id = " + customer_id + " name = " + name + " address = " + address + " salary = " + salary);
                CustomerDTO customerDTO = new CustomerDTO(customer_id, name, address, salary);
                customerDTOList.add(customerDTO);
            }
            return customerDTOList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public CustomerDTO getCustomer(String customer_id, Connection connection) {
        logger.info("Start getCustomer method.");
        try {
            var ps = connection.prepareStatement(GET_CUSTOMER_DATA);
            ps.setString(1, customer_id);
            var resultSet = ps.executeQuery();
            if (resultSet.next()) {
                String name = resultSet.getString(1);
                String address = resultSet.getString(2);
                double salary = resultSet.getDouble(3);
                logger.info("customer_id = " + customer_id + " name = " + name + " address = " + address + " salary = " + salary);
                System.out.println("customer_id = " + customer_id + " name = " + name + " address = " + address + " salary = " + salary);
                return new CustomerDTO(customer_id, name, address, salary);
            } else {
                logger.error("Can't find customer!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getNextCustomerID(Connection connection) {
        logger.info("Start getNextCustomerID method.");
        try {
            var ps = connection.prepareStatement(GET_LAST_CUSTOMER_ID);
            var resultSet = ps.executeQuery();
            if(resultSet.next()){
                String last_id = resultSet.getString(1);
                int next_id = Integer.parseInt(last_id.replace("C-", "")) + 1;
                return String.format("C-%04d", next_id);
            }
            return "C-0001";
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<CustomerDTO> getSearchCustomers(String search_term, Connection connection) {
        logger.info("Start getSearchCustomers method.");
        try {
            var ps = connection.prepareStatement(GET_SEARCH_CUSTOMER_DATA);
            ps.setString(1, "%" + search_term + "%");
            ps.setString(2, "%" + search_term + "%");
            ps.setString(3, "%" + search_term + "%");
            var resultSet = ps.executeQuery();
            List<CustomerDTO> customerDTOList = new ArrayList<>();
            while (resultSet.next()) {
                String customer_id = resultSet.getString(1);
                String name = resultSet.getString(2);
                String address = resultSet.getString(3);
                double salary = resultSet.getDouble(4);
                logger.info("customer_id = " + customer_id + " name = " + name + " address = " + address + " salary = " + salary);
                System.out.println("customer_id = " + customer_id + " name = " + name + " address = " + address + " salary = " + salary);
                CustomerDTO customerDTO = new CustomerDTO(customer_id, name, address, salary);
                customerDTOList.add(customerDTO);
            }
            return customerDTOList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveNewItem(ItemDTO item, Connection connection) {
        logger.info("Start saveNewItem method.");
        try {
            var ps = connection.prepareStatement(SAVE_ITEM_DATA);
            ps.setString(1, item.getItem_code());
            ps.setString(2, item.getDescription());
            ps.setDouble(3, item.getUnit_price());
            ps.setInt(4, item.getQty_on_hand());

            if (ps.executeUpdate() != 0) {
                logger.info("Item saved.");
            } else {
                logger.error("Failed to save the Item.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateItem(ItemDTO item, Connection connection) {
        logger.info("Start updateItem method.");
        try {
            var ps = connection.prepareStatement(UPDATE_ITEM_DATA);
            ps.setString(1, item.getDescription());
            ps.setDouble(2, item.getUnit_price());
            ps.setInt(3, item.getQty_on_hand());
            ps.setString(4, item.getItem_code());

            if (ps.executeUpdate() != 0) {
                logger.info("Item updated.");
            } else {
                logger.error("Failed to update the item.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}