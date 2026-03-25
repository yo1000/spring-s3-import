package com.yo1000.s3import.presentation;

import com.yo1000.s3import.application.UserApplicationService;
import com.yo1000.s3import.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserRestController {
    private final UserApplicationService userApplicationService;

    public UserRestController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @GetMapping
    public Page<User> getBySearchParams(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "givenName", required = false) String givenName,
            @RequestParam(value = "familyName", required = false) String familyName,
            @RequestParam(value = "address", required = false) String address,
            Pageable pageable) {
        return userApplicationService.search(
                username, email, givenName, familyName, address, pageable);
    }

    @GetMapping("/{username}")
    public User get(
            @PathVariable("username") String username) {
        return userApplicationService.lookupByUsername(username);
    }
}
