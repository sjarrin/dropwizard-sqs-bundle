package ch.rts.dropwizard.aws.sqs.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.List;

public class SqsBaseConfiguration {

    @JsonProperty
    @NotNull
    private List<String> queueNames;

    @JsonProperty
    @NotNull
    private String region;

    public List<String> getQueueNames() {
        return queueNames;
    }

    public void setQueueNames(List<String> queueNames) {
        this.queueNames = queueNames;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public String toString() {
        return new org.apache.commons.lang3.builder.ToStringBuilder(this)
                .append("queueNames", queueNames)
                .append("region", region)
                .toString();
    }

}
