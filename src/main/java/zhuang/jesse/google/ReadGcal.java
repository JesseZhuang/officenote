package zhuang.jesse.google;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 * Read google calendar events from office notes calendar and Madrona K8
 * bearfacts calendar. Sometimes people submit events to office notes but these
 * are not added to bearfacts calendar. Manually add them to office notes
 * calendar.
 */
public class ReadGcal {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "Google Calendar API "
            + "Madrona OfficeNotes";

    /**
     * Directory to store user credentials for this application.
     */
    private static final java.io.File DATA_STORE_DIR = new java.io.File("io",
            ".credentials/calendar-java-quickstart");

    /**
     * Global instance of the {@link FileDataStoreFactory}.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory
            .getDefaultInstance();

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;

    /**
     * Global instance of the scopes required by this quickstart.
     */
    private static final List<String> SCOPES = Arrays
            .asList(CalendarScopes.CALENDAR_READONLY);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
    /*
     * Tried to use 4 ..(double dots to step relative path out of this class's
     * folder). This method still works when the file is still in bin but
     * stopped working if the file is directly in workspace/OfficeNotes. So put
     * the json file with ReadGcal.class.
     *
     * could use absolute path starting with /, which means starting from the
     * classpath
     */
         InputStream in = ReadGcal.class.getResourceAsStream(
                 "/client_secret_madrona_officenotes.json");

//        InputStream in = Files.newInputStream(
//                Paths.get("target/classes/client_secret_madrona_officenotes.json"));
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(flow,
                new LocalServerReceiver()).authorize("user");
        // System.out.println("Credentials saved to "
        // + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Calendar client service.
     *
     * @return an authorized Calendar client service
     * @throws IOException
     */
    public static com.google.api.services.calendar.Calendar getCalendarService()
            throws IOException {
        Credential credential = authorize();
        return new com.google.api.services.calendar.Calendar.Builder(HTTP_TRANSPORT,
                JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
    }

    /**
     * Read events from the two calendars and write them to files for google doc
     * and mailchimp.
     *
     * @param textFile the google doc text file.
     * @param htmlFile the html file for mailchimp(forMailChimpLeft.html).
     * @throws IOException
     */
    public static void writeGcalEvents(String textFile, String htmlFile)
            throws IOException {
        // Build a new authorized API client service.
        // Note: Do not confuse this class with the
        // com.google.api.services.calendar.model.Calendar class.
        com.google.api.services.calendar.Calendar service = getCalendarService();

        // List the next 15 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events;
        events = service.events().list("madronabearfacts@gmail.com")
                .setMaxResults(15).setTimeMin(now).setOrderBy("startTime")
                .setSingleEvents(true).execute();
        Events officeNotesEvents = service.events()
                .list("madronaofficenotes@gmail.com").setMaxResults(15).setTimeMin(now)
                .setOrderBy("startTime").setSingleEvents(true).execute();
        List<Event> items = events.getItems();
        items.addAll(officeNotesEvents.getItems());
        items.sort(new Comparator<Event>() {
            @Override
            public int compare(Event e1, Event e2) {
                DateTime start1 = e1.getStart().getDateTime();
                DateTime start2 = e2.getStart().getDateTime();
                if (start1 == null) {
                    start1 = e1.getStart().getDate();
                }
                if (start2 == null) {
                    start2 = e2.getStart().getDate();
                }

                LocalDateTime s1 = LocalDateTime.ofEpochSecond(start1.getValue() / 1000,
                        0, ZoneOffset.ofHours(start1.getTimeZoneShift() / 60));
                LocalDateTime s2 = LocalDateTime.ofEpochSecond(start2.getValue() / 1000,
                        0, ZoneOffset.ofHours(start2.getTimeZoneShift() / 60));

                return s1.compareTo(s2);
            }
        });

        // Set<Event> dedupItems = new HashSet<>(items);
        // no order and removed some not duplicates

        DateTimeFormatter noMin = DateTimeFormatter.ofPattern("h a");
        DateTimeFormatter time = DateTimeFormatter.ofPattern("h:mm a");
        DateTimeFormatter dayOnly = DateTimeFormatter.ofPattern("MMM dd EEE");

        if (items.size() == 0) {
            System.out.println("No upcoming events found.");
        } else {

            OutputStreamWriter wr1 = new OutputStreamWriter(
                    new FileOutputStream(textFile), "UTF-8");
            OutputStreamWriter wr2 = new OutputStreamWriter(
                    new FileOutputStream(htmlFile), "UTF-8");

            final String calSticky = "io/calSticky.html";

            wr2.write("<span class=\"content\">\n");

            LocalDateTime lastStart = null;
            boolean allDayEvent = false, moreThanOneDay = false;

            for (Event event : items) {

                DateTime start = event.getStart().getDateTime();
                DateTime end = event.getEnd().getDateTime();
                // all day events, start == null
                if (start == null) {
                    start = event.getStart().getDate();
                    end = event.getEnd().getDate();
                    allDayEvent = true;
                } else allDayEvent = false;

        /*
         * google API datetime.getValue returns milliseconds from epoch in the
         * UTC time zone. Do not have to use ZonedDateTime
         * .now(ZoneId.of("America/Los_Angeles")))
         */
                LocalDateTime start1 = LocalDateTime.ofEpochSecond(
                        start.getValue() / 1000, 0,
                        ZoneOffset.ofHours(start.getTimeZoneShift() / 60));
                LocalDateTime end1 = LocalDateTime.ofEpochSecond(end.getValue() / 1000,
                        0, ZoneOffset.ofHours(start.getTimeZoneShift() / 60));

                if (start1.getYear() - LocalDateTime.now().getYear() > 1) break;

                // whether the event lasts for more than one day
                int days = end1.getDayOfYear() - start1.getDayOfYear();
                moreThanOneDay = days > 1 || (days < 0 && days > -364);

                // write a date and day
                if (lastStart == null
                        || start1.getDayOfYear() > lastStart.getDayOfYear()
                        || start1.getYear() > lastStart.getYear()) {

                    String day = start1.format(dayOnly);

                    // whole day event's end date is actually the second day
                    if (moreThanOneDay)
                        if (allDayEvent) day += " - " + end1.minusDays(1).format(dayOnly);
                        else day += " - " + end1.format(dayOnly);

                    wr1.write("\n\n" + day + "\n");
                    wr2.write("<br><span class=\"date\">" + day + "</span><br>\n");
                }

                // write an event entry and its time(optional)
                String entry = event.getSummary();
                if (!allDayEvent) {
                    if (start1.getMinute() != 0) entry += " " + start1.format(time);
                    else entry += " " + start1.format(noMin);
                }
                if (event.getLocation() != null) entry += " at " + event.getLocation();
                wr1.write(entry + "\n");
                wr2.write(entry + "<br>\n");

                // set lastStart to appropriate value
                if (allDayEvent && end1.getDayOfMonth() - start1.getDayOfMonth() > 1)
                    lastStart = null;
                else lastStart = start1;
            }

            wr1.write("\n\n");
            wr1.close();

      /*
       * delimit the input with \Z, which is the end of the string anchor. This
       * ultimately makes the input have one actual token, which is the entire
       * file, so it can be read with one call to next().
       */
            // write the calendar sticky section
            Scanner sc = new Scanner(new File(calSticky));
            String content = sc.useDelimiter("\\Z").next();
            sc.close();
            wr2.write("</span><br>" + content);
            wr2.close();
        }

    }

    /**
     * Obtain the list of google calendars associated with this account.
     *
     * @param service an obtained google calendar service.
     */
    public static void getCalList(
            com.google.api.services.calendar.Calendar service) {
        String pageToken = null;
        do {
            com.google.api.services.calendar.model.CalendarList calendarList;
            try {
                calendarList = service.calendarList().list().setPageToken(pageToken)
                        .execute();
                List<CalendarListEntry> items = calendarList.getItems();

                for (CalendarListEntry calendarListEntry : items) {
                    System.out.println("id: " + calendarListEntry.getId() + ", summary: "
                            + calendarListEntry.getSummary());
                }
                pageToken = calendarList.getNextPageToken();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } while (pageToken != null);
    }

    private static void testProcessEvents() throws IOException {
        // Build a new authorized API client service.
        // Note: Do not confuse this class with the
        // com.google.api.services.calendar.model.Calendar class.
        com.google.api.services.calendar.Calendar service = getCalendarService();

        // List the next 15 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events;
        events = service.events().list("madronabearfacts@gmail.com")
                .setMaxResults(15).setTimeMin(now).setOrderBy("startTime")
                .setSingleEvents(true).execute();
        Events officeNotesEvents = service.events()
                .list("madronaofficenotes@gmail.com").setMaxResults(15).setTimeMin(now)
                .setOrderBy("startTime").setSingleEvents(true).execute();
        List<Event> items = events.getItems();
        items.addAll(officeNotesEvents.getItems());
        items.sort(new Comparator<Event>() {
            @Override
            public int compare(Event e1, Event e2) {
                DateTime start1 = e1.getStart().getDateTime();
                DateTime start2 = e2.getStart().getDateTime();
                if (start1 == null) {
                    start1 = e1.getStart().getDate();
                }
                if (start2 == null) {
                    start2 = e2.getStart().getDate();
                }

                LocalDateTime s1 = LocalDateTime.ofEpochSecond(start1.getValue() / 1000,
                        0, ZoneOffset.ofHours(start1.getTimeZoneShift() / 60));
                LocalDateTime s2 = LocalDateTime.ofEpochSecond(start2.getValue() / 1000,
                        0, ZoneOffset.ofHours(start2.getTimeZoneShift() / 60));

                return s1.compareTo(s2);
            }
        });

        // Set<Event> dedupItems = new HashSet<>(items);
        // no order and removed some not duplicates

        DateTimeFormatter noMin = DateTimeFormatter.ofPattern("h a");
        DateTimeFormatter time = DateTimeFormatter.ofPattern("h:mm a");
        DateTimeFormatter dayOnly = DateTimeFormatter.ofPattern("MMM dd EEE");

        if (items.size() == 0) {
            System.out.println("No upcoming events found.");
        } else {

            LocalDateTime lastStart = null;
            boolean allDayEvent = false, moreThanOneDay = false;

            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                DateTime end = event.getEnd().getDateTime();
                // all day events, start == null
                if (start == null) {
                    start = event.getStart().getDate();
                    end = event.getEnd().getDate();
                    allDayEvent = true;
                } else allDayEvent = false;

        /*
         * google API datetime.getValue returns milliseconds from epoch in the
         * UTC time zone. Do not have to use ZonedDateTime
         * .now(ZoneId.of("America/Los_Angeles")))
         */
                LocalDateTime start1 = LocalDateTime.ofEpochSecond(
                        start.getValue() / 1000, 0,
                        ZoneOffset.ofHours(start.getTimeZoneShift() / 60));
                LocalDateTime end1 = LocalDateTime.ofEpochSecond(end.getValue() / 1000,
                        0, ZoneOffset.ofHours(start.getTimeZoneShift() / 60));

                // whether the event lasts for more than one day
                int days = end1.getDayOfYear() - start1.getDayOfYear();
                moreThanOneDay = days > 1 || (days < 0 && days > -364);

                // write a date and day
                if (lastStart == null
                        || start1.getDayOfYear() > lastStart.getDayOfYear()) {

                    String day = start1.format(dayOnly);

                    // whole day event's end date is actually the second day
                    if (moreThanOneDay) day += " - " + end1.minusDays(1).format(dayOnly);

                    System.out.println("\n\n" + day + "\n");
                }

                // write an event entry and its time(optional)
                String entry = event.getSummary();
                if (!allDayEvent) {
                    if (start1.getMinute() != 0) entry += " " + start1.format(time);
                    else entry += " " + start1.format(noMin);
                }
                if (event.getLocation() != null) entry += " at " + event.getLocation();
                System.out.println(entry + "\n");

                // set lastStart to appropriate value
                if (allDayEvent && end1.getDayOfMonth() - start1.getDayOfMonth() > 1)
                    lastStart = null;
                else lastStart = start1;
            }

            System.out.println("\n\n");
        }
    }

    public static void main(String[] args) throws IOException {

        // System.out.println(Calendar.getInstance().getTime());

        // writeGcalEvents("io/test1.txt", "io/test1.html");
        // getCalList(getCalendarService());
        testProcessEvents();

    }
}