package io.hhplus.tdd.repository;


import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PointHistoryRepository {

    private final PointHistoryTable pointHistoryTable;

    public List<PointHistory> selectDetailPointHistory(long userId){
        return pointHistoryTable.selectAllByUserId(userId);
    }

    public PointHistory insertDetailPointHistory(long userId, long amount){
        return pointHistoryTable.insert(userId, amount, TransactionType.CHARGE,System.currentTimeMillis());
    }


}
