package io.hhplus.tdd.service;


import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointHistoryServiceImpl implements PointHistoryService {

    private final PointHistoryRepository pointHistoryRepository;

    //포인트 조회 상세
    @Override
    public List<PointHistory> selectDetailPointHistory(long userId) {
        return pointHistoryRepository.selectDetailPointHistory(userId);
    }


}
