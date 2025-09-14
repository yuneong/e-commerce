package com.loopers.interfaces.api.ranking;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Tag(name = "Ranking V1 API", description = "랭킹(Ranking) API 입니다.")
public interface RankingV1ApiSpec {

    @Operation(
        summary = "랭킹 목록 조회",
        description = "랭킹 목록을 조회합니다."
    )
    ApiResponse<List<RankingV1Dto.RankingsResponse>> getRankings(
            @Parameter(
                    name = "date",
                    description = "랭킹 조회 날짜",
                    required = false
            )
            @RequestParam String date,
            @Parameter(
                    name = "pageable",
                    description = "페이징 정보 (쿼리 파라미터)",
                    required = false
            )
            @PageableDefault(size = 20, page = 1) Pageable pageable
    );

}
