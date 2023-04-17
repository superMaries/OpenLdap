package cn.ldap.ldap.common.util;

import com.baomidou.mybatisplus.extension.api.R;

import javax.swing.text.SimpleAttributeSet;
import java.text.DateFormat;

/**
 * @title: StaticValue
 * @Author Wy
 * @Date: 2023/4/7 11:48
 * @Version 1.0
 */
public class StaticValue {
    public final static String TOTAL = "total";
    public final static String CERT_TOTAL = "certTotal";
    public final static String CRL_TOTAL = "crlTotal";
    public final static String RX_PERCENT = "rxPercent";
    public final static String TX_PERCENT = "txPercent";
    public final static String TIME_FORMAT = "HH:mm:ss";
    public final static String DATEFORMAT = "yyyy-MM-dd HH:SS:MM";

    public final static Integer LENGTH = 10;
    public final static String IP_ADDRESS_INDEX_OF = ",";
    public final static Integer IP_ADDRESS_LENGTH = 15;
    public final static Integer LDAP_PAGE_SIZE = 1000;

    public final static float FLOAT = 0f;
    public final static float FLOAT_EQUALS = 1e-6f;
    public final static float DISK_INFO_NUM = 100;

    public final static Integer MSG_LENGTH = 200;

    public final static Integer COUNT = 1;

    public final static Integer COUN_TLIMIT = 1;
    public final static Long TOTAL_NODE_NUM = 0L;
    public final static String NUM_SUBORDINATES = "1.1";
    public final static String RDN = "rdn";
    public final static String RDN_NUM_KEY = "rdnNum";
    public final static String RDN_CHILD_NUM_KEY = "rdnChildNum";
    public final static Integer RDN_NUM = 0;
    public final static String SPLIT = ",";
    public final static String ADD = "\\+";
    public final static String LINE = "-";
    public final static String REPLACE = "";
    public final static Integer SPLIT_COUNT = 0;
    public final static Integer AUDIT_STATUS = 1;
    public final static Integer AUDIT_NOT_STATUS = 0;
    public final static String UNKNOWN = "-";
    public final static Integer ADMIN_ID = 0;

    public final static boolean TRUE = true;
    public final static boolean FALSE = false;
    public final static String SM2 = "SM2";
    public final static String SM3 = "SM3";
    public final static String VERTICAL = "|";
    public final static String LOG = "日志管理-审计日志";
    public final static String EQ = "=";
    public final static String OBJECT_CLASS = "objectClass";

    public final static Integer LDAP_SCOPE = 3;
}
