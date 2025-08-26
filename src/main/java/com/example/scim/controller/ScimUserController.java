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

    @PostMapping
    public ResponseEntity<ScimUser> create(@RequestBody ScimUser user) {
        String id = UUID.randomUUID().toString();
        user.setId(id);
        user.setActive(true);
        users.put(id, user);

        System.out.printf("[CREATE] id=%s, userName=%s%n", id, user.getUserName());
        return ResponseEntity.ok(user);
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
