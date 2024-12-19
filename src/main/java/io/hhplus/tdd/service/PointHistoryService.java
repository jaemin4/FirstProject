package io.hhplus.tdd.service;


import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;

import java.util.List;

public interface PointHistoryService {

    //포인트 상세조회
    public List<PointHistory> selectDetailPointHistory(long userId);



}
