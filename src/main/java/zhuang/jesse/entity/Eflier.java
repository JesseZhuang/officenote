package zhuang.jesse.entity;

import zhuang.jesse.constants.EflierConstants;
import zhuang.jesse.util.HtmlUtils;

import java.util.Optional;

public class Eflier {

    private String title;
    private String downloadUrl;
    private Optional<String> postedDate;

    public Eflier(String title, String downloadUrl, Optional<String> postedDate) {
        this.title = title;
        this.downloadUrl = downloadUrl;
        this.postedDate = postedDate;
    }

    @Override
    public String toString() {
        final String prefix = "<br />•";
        final String urlPrefix = ", eFlier at ";
        final String datePrefix = ": posted ";

        return prefix + HtmlUtils.removeHtml(title) + urlPrefix + downloadUrl + datePrefix + postedDate.orElse(EflierConstants.UNKOWN_DATE);
    }

    public static void main(String[] args) {
        System.out.println(new Eflier("title", "url", Optional.ofNullable(null)));
    }
}
