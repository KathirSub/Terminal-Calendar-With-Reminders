package edu.curtin.calplugins;

import edu.curtin.terminalgriddemo.PluginInterface;
import java.util.ArrayList;
import java.util.List;

public class NotifyPlugin implements PluginInterface {
    private String notificationText;

    public NotifyPlugin(String notificationText) {
        this.notificationText = notificationText;
    }

    @Override
    public List<String> processPlugin(String[] pluginParts) {
        List<String> eventsToNotify = new ArrayList<>();

        if (pluginParts.length >= 2) {
            String textArgument = extractValue(pluginParts, "text:");

            if (textArgument != null && textArgument.equals(notificationText)) {
                // Events matching the specified text
                for (String event : pluginParts) {
                    if (event.startsWith("event")) {
                        String title = extractValue(event.split(" "), "\"");
                        if (title.contains(notificationText)) {
                            eventsToNotify.add(event);
                        }
                    }
                }
            } else {
                System.out.println("No events found with the specified text");
            }
        }

        return eventsToNotify;
    }

    @Override
    public void notify(String text) {
        // Handle the notification action, e.g., print event details
        System.out.println("Notification: Event with text '" + notificationText + "' started");
        System.out.println("Event Details:");
        System.out.println(text);
        System.out.println("---------------------------");
    }

    private String extractValue(String[] parts, String key) {
        for (String part : parts) {
            if (part.startsWith(key)) {
                return part.substring(key.length());
            }
        }
        return null;
    }
}
