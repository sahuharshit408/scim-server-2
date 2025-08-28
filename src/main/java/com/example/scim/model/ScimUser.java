package com.example.scim.model;

import lombok.Data;

import java.util.List;

@Data
public class ScimUser {

    private List<String> schemas = List.of("urn:ietf:params:scim:schemas:core:2.0:User");
    private String id;
    private String userName;
    private Boolean active;
    private String displayName;
}

