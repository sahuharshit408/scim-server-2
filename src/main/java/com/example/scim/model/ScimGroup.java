package com.example.scim.model;

import lombok.Data;

import java.util.List;

@Data
public class ScimGroup {
    private List<String> schemas = List.of("urn:ietf:params:scim:schemas:core:2.0:Group");

    private String id;
    private String displayName;
    private List<Member> members;

    @Data
    public static class Member {
        private String value;   // user ID
        private String display; // user name (optional)
    }
}
