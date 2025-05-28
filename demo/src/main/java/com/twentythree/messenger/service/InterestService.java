package com.twentythree.messenger.service;

import com.twentythree.messenger.dto.InterestDto;
import java.util.List;

public interface InterestService {
    List<InterestDto> getAllInterests();
    // InterestDto createInterest(InterestDto interestDto); // For admin
    // ... other admin methods
}