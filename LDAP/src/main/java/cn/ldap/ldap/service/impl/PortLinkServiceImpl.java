package cn.ldap.ldap.service.impl;

import cn.ldap.ldap.common.entity.PortLink;
import cn.ldap.ldap.common.mapper.PortLinkMapper;
import cn.ldap.ldap.common.util.ResultUtil;
import cn.ldap.ldap.common.vo.NetstatVo;
import cn.ldap.ldap.common.vo.ResultVo;
import cn.ldap.ldap.service.PortLinkService;
import com.baomidou.mybatisplus.core.conditions.interfaces.Nested;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.ResultType;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class PortLinkServiceImpl extends ServiceImpl<PortLinkMapper, PortLink> implements PortLinkService {

    @Override
    public ResultVo<List<PortLink>> getPortLinkList() {
        List<PortLink> list = list();
        for (PortLink portLink : list) {
        try {
            Process process = Runtime.getRuntime().exec("netstat -ntlp");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                NetstatVo netstatVo = parseLine(line);
                if (portLink.getPort().equals(String.valueOf(netstatVo.getPort()))){
                    portLink.setStatus(String.valueOf(true));
                    break;
                }else {
                    portLink.setStatus(String.valueOf(false));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        }

        return ResultUtil.success(list);
    }

    public NetstatVo parseLine(String line) {
        // Define the regex pattern to match the netstat output
        Pattern pattern = Pattern.compile("^(tcp|udp)\\s+\\d+\\s+\\d+\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+):(\\d+)\\s+.*\\s+(\\d+)/.*$");
        Matcher matcher = pattern.matcher(line);
        NetstatVo netstatVo = new NetstatVo();
        if (matcher.matches()) {


            String protocol = matcher.group(1);
            netstatVo.setProtocol(protocol);

            String localAddress = matcher.group(2);
            netstatVo.setLocalAddress(localAddress);

            int localPort = Integer.parseInt(matcher.group(3));
            netstatVo.setPort(localPort);

            int pid = Integer.parseInt(matcher.group(4));
            netstatVo.setPid(pid);

            log.info("Protocol:{}",protocol);
            log.info("Local Address:{}",localAddress);
            log.info("Local Port:{}",localPort);
            log.info("Pid:{}",pid);

            //System.out.printf("Protocol: %s, Local Address: %s, Local Port: %d, PID: %d\n", protocol, localAddress, localPort, pid);
        }
        return netstatVo;
    }
}
