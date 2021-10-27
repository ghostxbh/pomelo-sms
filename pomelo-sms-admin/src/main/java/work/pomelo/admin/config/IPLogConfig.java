package work.pomelo.admin.config;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author ghostxbh
 * @date 2020/8/11
 * 
 */
public class IPLogConfig extends ClassicConverter {

    private static String ip = "";

    static {
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String convert(ILoggingEvent iLoggingEvent) {
        return ip;
    }
}