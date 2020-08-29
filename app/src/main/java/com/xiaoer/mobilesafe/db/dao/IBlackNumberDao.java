package com.xiaoer.mobilesafe.db.dao;

import com.xiaoer.mobilesafe.entity.BlackNumberInfo;

import java.util.List;

public interface IBlackNumberDao {

    /**
     * @return 返回数据库数据总数
     */
    int getTotalCount();

    /**
     * 插入一条数据
     */
    boolean insert(String phone,int model);

    /**
     * 删除指定电话号的数据
     */
    boolean deleteByPhone(String phone);

    /**
     * 更新指定电话号的拦截模式
     */
    boolean updateModelByPhone(String phone,int model);

    /**
     * 查询全部
     */
    List<BlackNumberInfo> queryAll();

    /**
     * 根据索引查询20条数据
     */
    List<BlackNumberInfo> queryByIndex(int index);

    /**
     * @param phone 电话号
     * @return 判断某个电话号是否存在 true:存在 false:不存在
     */
    boolean queryByPhone(String phone);

}
