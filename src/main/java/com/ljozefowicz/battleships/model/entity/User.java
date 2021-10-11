package com.ljozefowicz.battleships.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    public User(String username, String password, String passwordConfirm, String email, String emailConfirm, Collection<Role> roles, Settings settings) {
        this.username = username;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
        this.email = email;
        this.emailConfirm = emailConfirm;
        this.roles = roles;
        this.settings = settings;
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

    @ManyToOne//(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "settings_id")
    private Settings settings;
}
