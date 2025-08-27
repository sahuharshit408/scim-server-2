package com.example.scim.controller;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/scim/v2")
public class DiscoveryController {

    @GetMapping("/ServiceProviderConfig")
    public Map<String, Object> serviceProviderConfig() {
        return Map.of(
            "schemas", List.of("urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig"),
            "patch", Map.of("supported", true),
            "bulk", Map.of("supported", false),
            "filter", Map.of("supported", true, "maxResults", 200),
            "changePassword", Map.of("supported", false),
            "sort", Map.of("supported", false),
            "etag", Map.of("supported", false),
            "authenticationSchemes", List.of(
                Map.of(
                    "type", "oauthbearertoken",
                    "name", "OAuth Bearer Token",
                    "description", "Bearer token authentication for SCIM"
                )
            )
        );
    }
}
