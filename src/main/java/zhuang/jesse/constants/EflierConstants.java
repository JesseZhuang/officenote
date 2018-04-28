package zhuang.jesse.constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EflierConstants {

    public static final String ESD_DOMAIN = "https://www.edmonds.wednet.edu";
    public static final String EFLIER_PATH = "/community/community_e_fliers/";
    public static final String UNKOWN_DATE = "bad_date_format";

    public static final List<String> EFLIER_CATEGORIES = Arrays.asList(new String[]{
            "community_activites_events_and_information",
            "lessons_and_classes",
            "youth_organizations",
            "sports__camps__lessons__and_teams",
            "summer_activities_-_district___community"
    });

    public static List<String> EFLIER_URLS;

    static {
        EFLIER_URLS = new ArrayList<>();
        String prefix = ESD_DOMAIN + EFLIER_PATH;
        for (int i = 0; i < EFLIER_CATEGORIES.size(); i++) {
            EFLIER_URLS.add(prefix + EFLIER_CATEGORIES.get(i));
        }
    }
}
