package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.user.ChangePasswordRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.user.UpdateProfileRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.user.UserProfileResponse;

public interface UserService {

    UserProfileResponse getProfile(String username);

    UserProfileResponse updateProfile(String username, UpdateProfileRequest request);

    void changePassword(String username, ChangePasswordRequest request);
}
