package com.ljozefowicz.battleships.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = {"username", "email"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    public User(String username, String password, String passwordConfirm, String email, String emailConfirm, Collection<Role> roles) {
        this.username = username;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
        this.email = email;
        this.emailConfirm = emailConfirm;
        this.roles = roles;
    }

    @Id
    @GeneratedValue(generator = "users_sequence")
    private Long id;

    private String username;

    private String password;

    @Transient
    private String passwordConfirm;

    private String email;

    @Transient
    private String emailConfirm;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"))

    private Collection<Role> roles;
}
