package top.trumeet.common.plugin;

import org.junit.Test;

import static top.trumeet.common.plugin.PlatformUtils.getServiceSign;
import static top.trumeet.common.plugin.PlatformUtils.getSystemSign;

/**
 * Created by Trumeet on 2018/4/21.
 */
public class PlatformUtilsTest {
    @Test
    public void testGetSystemSign() throws Exception {
        String sign = getSystemSign();
        System.out.println("system:" + sign);
    }

    @Test
    public void testGetServiceSign() throws Exception {
        String sign = getServiceSign();
        System.out.println("service:" + sign);
    }

    @Test
    public void isServicePlatformSign() throws Exception {
    }

}