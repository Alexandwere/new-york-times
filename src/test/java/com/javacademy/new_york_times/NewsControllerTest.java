package com.javacademy.new_york_times;

import com.javacademy.new_york_times.dto.NewsDto;
import com.javacademy.new_york_times.dto.PageDto;
import com.javacademy.new_york_times.entity.NewsEntity;
import com.javacademy.new_york_times.repository.NewsRepository;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class NewsControllerTest {
    private final String testTitle = "Test news";
    private final String testAuthor = "Alexander";
    private final String testText = "This is test news";
    private final Integer testNumber = 1001;

    @Autowired
    private NewsRepository newsRepository;

    RequestSpecification requestSpecification = new RequestSpecBuilder()
            .setBasePath("/news")
            .setContentType(ContentType.JSON)
            .log(LogDetail.ALL)
            .build();

    ResponseSpecification responseSpecification = new ResponseSpecBuilder()
            .log(LogDetail.ALL)
            .build();

    @Test
    @DisplayName("Успешное сохранение новости")
    public void saveSuccess() {
        NewsDto newsDto = NewsDto.builder()
                .text(testText)
                .title(testTitle)
                .author(testAuthor)
                .build();
        NewsEntity expected = NewsEntity.builder()
                .number(testNumber)
                .title(testTitle)
                .author(testAuthor)
                .text(testText)
                .build();

        given(requestSpecification)
                .body(newsDto)
                .post()
                .then()
                .spec(responseSpecification)
                .statusCode(HttpStatus.CREATED.value());
        NewsEntity result = newsRepository.findByNumber(testNumber).get();

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Сохранение новости - ошибка")
    public void saveFailed() {
        Integer testNumber = 1000;

        NewsDto newsDto = NewsDto.builder()
                .number(testNumber)
                .text(testText)
                .title(testTitle)
                .author(testAuthor)
                .build();

        given(requestSpecification)
                .body(newsDto)
                .post()
                .then()
                .spec(responseSpecification)
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    @DisplayName("Успешное удаление новости")
    public void deleteSuccess() {
        NewsEntity newsEntity = NewsEntity.builder()
                .title(testTitle)
                .author(testAuthor)
                .text(testText)
                .build();
        newsRepository.save(newsEntity);
        Boolean result = given(requestSpecification)
                .pathParam("id", testNumber)
                .delete("/{id}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .as(Boolean.class);

        Optional<NewsEntity> news =  newsRepository.findByNumber(testNumber);
        assertTrue(news.isEmpty());
        assertTrue(result);
    }

    @Test
    @DisplayName("Успешное не удаление - отсутствует новость")
    public void deleteNotExistNewsSuccess() {
        int notExistNumber = 2000;
        Boolean result = given(requestSpecification)
                .pathParam("id", notExistNumber)
                .delete("/{id}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .as(Boolean.class);

        Optional<NewsEntity> news =  newsRepository.findByNumber(notExistNumber);
        assertTrue(news.isEmpty());
    }

    @Test
    @DisplayName("Успешное получение текста новости")
    public void getTextSuccess() {
        NewsEntity newsEntity = NewsEntity.builder()
                .title(testTitle)
                .author(testAuthor)
                .text(testText)
                .build();
        newsRepository.save(newsEntity);

        String result = given(requestSpecification)
                .pathParam("id", testNumber)
                .get("{id}/text")
                .then()
                .spec(responseSpecification)
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .asPrettyString();

        assertEquals(testText, result);
    }

    @Test
    @DisplayName("Получение текста - ошибка, нет новости")
    public void getTextFailed() {
        given(requestSpecification)
                .pathParam("id", testNumber)
                .get("{id}/text")
                .then()
                .spec(responseSpecification)
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    @DisplayName("Успешное получение автора новости")
    public void getAuthorSuccess() {
        NewsEntity newsEntity = NewsEntity.builder()
                .title(testTitle)
                .author(testAuthor)
                .text(testText)
                .build();
        newsRepository.save(newsEntity);

        String result = given(requestSpecification)
                .pathParam("id", testNumber)
                .get("{id}/author")
                .then()
                .spec(responseSpecification)
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .asPrettyString();

        assertEquals(testAuthor, result);
    }

    @Test
    @DisplayName("Получение автора - ошибка, нет новости")
    public void getAuthorFailed() {
        given(requestSpecification)
                .pathParam("id", testNumber)
                .get("{id}/author")
                .then()
                .spec(responseSpecification)
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    @DisplayName("Успешное получение всех новостей")
    public void findAllSuccess() {
        int currentPage = 1;
        int countPages = 100;
        int pageSize = 10;
        int maxPageSize = 10;

        PageDto<Object> pageDto = PageDto.builder()
                .currentPage(currentPage)
                .maxPageSize(maxPageSize)
                .countPages(countPages)
                .size(pageSize)
                .build();

        PageDto<NewsDto> result = given(requestSpecification)
                .queryParam("page", currentPage)
                .get("/find-all")
                .then()
                .spec(responseSpecification)
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .as(new TypeRef<>() { });

        assertEquals(countPages, result.getCountPages());
        assertEquals(currentPage, result.getCurrentPage());
        assertEquals(maxPageSize, result.getMaxPageSize());
        assertEquals(pageSize, result.getSize());
    }

    @Test
    @DisplayName("Успешное обновление новости")
    public void updateSuccess() {
        Integer updateNumber = 1;
        NewsDto newNewsDto = NewsDto.builder()
                .number(updateNumber)
                .author(testAuthor)
                .text(testText)
                .title(testTitle)
                .build();
        given(requestSpecification)
                .body(newNewsDto)
                .patch()
                .then()
                .spec(responseSpecification)
                .statusCode(HttpStatus.OK.value());

        NewsEntity result = newsRepository.findByNumber(updateNumber).get();
        assertEquals(newNewsDto.getNumber(), result.getNumber());
        assertEquals(newNewsDto.getText(), result.getText());
        assertEquals(newNewsDto.getAuthor(), result.getAuthor());
        assertEquals(newNewsDto.getTitle(), result.getTitle());
    }
}
