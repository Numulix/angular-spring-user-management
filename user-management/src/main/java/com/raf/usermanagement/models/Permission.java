package com.raf.usermanagement.models;

import javax.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class Permission {
    private int canCreateUser;
    private int canReadUser;
    private int canUpdateUser;
    private int canDeleteUser;
}
