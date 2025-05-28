package com.twentythree.messenger.controller;

import com.twentythree.messenger.dto.InterestDto;
import com.twentythree.messenger.service.InterestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/interests")
public class InterestController {

    @Autowired
    private InterestService interestService;

    @GetMapping
    public ResponseEntity<List<InterestDto>> getAllInterests() {
        List<InterestDto> interests = interestService.getAllInterests();
        return ResponseEntity.ok(interests);
    }

    // Add POST, PUT, DELETE for admin to manage interests if needed in the future
    // For now, assume interests are pre-populated or managed via DB directly
}