package cn.controller;

import cn.common.BaseContext;
import cn.common.R;
import cn.domain.ShoppingCart;
import cn.service.ShoppingCartService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService service;

    @PostMapping("/add")
    public R<ShoppingCart> list(@RequestBody ShoppingCart cart) {
        //根据请求的套餐id和菜品id存入购物车的不同id中
        log.info("{}",cart);

        Long userId = BaseContext.get();
        cart.setUserId(userId);
        log.info("userId={}", userId);

        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();

        //当前用户的当前菜品
        wrapper.eq(ShoppingCart::getUserId, userId);

        //判断添加菜品还是套餐
        Long dishId = cart.getDishId();
        if (dishId != null) {
            wrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            wrapper.eq(ShoppingCart::getSetmealId, cart.getSetmealId());
        }

        //获取当前数据库对象
        ShoppingCart shoppingCart = service.getOne(wrapper);

        if (shoppingCart != null) {
            //在原有数量上自增
            shoppingCart.setNumber(shoppingCart.getNumber()+1);
            service.updateById(shoppingCart);
        } else {
            cart.setNumber(1);
            cart.setCreateTime(LocalDateTime.now());            //注入少update, 手动设置
            service.save(cart);
            shoppingCart = cart;
        }

        return R.success(shoppingCart);
    }

    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        //购物车清单
        Long userId = BaseContext.get();

        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);

        List<ShoppingCart> list = service.list(wrapper);

        return R.success(list);
    }

    @PostMapping("/sub")         //提交请求, 对当前菜品套餐做累加, 避免重复但是数量都是1的数据
    public R<ShoppingCart> subNumber(@RequestBody ShoppingCart cart) {
        Long userId = BaseContext.get();

        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();

        //当前用户的当前菜品
        wrapper.eq(ShoppingCart::getUserId, userId);

        //判断是菜品还是套餐
        Long dishId = cart.getDishId();
        if (dishId != null) {
            wrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            wrapper.eq(ShoppingCart::getSetmealId, cart.getSetmealId());
        }

        //获取当前数量
        ShoppingCart shoppingCart = service.getOne(wrapper);
        Integer number = shoppingCart.getNumber();

        //判断当前数量是否为1, 若为1则移除
        if (number == 1) {
            service.remove(wrapper);
        } else {
            shoppingCart.setNumber(shoppingCart.getNumber()-1);
            service.updateById(shoppingCart);
        }

        return R.success(shoppingCart);
    }

    @DeleteMapping("/clean")
    public R<String> clean(){
        //清除购物车, 将当前用户下的所有菜品全部删除

        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        Long userId = BaseContext.get();
        wrapper.eq(ShoppingCart::getUserId,userId);

        service.remove(wrapper);

        return R.success("清空购物车成功");
    }


}
