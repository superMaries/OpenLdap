package cn.ldap.ldap.common.vo;

import lombok.Data;

import java.util.List;

/**
 * @title: pageVo
 * @Author Wy
 * @Date: 2023/3/31 16:52
 * @Version 1.0
 */
@Data
public class PageVo<T> {
    /**
     * 页码
     */
    public Long pageIndex=1L;
    /**
     * 条数
     */
    public Long pageSize=10L;
    /**
     * 总数
     */
    public Long total;
    /**
     * 显示的数据集合
     */
    public List<T> list;

    public PageVo(){super();}

    /**
     *
     * @param pageIndex 页码
     * @param pageSize  条数
     * @param total 总数
     * @param list 显示的数据
     */
    public PageVo(Long pageIndex,Long pageSize,Long total,List list){
        this.pageSize=pageSize;
        this.pageIndex=pageIndex;
        this.total=total;
        this.list=list;
    }

}
