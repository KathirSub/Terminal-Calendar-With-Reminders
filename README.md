# Terminal Calendar With Reminders
 Basic terminal Calendar in Java, where the User can browse through dates, set reminders, and translate to other languages.

# Terminal-Calendar-With-Reminders

## How to Run

To run the Terminal-Calendar-With-Reminders application, use 
### gradle run --args="calendarfile.utf16.cal"

## File Format 
![image](https://github.com/KathirSub/Terminal-Calendar-With-Reminders/assets/174324491/de46c979-3ed4-4acf-aff2-3cdde96828c7)

Example terms are given below : 
- event 2024-11-11 16:57:00 90 "Meeting 1" => Means there is a "Meeting 1" event at 16:57:00 that runs for 90 minutes on the 2024-11-11.
- event 2024-12-25 all-day "Christmas Day" = > Means there is a "Christmas Day" event that is all-day on 2024-12-25.
- plugin edu.curtin.calplugins.Repeat { title: "Tuition Repeat", startDate: "2023-11-01", startTime: "17:20:00", duration: "10", repeat: "7" } = > An event repeat called Tuition Repeat, starting on "2023-11-01" from 17:20:00 for 10 minutes, repeated for 7 times.
- plugin edu.curtin.calplugins.Notify { text: "Meeting 1" } = > A Notification Reminder for Meeting 1 Event.

## Calendar Grid
![image](https://github.com/KathirSub/Terminal-Calendar-With-Reminders/assets/174324491/6d2cd7e5-0aa8-4ab1-acae-aa1ebeb8cf8e)

### Navigation is as follows : 
- +d/-d : Add / Minus one day from the current day.
- +w/ -w : Add / Minus one week from the current day.
- +m/ -m : Add / Minus one month from the current day.
- y/ -y : Add / Minus one year from the current day.
- search : Used to search for a pre-existing event on the file given.
- locale : Used to change language interpretations from default English.
- quit : Used to quit the application.


## Search

The Terminal-Calendar-With-Reminders application includes a powerful search feature to help users quickly find specific events:

- **Free-Form Search:** Users can input a free-form search string to look for events.
- **Search Scope:** The application searches from the current date, covering all events up to one year beyond the current date.
- **Event Matching:** Events containing the specified search term in their titles are identified and displayed.

## Locale

To enhance accessibility, the Terminal-Calendar-With-Reminders application supports internationalization:

- **Locale Selection:** Users can select their preferred locale through a menu option, defaulting to the system's locale initially.
- **Translatable UI Text:** All user interface text is translatable based on the selected locale. The application supports English and one additional language.
- **Internationalized Handling:** Dates, times, and numbers are properly internationalized to ensure a seamless experience for users worldwide.

In the below example, the grid has been translated from english to spanish. 
![image](https://github.com/KathirSub/Terminal-Calendar-With-Reminders/assets/174324491/11e20311-7551-4f2c-8a1a-a36a8804ae35)

This comprehensive guide ensures you can make full use of the Terminal-Calendar-With-Reminders application. Enjoy managing your schedule and reminders efficiently!
