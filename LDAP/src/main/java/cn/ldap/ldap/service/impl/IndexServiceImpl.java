package cn.ldap.ldap.service.impl;

import cn.ldap.ldap.common.dto.DeviceStatusRespVo;
import cn.ldap.ldap.common.dto.NetSpeedRespVo;
import cn.ldap.ldap.common.util.NetWorkUtil;
import cn.ldap.ldap.service.IndexService;
import com.google.common.collect.EvictingQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * @title: IndexServiceImpl
 * @Author Wy
 * @Date: 2023/3/31 13:30
 * @Version 1.0
 */
@Service
@Slf4j
public class IndexServiceImpl implements IndexService {
    @Override
    public Object list() {
        return null;
    }

    /**
     * 获取设备状态信息
     *
     * @return
     */
    @Override
    public DeviceStatusRespVo listDeviceStatus() {
        log.info("获取设备状态信息:");
        float cpuInfo = NetWorkUtil.getCpuInfo();
        log.info("获取cpu信息:" + cpuInfo);
        int diskInfo = 0;
        int memInfo = 0;
        try {
             memInfo = NetWorkUtil.getMemInfo();
            log.info("获取内存信息:" + memInfo);
        } catch (Exception e) {
            memInfo = 0;
            log.error("获取内存信息:" + 0);
        }
        float diskInfos;
        try {
            diskInfos =NetWorkUtil.getDiskInfo();
            log.info("获取硬盘信息:" + diskInfos);
        } catch (IOException e) {
            log.error("获取硬盘信息:" + e.getMessage());
            diskInfos = 0f;
        } catch (InterruptedException e) {
            log.error("获取硬盘信息:" + e.getMessage());
            diskInfos = 0f;
        }
        DeviceStatusRespVo deviceStatusRespVo = new DeviceStatusRespVo();
        deviceStatusRespVo.setCpuRate(cpuInfo);
        deviceStatusRespVo.setMemoryRate(memInfo);
        deviceStatusRespVo.setServerStatus(true);
        log.info("获取设备状态信息:" + deviceStatusRespVo);
        return deviceStatusRespVo;
    }

    /**
     * 获取网络吞吐量
     *
     * @return
     */

    EvictingQueue<NetSpeedRespVo> queue = EvictingQueue.create(10);

    @Override
    public EvictingQueue<NetSpeedRespVo> getNetSpeed() {
        log.info("获取网络吞吐量");
        Map<String, String> netWorkDownUp = NetWorkUtil.getNetWorkDownUp();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        if (queue.size() == 0) {
            for (int j = 10; j > 0; j--) {
                NetSpeedRespVo netSpeedRespVo;
                if (j == 1) {
                    netSpeedRespVo = new NetSpeedRespVo();
                    netSpeedRespVo.setDateTime(now.format(dateTimeFormatter));
                    netSpeedRespVo.setDownSpeed(netWorkDownUp.get("rxPercent"));
                    netSpeedRespVo.setUpSpeed(netWorkDownUp.get("txPercent"));
                }
                else {
                    netSpeedRespVo = new NetSpeedRespVo();
                    String format = now.minusSeconds((j - 1) * 5).format(dateTimeFormatter);
                    netSpeedRespVo.setDateTime(format);
                    netSpeedRespVo.setDownSpeed("0");
                    netSpeedRespVo.setUpSpeed("0");
                }
                queue.add(netSpeedRespVo) ;
            }
        }
        NetSpeedRespVo netSpeedRespVo=new NetSpeedRespVo();
        netSpeedRespVo.setDateTime(now.format(dateTimeFormatter));
        netSpeedRespVo.setDownSpeed(netWorkDownUp.get("rxPercent"));
        netSpeedRespVo.setUpSpeed(netWorkDownUp.get("txPercent"));
        queue.add(netSpeedRespVo);
        log.info("获取网络吞吐量："+netSpeedRespVo);
        return queue;
    }
}




