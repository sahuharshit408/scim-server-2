package com.example.scim.controller;

import com.example.scim.model.ScimGroup;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/scim/v2/Groups")
public class ScimGroupController {

    private final Map<String, ScimGroup> groups = new ConcurrentHashMap<>();

    @PostMapping
    public ResponseEntity<ScimGroup> createGroup(@RequestBody ScimGroup group) {
        String id = UUID.randomUUID().toString();
        group.setId(id);
        groups.put(id, group);
        System.out.printf("[CREATE GROUP] id=%s, name=%s%n", id, group.getDisplayName());
        return ResponseEntity.ok(group);
    }

    @GetMapping
    public ResponseEntity<?> listGroups() {
        List<ScimGroup> groupList = new ArrayList<>(groups.values());

        Map<String, Object> response = Map.of(
            "schemas", List.of("urn:ietf:params:scim:api:messages:2.0:ListResponse"),
            "totalResults", groupList.size(),
            "startIndex", 1,
            "itemsPerPage", groupList.size(),
            "Resources", groupList
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScimGroup> getGroup(@PathVariable String id) {
        ScimGroup group = groups.get(id);
        return group != null ? ResponseEntity.ok(group) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable String id) {
        groups.remove(id);
        System.out.printf("[DELETE GROUP] id=%s%n", id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ScimGroup> patchGroup(@PathVariable String id, @RequestBody Map<String, Object> patch) {
        ScimGroup group = groups.get(id);
        if (group == null) return ResponseEntity.notFound().build();

        @SuppressWarnings("unchecked")
            List<Map<String, Object>> operations = (List<Map<String, Object>>) patch.get("Operations");

            for (Map<String, Object> op : operations) {
                String operation = (String) op.get("op");
                String path = (String) op.get("path");
                Object rawValue = op.get("value");

                // Handle "add" members
                if ("add".equalsIgnoreCase(operation) && "members".equalsIgnoreCase(path) && rawValue instanceof List<?> list) {
                    for (Object obj : list) {
                        if (obj instanceof Map<?, ?> memberMap) {
                            ScimGroup.Member member = new ScimGroup.Member();
                            member.setValue(String.valueOf(memberMap.get("value")));
                            member.setDisplay(String.valueOf(memberMap.get("display")));
                            group.getMembers().add(member);
                        }
                    }
                }

                // Handle "remove" for specific user ID
                if ("remove".equalsIgnoreCase(operation) && path != null && path.startsWith("members")) {
                    String userId = path.replaceAll("members\\[value eq \\\"", "").replaceAll("\"\\]", "");
                    group.getMembers().removeIf(m -> m.getValue().equals(userId));
                }

                // Handle "replace" entire member list
                if ("replace".equalsIgnoreCase(operation) && rawValue instanceof Map<?, ?> map) {
                    Object memberList = map.get("members");
                    if (memberList instanceof List<?> list) {
                        group.getMembers().clear();
                        for (Object obj : list) {
                            if (obj instanceof Map<?, ?> memberMap) {
                                ScimGroup.Member member = new ScimGroup.Member();
                                member.setValue(String.valueOf(memberMap.get("value")));
                                member.setDisplay(String.valueOf(memberMap.get("display")));
                                group.getMembers().add(member);
                            }
                        }
                    }
                }
            }

        System.out.printf("[PATCH GROUP] id=%s, members=%s%n", id, group.getMembers());
        return ResponseEntity.ok(group);
    }

}

    

