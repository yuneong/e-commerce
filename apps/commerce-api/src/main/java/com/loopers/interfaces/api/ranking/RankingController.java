package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.RankingFacade;
import com.loopers.application.ranking.RankingInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/rankings")
public class RankingController implements RankingV1ApiSpec {

    private final RankingFacade rankingFacade;

    @GetMapping("")
    @Override
    public ApiResponse<List<RankingV1Dto.RankingsResponse>> getRankings(
            @RequestParam (required = false) String date,
            @PageableDefault(size = 20, page = 1) Pageable pageable
    ) {
        List<RankingInfo> infos = rankingFacade.getRankings(date, pageable);
        List<RankingV1Dto.RankingsResponse> responses = RankingV1Dto.RankingsResponse.from(infos);

        return ApiResponse.success(responses);
    }

}
