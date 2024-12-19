package io.hhplus.study.junit;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.security.cert.Extension;
import java.util.logging.Logger;
import java.util.stream.Stream;

@DisplayName("Parameter 에노테이션 테스트")
public class ParameterAnnotation {

    private static final Logger logger = Logger.getLogger(ParameterAnnotation.class.getName());

/*  간단한 데이터 배열(문자열, 정수, 부울 등)을 파라미터로 제공합니다.
    지원되는 데이터 타입:
    short, byte, int, long, float, double, char, boolean, String
*/
    //@ValueSource
    @ParameterizedTest
    @ValueSource(ints = {1,2,3,4,5})
    void testWithParameters(int number){
        logger.info("ValueSource Int : " + number);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1","2","3","4","5"})
    void testWithParameters(String number){
        logger.info("ValueSource String : " + number);
    }


/*  여러 컬럼을 가진 데이터 행을 정의할 수 있습니다.
    각 행의 데이터는 매개변수 순서에 따라 매핑됩니다.
    문자열 값에 쉼표를 포함하려면 "로 감쌉니다
    외부 CSV 파일에서 데이터를 읽어 파라미터로 제공가능합니다.
*/
    //@CsvSource
    @ParameterizedTest
    @CsvSource({
            "1, One",
            "2, Two",
            "3, Three"
    })
    void testWithCsvSource(int number, String word){
        logger.info("CsvSource int : " + number + " String :" + word);
    }

    @Disabled
    @ParameterizedTest
    @CsvFileSource(resources = "/test-data.csv", numLinesToSkip = 1)// 첫 줄(헤더)은 건너뜀{
    void testWithFileSource(int number, String word){
        logger.info("CsvFileSource String : " + word + "Number : " + number);
    }



    //커스텀 데이터 공급 클래스를 사용하여 파라미터를 제공
    //@MethodSourceajfeh djsehkse
    @ParameterizedTest
    @MethodSource("provideComplexData")
    void testWithMultipleParameter(String name, int age)
    {
        logger.info("Name : " + name + ", Age : " + age);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> provideComplexData() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of("Alice", 25),
                org.junit.jupiter.params.provider.Arguments.of("Bob", 30)
        );
    }





}
