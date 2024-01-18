package lk.ijse.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderdetailsDTO implements Serializable {
    private String order_id;
    private String item_code;
    private String description;
    private double unit_price;
    private int get_qty;
}