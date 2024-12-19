package io.hhplus.study.junit;

import org.junit.jupiter.api.*;

import java.util.logging.Logger;

// 테스트 클래스 설명 추가
@DisplayName("JUnit 라이프사이클 어노테이션 테스트")
public class LifeCycleAnnotation {

    private static final Logger logger = Logger.getLogger(LifeCycleAnnotation.class.getName());
    private static final String comment1 = "실행중";

    //@BeforeAll 사용법: 모든 테스트 실행 전에 한 번 실행
    @BeforeAll
    @DisplayName("BeforeAll - 모든 테스트 전 실행")
    static void setupAll() {
        logger.info("@BeforeAll : " + comment1);
    }

    //@BeforeEach 사용법: 각 테스트 실행 전에 실행
    @BeforeEach
    @DisplayName("BeforeEach - 각 테스트 실행 전 실행")
    void setUp() {
        logger.info("@BeforeEach : " + comment1);
    }

    //@Test 사용법: 테스트 메서드
    @Test
    @DisplayName("Test1 - 첫 번째 테스트")
    void test1() {
        logger.info("@Test : " + comment1);
    }

    @Test
    @DisplayName("Test2 - 두 번째 테스트")
    void test2() {
        logger.info("@Test : 두 번째 " + comment1);
    }

    //@AfterEach 사용법: 각 테스트 실행 후 실행
    @AfterEach
    @DisplayName("AfterEach - 각 테스트 실행 후 실행")
    void tearDown() {
        logger.info("@AfterEach : " + comment1);
    }

    //@AfterAll 사용법: 모든 테스트 실행 후 한 번 실행
    @AfterAll
    @DisplayName("AfterAll - 모든 테스트 후 실행")
    static void tearDownAll() {
        logger.info("@AfterAll : " + comment1);
    }
}
