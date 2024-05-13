package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询对应的套餐id
     * @param dishIds 菜品id
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

//    /**
//     * 新增套餐-菜品关系
//     * @param setmealDish
//     */
//    @Insert("insert into setmeal_dish (setmeal_id, dish_id, name, price, copies) values " +
//            "(#{setmealId}, #{dishId}, #{name}, #{price}, #{copies})")
//    void insert(SetmealDish setmealDish);

    /**
     * 批量插入套餐-菜品关系数据
     * @param setmealDishList
     */
    void insertBatch(List<SetmealDish> setmealDishList);

    /**
     * 根据套餐id批量删除套餐-菜品关系
     * @param setmealIds
     */
    void deleteBySetmealIds(List<Long> setmealIds);

    /**
     * 根据套餐id查询套餐-菜品信息
     * @param setmealId
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long setmealId);
}
