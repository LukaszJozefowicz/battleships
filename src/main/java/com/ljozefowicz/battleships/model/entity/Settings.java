package com.ljozefowicz.battleships.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ljozefowicz.battleships.enums.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames={"difficulty"}))

public class Settings {

    @Id
    @GeneratedValue(generator = "settings_sequence")
    private Long id;

    @Column(unique = true)
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @OneToMany(mappedBy = "settings") //cascade = {CascadeType.MERGE, CascadeType.PERSIST}
    private Set<User> users;
}
