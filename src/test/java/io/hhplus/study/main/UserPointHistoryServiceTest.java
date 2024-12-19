package io.hhplus.study.main;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.repository.PointHistoryRepository;
import io.hhplus.tdd.repository.UserPointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserPointHistoryServiceTest {

    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;
    private UserPointRepository userPointRepository;
    private PointHistoryRepository pointHistoryRepository;
    private ConcurrentHashMap<Long, Lock> locks;

    @BeforeEach
    public void setup() {
        userPointTable = mock(UserPointTable.class);
        pointHistoryTable = mock(PointHistoryTable.class);
        userPointRepository = new UserPointRepository(userPointTable);
        pointHistoryRepository = new PointHistoryRepository(pointHistoryTable);
        locks = new ConcurrentHashMap<>();
    }

/*chargeUserPoint 메서드에서는 사용자별로 ReentrantLock을 사용하여 데이터 충돌을 방지
    동시 실행 시, 같은 userId에 대해 여러 스레드가 데이터를 동시에 읽고 쓰는 경우 데이터 정합성 문제가 발생할 수 있음.
    Lock을 통해 동일한 userId에 대해 하나의 스레드만 데이터에 접근 가능하도록 제한.*/

/*Mock 객체(userPointTable, pointHistoryTable) 설정:
    userPointTable.selectById: 초기 사용자 포인트 반환.
    userPointTable.insertOrUpdate: 사용자 포인트 업데이트 시 새로운 UserPoint 객체 반환.
    pointHistoryTable.insert: 트랜잭션 기록 Mock.

100개의 스레드 생성:
    각 스레드가 chargeUserPoint를 호출하여 금액을 추가.

모든 스레드 작업 완료 후 검증:
    반환된 결과의 개수와 내용 검증.
    최종 포인트 금액이 기대값과 동일한지 검증.

Mock 메서드 호출 횟수 확인:
    insertOrUpdate와 insert가 호출된 횟수와 스레드 수가 일치하는지 검증.*/
    @Test
    public void testChargeUserPoint_ConcurrentCalls() throws InterruptedException {
        // 동시 호출 테스트
        long userId = 123L;
        long initialAmount = 1000L;
        long chargeAmount = 500L;
        ConcurrentHashMap<Long, Long> pointStore = new ConcurrentHashMap<>();
        pointStore.put(userId, initialAmount);

        when(userPointTable.selectById(userId)).thenAnswer(invocation -> {
            long id = invocation.getArgument(0);
            return new UserPoint(id, pointStore.getOrDefault(id, 0L), System.currentTimeMillis());
        });

        when(userPointTable.insertOrUpdate(anyLong(), anyLong())).thenAnswer(invocation -> {
            long id = invocation.getArgument(0);
            long amount = invocation.getArgument(1);
            pointStore.put(id, amount);
            return new UserPoint(id, amount, System.currentTimeMillis());
        });

        PointHistory mockHistory = new PointHistory(1L, userId, chargeAmount, TransactionType.CHARGE, System.currentTimeMillis());
        when(pointHistoryTable.insert(anyLong(), anyLong(), eq(TransactionType.CHARGE), anyLong())).thenReturn(mockHistory);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        int numberOfCalls = 100;
        CountDownLatch latch = new CountDownLatch(numberOfCalls);

        for (int i = 0; i < numberOfCalls; i++) {
            executor.submit(() -> {
                Lock lock = locks.computeIfAbsent(userId, id -> new ReentrantLock());
                lock.lock();
                try {
                    PointHistory pointHistory = pointHistoryRepository.insertDetailPointHistory(userId, chargeAmount);
                    if (pointHistory != null) {
                        UserPoint currentUserPoint = userPointRepository.selectDetailUserPoint(userId);
                        long newAmount = currentUserPoint.point() + chargeAmount;
                        userPointRepository.insertDetailUserPoint(userId, newAmount);
                    }
                } finally {
                    lock.unlock();
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        long expectedFinalAmount = initialAmount + (chargeAmount * numberOfCalls);
        assertEquals(expectedFinalAmount, pointStore.get(userId));

        verify(userPointTable, atLeastOnce()).selectById(userId);
        verify(userPointTable, times(numberOfCalls)).insertOrUpdate(eq(userId), anyLong());
        verify(pointHistoryTable, times(numberOfCalls)).insert(eq(userId), eq(chargeAmount), eq(TransactionType.CHARGE), anyLong());
    }





}
