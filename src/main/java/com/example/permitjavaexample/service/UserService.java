package com.example.permitjavaexample.service;

import com.example.permitjavaexample.exception.ForbiddenAccessException;
import com.example.permitjavaexample.exception.UnauthorizedException;
import io.permit.sdk.Permit;
import io.permit.sdk.PermitConfig;
import io.permit.sdk.api.PermitApiError;
import io.permit.sdk.api.PermitContextError;
import io.permit.sdk.enforcement.Resource;
import io.permit.sdk.enforcement.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class UserService {
    private final Permit permit;

    public UserService(@Value("${permit.api-key}") String apiKey, @Value("${permit.pdp-url}") String pdpUrl) {
        this.permit = new Permit(
                new PermitConfig.Builder(apiKey)
                        .withPdpAddress(pdpUrl)
                        .build()
        );
    }

    public User login(String key) {
        return new User.Builder(key).build();
    }

    public User signup(String key) {
        var user = new User.Builder(key).build();
        try {
            permit.api.users.sync(user);
        } catch (PermitContextError | PermitApiError | IOException e) {
            throw new RuntimeException("Failed to create user", e);
        }
        return user;
    }


    public void authorize(User user, String action, Resource resource) {
        if (user == null) {
            throw new UnauthorizedException("Not logged in");
        }
        try {
            var permitted = permit.check(user, action, resource);
            if (!permitted) {
                throw new ForbiddenAccessException("Access denied");
            }
        } catch (PermitApiError | IOException e) {
            throw new RuntimeException("Failed to authorize user", e);
        }
    }
}
