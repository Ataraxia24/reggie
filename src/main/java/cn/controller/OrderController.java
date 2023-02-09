package cn.controller;

import cn.common.BaseContext;
import cn.common.R;
import cn.domain.Orders;
import cn.domain.User;
import cn.dto.OrdersDto;
import cn.service.OrderService;
import cn.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.DateFormatter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }

    @GetMapping("/userPage")        //移动端最新订单
    public R<Page> orderList(Integer page, Integer pageSize) {
        //orders库中, 只需要按时间全部查找, 前端使用数组接收通过下标展现第一个订单
        log.info("new page={},pageSize={}", page, pageSize);

        //获取当前用户id
        Long userId = BaseContext.get();

        Page<Orders> pageOrders = new Page<>(page, pageSize);

        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Orders::getOrderTime);
        wrapper.eq(Orders::getUserId, userId);

        orderService.page(pageOrders, wrapper);

        return R.success(pageOrders);
    }

    @GetMapping("/page")        //pc后台管理订单
    public R<Page> PC_orders_list(Integer page, Integer pageSize, String number, String beginTime, String endTime) {
        log.info("page={},pageSize={},number={},time{}->{}", page, pageSize, number, beginTime, endTime);

        //创建条件对象
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();

        //数据库接收是dateTime, 传递的是String类型, 判断前将其转换
        //创建转换对象
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss");

        //避免空指针转换
        if (beginTime != null && endTime != null) {
            //String bgTime = formatter.format(beginTime);                          dateTime转String
            LocalDateTime bgTime = LocalDateTime.parse(beginTime, formatter);     //String转dateTime
            LocalDateTime edTime = LocalDateTime.parse(endTime, formatter);

            //大于等于beginTime小于等于endTime              大于:gt  小于lt
            wrapper.ge(beginTime != null, Orders::getOrderTime, bgTime).le(endTime != null, Orders::getOrderTime, edTime);
        }

        wrapper.like(StringUtils.hasText(number), Orders::getNumber, number);       //hasText() 判断当前number是否有值

        //设置展示页面受限
        Page<Orders> pageInfo = new Page<>(page, pageSize);

        orderService.page(pageInfo, wrapper);

        //service调用查询出数据之后, 存储在page的records中,dto无法查询数据, 将page中的数据剥离到dto中
        //将除了records外的所有配置拷贝, records中的每一行都需要修改username属性, 循环
        Page<OrdersDto> pageDto = new Page();

        BeanUtils.copyProperties(pageInfo, pageDto, "records");

        //单独获取records进行循环
        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> dtoRecords = null;

        dtoRecords = records.stream().map((item)->{
            OrdersDto ordersDto = new OrdersDto();

            //拷贝除无法获取的name属性外的所有属性值
            BeanUtils.copyProperties(item, ordersDto);

            //根据用户id查找下单的用户名称
            User users = userService.getById(item.getUserId());

            //逐一修改
            // TODO: 这里在页面上无法显示, 原因：账号无需注册直接登陆无法修改用户姓名, 数据库user姓名栏为空, 取出为null
            ordersDto.setUserName(users.getName());

            return ordersDto;
        }).collect(Collectors.toList());
        log.info("dtoRecords={}",dtoRecords);

        //最后存入修改完的records
        pageDto.setRecords(dtoRecords);

        return R.success(pageDto);
    }

    @PutMapping
    public R<String> setStatus(@RequestBody Orders orders) {
        //根据id修改当前订单状态
        log.info("id={},status={}", orders.getId(), orders.getStatus());

        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getId, orders.getId());

        orderService.update(orders, wrapper);

        return R.success("修改成功");
    }
}
