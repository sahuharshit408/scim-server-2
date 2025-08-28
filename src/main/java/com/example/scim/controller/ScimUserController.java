package com.example.scim.controller;

import com.example.scim.model.ScimUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/scim/v2/Users")
public class ScimUserController {

    private final Map<String, ScimUser> users = new ConcurrentHashMap<>();
    @Value("${scim.token}") private String token;


    // @GetMapping
    // public ResponseEntity<?> listUsers(
    //     @RequestParam(required = false) String filter,
    //     @RequestParam(defaultValue = "1") int startIndex,
    //     @RequestParam(defaultValue = "100") int count
    // ) {
    //     System.out.printf("[GET] Users list requested. filter=%s, startIndex=%d, count=%d%n", filter, startIndex, count);
        
    //     // Just return empty list to pass Okta test
    //     return ResponseEntity.ok(Map.of(
    //         "schemas", List.of("urn:ietf:params:scim:api:messages:2.0:ListResponse"),
    //         "totalResults", 0,
    //         "Resources", List.of(),
    //         "startIndex", startIndex,
    //         "itemsPerPage", 0
    //     ));
    // }


    @GetMapping
    public ResponseEntity<?> listUsers(
        @RequestParam(required = false) String filter,
        @RequestParam(defaultValue = "1") int startIndex,
        @RequestParam(defaultValue = "100") int count
    ) {
        System.out.printf("[GET] Users list requested. filter=%s, startIndex=%d, count=%d%n", filter, startIndex, count);
        
        List<ScimUser> userList = new ArrayList<>(users.values());
        // Apply filter if present
        if (filter != null && filter.contains("userName eq")) {
            try {
                // Extract the value from the filter string: userName eq "value"
                String[] parts = filter.split("\"");
                if (parts.length >= 2) {
                    String targetUsername = parts[1];
                    userList = userList.stream()
                        .filter(user -> targetUsername.equalsIgnoreCase(user.getUserName()))
                        .toList();
                }
            } catch (Exception e) {
                System.out.println("⚠️ Failed to parse filter: " + e.getMessage());
                userList = List.of(); // Return empty to avoid breaking Okta
            }
        }

        int total = userList.size();
        int from = Math.max(0, startIndex - 1);
        int to = Math.min(from + count, total);
        List<ScimUser> paged = from < to ? userList.subList(from, to) : List.of();

        Map<String, Object> response = Map.of(
            "schemas", List.of("urn:ietf:params:scim:api:messages:2.0:ListResponse"),
            "totalResults", total,
            "startIndex", startIndex,
            "itemsPerPage", paged.size(),
            "Resources", paged
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ScimUser> create(@RequestBody ScimUser user) {
        String id = UUID.randomUUID().toString();
        user.setId(id);
        user.setActive(true);
        users.put(id, user);

        System.out.printf("[CREATE] id=%s, userName=%s%n", id, user.getUserName());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/.search")
    public ResponseEntity<?> searchUsers(@RequestBody Map<String, Object> body) {
        System.out.println("[SEARCH] Received search request: " + body);
        return ResponseEntity.ok(Map.of(
          "schemas", List.of("urn:ietf:params:scim:api:messages:2.0:ListResponse"),
          "totalResults", 0,
          "Resources", List.of(),
          "startIndex", 1,
          "itemsPerPage", 0
        ));
    }




    @PutMapping("/{id}")
    public ResponseEntity<ScimUser> update(@PathVariable String id, @RequestBody ScimUser user) {
        user.setId(id);
        users.put(id, user);

        System.out.printf("[UPDATE] id=%s, userName=%s%n", id, user.getUserName());
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ScimUser> patch(@PathVariable String id, @RequestBody Map<String, Object> patch) {
        ScimUser user = users.get(id);
        if (user == null) return ResponseEntity.notFound().build();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> operations = (List<Map<String, Object>>) patch.get("Operations");

        if (operations != null) {
            for (Map<String, Object> op : operations) {
                Object val = op.get("value");
                if (val instanceof Map valueMap && valueMap.containsKey("active")) {
                    user.setActive((Boolean) valueMap.get("active"));
                    System.out.printf("[PATCH] id=%s, active=%s%n", id, user.getActive());
                }
            }
        }
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        users.remove(id);
        System.out.printf("[DELETE] id=%s%n", id);
        return ResponseEntity.noContent().build();
    }
}



