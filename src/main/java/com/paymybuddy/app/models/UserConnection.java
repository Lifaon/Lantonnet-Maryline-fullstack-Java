package com.paymybuddy.app.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity(name = "user_connection")
@Table(name = "user_connection")
public class UserConnection {

    @EmbeddedId
    private UserConnectionId id;

    @MapsId("userId")
    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "FK_USER_CONNECTION_USER"))
    @OnDelete(action = OnDeleteAction.CASCADE)  // Cascade delete on user
    private User user;

    @MapsId("connectionId")
    @ManyToOne
    @JoinColumn(name = "connection_id", foreignKey = @ForeignKey(name = "FK_USER_CONNECTION_CONNECTION"))
    @OnDelete(action = OnDeleteAction.CASCADE)  // Cascade delete on connection
    private User connection;

    public UserConnection() {}

    public UserConnection(User user, User connection) {
        id = new UserConnectionId(user.getId(), connection.getId());
        this.user = user;
        this.connection = connection;
    }
}
