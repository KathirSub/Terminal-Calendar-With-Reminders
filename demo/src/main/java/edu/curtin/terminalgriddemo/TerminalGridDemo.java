package edu.curtin.terminalgriddemo;

import java.util.Scanner;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import edu.curtin.calplugins.RepeatPlugin;
import edu.curtin.terminalgrid.TerminalGrid;

public class TerminalGridDemo {


    private static Locale currentLocale = Locale.getDefault();
    private static ResourceBundle messages = ResourceBundle.getBundle("messages", currentLocale);
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("E, MMM dd, uuuu", currentLocale);
    private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", currentLocale);

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println(messages.getString("usage"));
            return;
        }

        String inputFileName = args[0];

        List<EventPlanner> events = readEventsFromFile(inputFileName);

        LocalDate currentDate = LocalDate.now();
        var terminalGrid = TerminalGrid.create(System.out, 125);


        try (Scanner scanner = new Scanner(System.in)) {

            boolean quit = false;

            while (!quit) {


                displayEventsForSevenDays(terminalGrid, events, currentDate);

                System.out.println(messages.getString("promptCommand"));
                String command = scanner.nextLine().trim();

                // Normalize user input
                command = Normalizer.normalize(command, Normalizer.Form.NFC);

                if (command.equalsIgnoreCase(messages.getString("commandQuit"))) {
                    quit = true;
                    System.out.println("Exiting the Event Planner. Goodbye!");
                    System.exit(0); // Terminate the application
                } else if (command.equalsIgnoreCase(messages.getString("commandSearch"))) {
                    currentDate = handleSearchCommand(events, currentDate, scanner);
                } else if (command.equalsIgnoreCase(messages.getString("commandLocale"))) {
                    selectLocale(scanner);
                } else {
                    currentDate = handleDateCommand(currentDate, command);
                }
            }
        }
    }

    public static List<EventPlanner> readEventsFromFile(String fileName) {
        List<EventPlanner> events = new ArrayList<>();

    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    PluginInterface repeatPlugin = new RepeatPlugin();
    ScriptHandler scriptHandler = new ScriptHandler();
    Charset charset = getCharsetFromFileName(fileName);

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), charset))) {
        String line;
        String title = "";
        LocalDate startDate = null;
        LocalTime startTime = null;
        int durationMinutes = 0;
        boolean allDay = false;

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("event")) {
                String[] parts = line.split(" ");
                int partIndex = 1;

                if (parts.length >= 2) {
                    startDate = LocalDate.parse(parts[partIndex++]);
                }

                if (partIndex < parts.length) {
                    if (parts[partIndex].matches("\\d{2}:\\d{2}:\\d{2}")) {
                        startTime = LocalTime.parse(parts[partIndex++]);
                        durationMinutes = Integer.parseInt(parts[partIndex++]);
                    } else if (parts[partIndex].equals("all-day")) {
                        allDay = true;
                        partIndex++;
                    }

                    if (allDay && partIndex < parts.length) {
                        title = extractTitleFromLine(line);
                    }

                    if (!allDay && partIndex < parts.length) {
                        title = extractTitleFromLine(line);
                    }

                    if (allDay) {

                        startTime = LocalTime.of(0, 0);
                    }

                    events.add(new EventPlanner(title, startDate, startTime, durationMinutes, allDay));
                }
            }  else if (line.startsWith("plugin edu.curtin.calplugins.Repeat")) {
            String[] pluginParts = line.split("\\s+");
            List<String> repeatingEvents = repeatPlugin.processPlugin(pluginParts);

            // Debugging: Print out events obtained from the Repeat Plugin
            /*System.out.println("Events obtained from Repeat Plugin:");
            for (String event : repeatingEvents) {
                System.out.println(event);
            }*/

            for (String event : repeatingEvents) {
                EventPlanner eventPlanner = EventPlanner.parseEvent(event);
                if (eventPlanner != null) {
                    events.add(eventPlanner);
                }
            }
            } if (line.startsWith("plugin edu.curtin.calplugins.Notify")) {
                String[] pluginParts = line.split("\\s+");
                String text = extractTextFromLine(line);

                for (EventPlanner event : events) {
                    if (event.getTitle().equals(text)) {
                        scheduleNotification(executor, event);
                        break;
                    }
                }
            } else if (line.startsWith("script")) {
                 //Process script declaration
                String script = extractScriptFromLine(line);
                scriptHandler.runScript(script);
            }
        }
    } catch (IOException e) {
        System.out.println("Exception" + e);
    }

    return events;
}

    private static String extractTextFromLine(String line) {
        int startIndex = line.indexOf("\"");
        int endIndex = line.lastIndexOf("\"");
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return line.substring(startIndex + 1, endIndex);
        }
        return "";
    }

    private static String extractScriptFromLine(String line) {
        int startIndex = line.indexOf("\"");
        int endIndex = line.lastIndexOf("\"");
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return line.substring(startIndex + 1, endIndex);
        }
        return "";
    }

    private static void scheduleNotification(ScheduledExecutorService executor, EventPlanner event) {
        LocalDateTime eventDateTime;
        if (event.isAllDay()) {
            eventDateTime = event.getStartDate().atStartOfDay(); // Set time to midnight for all-day events
        } else {
            eventDateTime = event.getStartDate().atTime(event.getStartDateTime());
        }

        long initialDelay = calculateInitialDelay(eventDateTime);

        executor.schedule(() -> notifyEvent(event), initialDelay, TimeUnit.SECONDS);
    }

    private static long calculateInitialDelay(LocalDateTime eventDateTime) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        long seconds = java.time.Duration.between(currentDateTime, eventDateTime).getSeconds();
        return seconds > 0 ? seconds : 0;
    }

    private static void notifyEvent(EventPlanner event) {
        System.out.println("Notification: Event '" + event.getTitle() + "' started at " + event.getStartDateTime());
    }

    
    // Helper method to extract the title from the line considering quotes
    private static String extractTitleFromLine(String line) {
        int startIndex = line.indexOf("\"");
        int endIndex = line.lastIndexOf("\"");
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return line.substring(startIndex + 1, endIndex);
        }
        return "";
    }
    
    
    private static Charset getCharsetFromFileName(String fileName) {
        if (fileName.endsWith(".utf16.cal")) {
            return StandardCharsets.UTF_16;
        } else if (fileName.endsWith(".utf32.cal")) {
            return Charset.forName("UTF-32");
        } else {
            return StandardCharsets.UTF_8;
        }
    }


    private static void displayEventsForSevenDays(TerminalGrid terminalGrid, List<EventPlanner> events, LocalDate currentDate) {
        LocalDate endDate = currentDate.plusDays(6); // Seven days from the current date

        List<String> colHeadings = new ArrayList<>();
        for (LocalDate date = currentDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            colHeadings.add(date.format(dateTimeFormatter));
        }

        List<List<String>> table = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            List<String> row = new ArrayList<>();
            LocalTime startTime = LocalTime.of(hour, 0);

            for (LocalDate date = currentDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                EventPlanner eventFound = null;
                boolean eventFoundFromPlugin = false;

                for (EventPlanner event : events) {
                    if (!event.isAllDay() && event.isWithinTimeRange(date, startTime, startTime.plusHours(1))) {
                        if (event.getStartDateTime().equals(startTime)
                                || (event.getStartDateTime().isAfter(startTime)
                                && event.getStartDateTime().isBefore(startTime.plusHours(1)))) {
                            eventFound = event;
                            break;
                        }
                    }
                }

                if (eventFound == null) {
                    for (EventPlanner event : events) {
                        if (!event.isAllDay() && event.getDurationMinutes() == 0 &&
                                event.isWithinTimeRange(date, startTime, startTime.plusHours(1))) {
                            if (event.getStartDateTime().equals(startTime)
                                    || (event.getStartDateTime().isAfter(startTime)
                                    && event.getStartDateTime().isBefore(startTime.plusHours(1)))) {
                                eventFound = event;
                                eventFoundFromPlugin = true;
                                break;
                            }
                        }
                    }
                }

                if (eventFound != null) {
                    if (eventFoundFromPlugin) {
                        row.add("Plugin All Day Repeat");
                    } else {
                        String eventInfo = eventFound.getStartDateTime().format(timeFormatter)
                                + " (" + eventFound.getDurationMinutes() + " mins) " + eventFound.getTitle();
                        row.add(eventInfo);
                    }
                } else {
                    row.add("");
                }
            }

            table.add(row);
        }

        List<String> allDayEventsRow = new ArrayList<>();
        for (LocalDate date = currentDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            boolean hasAllDayEvent = false;
            boolean hasAllDayRepeatPlugin = false;

            for (EventPlanner event : events) {
                if (event.isAllDay() && event.isWithinDateRange(date, date)) {
                    if (event.getDurationMinutes() == 0) {
                        hasAllDayRepeatPlugin = true;
                    } else {
                        allDayEventsRow.add(event.getTitle());
                        hasAllDayEvent = true;
                    }
                }
            }

            if (!hasAllDayEvent && !hasAllDayRepeatPlugin) {
                allDayEventsRow.add("");
            }
            if (!hasAllDayEvent && hasAllDayRepeatPlugin) {
                String allDayPluginTitle = null;
                for (EventPlanner event : events) {
                    if (event.isAllDay() && event.getDurationMinutes() == 0 && event.isWithinDateRange(date, date)) {
                        allDayPluginTitle = event.getTitle();
                        break;
                    }
                }
                if (allDayPluginTitle != null) {
                    allDayEventsRow.add(allDayPluginTitle);
                } else {
                    allDayEventsRow.add("Plugin All Day Repeat");
                }
            }
        }

        table.add(allDayEventsRow);

        List<String> timeSlots = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            timeSlots.add(String.format("%02d:00", hour));
        }

        List<String> rowHeadings = new ArrayList<>(timeSlots);
        rowHeadings.add("All Day Events");

        terminalGrid.print(table, rowHeadings, colHeadings);
    }




    private static LocalDate handleDateCommand(LocalDate currentDate, String command) {
        switch (command) {
            case "+d":
                return currentDate.plusDays(1);
            case "+w":
                return currentDate.plusWeeks(1);
            case "+m":
                return currentDate.plusMonths(1);
            case "+y":
                return currentDate.plusYears(1);
            case "-d":
                return currentDate.minusDays(1);
            case "-w":
                return currentDate.minusWeeks(1);
            case "-m":
                return currentDate.minusMonths(1);
            case "-y":
                return currentDate.minusYears(1);
            case "t":
                return LocalDate.now();
            default:
                System.out.println(messages.getString("invalidCommand"));
                return LocalDate.now();
        }
    }

    private static LocalDate handleSearchCommand(List<EventPlanner> events, LocalDate currentDate, Scanner scanner) {
        System.out.print(messages.getString("searchPrompt"));
        String searchTerm = scanner.nextLine();

        // Normalize the search term
        searchTerm = Normalizer.normalize(searchTerm, Normalizer.Form.NFC);

        boolean found = false;

        for (EventPlanner event : events) {
            String eventTitle = Normalizer.normalize(event.getTitle(), Normalizer.Form.NFC);
            if (eventTitle.contains(searchTerm) && event.getStartDate().isAfter(currentDate.minusDays(1))) {
                System.out.println(messages.getString("matchingEventDetails"));
                System.out.println(messages.getString("title") + ": " + eventTitle);
                System.out.println(messages.getString("startDate") + ": " + event.getStartDate().format(dateTimeFormatter));

                if (event.isAllDay()) {
                    System.out.println(messages.getString("allDayEvent"));
                } else {
                    System.out.println(messages.getString("startTime") + ": " + event.getStartDateTime().format(timeFormatter));
                    System.out.println(messages.getString("duration") + " (minutes): " + event.getDurationMinutes());
                }

                currentDate = event.getStartDate();
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println(messages.getString("noMatchingEvent"));
        }

        return currentDate;
    }


    private static void selectLocale(Scanner scanner) {
        System.out.print(messages.getString("enterLocale"));
        String localeTag = scanner.nextLine();
        try {
            Locale newLocale = Locale.forLanguageTag(localeTag);
            Locale.setDefault(newLocale);
            currentLocale = newLocale;
            messages = ResourceBundle.getBundle("messages", currentLocale);
            dateTimeFormatter = DateTimeFormatter.ofPattern("E, MMM dd, uuuu", currentLocale);
            timeFormatter = DateTimeFormatter.ofPattern("HH:mm", currentLocale);
            System.out.println(messages.getString("localeChanged") + " " + newLocale.toLanguageTag());
        } catch (IllegalArgumentException e) {
            System.out.println(messages.getString("invalidLocale"));
        }
    }


}