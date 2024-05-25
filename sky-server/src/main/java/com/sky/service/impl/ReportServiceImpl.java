package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 统计指定时间区间内的营业额数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 用于存放从begin到end范围内的每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        // 用于存放从begin到end范围内的每天的营业额
        List<BigDecimal> turnoverList = new ArrayList<>();
        begin.datesUntil(end.plusDays(1)).forEach(e -> {
            dateList.add(e);
            BigDecimal turnover = orderMapper.getTurnoverByTime(
                    LocalDateTime.of(e, LocalTime.MIN),
                    LocalDateTime.of(e, LocalTime.MAX)
            );
            turnoverList.add(turnover == null ? BigDecimal.ZERO : turnover);
        });
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 统计指定时间区间内的用户数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 用于存放从begin到end范围内的每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        // 用于存放从begin到end范围内的截至每天的注册用户总数
        List<Integer> totalUserList = new ArrayList<>();
        // 用于存放从begin到end范围内的每天新增的注册用户数
        List<Integer> newUserList = new ArrayList<>();
        begin.datesUntil(end.plusDays(1)).forEach(e -> {
            dateList.add(e);
            Integer newUser = userMapper.countByCreateTime(
                    LocalDateTime.of(e, LocalTime.MIN),
                    LocalDateTime.of(e, LocalTime.MAX)
            );
            totalUserList.add((totalUserList.isEmpty() ? 0 : totalUserList.get(totalUserList.size() - 1)) + newUser);
            newUserList.add(newUser);
        });
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    /**
     * 统计指定时间区间内的订单数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        // 用于存放从begin到end范围内的每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        // 用于存放从begin到end范围内的每天的订单数
        List<Integer> orderCountList = new ArrayList<>();
        // 用于存放从begin到end范围内的每天的有效订单数
        List<Integer> validOrderCountList = new ArrayList<>();
        // totalCnt[0]: 订单总数, totalCnt[1]: 有效订单总数
        int[] totalCnt = new int[2];
        begin.datesUntil(end.plusDays(1)).forEach(e -> {
            dateList.add(e);
            LocalDateTime beginTime = LocalDateTime.of(e, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(e, LocalTime.MAX);
            Integer orderCount = orderMapper.countByTimeAndStatus(beginTime, endTime, null);
            Integer validOrderCount = orderMapper.countByTimeAndStatus(beginTime, endTime, Orders.COMPLETED);
            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
            totalCnt[0] += orderCount;
            totalCnt[1] += validOrderCount;
        });
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalCnt[0])
                .validOrderCount(totalCnt[1])
                .orderCompletionRate(totalCnt[0] == 0 ? 0.0 : (1.0 * totalCnt[1] / totalCnt[0]))
                .build();
    }

    /**
     * 统计指定时间区间内的销量排名前10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        // 如果没有查到任何数据，直接返回空结果
        if (salesTop10 == null || salesTop10.isEmpty()) {
            return SalesTop10ReportVO.builder()
                    .nameList("")
                    .numberList("")
                    .build();
        }

        StringBuilder nameList = new StringBuilder();
        StringBuilder numberList = new StringBuilder();
        salesTop10.forEach(e -> {
            nameList.append(e.getName()).append(",");
            numberList.append(e.getNumber()).append(",");
        });
        nameList.deleteCharAt(nameList.length() - 1);
        numberList.deleteCharAt(numberList.length() - 1);

        return SalesTop10ReportVO.builder()
                .nameList(nameList.toString())
                .numberList(numberList.toString())
                .build();
    }

    /**
     * 导出运营数据报表
     * @param response
     */
    @Override
    public void exportBusisnessData(HttpServletResponse response) {
        // 查询数据库，获取营业数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN),
                LocalDateTime.of(dateEnd, LocalTime.MAX));

        // 通过POI将数据写入Excel文件中
        try (
                // 基于模板创建一个新的Excel文件
                XSSFWorkbook excel = new XSSFWorkbook(this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx"));
                ServletOutputStream out = response.getOutputStream();
                ) {
            // 获取表格文件的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            // 填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间："  + dateBegin + "至" + dateEnd);

            // 获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover().doubleValue());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            // 获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            // 填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                // 查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX)
                );

                // 获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover().doubleValue());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            // 通过输出流将Excel文件下载到客户端浏览器
            excel.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 通过输出流将Excel文件下载到客户端浏览器

    }
}
