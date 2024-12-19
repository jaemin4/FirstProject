package io.hhplus.study.main;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.repository.UserPointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserPointRepositoryTest {

    private UserPointTable userPointTable;
    private UserPointRepository userPointRepository;

    @BeforeEach
    public void setup() {
        userPointTable = mock(UserPointTable.class);
        userPointRepository = new UserPointRepository(userPointTable);
    }


    // 정상적인 상황: 주어진 userId에 대해 예상되는 데이터를 반환
    @Test
    public void testSelectDetailUserPoint_ValidUserId() {
        long userId = 123L;
        UserPoint expectedPoint = new UserPoint(userId, 1000L, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(expectedPoint);

        UserPoint result = userPointRepository.selectDetailUserPoint(userId);

        assertNotNull(result);
        assertEquals(expectedPoint, result);
        verify(userPointTable, times(1)).selectById(userId);
    }

    // 응답 지연 테스트: 데이터베이스 호출이 지연되어도 올바르게 동작하는지 확인
    @Test
    public void testSelectDetailUserPoint_ResponseDelay() {
        long userId = 123L;
        UserPoint expectedPoint = new UserPoint(userId, 1000L, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenAnswer(invocation -> {
            Thread.sleep(2000);
            return expectedPoint;
        });

        UserPoint result = userPointRepository.selectDetailUserPoint(userId);

        assertNotNull(result);
        assertEquals(expectedPoint, result);
        verify(userPointTable, times(1)).selectById(userId);
    }

    // 사용자 데이터가 없는 경우: 기본값 반환
    @Test
    public void testSelectDetailUserPoint_NoData() {
        long userId = 123L;
        UserPoint defaultPoint = UserPoint.empty(userId);
        when(userPointTable.selectById(userId)).thenReturn(defaultPoint);

        UserPoint result = userPointRepository.selectDetailUserPoint(userId);

        assertNotNull(result);
        assertEquals(defaultPoint, result);
        verify(userPointTable, times(1)).selectById(userId);
    }

    // userId가 0 또는 음수일 경우: 기본값 반환
    @Test
    public void testSelectDetailUserPoint_InvalidUserId() {
        long userId = -1L;
        UserPoint defaultPoint = UserPoint.empty(userId);
        when(userPointTable.selectById(userId)).thenReturn(defaultPoint);

        UserPoint result = userPointRepository.selectDetailUserPoint(userId);

        assertNotNull(result);
        assertEquals(defaultPoint, result);
        verify(userPointTable, times(1)).selectById(userId);
    }



    //100개의 요청이 동시에 실행될 때도 올바른 결과를 반환하는지
    //반환된 모든 결과가 expectedHistory와 동일한지
    // Mock 객체가 정확히 numberOfCalls 횟수만큼 호출되는지
    @Test
    public void testSelectDetailUserPoint_ConcurrentCalls() throws InterruptedException {
        long userId = 123L;
        UserPoint expectedPoint = new UserPoint(userId, 1000L, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(expectedPoint);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        int numberOfCalls = 100;
        CountDownLatch latch = new CountDownLatch(numberOfCalls);
        ConcurrentLinkedQueue<UserPoint> results = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < numberOfCalls; i++) {
            executor.submit(() -> {
                try {
                    UserPoint result = userPointRepository.selectDetailUserPoint(userId);
                    results.add(result);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals(numberOfCalls, results.size());
        results.forEach(result -> {
            assertNotNull(result);
            assertEquals(expectedPoint, result);
        });

        verify(userPointTable, times(numberOfCalls)).selectById(userId);
    }

    // 새로운 사용자 데이터 삽입 테스트
    @Test
    public void testInsertDetailUserPoint_NewUser() {
        long userId = 123L;
        long amount = 1000L;
        UserPoint expectedPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        when(userPointTable.insertOrUpdate(userId, amount)).thenReturn(expectedPoint);

        UserPoint result = userPointRepository.insertDetailUserPoint(userId, amount);

        assertNotNull(result);
        assertEquals(expectedPoint.id(), result.id());
        assertEquals(expectedPoint.point(), result.point());
        verify(userPointTable, times(1)).insertOrUpdate(userId, amount);
    }

    // 기존 사용자 데이터 업데이트 테스트
    @Test
    public void testInsertDetailUserPoint_UpdateExistingUser() {
        long userId = 123L;
        long amount = 2000L;
        UserPoint expectedPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        when(userPointTable.insertOrUpdate(userId, amount)).thenReturn(expectedPoint);

        UserPoint result = userPointRepository.insertDetailUserPoint(userId, amount);

        assertNotNull(result);
        assertEquals(expectedPoint.id(), result.id());
        assertEquals(expectedPoint.point(), result.point());
        verify(userPointTable, times(1)).insertOrUpdate(userId, amount);
    }

    @Test
    public void testInsertDetailUserPoint_InternalException() {
        // 내부 예외 발생 테스트
        long userId = 123L;
        long amount = 1000L;

        when(userPointTable.insertOrUpdate(userId, amount)).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userPointRepository.insertDetailUserPoint(userId, amount);
        });

        assertEquals("Database error", exception.getMessage());
        verify(userPointTable, times(1)).insertOrUpdate(userId, amount);
    }

    @Test
    public void testInsertDetailUserPoint_ZeroAmount() {
        // 금액이 0인 경우
        long userId = 123L;
        long amount = 0L;
        UserPoint expectedPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        when(userPointTable.insertOrUpdate(userId, amount)).thenReturn(expectedPoint);

        UserPoint result = userPointRepository.insertDetailUserPoint(userId, amount);

        assertNotNull(result);
        assertEquals(expectedPoint.id(), result.id());
        assertEquals(expectedPoint.point(), result.point());
        verify(userPointTable, times(1)).insertOrUpdate(userId, amount);
    }

    @Test
    public void testInsertDetailUserPoint_NegativeAmount() {
        // 금액이 음수일 경우 예외 처리
        long userId = 123L;
        long amount = -1000L;

        when(userPointTable.insertOrUpdate(userId, amount)).thenThrow(new IllegalArgumentException("Amount cannot be negative"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userPointRepository.insertDetailUserPoint(userId, amount);
        });

        assertEquals("Amount cannot be negative", exception.getMessage());
        verify(userPointTable, times(1)).insertOrUpdate(userId, amount);
    }

    // 동시 호출 테스트
    @Test
    public void testInsertDetailUserPoint_ConcurrentCalls() throws InterruptedException {
        long userId = 123L;
        long initialAmount = 1000L;
        UserPoint initialPoint = new UserPoint(userId, initialAmount, System.currentTimeMillis());

        when(userPointTable.insertOrUpdate(anyLong(), anyLong())).thenReturn(initialPoint);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        int numberOfCalls = 100;
        CountDownLatch latch = new CountDownLatch(numberOfCalls);
        ConcurrentLinkedQueue<UserPoint> results = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < numberOfCalls; i++) {
            executor.submit(() -> {
                try {
                    UserPoint result = userPointRepository.insertDetailUserPoint(userId, 100);
                    results.add(result);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals(numberOfCalls, results.size());
        results.forEach(result -> {
            assertNotNull(result);
            assertEquals(userId, result.id());
        });

        verify(userPointTable, times(numberOfCalls)).insertOrUpdate(eq(userId), anyLong());
    }

}
