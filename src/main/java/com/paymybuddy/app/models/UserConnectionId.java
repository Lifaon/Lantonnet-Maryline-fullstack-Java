package com.paymybuddy.app.models;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class UserConnectionId implements Serializable {

    private Long userId;
    private Long connectionId;

    // Constructors
    public UserConnectionId() {}

    public UserConnectionId(Long user1Id, Long user2Id) {
        this.userId = user1Id;
        this.connectionId = user2Id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof UserConnectionId other))
            return false;
        boolean userIdEquals = (this.userId == null && other.userId == null)
                || (this.userId != null && this.userId.equals(other.userId));
        boolean connectionIdEquals = (this.connectionId == null && other.connectionId == null)
                || (this.connectionId != null && this.connectionId.equals(other.connectionId));
        return userIdEquals && connectionIdEquals;
    }

    @Override
    public final int hashCode() {
        int result = 17;
        if (userId != null) {
            result = 31 * result + userId.hashCode();
        }
        if (connectionId != null) {
            result = 31 * result + connectionId.hashCode();
        }
        return result;
    }
}
