package com.twentythree.messenger.service.impl;

import com.twentythree.messenger.dto.InterestDto;
import com.twentythree.messenger.entity.Interest;
import com.twentythree.messenger.repository.InterestRepository;
import com.twentythree.messenger.service.InterestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterestServiceImpl implements InterestService {

    @Autowired
    private InterestRepository interestRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InterestDto> getAllInterests() {
        List<Interest> interests = interestRepository.findAll();
        return interests.stream()
                .map(interest -> new InterestDto(interest.getId(), interest.getName(), interest.getDescription()))
                .collect(Collectors.toList());
    }
}