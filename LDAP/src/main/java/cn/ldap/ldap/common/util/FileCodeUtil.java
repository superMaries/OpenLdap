package cn.ldap.ldap.common.util;

import java.io.*;

/**
 * @title: FileCodeUtil
 * @Author Wy
 * @Date: 2023/5/9 15:46
 * @Version 1.0
 */
public class FileCodeUtil {
    public static String codeString(String fileName) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(new FileInputStream(fileName));
        return codeStr(bin);
    }

    public static String codeStr(BufferedInputStream bin) throws IOException {
        int p = (bin.read() << 8) + bin.read();
        bin.close();
        String code = null;

        switch (p) {
            case 0xefbb:
                code = StaticValue.UTF_8;
                break;
            case 0xfffe:
                code = StaticValue.UNICODE;
                break;
            case 0xfeff:
                code = StaticValue.UTF_16BE;
                break;
            default:
                code = StaticValue.GBK;
        }
        return code;
    }

    public static String codeStr(InputStream inputStream) throws IOException {
        byte[] head = new byte[StaticValue.THREE];
        inputStream.read(head);
        if (head[StaticValue.ZEE] == StaticValue._ONE &&
                head[StaticValue.ONE] == StaticValue._TWO) {
            return StaticValue.UTF_16;
        }
        if (head[StaticValue.ZEE] == StaticValue._TWO &&
                head[StaticValue.ONE] == StaticValue._ONE) {
            return StaticValue.UNICODE;
        }
        if (head[StaticValue.ZEE] == StaticValue._27 &&
                head[StaticValue.ONE] == StaticValue._101
                && head[StaticValue.TWO] == StaticValue._98) {
            return StaticValue.UTF_16;
        }
        if (head[StaticValue.ZEE] == StaticValue._17 &&
                head[StaticValue.ONE] == StaticValue._69
                && head[StaticValue.TWO] == StaticValue._65) {
            return StaticValue.UTF_8_BOM;
        }
        return StaticValue.GBK;
    }
}
