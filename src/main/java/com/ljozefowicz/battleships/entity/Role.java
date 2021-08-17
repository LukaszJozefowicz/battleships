package com.ljozefowicz.battleships.entity;

import com.ljozefowicz.battleships.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "roles", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

//    public Role(UserRole name) {
//        this.name = name;
//    }

    @Id
    //@GeneratedValue(generator = "roles_sequence")
    private Long id;

    @Enumerated(EnumType.STRING)
    private UserRole name;

}
