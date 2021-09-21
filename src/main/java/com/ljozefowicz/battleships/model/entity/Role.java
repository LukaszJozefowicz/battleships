package com.ljozefowicz.battleships.model.entity;

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

    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    private UserRole name;

}
