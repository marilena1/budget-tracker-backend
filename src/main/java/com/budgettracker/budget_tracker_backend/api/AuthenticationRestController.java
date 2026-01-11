package com.budgettracker.budget_tracker_backend.api;

import com.budgettracker.budget_tracker_backend.authentication.AuthenticationService;
import com.budgettracker.budget_tracker_backend.dto.auth.AuthenticationRequestDTO;
import com.budgettracker.budget_tracker_backend.dto.auth.AuthenticationResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationRestController {

    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Authenticate user",
            description = "Returns JWT token for valid credentials",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Authentication successful",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponseDTO.class))),
                    @ApiResponse(
                            responseCode = "401", description = "Unauthorized - Invalid credentials",
                            content = @Content),
                    @ApiResponse(
                            responseCode = "400",  description = "Bad request - Missing/invalid parameters",
                            content = @Content)
            }
    )
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponseDTO> authenticate(
            @Valid @RequestBody AuthenticationRequestDTO authenticationRequestDTO) {
        log.info("Authentication attempt for user: {}", authenticationRequestDTO.username());

        AuthenticationResponseDTO authenticationResponseDTO = authenticationService.authenticate(authenticationRequestDTO);

        log.info("Authentication successful for user: {}", authenticationRequestDTO.username());
        return new ResponseEntity<>(authenticationResponseDTO, HttpStatus.OK);
    }
}
