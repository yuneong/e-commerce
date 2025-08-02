package com.loopers.infrastructure.like.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LikeCountDto {

    private Long productId;
    private Long count;

}
