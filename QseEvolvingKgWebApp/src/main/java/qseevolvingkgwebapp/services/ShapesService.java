package qseevolvingkgwebapp.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import org.hibernate.Session;
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
    @PersistenceContext
    private EntityManager entityManager;

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

    @Transactional
    public ExtractedShapes getWithNodeShapes(Long id) {
//        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//        CriteriaQuery<ExtractedShapes> cq = cb.createQuery(ExtractedShapes.class);
//        Root<ExtractedShapes> rider = cq.from(Rider.class);
//        Join<Rider, Trip> riderTripJoin = rider.join("trips");
//
//        cq.select(rider);
//        Predicate startExpression = cb.and();
//        Predicate endExpression = cb.and();
//        if(start !=  null)
//            startExpression = cb.greaterThan(riderTripJoin.get("created"), start);
//
//        if(end !=  null)
//            endExpression = cb.lessThanOrEqualTo(riderTripJoin.get("created"), end);
//
//        cq.where(cb.and(
//                        cb.equal(riderTripJoin.get("state"), TripState.CANCELLED)),
//                cb.lessThanOrEqualTo(rider.get("avgRating"),2),
//                startExpression, endExpression);
//
//        cq.groupBy(rider.get("id"));
//        cq.orderBy(cb.desc(cb.count(riderTripJoin)));
//
//        TypedQuery<IRider> q = entityManager.createQuery(cq).setMaxResults(3);
//        List<IRider> allRiders = q.getResultList().stream().collect(Collectors.toList());
//        return allRiders;
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ExtractedShapes> query = criteriaBuilder.createQuery(ExtractedShapes.class);
        Root<ExtractedShapes> root = query.from(ExtractedShapes.class);
        root.fetch("nodeShapes"); // Eagerly fetch associated nodeShapes
        query.select(root);

        return entityManager.createQuery(query).getSingleResult();
//        Session session = entityManager.unwrap(Session.class);
//
//        return session(ExtractedShapes.class)
//                .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
//                .setFetchMode("nodeShapes", FetchMode.JOIN)
//                .setFetchMode("nodeShapes.propertyShapes", FetchMode.JOIN)
//                .list();
//        var query =  entityManager.createQuery("SELECT u FROM ExtractedShapes u JOIN FETCH u.nodeShapes");
//        return (ExtractedShapes) query.getSingleResult();
//        try {
//            Query query = entityManager.createQuery("SELECT u FROM ExtractedShapes u");
//            List<ExtractedShapes> resultList = query.getResultList();
//            // Handle the resultList as needed
//            return null;
//        } catch (Exception e) {
//            // Handle any exceptions that might occur during query execution
//            e.printStackTrace();
//            return null;
//        }
    }
}
