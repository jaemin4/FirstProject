package io.hhplus.tdd.service;


import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.repository.PointHistoryRepository;
import io.hhplus.tdd.repository.UserPointRepository;
import io.hhplus.tdd.util.CommUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class UserPointServiceImpl implements UserPointService{

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final ConcurrentHashMap<Long, Lock> locks = new ConcurrentHashMap<>();


    @Override
    public UserPoint selectDetailUserPoint(long id) {
        return userPointRepository.selectDetailUserPoint(id);
    }

    @Override
    public UserPoint chargeUserPoint(long userId, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        return processUserPoint(userId, amount, true);
    }

    @Override
    public UserPoint useUserPoint(long userId, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        return processUserPoint(userId, amount, false);
    }

    private UserPoint processUserPoint(long userId, long amount, boolean isCharge) {
        final Lock lock = locks.computeIfAbsent(userId, id -> new ReentrantLock());
        lock.lock();
        try {
            PointHistory pointHistory = pointHistoryRepository.insertDetailPointHistory(userId, amount);
            if (CommUtil.isNullOrEmpty(pointHistory)) {
                throw new IllegalStateException("PointHistory가 null이거나 비어 있습니다.");
            }
            UserPoint currentUserPoint = userPointRepository.selectDetailUserPoint(pointHistory.userId());
            long currentUserPointAmount = currentUserPoint.point();
            long updatedAmount = isCharge ? currentUserPointAmount + amount : currentUserPointAmount - amount;

            if (!isCharge && updatedAmount < 0) {
                throw new IllegalStateException("Insufficient points: current=" + currentUserPointAmount + ", required=" + amount);
            }

            return userPointRepository.insertDetailUserPoint(pointHistory.userId(), updatedAmount);
        } finally {
            lock.unlock();
        }
    }

}
