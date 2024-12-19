package io.hhplus.tdd.repository;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.service.PointHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserPointRepository {

    private final UserPointTable userPointTable;

    public UserPoint selectDetailUserPoint(long id){
        return userPointTable.selectById(id);
    }

    public UserPoint insertDetailUserPoint(long id, long amount) {
        return userPointTable.insertOrUpdate(id, amount);
    }


}
