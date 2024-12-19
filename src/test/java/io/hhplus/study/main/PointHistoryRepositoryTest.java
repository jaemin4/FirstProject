package io.hhplus.study.main;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.repository.PointHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PointHistoryRepositoryTest {

    private PointHistoryTable pointHistoryTable;
    private PointHistoryRepository pointHistoryRepository;

    @BeforeEach
    public void setup() {
        pointHistoryTable = mock(PointHistoryTable.class);
        pointHistoryRepository = new PointHistoryRepository(pointHistoryTable);
    }

    //정상적인 상황 주어진 userId에 대해 예상되는 데이터를 반환
    //사용자 데이터가 없는 경우
    //userId가 0 또는 음수일경우
    //내부 예외 상황
    //데이터베이스 시간 지연 또는 비동기적 상황(데이터베이스 호출이 오래 걸리는 경우 정상적으로 처리되는지 확인, 이 경우 Mocking이나 시간 제한 검증을 통해 테스트)
    @Test
    public void testSelectDetailPointHistory_ValidUserId() {
        // 정상적인 상황: 주어진 userId에 대해 예상되는 데이터를 반환
        long userId = 123L;
        List<PointHistory> expectedHistory = Arrays.asList(
                new PointHistory(1L, userId, 1000L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, userId, -500L, TransactionType.USE, System.currentTimeMillis())
        );
        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(expectedHistory);

        List<PointHistory> result = pointHistoryRepository.selectDetailPointHistory(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedHistory, result);
        verify(pointHistoryTable, times(1)).selectAllByUserId(userId);
    }

    // 사용자 데이터가 없는 경우: 빈 리스트 반환
    @Test
    public void testSelectDetailPointHistory_NoData() {
        long userId = 123L;
        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(Arrays.asList());

        List<PointHistory> result = pointHistoryRepository.selectDetailPointHistory(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(pointHistoryTable, times(1)).selectAllByUserId(userId);
    }

    // userId가 0 또는 음수일 경우, 실제 데이터베이스에서는 문제가 될지 모르겠으나 자바에서는 문제 없이 잘 돌아가는듯?
    @Test
    public void testSelectDetailPointHistory_InvalidUserId() {

        long userId = -1L;
        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(Arrays.asList());

        List<PointHistory> result = pointHistoryRepository.selectDetailPointHistory(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(pointHistoryTable, times(1)).selectAllByUserId(userId);
    }



    // 데이터베이스 시간 지연: Mocking으로 응답 지연 시뮬레이션
    //지연이 발생해도 문제가 발생하는지
    @Test
    public void testSelectDetailPointHistory_DelayInResponse() {

        long userId = 123L;
        when(pointHistoryTable.selectAllByUserId(userId)).thenAnswer(invocation -> {
            Thread.sleep(20000);
            return Arrays.asList(new PointHistory(1L, userId, 1000L, TransactionType.CHARGE, System.currentTimeMillis()));
        });

        List<PointHistory> result = pointHistoryRepository.selectDetailPointHistory(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(pointHistoryTable, times(1)).selectAllByUserId(userId);
    }

    //100개의 요청이 동시에 실행될 때도 올바른 결과를 반환하는지
    //반환된 모든 결과가 expectedHistory와 동일한지
    // Mock 객체가 정확히 numberOfCalls 횟수만큼 호출되는지
    //각 스레드의 호출이 다른 스레드에 영향을 미치지 않는지
    @Test
    public void testSelectDetailPointHistory_ConcurrentCalls() throws InterruptedException {
        long userId = 123L;
        List<PointHistory> expectedHistory = Arrays.asList(
                new PointHistory(1L, userId, 1000L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, userId, -500L, TransactionType.USE, System.currentTimeMillis())
        );

        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(expectedHistory);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        int numberOfCalls = 100;
        CountDownLatch latch = new CountDownLatch(numberOfCalls);


        ConcurrentLinkedQueue<List<PointHistory>> results = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < numberOfCalls; i++) {
            executor.submit(() -> {
                try {

                    List<PointHistory> result = pointHistoryRepository.selectDetailPointHistory(userId);
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
            assertEquals(2, result.size());
            assertEquals(expectedHistory, result);
        });

        verify(pointHistoryTable, times(numberOfCalls)).selectAllByUserId(userId);
    }

    // 새로운 사용자 데이터 삽입 테스트
    @Test
    public void testInsertDetailPointHistory_NewUser() {
        // 새로운 사용자 데이터 삽입 테스트
        long userId = 123L;
        long amount = 1000L;
        PointHistory expectedHistory = new PointHistory(1L, userId, amount, TransactionType.CHARGE, System.currentTimeMillis());

        when(pointHistoryTable.insert(eq(userId), eq(amount), eq(TransactionType.CHARGE), anyLong())).thenReturn(expectedHistory);

        PointHistory result = pointHistoryRepository.insertDetailPointHistory(userId, amount);

        assertNotNull(result);
        assertEquals(expectedHistory.id(), result.id());
        assertEquals(expectedHistory.userId(), result.userId());
        assertEquals(expectedHistory.amount(), result.amount());
        verify(pointHistoryTable, times(1)).insert(eq(userId), eq(amount), eq(TransactionType.CHARGE), anyLong());
    }

    // 음수 금액 삽입 시 예외 발생
    @Test
    public void testInsertDetailPointHistory_NegativeAmount() {
        long userId = 123L;
        long amount = -1000L;

        when(pointHistoryTable.insert(eq(userId), eq(amount), eq(TransactionType.CHARGE), anyLong())).thenThrow(new IllegalArgumentException("Amount cannot be negative"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointHistoryRepository.insertDetailPointHistory(userId, amount);
        });

        assertEquals("Amount cannot be negative", exception.getMessage());
        verify(pointHistoryTable, times(1)).insert(eq(userId), eq(amount), eq(TransactionType.CHARGE), anyLong());
    }

    // 금액이 0일 경우 처리
    @Test
    public void testInsertDetailPointHistory_ZeroAmount() {
        long userId = 123L;
        long amount = 0L;
        PointHistory expectedHistory = new PointHistory(1L, userId, amount, TransactionType.CHARGE, System.currentTimeMillis());

        when(pointHistoryTable.insert(eq(userId), eq(amount), eq(TransactionType.CHARGE), anyLong())).thenReturn(expectedHistory);

        PointHistory result = pointHistoryRepository.insertDetailPointHistory(userId, amount);

        assertNotNull(result);
        assertEquals(expectedHistory.id(), result.id());
        assertEquals(expectedHistory.userId(), result.userId());
        assertEquals(expectedHistory.amount(), result.amount());
        verify(pointHistoryTable, times(1)).insert(eq(userId), eq(amount), eq(TransactionType.CHARGE), anyLong());
    }

    // 동시 호출 테스트
    @Test
    public void testInsertDetailPointHistory_ConcurrentCalls() throws InterruptedException {
        long userId = 123L;
        long amount = 1000L;
        PointHistory expectedHistory = new PointHistory(1L, userId, amount, TransactionType.CHARGE, System.currentTimeMillis());

        when(pointHistoryTable.insert(anyLong(), anyLong(), eq(TransactionType.CHARGE), anyLong())).thenReturn(expectedHistory);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        int numberOfCalls = 100;
        CountDownLatch latch = new CountDownLatch(numberOfCalls);
        ConcurrentLinkedQueue<PointHistory> results = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < numberOfCalls; i++) {
            executor.submit(() -> {
                try {
                    PointHistory result = pointHistoryRepository.insertDetailPointHistory(userId, amount);
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
            assertEquals(userId, result.userId());
        });

        verify(pointHistoryTable, times(numberOfCalls)).insert(eq(userId), eq(amount), eq(TransactionType.CHARGE), anyLong());
    }



}
