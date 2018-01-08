package zhuang.jesse.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Time manipulation functions.
 */
public class TimeUtils {

    private static final Logger LOGGER = Logger.getLogger(TimeUtils.class.getName());
    private static final LocalDate TODAY = LocalDate.now();

    public static final DateTimeFormatter CAMPAIGN_SCHEDULE = DateTimeFormatter.ofPattern("yyyy-LL-dd'T'kk-mmxxx");
    public static final DateTimeFormatter CAMPAIGN_TITLE = DateTimeFormatter.ofPattern("yyyy-LL-dd");


    /**
     * @return the Monday before the most recent one.
     */
    public static LocalDate getLastMonday() {
        return getComingMonday().minusDays(14);
    }

    public static LocalDate getComingMonday() {
        int old = TODAY.getDayOfWeek().getValue();
        return TODAY.plusDays(8 - old);
    }

    public static ZonedDateTime getComingMonday6am() {
        // office notes goes out every Monday 6 am pacific time
        LocalDateTime monday6am = getComingMonday().atTime(6, 0);
        return ZonedDateTime.of(monday6am, ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC);
//        return monday6am.atZone(ZoneId.of("Z"));
    }

    public static LocalDate parseDate(String date) {
        if (date == null) return null;
//        DateTimeFormatter twoDigitYear = DateTimeFormatter.ofPattern("M/d/yy");
//        DateTimeFormatter fourDigitYear = DateTimeFormatter.ofPattern("M/d/yyyy");
//        DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendOptional(fourDigitYear).
//                appendOptional(twoDigitYear).toFormatter();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[M/d/yyyy][M/d/yy]");
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy"); does not work for 01/05/18
        LocalDate result = null;
        try {
            result = LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
        ZonedDateTime nowUTC = ZonedDateTime.now(ZoneOffset.UTC);

        // UTC is 7 hour ahead of Pacific time
        LOGGER.info("Right now UTC time is " + DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(nowUTC));

        for (int i = 1; i < 7; i++) {
            LOGGER.info(getComingMonday6am().format(CAMPAIGN_SCHEDULE));
        }

        System.out.println(LocalDate.of(2003, 5, 23));

        System.out.println(LocalDate.of(2003, 5, 23).isAfter(LocalDate.of(2003, 5, 22)));

        System.out.println(getLastMonday());

        System.out.println(parseDate("12/20/2017"));
        System.out.println(parseDate("01/08/2018")); //Exception with M/d/yy
        System.out.println(parseDate("01/05/18")); //0018-01-05 with M/d/y, Exception with M/d/yyyy
        System.out.println(parseDate("01/05/97")); //2097-01-05 with [M/d/yyyy][M/d/yy], ideally should be 1997
        System.out.println(parseDate("01/15/02"));
    }
}
