package cn.service;

import cn.common.R;
import cn.domain.Orders;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface OrderService extends IService<Orders> {
    void submit(Orders orders);
}
