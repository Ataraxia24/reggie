package cn.common;

import cn.common.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
public class ExceptionHandlerAdvice {     //全局异常处理

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)       //SQL异常
    public R<String> DuplicateExceptionHandler(SQLIntegrityConstraintViolationException exception) {
        //判断异常是否是关键词重复异常
        if (exception.getMessage().contains("Duplicate entry")) {
            //提示哪个名称重复, 获取该名称
            String[] split = exception.getMessage().split(" ");
            String userMsg = split[2] + "已重复!";

            return R.fail(userMsg);
        }

        return R.fail("未知错误");
    }

    @ExceptionHandler(CustomException.class)
    public R<String> CustomExceptionHandler(CustomException ex) {
        return R.fail(ex.getMessage());
    }
}
