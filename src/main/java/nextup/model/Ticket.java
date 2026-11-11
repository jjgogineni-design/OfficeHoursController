package nextup.model;

import java.time.Instant;

/** One student's spot in line. Immutable. Only model code can create one. */
public class Ticket {

    private final String name;
    private final String topic;
    private final Instant joinedAt;

    /** Stores already-cleaned values. Only OfficeHours calls this. */
    Ticket(String name, String topic, Instant joinedAt) {
        this.name = name;
        this.topic = topic;
        this.joinedAt = joinedAt;
    }

    public String getName() {
        return name;
    }

    public String getTopic() {
        return topic;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}
