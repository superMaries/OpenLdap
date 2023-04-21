package cn.ldap.ldap.common.util;

import com.baomidou.mybatisplus.extension.api.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import javax.xml.transform.Result;
import java.io.*;
import java.math.BigDecimal;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 网络吞吐量工具类
 *
 * @title: NetWorkUtil
 * @Author Wy
 * @Date: 2023/3/31 13:40
 * @Version 1.0
 */
@Slf4j
public class NetWorkUtil {
    private static final int SLEEP_TIME = 2 * 1000;

    public static Map<String, String> getNetWorkDownUp() {
        Properties props = System.getProperties();
        String os = props.getProperty("os.name").toLowerCase();
        os = os.startsWith("win") ? "windows" : "linux";
        Map<String, String> result = new HashMap<>();
        Process pro = null;
        Runtime r = Runtime.getRuntime();
        BufferedReader input = null;
        String rxPercent = "";
        String txPercent = "";
        try {
            String command = "windows".equals(os) ? "netstat -e" : "ifconfig";
            pro = r.exec(command);
            input = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            long[] resultLine = readInLine(input, os);
            Thread.sleep(SLEEP_TIME);
            pro.destroy();
            input.close();
            pro = r.exec(command);
            input = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            long[] resuReadLine = readInLine(input, os);
            rxPercent = formatNumber((resuReadLine[0] - resultLine[0]) / (1024.0 * (SLEEP_TIME / 1000)));
            txPercent = formatNumber((resuReadLine[1] - resultLine[1]) / (1024.0 * (SLEEP_TIME / 1000)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Optional.ofNullable(pro).ifPresent(p -> p.destroy());
        }
        result.put("rxPercent", rxPercent);
        result.put("txPercent", txPercent);
        return result;
    }

    private static long[] readInLine(BufferedReader input, String osType) throws IOException {
        long[] arr = new long[2];
        StringTokenizer tokenStat = null;
        try {
            String linux = "linux";
            if (osType.equals(linux)) {
                long rx = 0, tx = 0;
                String line = null;
                //RX packets:4171603 errors:0 dropped:0 overruns:0 frame:0
                //TX packets:4171603 errors:0 dropped:0 overruns:0 carrier:0
                while ((line = input.readLine()) != null) {
                    if (line.indexOf("RX packets") >= 0) {
                        rx += Long.parseLong(line.substring(line.indexOf("RX packets") + 11, line.indexOf(" ", line.indexOf("RX packets") + 11)));
                    } else if (line.indexOf("TX packets") >= 0) {
                        tx += Long.parseLong(line.substring(line.indexOf("TX packets") + 11, line.indexOf(" ", line.indexOf("TX packets") + 11)));
                    }
                }
                arr[0] = rx;
                arr[1] = tx;
            } else {
                input.readLine();
                input.readLine();
                input.readLine();
                input.readLine();
                tokenStat = new StringTokenizer(input.readLine());
                tokenStat.nextToken();
                arr[0] = Long.parseLong(tokenStat.nextToken());
                arr[1] = Long.parseLong(tokenStat.nextToken());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arr;
    }

    private static String formatNumber(double f) {
        return new Formatter().format("%.2f", f).toString();
    }


    /**
     * 获取CPU信息
     *
     * @return
     */
    public static float getCpuInfo() {
        File file = new File("/proc/stat");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            StringTokenizer tokenizer = new StringTokenizer(reader.readLine());
            tokenizer.nextToken();
            int user1 = Integer.parseInt(tokenizer.nextToken());
            int nice1 = Integer.parseInt(tokenizer.nextToken());
            int sys1 = Integer.parseInt(tokenizer.nextToken());
            int idle1 = Integer.parseInt(tokenizer.nextToken());

            Thread.sleep(1000);

            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            tokenizer = new StringTokenizer(reader.readLine());
            tokenizer.nextToken();
            int user2 = Integer.parseInt(tokenizer.nextToken());
            int nice2 = Integer.parseInt(tokenizer.nextToken());
            int sys2 = Integer.parseInt(tokenizer.nextToken());
            int idle2 = Integer.parseInt(tokenizer.nextToken());

            Integer v1 = (user2 + nice2 + sys2) - (user1 + nice1 + sys1);

            Integer v2 = (user2 + nice2 + sys2 + idle2) - (user1 + nice1 + sys1 + idle1);
            float v3 = (v1 * 1.0f / v2) * 100;
            return v3;

        } catch (FileNotFoundException e) {
            log.error("获取CPU信息失败" + e.getMessage());
            return 0;
        } catch (IOException e) {
            log.error("获取CPU信息失败" + e.getMessage());
            return 0;
        } catch (InterruptedException e) {
            log.error("获取CPU信息失败" + e.getMessage());
            return 0;
        } catch (Exception e) {
            log.error("获取CPU信息失败" + e.getMessage());
            return 0;
        }
    }

    /**
     * 获取内存信息
     *
     * @return
     */
    public static int getMemInfo() {
        String totalCmd = "free -m |awk 'NR==2 {print $2}'";
        String usedCmd = "free -m |awk 'NR==2 {print $3}'";
        try {
            Map<String, Object> totalCmdResult = listInfo(totalCmd);
            Map<String, Object> usedCmdCmdResult = listInfo(usedCmd);
            List totalData = (List) totalCmdResult.get("data");
            List usedCmdData = (List) totalCmdResult.get("data");
            String totalStr = (String) totalData.get(0);
            String usedStr = (String) totalData.get(0);

            Integer memTotal = Integer.parseInt(totalStr);
            Integer meUsed = Integer.parseInt(usedStr);

            int intValue = new BigDecimal(usedStr)
                    .divide(BigDecimal.valueOf(memTotal), 2, BigDecimal.ROUND_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .intValue();
            return intValue;
        } catch (Exception e) {
            log.error("获取内存信息 失败" + e.getMessage());
            return 0;
        }
    }

    public static Map<String, Object> listInfo(String cmd) throws Exception {
        log.info("命令：{}", cmd);
       // String[] comands = new String[]{"/bin/sh", cmd};
        Process process = null;
        BufferedReader bufrIn = null;
        BufferedReader bufrError = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            //方法阻塞，等待名录库执行完成
            process.waitFor();

            bufrIn = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));

            String line;
            Map<String, Object> map = new HashMap<>();
            while ((line = bufrError.readLine()) != null) {
                log.error("获取配置,{}", line);
                map.put("status", false);
                map.put("data", line);
                return map;
            }
            ArrayList<String> list = new ArrayList<>();
            while ((line = bufrIn.readLine()) != null) {
                list.add(line);
            }
            map.put("status", true);
            map.put("data", list);
            return map;
        } finally {
            if (bufrIn != null) {
                bufrIn.close();
            }
            if (bufrError != null) {
                bufrError.close();
            }
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * 获取硬盘信息
     *
     * @return
     */
    public static float getDiskInfo() throws InterruptedException, IOException {
        String command = "df -h";
        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec(command);
        p.waitFor();
        BufferedReader in = null;
        in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String str = null;
        String[] s = null;

        int line = 0;
        float diskInfo = 0;
        float yDiskInfo = 0;
        while ((str = in.readLine()) != null) {
            s = str.split(" ");
            int count = 0;
            if (str.contains("dev")) {
                if (!(str.contains("tmpfs") || str.contains("udev"))) {
                    for (String para : s) {
                        boolean info = false;
                        if (count == 0) {
                            if (!"0".equals(para)) {
                                Float aFloat = 0f;
                                if (para.endsWith("G")) {
                                    info = true;
                                    aFloat = Float.valueOf(para.substring(0, para.length() - 1));
                                } else if (para.endsWith("M")) {
                                    info = true;
                                    aFloat = Float.valueOf(para.substring(0, para.length() - 1)) / 1000;
                                } else if (para.endsWith("K")) {
                                    info = true;
                                    aFloat = Float.valueOf(para.substring(0, para.length() - 1)) / 1000 / 1000;
                                }
                                diskInfo += aFloat;
                            } else if (count == 1) {
                                if (!"0".equals(para)) {
                                    Float aFloat = 0f;
                                    if (para.endsWith("G")) {
                                        info = false;
                                        aFloat = Float.valueOf(para.substring(0, para.length() - 1));
                                        count = 0;
                                        yDiskInfo += aFloat;
                                        break;
                                    } else if (para.endsWith("M")) {
                                        info = false;
                                        aFloat = Float.valueOf(para.substring(0, para.length() - 1)) / 1000;
                                        count = 0;
                                        yDiskInfo += aFloat;
                                        break;
                                    } else if (para.endsWith("K")) {
                                        info = false;
                                        aFloat = Float.valueOf(para.substring(0, para.length() - 1)) / 1000 / 1000;
                                        count = 0;
                                        yDiskInfo += aFloat;
                                        break;
                                    }
                                    diskInfo += aFloat;
                                } else {
                                    count = 0;
                                    yDiskInfo += 0;
                                    break;
                                }
                            }
                        } else {
                            count++;
                        }
                    }
                }
            }
        }
        //diskInfo 为0
        if (Math.abs(diskInfo - StaticValue.FLOAT) < StaticValue.FLOAT_EQUALS) {
            return StaticValue.FLOAT;
        }
        float yRate = yDiskInfo / diskInfo / StaticValue.DISK_INFO_NUM;
        return yRate;
    }
}

