package com.ncpl.sales.cashflow.dto;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExecutiveSummaryDTO {

    private YearMonth month;
    private Integer week;
    private double totalInflow;
    private double totalOutflow;
    private double netCashFlow;
    private double overdueAmount;
    private int overdueCount;
    private double upcomingAmount;
    private int upcomingCount;
    private List<String> highlights = new ArrayList<>();
    private List<ActionItem> actionItems = new ArrayList<>();
    private List<CalendarEvent> calendarEvents = new ArrayList<>();

    public YearMonth getMonth() {
        return month;
    }

    public void setMonth(YearMonth month) {
        this.month = month;
    }

    public Integer getWeek() {
        return week;
    }

    public void setWeek(Integer week) {
        this.week = week;
    }

    public double getTotalInflow() {
        return totalInflow;
    }

    public void setTotalInflow(double totalInflow) {
        this.totalInflow = totalInflow;
    }

    public double getTotalOutflow() {
        return totalOutflow;
    }

    public void setTotalOutflow(double totalOutflow) {
        this.totalOutflow = totalOutflow;
    }

    public double getNetCashFlow() {
        return netCashFlow;
    }

    public void setNetCashFlow(double netCashFlow) {
        this.netCashFlow = netCashFlow;
    }

    public double getOverdueAmount() {
        return overdueAmount;
    }

    public void setOverdueAmount(double overdueAmount) {
        this.overdueAmount = overdueAmount;
    }

    public int getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(int overdueCount) {
        this.overdueCount = overdueCount;
    }

    public double getUpcomingAmount() {
        return upcomingAmount;
    }

    public void setUpcomingAmount(double upcomingAmount) {
        this.upcomingAmount = upcomingAmount;
    }

    public int getUpcomingCount() {
        return upcomingCount;
    }

    public void setUpcomingCount(int upcomingCount) {
        this.upcomingCount = upcomingCount;
    }

    public List<String> getHighlights() {
        return highlights;
    }

    public void setHighlights(List<String> highlights) {
        this.highlights = (highlights != null ? highlights : new ArrayList<>());
    }

    public List<ActionItem> getActionItems() {
        return actionItems;
    }

    public void setActionItems(List<ActionItem> actionItems) {
        this.actionItems = (actionItems != null ? actionItems : new ArrayList<>());
    }

    public List<CalendarEvent> getCalendarEvents() {
        return calendarEvents;
    }

    public void setCalendarEvents(List<CalendarEvent> calendarEvents) {
        this.calendarEvents = (calendarEvents != null ? calendarEvents : new ArrayList<>());
    }

    public static class ActionItem {
        private String title;
        private String description;
        private LocalDate dueDate;
        private String priority;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }

        public void setDueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
        }

        public String getPriority() {
            return priority;
        }

        public void setPriority(String priority) {
            this.priority = priority;
        }
    }

    public static class CalendarEvent {
        private LocalDate date;
        private String label;
        private String type;

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}

