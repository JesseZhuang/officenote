package zhuang.jesse.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Time manipulation functions.
 */
public class TimeUtils {

    private static final Logger LOGGER = Logger.getLogger(TimeUtils.class.getName());

    private static final DateTimeFormatter CAMPAIGN_SCHEDULE = DateTimeFormatter.ofPattern("yyyy-LL-dd'T'kk-mmxxx");

    public static LocalDate getComingMonday(){
        LocalDate today = LocalDate.now();
        int old = today.getDayOfWeek().getValue();
        return today.plusDays(8 - old);
    }

    public static ZonedDateTime getComingMonday6am(LocalDate date){
        int old = date.getDayOfWeek().getValue();

        // office notes goes out every Monday 6 am pacific time
        LocalDateTime monday = date.plusDays(8 - old).atTime(6, 0);
        return ZonedDateTime.of(monday, ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC);
    }

    public static void main(String[] args) {
        ZonedDateTime nowUTC = ZonedDateTime.now(ZoneOffset.UTC);

        // UTC is 7 hour ahead of Pacific time
        LOGGER.info("Right now UTC time is " + DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(nowUTC));

        LocalDate today = LocalDate.now();

        for( int i = 1; i < 7; i++) {
            LOGGER.info(getComingMonday6am(today.plusDays(i)).
                    format(CAMPAIGN_SCHEDULE));
        }
    }
}
