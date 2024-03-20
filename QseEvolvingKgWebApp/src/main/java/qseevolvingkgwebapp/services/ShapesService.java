package qseevolvingkgwebapp.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import qseevolvingkgwebapp.data.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShapesService {
    private final ShapeRepository repository;

    public ShapesService(ShapeRepository repository) {
        this.repository = repository;
    }

    public Optional<ExtractedShapes> get(Long id) {
        return repository.findById(id);
    }

    public void update(ExtractedShapes entity) {
        repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<ExtractedShapes> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public List<ExtractedShapes> listAll() {
        var all =  repository.findAll();
        Comparator<ExtractedShapes> shapeComparator = Comparator.comparing(ExtractedShapes::getGraphCreationTime)
                .thenComparing(ExtractedShapes::getVersionCreationTime)
                .thenComparing(ExtractedShapes::getCreatedAt);
        return all.stream().sorted(shapeComparator).collect(Collectors.toList());
    }

    public Page<ExtractedShapes> list(Pageable pageable, Specification<ExtractedShapes> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    public void insert(ExtractedShapes extractedShapes) {
        ExtractedShapes s = repository.save(extractedShapes);
        repository.flush();
    }

    public List<ExtractedShapes> listByVersionId(Long versionId) {
        return repository.findAll().stream().filter(s -> s.getVersionObject().getId().equals(versionId)).collect(Collectors.toList());
    }
}
