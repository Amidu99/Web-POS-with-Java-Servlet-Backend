package lk.ijse.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ItemDTO implements Serializable {
    private String item_code;
    private String description;
    private double unit_price;
    private int qty_on_hand;
}