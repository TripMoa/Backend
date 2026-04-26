package com.tripmoa.user.controller;

import com.tripmoa.user.dto.MySanctionStatusResponse;
import com.tripmoa.security.principal.CustomUserDetails;
import com.tripmoa.user.service.UserSanctionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "UserSanction", description = "사용자 제재 상태 API")
@RestController
@RequestMapping("/api/users/me/sanction")
@RequiredArgsConstructor
public class UserSanctionController {

    private final UserSanctionService userSanctionService;

    @GetMapping
    public ResponseEntity<MySanctionStatusResponse> getMySanctionStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MySanctionStatusResponse response =
                userSanctionService.getMySanctionStatus(userDetails.getUser().getId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/warning-popup/read")
    public ResponseEntity<Void> markWarningPopupRead(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userSanctionService.markWarningPopupRead(userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }
}