package com.example.scim.model;

import lombok.Data;

@Data
public class ScimUser {
    private String id;
    private String userName;
    private Boolean active;
    private String displayName;
}
