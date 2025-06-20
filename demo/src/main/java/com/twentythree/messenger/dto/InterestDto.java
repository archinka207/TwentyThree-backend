package com.twentythree.messenger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterestDto {
    private Long id;
    private String name;
    private String description;
}