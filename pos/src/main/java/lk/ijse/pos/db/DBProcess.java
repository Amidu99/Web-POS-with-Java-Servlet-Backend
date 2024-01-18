package lk.ijse.pos.db;

import lk.ijse.pos.dto.CustomerDTO;
import lk.ijse.pos.dto.ItemDTO;
import lk.ijse.pos.dto.OrderdetailsDTO;
import lk.ijse.pos.dto.OrderinfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.Date;
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
    private static final String DELETE_ITEM_DATA = "DELETE FROM item WHERE item_code=?";
    private static final String GET_ALL_ITEM_DATA = "SELECT * FROM item ORDER BY item_code";
    private static final String GET_ITEM_DATA = "SELECT description, unit_price, qty_on_hand FROM item WHERE item_code=?";
    private static final String GET_LAST_ITEM_CODE = "SELECT item_code FROM item ORDER BY item_code DESC LIMIT 1";
    private static final String GET_SEARCH_ITEM_DATA = "SELECT * FROM item WHERE item_code LIKE ? OR description LIKE ?";
    private static final String SAVE_ORDER_DATA = "INSERT INTO orderinfo (order_id, date, customer_id, discount, total) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_ORDER_DATA = "UPDATE orderinfo SET date=?, customer_id=?, discount=?, total=? WHERE order_id=?";
    private static final String DELETE_ORDER_DATA = "DELETE FROM orderinfo WHERE order_id=?";
    private static final String GET_ALL_ORDER_DATA = "SELECT * FROM orderinfo ORDER BY order_id";
    private static final String GET_ORDER_DATA = "SELECT date, customer_id, discount, total FROM orderinfo WHERE order_id=?";
    private static final String GET_LAST_ORDER_ID = "SELECT order_id FROM orderinfo ORDER BY order_id DESC LIMIT 1";
    private static final String GET_SEARCH_ORDERS_DATA = "SELECT * FROM orderinfo WHERE order_id LIKE ? OR customer_id LIKE ?";
    private static final String SAVE_ORDERDETAILS_DATA = "INSERT INTO orderdetails (order_id, item_code, description, unit_price, get_qty) VALUES (?, ?, ?, ?, ?)";
    private static final String DELETE_ORDERDETAILS_DATA = "DELETE FROM orderdetails WHERE order_id=?";
    private static final String GET_ALL_ORDERDETAILS_DATA = "SELECT * FROM orderdetails WHERE order_id=?";

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

    public void deleteItem(ItemDTO item, Connection connection) {
        logger.info("Start deleteItem method.");
        try {
            var ps = connection.prepareStatement(DELETE_ITEM_DATA);
            ps.setString(1, item.getItem_code());

            if (ps.executeUpdate() != 0) {
                logger.info("Item deleted.");
            } else {
                logger.error("Failed to delete the item.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ItemDTO> getAllItems(Connection connection) {
        logger.info("Start getAllItems method.");
        try {
            var ps = connection.prepareStatement(GET_ALL_ITEM_DATA);
            var resultSet = ps.executeQuery();
            List<ItemDTO> itemDTOList = new ArrayList<>();
            while (resultSet.next()) {
                String item_code = resultSet.getString(1);
                String description = resultSet.getString(2);
                double unit_price = resultSet.getDouble(3);
                int qty_on_hand = resultSet.getInt(4);
                logger.info("item_code = " + item_code + " description = " + description + " unit_price = " + unit_price + " qty_on_hand = " + qty_on_hand);
                System.out.println("item_code = " + item_code + " description = " + description + " unit_price = " + unit_price + " qty_on_hand = " + qty_on_hand);
                ItemDTO itemDTO = new ItemDTO(item_code, description, unit_price, qty_on_hand);
                itemDTOList.add(itemDTO);
            }
            return itemDTOList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ItemDTO getItem(String item_code, Connection connection) {
        logger.info("Start getItem method.");
        try {
            var ps = connection.prepareStatement(GET_ITEM_DATA);
            ps.setString(1, item_code);
            var resultSet = ps.executeQuery();
            if (resultSet.next()) {
                String description = resultSet.getString(1);
                double unit_price = resultSet.getDouble(2);
                int qty_on_hand = resultSet.getInt(3);
                logger.info("item_code = " + item_code + " description = " + description + " unit_price = " + unit_price + " qty_on_hand = " + qty_on_hand);
                System.out.println("item_code = " + item_code + " description = " + description + " unit_price = " + unit_price + " qty_on_hand = " + qty_on_hand);
                return new ItemDTO(item_code, description, unit_price, qty_on_hand);
            } else {
                logger.error("Can't find item!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getNextItemCode(Connection connection) {
        logger.info("Start getNextItemCode method.");
        try {
            var ps = connection.prepareStatement(GET_LAST_ITEM_CODE);
            var resultSet = ps.executeQuery();
            if(resultSet.next()){
                String last_code = resultSet.getString(1);
                int next_code = Integer.parseInt(last_code.replace("I-", "")) + 1;
                return String.format("I-%04d", next_code);
            }
            return "I-0001";
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<ItemDTO> getSearchItems(String search_term, Connection connection) {
        logger.info("Start getSearchItems method.");
        try {
            var ps = connection.prepareStatement(GET_SEARCH_ITEM_DATA);
            ps.setString(1, "%" + search_term + "%");
            ps.setString(2, "%" + search_term + "%");
            var resultSet = ps.executeQuery();
            List<ItemDTO> itemDTOList = new ArrayList<>();
            while (resultSet.next()) {
                String item_code = resultSet.getString(1);
                String description = resultSet.getString(2);
                double unit_price = resultSet.getDouble(3);
                int qty_on_hand = resultSet.getInt(4);
                logger.info("item_code = " + item_code + " description = " + description + " unit_price = " + unit_price + " qty_on_hand = " + qty_on_hand);
                System.out.println("item_code = " + item_code + " description = " + description + " unit_price = " + unit_price + " qty_on_hand = " + qty_on_hand);
                ItemDTO itemDTO = new ItemDTO(item_code, description, unit_price, qty_on_hand);
                itemDTOList.add(itemDTO);
            }
            return itemDTOList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveNewOrder(OrderinfoDTO orderinfo, Connection connection) {
        logger.info("Start saveNewOrder method.");
        try {
            var ps = connection.prepareStatement(SAVE_ORDER_DATA);
            ps.setString(1, orderinfo.getOrder_id());
            ps.setDate(2, orderinfo.getDate());
            ps.setString(3, orderinfo.getCustomer_id());
            ps.setDouble(4, orderinfo.getDiscount());
            ps.setDouble(5, orderinfo.getTotal());

            if (ps.executeUpdate() != 0) {
                logger.info("Order saved.");
            } else {
                logger.error("Failed to save the Order.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateOrder(OrderinfoDTO orderinfo, Connection connection) {
        logger.info("Start updateOrder method.");
        try {
            var ps = connection.prepareStatement(UPDATE_ORDER_DATA);
            ps.setDate(1, orderinfo.getDate());
            ps.setString(2, orderinfo.getCustomer_id());
            ps.setDouble(3, orderinfo.getDiscount());
            ps.setDouble(4, orderinfo.getTotal());
            ps.setString(5, orderinfo.getOrder_id());

            if (ps.executeUpdate() != 0) {
                logger.info("Order updated.");
            } else {
                logger.error("Failed to update the Order.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteOrder(OrderinfoDTO orderinfoDTO, Connection connection) {
        logger.info("Start deleteOrder method.");
        try {
            var ps = connection.prepareStatement(DELETE_ORDER_DATA);
            ps.setString(1, orderinfoDTO.getOrder_id());

            if (ps.executeUpdate() != 0) {
                logger.info("Order deleted.");
            } else {
                logger.error("Failed to delete the order.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<OrderinfoDTO> getAllOrders(Connection connection) {
        logger.info("Start getAllOrders method.");
        try {
            var ps = connection.prepareStatement(GET_ALL_ORDER_DATA);
            var resultSet = ps.executeQuery();
            List<OrderinfoDTO> orderinfoDTOList = new ArrayList<>();
            while (resultSet.next()) {
                String order_id = resultSet.getString(1);
                Date date = resultSet.getDate(2);
                String customer_id = resultSet.getString(3);
                double discount = resultSet.getDouble(4);
                double total = resultSet.getDouble(5);
                logger.info("order_id = " + order_id + " date = " + date + " customer_id = " + customer_id + " discount = " + discount + " total = " + total);
                System.out.println("order_id = " + order_id + " date = " + date + " customer_id = " + customer_id + " discount = " + discount + " total = " + total);
                OrderinfoDTO orderinfoDTO = new OrderinfoDTO(order_id, date, customer_id, discount, total);
                orderinfoDTOList.add(orderinfoDTO);
            }
            return orderinfoDTOList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public OrderinfoDTO getOrder(String order_id, Connection connection) {
        logger.info("Start getOrder method.");
        try {
            var ps = connection.prepareStatement(GET_ORDER_DATA);
            ps.setString(1, order_id);
            var resultSet = ps.executeQuery();
            if (resultSet.next()) {
                Date date = resultSet.getDate(1);
                String customer_id = resultSet.getString(2);
                double discount = resultSet.getDouble(3);
                double total = resultSet.getDouble(4);
                logger.info("order_id = " + order_id + " date = " + date + " customer_id = " + customer_id + " discount = " + discount + " total = " + total);
                System.out.println("order_id = " + order_id + " date = " + date + " customer_id = " + customer_id + " discount = " + discount + " total = " + total);
                return new OrderinfoDTO(order_id, date, customer_id, discount, total);
            } else {
                logger.error("Can't find order!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getNextOrderID(Connection connection) {
        logger.info("Start getNextOrderID method.");
        try {
            var ps = connection.prepareStatement(GET_LAST_ORDER_ID);
            var resultSet = ps.executeQuery();
            if(resultSet.next()){
                String last_id = resultSet.getString(1);
                int next_id = Integer.parseInt(last_id.replace("O-", "")) + 1;
                return String.format("O-%04d", next_id);
            }
            return "O-0001";
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<OrderinfoDTO> getSearchOrders(String search_term, Connection connection) {
        logger.info("Start getSearchOrders method.");
        try {
            var ps = connection.prepareStatement(GET_SEARCH_ORDERS_DATA);
            ps.setString(1, "%" + search_term + "%");
            ps.setString(2, "%" + search_term + "%");
            var resultSet = ps.executeQuery();
            List<OrderinfoDTO> orderinfoDTOList = new ArrayList<>();
            while (resultSet.next()) {
                String order_id = resultSet.getString(1);
                Date date = resultSet.getDate(2);
                String customer_id = resultSet.getString(3);
                double discount = resultSet.getDouble(4);
                double total = resultSet.getDouble(5);
                logger.info("order_id = " + order_id + " date = " + date + " customer_id = " + customer_id + " discount = " + discount + " total = " + total);
                System.out.println("order_id = " + order_id + " date = " + date + " customer_id = " + customer_id + " discount = " + discount + " total = " + total);
                OrderinfoDTO orderinfoDTO = new OrderinfoDTO(order_id, date, customer_id, discount, total);
                orderinfoDTOList.add(orderinfoDTO);
            }
            return orderinfoDTOList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveOrderDetails(List<OrderdetailsDTO> dtoList, Connection connection) {
        logger.info("Start saveOrderDetails method.");
        for(OrderdetailsDTO dto : dtoList){
            try {
                var ps = connection.prepareStatement(SAVE_ORDERDETAILS_DATA);
                ps.setString(1, dto.getOrder_id());
                ps.setString(2, dto.getItem_code());
                ps.setString(3, dto.getDescription());
                ps.setDouble(4, dto.getUnit_price());
                ps.setInt(5, dto.getGet_qty());

                if (ps.executeUpdate() != 0) {
                    logger.info("Order details saved.");
                } else {
                    logger.info("Failed to save Order details.");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void deleteOrderDetails(String order_id, Connection connection) {
        logger.info("Start deleteOrderDetails method.");
        try {
            var ps = connection.prepareStatement(DELETE_ORDERDETAILS_DATA);
            ps.setString(1, order_id);

            if (ps.executeUpdate() != 0) {
                logger.info("OrderDetails deleted.");
            } else {
                logger.error("Failed to delete the OrderDetails.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<OrderdetailsDTO> getAllOrderDetails(String order_id, Connection connection) {
        logger.info("Start getAllOrderDetails method.");
        try {
            var ps = connection.prepareStatement(GET_ALL_ORDERDETAILS_DATA);
            ps.setString(1, order_id);
            var resultSet = ps.executeQuery();
            List<OrderdetailsDTO> orderdetailsDTOList = new ArrayList<>();
            while (resultSet.next()) {
                String item_code = resultSet.getString(2);
                String description = resultSet.getString(3);
                double unit_price = resultSet.getDouble(4);
                int get_qty = resultSet.getInt(5);
                logger.info("order_id = " + order_id + "item_code = " + item_code + " description = " + description + " unit_price = " + unit_price + " get_qty = " + get_qty);
                System.out.println("order_id = " + order_id + "item_code = " + item_code + " description = " + description + " unit_price = " + unit_price + " get_qty = " + get_qty);
                OrderdetailsDTO orderdetailsDTO = new OrderdetailsDTO(order_id, item_code, description, unit_price, get_qty);
                orderdetailsDTOList.add(orderdetailsDTO);
            }
            return orderdetailsDTOList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}