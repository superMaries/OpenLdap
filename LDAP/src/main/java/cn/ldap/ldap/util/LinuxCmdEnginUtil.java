package cn.ldap.ldap.util;

import cn.ldap.ldap.common.enums.ExceptionEnum;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.vo.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class LinuxCmdEnginUtil {
    @SuppressWarnings("finally")
    public static final String processUseSudo(String command) {
        Process p = null;
        StringBuilder sb = new StringBuilder();
        String resultCode = "0::";// 表示命令执行成功
        try {
            String[] comands = new String[]{"/bin/sh", "-c", "sudo " + command};

//			System.out.println("Process comands:" + command);
            p = Runtime.getRuntime().exec(comands);
            String outInfo = read(p.getInputStream());
            String error = read(p.getErrorStream());

            if (error.length() == 0) {
                sb.append(resultCode);
                sb.append(outInfo);
            } else {
                resultCode = "1::";
                sb.append(resultCode);
                sb.append(error);
            }
            p.waitFor();
        } catch (Exception e) {
            resultCode = "2::";
            sb.append(resultCode);
            sb.append(e.getMessage());
        } finally {
            try {
                p.getOutputStream().close();
            } catch (Exception e) {
                resultCode = "3::";
                sb.append(resultCode);
                sb.append(e.getMessage());
            }

            return sb.toString();
//			String rtnStr = "";
//			try{
//				rtnStr = new String(sb.toString().getBytes("iso8859-1"),"UTF-8");
//			}catch (UnsupportedEncodingException e){
//				rtnStr = "4::字符转换异常！";
//				return rtnStr;
//			}
//			return rtnStr;
        }
    }


    @SuppressWarnings("finally")
    public static final String processUserCmd1(String command) {
        Process p = null;
        StringBuilder sb = new StringBuilder();
        try {
            String[] comands = new String[]{"/bin/sh", "-c", command};
            p = Runtime.getRuntime().exec(comands);
            String outInfo = read(p.getInputStream());
            String error = read(p.getErrorStream());
            String resultCode = "0::";// 表示命令执行成功
            if (error.length() == 0) {
                sb.append(resultCode);
                sb.append(outInfo);
            } else {
                resultCode = "1::";
                sb.append(resultCode);
                sb.append(error);
            }
            p.waitFor();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try {
                p.getOutputStream().close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            return sb.toString();//安可GBK--UTF-8
//			String rtnStr = "";
//			try{
//				rtnStr = new String(sb.toString().getBytes("iso8859-1"),"UTF-8");
//			}catch (UnsupportedEncodingException e){
//				rtnStr = "4::字符转换异常！";
//				return rtnStr;
//			}
//			return rtnStr;
        }
    }

    public static String processUserCmd(String cmd) {
        StringBuilder result = new StringBuilder();
        StringBuilder resultError = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        Process process = null;
        BufferedReader bufrIn = null;
        BufferedReader bufrError = null;
        String resultCode = "0::";// 表示命令执行成功
        try {
            // 执行命令, 返回一个子进程对象（命令在子进程中执行）
            //process = Runtime.getRuntime().exec(cmd, null, dir);
            String[] comands = new String[]{"/bin/sh", "-c", cmd};
            process = Runtime.getRuntime().exec(comands);
            // 方法阻塞, 等待命令执行完成（成功会返回0）
            process.waitFor();

            // 获取命令执行结果, 有两个结果: 正常的输出 和 错误的输出（PS: 子进程的输出就是主进程的输入）
            bufrIn = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));

            // 读取输出
            String line = null;
            String lineerror = null;
            while ((line = bufrIn.readLine()) != null) {
                result.append(line);
            }
            while ((lineerror = bufrError.readLine()) != null) {
                resultError.append(lineerror);
            }
            if (resultError.length() == 0) {
                sb.append(resultCode);
                sb.append(result.toString());
            } else {
                resultCode = "1::";
                sb.append(resultCode);
                sb.append(resultError.toString());
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            resultCode = "4::";
            sb.append(resultCode);
            sb.append(e.getMessage());
            return sb.toString();
        } finally {
            closeStream(bufrIn);
            closeStream(bufrError);

            // 销毁子进程
            if (process != null) {
                process.destroy();
            }
        }

        // 返回执行结果
        return sb.toString();
    }


    public static final String read(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        int ch;
        while (-1 != (ch = in.read())) {
            sb.append((char) ch);
        }
        return sb.toString();
    }

    public static final String readBuffer(InputStreamReader in) throws IOException {
        StringBuilder sb = new StringBuilder();
        int ch;
        while (-1 != (ch = in.read())) {
            sb.append((char) ch);
        }
        return sb.toString();
    }

    public static final String readWin(InputStream ins) throws IOException {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(ins, "GBK"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                // System.out.println(line);
                sb.append(line);
            }
        } catch (Exception e) {
            throw (new IOException());
        }
        return sb.toString();
    }

    @SuppressWarnings("finally")
    public static final String processWinCmd(String command) {
        Process p = null;
        StringBuilder sb = new StringBuilder();
        try {
            String[] comands = new String[]{"cmd.exe", "/C", command};
            p = Runtime.getRuntime().exec(comands);
            // new Thread(new StreamDrainer(p.getInputStream())).start();
            // new Thread(new StreamDrainer(p.getErrorStream())).start();

            String outInfo = readWin(p.getInputStream());

            String error = readWin(p.getErrorStream());
            String resultCode = "0::";// 表示命令执行成功
            if (error.length() == 0) {
                sb.append(resultCode);
                sb.append(outInfo);
            } else {
                resultCode = "1::";
                sb.append(resultCode);
                sb.append(error);
            }
            int exitValue = p.waitFor();
            System.out.println("返回值：" + exitValue);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try {
                p.getOutputStream().close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        }
    }


    /**
     * 执行系统命令, 返回执行结果
     *
     * @param cmd 需要执行的命令
     * @param dir 执行命令的子进程的工作目录, null 表示和当前主进程工作目录相同
     */
    public static List<String> execCmd(String cmd, File dir) throws Exception {
        StringBuilder result = new StringBuilder();
        List<String> listResult = new ArrayList<String>();
        Process process = null;
        BufferedReader bufrIn = null;
        BufferedReader bufrError = null;

        try {
            // 执行命令, 返回一个子进程对象（命令在子进程中执行）
            process = Runtime.getRuntime().exec(cmd, null, dir);


            // 方法阻塞, 等待命令执行完成（成功会返回0）
            process.waitFor();

            // 获取命令执行结果, 有两个结果: 正常的输出 和 错误的输出（PS: 子进程的输出就是主进程的输入）
            bufrIn = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));

            // 读取输出
            String line = null;
            while ((line = bufrIn.readLine()) != null) {
                result.append(line).append('\n');
                listResult.add(line);
            }
            while ((line = bufrError.readLine()) != null) {
                result.append(line).append('\n');
                listResult.add(line);
            }

        } finally {
            closeStream(bufrIn);
            closeStream(bufrError);

            // 销毁子进程
            if (process != null) {
                process.destroy();
            }
        }

        // 返回执行结果
        //return result.toString();
        return listResult;
    }

    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
                // nothing
            }
        }
    }

    /**
     * @param args netcfg命令拷贝到usr/bin目录下，并赋予sudo无口令权限。
     */
    public static void main(String[] args) {
        List<String> result = null;
        try {
            result = LinuxCmdEnginUtil.execCmd("java -version", null);
        } catch (Exception e) {
            System.out.println("Exception ：" + e.getMessage());
        }
        for (int i = 0; i < result.size(); i++) {
            System.out.println(result.get(i));
        }

        // String renstr = LinuxCmdEngin.processUseSudo("/sbin/reboot -f ");

        // System.out.println("LinuxCmdEngin.processUseSudo('args[0]')返回结果：" +
        // renstr);

        // TODO Auto-generated method stub
        // 运行时的命令行：java isc/tsa/test/LinuxCmdEngin
        // "netcfg eth0 192.168.104.101 netmask 255.255.255.0 gateway 192.168.104.254"
        // 获取Linux默认网关：route -n |grep eth0 |grep UG |awk '{print $2}'
        /*
         * String renstr = ""; String osName = System.getProperty("os.name"); System.out.println("args.length===============" + args.length); System.out.println("osName===============" + osName); if (osName.equals("WindowsNT") || osName.equals("Windows Vista")) { if (args.length > 0) { renstr = LinuxCmdEngin.processWinCmd(args[0]); } else { // renstr = // LinuxCmdEngin.processWinCmd("wmic process get name"); renstr = LinuxCmdEngin.processWinCmd("ipconfig /all"); } System.out.println("LinuxCmdEngin.processWinCmd()返回结果：" + renstr);
         *
         * } else {
         *
         * if (args.length > 0) { renstr = LinuxCmdEngin.processUseSudo(args[0]); } else { renstr = LinuxCmdEngin.processUseSudo("ifconfig "); } System.out.println("LinuxCmdEngin.processUseSudo('args[0]')返回结果：" + renstr);
         *
         * if (args.length > 0) { renstr = LinuxCmdEngin.processUserCmd(args[0]); } else { renstr = LinuxCmdEngin.processUserCmd("ifconfig "); } System.out.println("LinuxCmdEngin.processUserCmd('args[0]')返回结果：" + renstr);
         *
         * System.out.println(); System.out.println("=============测试获取Linux网卡信息================="); String[][] netInfos = IpUtil.getNetInfo(); for (int i = 0; i < netInfos.length; i++) { System.out.println("netInfos[" + i + "][0]:" + netInfos[i][0]); System.out.println("netInfos[" + i + "][1]:" + netInfos[i][1]); System.out.println("netInfos[" + i + "][2]:" + netInfos[i][2]); System.out.println("netInfos[" + i + "][3]:" + netInfos[i][3]); // System.out.println("gateway=" + gateway[1]); } } String command = "/home/tomcat/tomcat/bin/shutdown.sh "; String rtnstr = LinuxCmdEngin.processUseSudo(command); int result = Integer.parseInt(rtnstr.substring(0, 1)); System.out.println(result);
         */

    }

    public static ResultVo getInfo(String cmd) throws IOException, InterruptedException {

        log.info("命令： {}", cmd);
        String[] comands = new String[]{"/bin/sh", "-c", cmd};

        Process process = null;
        BufferedReader bufrIn = null;
        BufferedReader bufrError = null;
        try {
            process = Runtime.getRuntime().exec(comands);


            // 方法阻塞, 等待命令执行完成（成功会返回0）
            process.waitFor();

            // 获取命令执行结果, 有两个结果: 正常的输出 和 错误的输出（PS: 子进程的输出就是主进程的输入）
            bufrIn = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));

            String line;
            StringBuilder str = new StringBuilder();
            while ((line = bufrError.readLine()) != null) {
                str.append(line).append("\n");

            }
            if (!ObjectUtils.isEmpty(str.toString())) {
                log.error("获取配置,{}", str.toString());
                return ResultUtil.fail(ExceptionEnum.LINUX_ERROR, str.toString());
            }

            while ((line = bufrIn.readLine()) != null) {
                str.append(line).append("\n");
            }
            return ResultUtil.success(str.toString());
        } finally {
            closeStream(bufrIn);
            closeStream(bufrError);

            // 销毁子进程
            if (process != null) {
                process.destroy();
            }
        }
    }

    public static ResultVo execLinux(String cmd, String dir) throws IOException, InterruptedException {

        log.info("命令： {}", cmd);
        String[] comands = new String[]{"/bin/sh", "-c", cmd};
        String[] envp = new String[]{dir};
        Process process = null;
        BufferedReader bufrIn = null;
        BufferedReader bufrError = null;
        try {
            process = Runtime.getRuntime().exec(comands, envp);

            // 方法阻塞, 等待命令执行完成（成功会返回0）
            process.waitFor();

            // 获取命令执行结果, 有两个结果: 正常的输出 和 错误的输出（PS: 子进程的输出就是主进程的输入）
            bufrIn = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
            bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(), "GBK"));

            String line;
            while ((line = bufrError.readLine()) != null) {
                log.error("获取配置,{}", line);
                return ResultUtil.fail(ExceptionEnum.LINUX_ERROR, line);
            }

            StringBuilder str = new StringBuilder();
            while ((line = bufrIn.readLine()) != null) {
                str.append(line).append("\n");
            }
            return ResultUtil.success(str.toString());
        } finally {
            closeStream(bufrIn);
            closeStream(bufrError);

            // 销毁子进程
            if (process != null) {
                process.destroy();
            }
        }
    }

    public static ResultVo listInfo(String cmd) throws IOException, InterruptedException {

        log.info("命令： {}", cmd);
        String[] comands = new String[]{"/bin/sh", "-c", cmd};

        Process process = null;
        BufferedReader bufrIn = null;
        BufferedReader bufrError = null;
        try {
            process = Runtime.getRuntime().exec(comands);

            // 方法阻塞, 等待命令执行完成（成功会返回0）
            process.waitFor();

            // 获取命令执行结果, 有两个结果: 正常的输出 和 错误的输出（PS: 子进程的输出就是主进程的输入）
            bufrIn = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));

            String line;
            while ((line = bufrError.readLine()) != null) {
                log.error("获取配置,{}", line);
                return ResultUtil.fail(ExceptionEnum.LINUX_ERROR, line);
            }

            ArrayList<String> list = new ArrayList<>();
            while ((line = bufrIn.readLine()) != null) {
                list.add(line);
            }
            return ResultUtil.success(list);
        } finally {
            closeStream(bufrIn);
            closeStream(bufrError);

            // 销毁子进程
            if (process != null) {
                process.destroy();
            }
        }
    }

    public static ResultVo listInfoFromDir(String cmd, String dir) throws IOException, InterruptedException {

        Process process = null;
        BufferedReader bufrIn = null;
        BufferedReader bufrError = null;
        try {
            process = Runtime.getRuntime().exec(cmd, null, new File(dir));

            // 方法阻塞, 等待命令执行完成（成功会返回0）
            process.waitFor();

            // 获取命令执行结果, 有两个结果: 正常的输出 和 错误的输出（PS: 子进程的输出就是主进程的输入）
            bufrIn = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));

            String line;
            while ((line = bufrError.readLine()) != null) {
                log.error("获取配置,{}", line);
                return ResultUtil.fail(ExceptionEnum.LINUX_ERROR, line);
            }
            StringBuilder str = new StringBuilder();
            while ((line = bufrIn.readLine()) != null) {
                str.append(line).append("\n");
            }
            return ResultUtil.success( str.toString());
        } finally {
            closeStream(bufrIn);
            closeStream(bufrError);

            // 销毁子进程
            if (process != null) {
                process.destroy();
            }
        }
    }


}
