package com.sky.context;

public class BaseContext {

    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        Long currentId = threadLocal.get();
//        // 取出id后释放threadLocal对象，防止内存泄漏
//        removeCurrentId();
        return currentId;
    }

    public static void removeCurrentId() {
        threadLocal.remove();
    }

}
