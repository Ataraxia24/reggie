package cn.dto;

import cn.domain.Orders;
import lombok.Data;

@Data
public class OrdersDto extends Orders {
    private String userName;
}
