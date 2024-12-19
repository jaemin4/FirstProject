package io.hhplus.tdd.service;


import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.repository.PointHistoryRepository;
import io.hhplus.tdd.repository.UserPointRepository;
import io.hhplus.tdd.util.CommUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPointServiceImpl implements UserPointService{

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;


    @Override
    public UserPoint selectDetailUserPoint(long id) {
        return userPointRepository.selectDetailUserPoint(id);
    }

    //포인트 충전
    @Override
    public UserPoint chargeUserPoint(long userId, long amount) {
        PointHistory pointHistory = pointHistoryRepository.insertDetailPointHistory(userId, amount);

        if (!CommUtil.isNullOrEmpty(pointHistory)) {
            UserPoint currentUserPoint = userPointRepository.selectDetailUserPoint(pointHistory.userId());

            System.out.println(currentUserPoint.point());
            long currentUserPointAmount = currentUserPoint.point();

            return userPointRepository.insertDetailUserPoint(
                    pointHistory.userId(),
                    currentUserPointAmount + amount
            );
        } else {

            throw new IllegalStateException("PointHistory가 null이거나 비어 있습니다.");
        }
    }
}
