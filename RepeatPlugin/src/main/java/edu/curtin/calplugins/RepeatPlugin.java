package edu.curtin.calplugins;

import edu.curtin.terminalgriddemo.PluginInterface;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RepeatPlugin implements PluginInterface {
    @Override
    public List<String> processPlugin(String[] pluginParts) {
        List<String> events = new ArrayList<>();

        if (pluginParts.length >= 4) {
            String pluginData = String.join(" ", pluginParts);

            String title = extractValue(pluginData, "title:");
            LocalDate startDate = LocalDate.parse(extractValue(pluginData, "startDate:"), DateTimeFormatter.ofPattern("uuuu-MM-dd"));
            int repeat = Integer.parseInt(extractValue(pluginData, "repeat:"));

            // Check if it's a normal plugin event
            if (pluginData.contains("duration:")) {


                LocalTime startTime = LocalTime.parse(extractValue(pluginData, "startTime:"), DateTimeFormatter.ofPattern("HH:mm:ss"));
                int duration = Integer.parseInt(extractValue(pluginData, "duration:"));

                LocalDate currentDate = startDate;

                while (currentDate.isBefore(startDate.plusYears(1))) {
                    events.add(String.format("event %s %s %d \"%s\"", currentDate, startTime, duration, title));
                    currentDate = currentDate.plusDays(repeat);
                }
            } else {
                // All-day plugin event
                int duration = 0;
                LocalTime startTime = LocalTime.MIN; // Set a dummy value

                LocalDate currentDate = startDate;

                while (currentDate.isBefore(startDate.plusYears(1))) {
                    events.add(String.format("event %s %s %d \"%s\" all-day", currentDate, startTime, duration, title));
                    currentDate = currentDate.plusDays(repeat);
                }
            }
        } else {
            System.out.println("Invalid Plugin Event Data");
        }

        return events;
    }

    // Extract the value for a given key using regex
    private String extractValue(String text, String key) {
        Pattern pattern = Pattern.compile(key + "\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    @Override
    public void notify(String text) {
    }

}