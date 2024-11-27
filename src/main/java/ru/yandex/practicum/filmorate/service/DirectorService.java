package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

@Service
public class DirectorService {
    private  final DirectorRepository directorRepository;

    public DirectorService(DirectorRepository directorRepository) {
        this.directorRepository = directorRepository;
    }

    public Director addDirector(Director director) {
        return directorRepository.save(director);
    }

    public List<Director> getAll() {
        return directorRepository.findAll();
    }

    public Director getDirectorById(long directorId) {
        return directorRepository.findById(directorId);
    }

    public Director updateDirector(Director director) {
        return directorRepository.updateDirector(director);
    }

    public void deleteDirector(long directorId) {
        directorRepository.deleteDirector(directorId);
    }

}
