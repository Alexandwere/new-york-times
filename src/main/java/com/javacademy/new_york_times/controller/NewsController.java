package com.javacademy.new_york_times.controller;

import com.javacademy.new_york_times.dto.NewsDto;
import com.javacademy.new_york_times.dto.PageDto;
import com.javacademy.new_york_times.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Сделать 7 операций внутри контроллера.
 * 1. Создание новости. Должно чистить кэш.
 * 2. Удаление новости по id. Должно чистить кэш.
 * 3. Получение новости по id. Должно быть закэшировано.
 * 4. Получение всех новостей (новости должны отдаваться порциями по 10 штук). Должно быть закэшировано.
 * 5. Обновление новости по id. Должно чистить кэш.
 * 6. Получение текста конкретной новости.
 * 7. Получение автора конкретной новости.
 *
 */
@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;

    @PostMapping
    @CacheEvict(value = "id", allEntries = true)
    @ResponseStatus(HttpStatus.CREATED)
    public void createNews(@RequestBody NewsDto newsDto) {
        newsService.save(newsDto);
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "id", allEntries = true)
    public boolean deleteNews(@PathVariable Integer id) {
        return newsService.deleteByNumber(id);
    }

    @GetMapping("/find-all")
    @Cacheable(value = "page")
    public PageDto<NewsDto> findAll(@RequestParam Integer page) {
        return newsService.findAll(page);
    }

    @PatchMapping("{id}")
    @Cacheable(value = "id")
    public NewsDto findByNumber(@PathVariable Integer id) {
        return newsService.findByNumber(id);
    }

    @PatchMapping
    @CacheEvict(value = "id", allEntries = true)
    public void updateNews(@RequestBody NewsDto newsDto) {
        newsService.update(newsDto);
    }

    @GetMapping("{id}/text")
    public String getTextByNews(@PathVariable Integer id) {
        return newsService.getNewsText(id);
    }

    @GetMapping("{id}/author")
    public String getAuthorByNews(@PathVariable Integer id) {
        return newsService.getNewsAuthor(id);
    }
}
