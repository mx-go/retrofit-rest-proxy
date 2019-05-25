package com.github.proxy.common.utils;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * 异常处理
 */
public class ExceptionUtil {
    private static Unsafe unsafe;

    static {
        try {
            Class unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
            boolean originalAccessible = theUnsafeField.isAccessible();
            theUnsafeField.setAccessible(true);
            //unsafeInstance就是Unsafe的实例
            unsafe = (Unsafe) theUnsafeField.get(null);
            theUnsafeField.setAccessible(originalAccessible);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void throwException(Throwable e) {
        unsafe.throwException(e);
    }
}
