// src/main/java/com/example/demo/service/StatsService.java
package com.example.demo.service;

import com.example.demo.dto.stats.RevenueDataPointDTO;
import com.example.demo.dto.stats.StatsDTO;

import java.time.LocalDate;
import java.util.List;


public interface StatsService {


    StatsDTO getBasicStats(LocalDate startDate, LocalDate endDate);

    StatsDTO getTodayStats();

    StatsDTO getLast7DaysStats();

    StatsDTO getCurrentMonthStats();

    List<RevenueDataPointDTO> getRevenueOverTime(LocalDate startDate, LocalDate endDate);

}