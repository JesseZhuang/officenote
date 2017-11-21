package zhuang.jesse.util;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.junit.Test;

import java.time.LocalDate;

import org.junit.Assert;

public class TimeUtilsTests {

    @Test
    public void parseValidDateTest() {
        String date = "09/26/11";
        Assert.assertEquals(LocalDate.of(2011, 9, 26), TimeUtils.parseDate(date));
    }
}
