package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号和用户id查询订单
     * @param orderNumber
     * @param userId
     * @return
     */
    @Select("select * from orders where number = #{orderNumber} and user_id = #{userId}")
    Orders getByNumberAndUserId(String orderNumber, Long userId);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     * @return
     */
    @Select("select id from orders where number = #{orderNumber}")
    Long getOrderId(String orderNumber);


    /**
     * 根据订单主键id更新订单状态
     * @param orderStatus
     * @param orderPaidStatus
     * @param checkoutTime
     * @param id
     */
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime checkoutTime, Long id);

    /**
     * 订单分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单
     * @param id
     */
    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);

    /**
     * 根据状态统计订单数量
     * @param status
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer status);

    /**
     * 根据订单状态和下单时间查询订单
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTimeTL(Integer status, LocalDateTime orderTime);

    /**
     * 统计给定时间内的营业额并返回
     * @param beginTime
     * @param endTime
     * @return
     */
    @Select("select sum(amount) from orders " +
            "where order_time between #{beginTime} and #{endTime} and status = 5")
    BigDecimal getTurnoverByTime(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 统计给定时间内的给定状态的订单数
     * @param beginTime
     * @param endTime
     * @return
     */
    Integer countByTimeAndStatus(LocalDateTime beginTime, LocalDateTime endTime, Integer status);

    /**
     * 统计指定时间区间内的销量排名前10
     * @param beginTime
     * @param endTime
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime beginTime, LocalDateTime endTime);
}
