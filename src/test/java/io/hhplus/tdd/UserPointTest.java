package io.hhplus.tdd;


import org.apache.tomcat.util.bcel.classfile.JavaClass;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.logging.Logger;

@SpringBootTest
public class UserPointTest {

    private static final Logger logger = Logger.getLogger(UserPointTest.class.getName());

    @Test
    @DisplayName("테스트")
    void test(){
        logger.info("테스트 실행중");
    }







}
