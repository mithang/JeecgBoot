package org.jeecg.common.util;


import org.jeecg.common.exception.JeecgBootAssertException;

/**
 * Công cụ kiểm tra assertion
 * for for [QQYUN-10990]AIRAG
 * @author chenrui
 * @date 2017-06-22 10:05:56
 */
public class AssertUtils {

    /**
     * Đảm bảo đối tượng rỗng,Nếu không rỗng sẽ ném ngoại lệ
     *
     * @param msg
     * @param obj
     * @throws JeecgBootAssertException
     * @author chenrui
     * @date 2017-06-22 10:05:56
     */
    public static void assertEmpty(String msg, Object obj) {
        if (oConvertUtils.isObjectNotEmpty(obj)) {
            throw new JeecgBootAssertException(msg);
        }
    }


    /**
     * Đảm bảo đối tượng không rỗng,Nếu rỗng sẽ ném ngoại lệ
     *
     * @param msg
     * @param obj
     * @throws JeecgBootAssertException
     * @author chenrui
     * @date 2017-06-22 10:05:56
     */
    public static void assertNotEmpty(String msg, Object obj) {
        if (oConvertUtils.isObjectEmpty(obj)) {
            throw new JeecgBootAssertException(msg);
        }
    }


    /**
     * Kiểm chứng đối tượng có giống nhau không
     *
     * @param message
     * @param expected
     * @param actual
     * @author chenrui
     * @date 2018/9/12 15:45
     */
    public static void assertEquals(String message, Object expected,
                                    Object actual) {
        if (oConvertUtils.isEqual(expected, actual)) {
            return;
        }
        throw new JeecgBootAssertException(message);
    }

    /**
     * Kiểm chứng không giống nhau
     *
     * @param message
     * @param expected
     * @param actual
     * @author chenrui
     * @date 2018/9/12 15:45
     */
    public static void assertNotEquals(String message, Object expected,
                                       Object actual) {
        if (oConvertUtils.isEqual(expected, actual)) {
            throw new JeecgBootAssertException(message);
        }

    }

    /**
     * Kiểm chứng có bằng nhau không
     *
     * @param message
     * @param expected
     * @param actual
     * @author chenrui
     * @date 2018/9/12 15:45
     */
    public static void assertSame(String message, Object expected,
                                  Object actual) {
        if (expected == actual) {
            return;
        }
        throw new JeecgBootAssertException(message);
    }

    /**
     * Kiểm chứng không bằng nhau
     *
     * @param message
     * @param unexpected
     * @param actual
     * @author chenrui
     * @date 2018/9/12 15:45
     */
    public static void assertNotSame(String message, Object unexpected,
                                     Object actual) {
        if (unexpected == actual) {
            throw new JeecgBootAssertException(message);
        }
    }

    /**
     * Kiểm chứng có phải là true không
     *
     * @param message
     * @param condition
     */
    public static void assertTrue(String message, boolean condition) {
        if (!condition) {
            throw new JeecgBootAssertException(message);
        }
    }

    /**
     * Kiểm chứng condition có phải là false
     *
     * @param message
     * @param condition
     */
    public static void assertFalse(String message, boolean condition) {
        assertTrue(message, !condition);
    }


    /**
     * Kiểm chứng có tồn tại không
     *
     * @param message
     * @param obj
     * @param objs
     * @param <T>
     * @throws JeecgBootAssertException
     * @author chenrui
     * @date 2018/1/31 22:14
     */
    public static <T> void assertIn(String message, T obj, T... objs) {
        assertNotEmpty(message, obj);
        assertNotEmpty(message, objs);
        if (!oConvertUtils.isIn(obj, objs)) {
            throw new JeecgBootAssertException(message);
        }
    }

    /**
     * Kiểm chứng có không tồn tại không
     *
     * @param message
     * @param obj
     * @param objs
     * @param <T>
     * @throws JeecgBootAssertException
     * @author chenrui
     * @date 2018/1/31 22:14
     */

    public static <T> void assertNotIn(String message, T obj, T... objs) {
        assertNotEmpty(message, obj);
        assertNotEmpty(message, objs);
        if (oConvertUtils.isIn(obj, objs)) {
            throw new JeecgBootAssertException(message);
        }
    }


    /**
     * Đảm bảo src lớn hơn des
     *
     * @param message
     * @param src
     * @param des
     * @author chenrui
     * @date 2018/9/19 15:30
     */
    public static void assertGt(String message, Number src, Number des) {
        if (oConvertUtils.isGt(src, des)) {
            return;
        }
        throw new JeecgBootAssertException(message);
    }

    /**
     * Đảm bảo src lớn hơn hoặc bằng des
     *
     * @param message
     * @param src
     * @param des
     * @author chenrui
     * @date 2018/9/19 15:30
     */
    public static void assertGe(String message, Number src, Number des) {
        if (oConvertUtils.isGe(src, des)) {
            return;
        }
        throw new JeecgBootAssertException(message);
    }


    /**
     * Đảm bảo src nhỏ hơn des
     *
     * @param message
     * @param src
     * @param des
     * @author chenrui
     * @date 2018/9/19 15:30
     */
    public static void assertLt(String message, Number src, Number des) {
        if (oConvertUtils.isGe(src, des)) {
            throw new JeecgBootAssertException(message);
        }
    }

    /**
     * Đảm bảo src nhỏ hơn hoặc bằng des
     *
     * @param message
     * @param src
     * @param des
     * @author chenrui
     * @date 2018/9/19 15:30
     */
    public static void assertLe(String message, Number src, Number des) {
        if (oConvertUtils.isGt(src, des)) {
            throw new JeecgBootAssertException(message);
        }
    }

}
