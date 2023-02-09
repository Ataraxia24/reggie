package cn.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class R<T> implements Serializable {        //前后端交互协议
    private Integer code;
    private String msg;
    private T data;

    //编码固定, 0为失败 1为成功  以此创建泛型方法
    public static <T> R<T> success(T object) {
        R<T> r = new R();
        r.code = 1;
        r.data = object;
        return r;
    }

    public static <T> R<T> fail(String msg) {
        R<T> r = new R();
        r.code = 0;
        r.msg = msg;
        return r;
    }
}
