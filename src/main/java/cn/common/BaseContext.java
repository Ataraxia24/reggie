package cn.common;

public class BaseContext {
    //当前编辑修改更新请求为单一线程不会混乱, 在过滤器初始登录通过时将session中的id存入本地线程, 可以共享数据, 为元数据填充属性做动态效果

    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    public static void set(Long id) {         //存入id
        threadLocal.set(id);
    }

    public static Long get() {
        return threadLocal.get();
    }
}
