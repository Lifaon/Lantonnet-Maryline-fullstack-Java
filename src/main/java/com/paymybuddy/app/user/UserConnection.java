package com.paymybuddy.app.user;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
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
    @JsonBackReference
    private User user;

    @MapsId("connectionId")
    @ManyToOne
    @JoinColumn(name = "connection_id", foreignKey = @ForeignKey(name = "FK_USER_CONNECTION_CONNECTION"))
    @OnDelete(action = OnDeleteAction.CASCADE)  // Cascade delete on connection
    @JsonBackReference
    private User connection;

    public UserConnection() {}

    public UserConnection(User user, User connection) {
        id = new UserConnectionId(user.getId(), connection.getId());
        this.user = user;
        this.connection = connection;
    }

    public UserConnectionId getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        id = new UserConnectionId(user.getId(), connection.getId());
    }

    public User getConnection() {
        return connection;
    }

    public void setConnection(User connection) {
        this.connection = connection;
        id = new UserConnectionId(user.getId(), connection.getId());
    }
}
