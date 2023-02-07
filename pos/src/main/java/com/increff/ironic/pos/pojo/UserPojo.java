package com.increff.ironic.pos.pojo;

import com.increff.ironic.pos.model.auth.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "user")
@Table(
        uniqueConstraints = {@UniqueConstraint(columnNames = {"email"})},
        indexes = {@Index(columnList = "email")}
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserPojo extends BaseEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private UserRole role;

}
