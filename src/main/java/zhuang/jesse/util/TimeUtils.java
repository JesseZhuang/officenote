package zhuang.jesse.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Time manipulation functions.
 */
public class TimeUtils {

    private static final Logger LOGGER = Logger.getLogger(TimeUtils.class.getName());
    private static final LocalDate TODAY = LocalDate.now();

    public static final DateTimeFormatter CAMPAIGN_SCHEDULE = DateTimeFormatter.ofPattern("yyyy-LL-dd'T'kk-mmxxx");
    public static final DateTimeFormatter CAMPAIGN_TITLE = DateTimeFormatter.ofPattern("yyyy-LL-dd");



    public static LocalDate getComingMonday(){
        int old = TODAY.getDayOfWeek().getValue();
        return TODAY.plusDays(8 - old);
    }

    public static ZonedDateTime getComingMonday6am(){
        // office notes goes out every Monday 6 am pacific time
        LocalDateTime monday6am = getComingMonday().atTime(6, 0);
        return ZonedDateTime.of(monday6am, ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC);
//        return monday6am.atZone(ZoneId.of("Z"));
    }

    public static void main(String[] args) {
        ZonedDateTime nowUTC = ZonedDateTime.now(ZoneOffset.UTC);

        // UTC is 7 hour ahead of Pacific time
        LOGGER.info("Right now UTC time is " + DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(nowUTC));

        for( int i = 1; i < 7; i++) {
            LOGGER.info(getComingMonday6am().format(CAMPAIGN_SCHEDULE));
        }
    }
}
