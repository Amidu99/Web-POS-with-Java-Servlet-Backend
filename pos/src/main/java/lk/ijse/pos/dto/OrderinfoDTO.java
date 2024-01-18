package lk.ijse.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.sql.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderinfoDTO implements Serializable {
    private String order_id;
    private Date date;
    private String customer_id;
    private double discount;
    private double total;
}