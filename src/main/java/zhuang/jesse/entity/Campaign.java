package zhuang.jesse.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A mailchimp campaign.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Campaign {

    private String type;
    private String id;
    private String emailSubject = "Office Notes *|DATE:M d Y|*";

    public Campaign(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
