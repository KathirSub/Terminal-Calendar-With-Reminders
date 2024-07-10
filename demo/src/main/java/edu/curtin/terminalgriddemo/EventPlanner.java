package edu.curtin.terminalgriddemo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class EventPlanner {
    private String title;
    private LocalDate startDate;
    private LocalTime startTime;
    private int durationMinutes;
    private boolean allDay;

    public EventPlanner(String title, LocalDate startDate, LocalTime startTime, int durationMinutes, boolean allDay) {
        this.title = title;
        this.startDate = startDate;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.allDay = allDay;
    }

    

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public LocalTime getStartDateTime() {
        return startTime;
    }

    public boolean isWithinTimeRange(LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (allDay) {
            // For all-day events, check if the event's date matches the specified date
            if (this.startDate.equals(date)) {
                return true; // All-day events span the entire day
            }
            return false;
        }

        if (this.startTime == null || startTime == null) {
            return false; // Cannot check time range without a start time
        }

        LocalTime eventEndTime = this.startTime.plusMinutes(this.durationMinutes);
        if (this.startDate.equals(date)) {
            if ((this.startTime.isBefore(endTime) && eventEndTime.isAfter(startTime)) ||
                    (this.startTime.equals(startTime) || eventEndTime.equals(endTime))) {
                // Event overlaps with or is contained within the time range
                return true;
            }
        }
        return false;
    }

    public static EventPlanner parseEvent(String eventStr) {
        String[] parts = eventStr.split(" ");
        if (parts.length >= 3 && parts[0].equals("event")) {
            String title = eventStr.substring(eventStr.indexOf("\"") + 1, eventStr.lastIndexOf("\""));
            LocalDate startDate = LocalDate.parse(parts[1]);

            LocalTime startTime = null;
            int durationMinutes = 0;
            boolean allDay = false;

            for (int i = 2; i < parts.length; i++) {
                if (parts[i].matches("\\d{2}:\\d{2}:\\d{2}")) {
                    startTime = LocalTime.parse(parts[i]);
                    durationMinutes = Integer.parseInt(parts[i + 1]);
                    break;
                } else if (parts[i].equals("all-day")) {
                    allDay = true;
                    break;
                }
            }

            if (durationMinutes == 0) {
                startTime = LocalTime.parse(parts[2]); // Assuming the time is at a specific index in parts array
            }

            return new EventPlanner(title, startDate, startTime, durationMinutes, allDay);
        }
        return null; //  for invalid input
    }

    @Override
    public String toString() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        if (allDay) {
            return title + " (All-Day)";
        } else {
            return title + " (from " + startTime.format(timeFormatter) + " for " + durationMinutes + " minutes)";
        }
    }


    public boolean isWithinDateRange(LocalDate startDate, LocalDate endDate) {
        return this.startDate.isEqual(startDate) || (this.startDate.isAfter(startDate) && this.startDate.isBefore(endDate));
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public String getTitle() {
        return title;
    }
    public boolean isAllDay() {
        return allDay;
    }
}
