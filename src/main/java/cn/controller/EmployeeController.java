package cn.controller;

import cn.domain.Employee;
import cn.common.R;
import cn.service.EmployeeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService service;

    @PostMapping("/login")
    public R<Employee> login( HttpServletRequest request, @RequestBody Employee employee) {
        log.info("登录..");
        //请求的账户密码
        String username = employee.getUsername();
        String password = employee.getPassword();

        //对密码md5加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //判断用户名是否存在
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper();        //条件
        wrapper.eq(Employee::getUsername, username);            //数据库映射的账户密码
        Employee user = service.getOne(wrapper);

        if (user == null) {
            return R.fail("用户名不存在!");
        }

        //判断密码是否正确
        if (!user.getPassword().equals(password)) {
            return R.fail("密码不正确!");
        }

        //判断用户是否处于禁用
        if (user.getStatus() == 0) {
            return R.fail("用户未激活!");
        }

        //通过后将用户id存入session, 用以过滤器过滤已登录用户
        request.getSession().setAttribute("getUserId", user.getId());

        return R.success(user);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("getUserId");
        return R.success("退出成功!");
    }

    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));      //默认密码为123456加密

        service.save(employee);

        return R.success("添加成功");
    }

    @GetMapping("/page")
    public R<Page> getByPage(Integer page, Integer pageSize, String name) {         //字段与请求需对应
        log.info("page={},pageSize={},name={}", page, pageSize, name);
        Page pageInfo = new Page(page, pageSize);

        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper();
        wrapper.like(StringUtils.hasText(name), Employee::getName, name);       //name是否为空, 空为false, 不执行该语句

        Page data = service.page(pageInfo, wrapper);

        return R.success(data);
    }

    @PutMapping
    public R<String> updateStatus(HttpServletRequest request, @RequestBody Employee employee) {           //更改用户状态
        log.info("current status={}", employee.getStatus());
        //当前用户
        Long id = (Long) request.getSession().getAttribute("getUserId");

        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper();
        wrapper.eq(Employee::getId, employee.getId());                      //根据用户修改值

        service.update(employee, wrapper);

        return R.success("修改成功!");
    }

    @GetMapping("/{id}")
    public R<Employee> updateEmployee(@PathVariable Long id) {
        Employee byId = service.getById(id);

        if (byId != null) {
            return R.success(byId);            //将原查询的数据映射到表格内
        }

        return R.fail("查无此人!");
    }
}
