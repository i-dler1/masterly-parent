package com.masterly.web.controller;

import com.masterly.web.client.CoreServiceClient;
import com.masterly.web.dto.AppointmentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/appointments/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CoreServiceClient coreServiceClient;

    @GetMapping
    public String showCalendar(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().toString()}") String date,
            @RequestParam(defaultValue = "week") String view,
            Model model) {

        log.debug("Showing calendar - date: {}, view: {}", date, view);

        // Маппинг месяцев для русского языка (именительный падеж)
        Map<Integer, String> monthNames = new HashMap<>();
        monthNames.put(1, "Январь");
        monthNames.put(2, "Февраль");
        monthNames.put(3, "Март");
        monthNames.put(4, "Апрель");
        monthNames.put(5, "Май");
        monthNames.put(6, "Июнь");
        monthNames.put(7, "Июль");
        monthNames.put(8, "Август");
        monthNames.put(9, "Сентябрь");
        monthNames.put(10, "Октябрь");
        monthNames.put(11, "Ноябрь");
        monthNames.put(12, "Декабрь");

        Long masterId = 1L;
        LocalDate currentDate = LocalDate.parse(date);
        LocalDate today = LocalDate.now();

        LocalDate startDate;
        LocalDate endDate;

        String monthName = monthNames.get(currentDate.getMonthValue());
        model.addAttribute("monthName", monthName);
        model.addAttribute("year", currentDate.getYear());
        model.addAttribute("today", today);

        if ("month".equals(view)) {
            LocalDate firstDayOfMonth = currentDate.withDayOfMonth(1);
            LocalDate lastDayOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth());

            // Находим первый понедельник месяца (для заполнения пустых ячеек)
            int firstDayOfWeekValue = firstDayOfMonth.getDayOfWeek().getValue(); // 1=пн, 7=вс
            int offset = firstDayOfWeekValue - 1;

            startDate = firstDayOfMonth.minusDays(offset);
            endDate = lastDayOfMonth.plusDays(7 - lastDayOfMonth.getDayOfWeek().getValue());

            log.debug("Month view - start: {}, end: {}", startDate, endDate);

            model.addAttribute("offset", offset);
            model.addAttribute("firstDayOfMonth", firstDayOfMonth);
            model.addAttribute("lastDayOfMonth", lastDayOfMonth);
        } else {
            startDate = currentDate.minusDays(currentDate.getDayOfWeek().getValue() - 1);
            endDate = startDate.plusDays(6);
            log.debug("Week view - start: {}, end: {}", startDate, endDate);
        }

        List<AppointmentDto> appointments = coreServiceClient.getAppointmentsByDateRange(
                startDate.toString(),
                endDate.toString(),
                masterId
        );

        log.debug("Found {} appointments in date range", appointments.size());

        model.addAttribute("appointments", appointments);
        model.addAttribute("currentDate", currentDate);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("view", view);

        if ("month".equals(view)) {
            // Создаем список всех дней в отображаемом диапазоне
            List<LocalDate> daysInRange = new ArrayList<>();
            LocalDate day = startDate;
            while (!day.isAfter(endDate)) {
                daysInRange.add(day);
                day = day.plusDays(1);
            }
            model.addAttribute("daysInRange", daysInRange);
            log.debug("Generated {} days for month view", daysInRange.size());
        }

        return "appointments/calendar";
    }
}