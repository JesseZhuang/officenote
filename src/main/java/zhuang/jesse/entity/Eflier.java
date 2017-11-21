package zhuang.jesse.entity;

import java.time.LocalDate;

public class Eflier {

    private String title;
    private String downloadUrl;
    private LocalDate postedDate;

    public Eflier(String title, String downloadUrl, LocalDate postedDate) {
        this.title = title;
        this.downloadUrl = downloadUrl;
        this.postedDate = postedDate;
    }

    @Override
    public String toString() {
        final String prefix = "<br />•";
        final String urlPrefix = ", eFlier at ";
        final String datePrefix = ": posted ";

        return prefix + title + urlPrefix + downloadUrl + datePrefix + postedDate;
    }

    public static void main(String[] args) {

    }
}
