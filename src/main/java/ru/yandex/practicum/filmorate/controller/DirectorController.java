package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService service;

    @GetMapping
    public Collection<Director> findAll(){
        return service.findAll();
    }

    @PostMapping
    public Director add(@Valid @RequestBody Director director){
        return service.add(director);
    }

    @DeleteMapping("/{id}")
    public void deleteDirectorById(@PathVariable int id){service.deleteDirectorById(id);}

    @GetMapping("/{id}")
    public Director findById(@PathVariable int id){return service.findById(id);}

    @PutMapping
    public Director update(@Valid @RequestBody Director newDirector){return service.update(newDirector);}


}
