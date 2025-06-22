package com.coditas.tool.management.system.specification;

import com.coditas.tool.management.system.entity.Workstation;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class WorkstationSpecification {

    public static Specification<Workstation> belongsToWorkplace(Long workplaceId) {
        return (root, query, cb) -> cb.equal(root.get("workplace").get("id"), workplaceId);
    }

    public static Specification<Workstation> filterByFields(String search, List<String> fields) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank() || fields == null || fields.isEmpty()) {
                return cb.conjunction(); // No filtering
            }

            String pattern = "%" + search.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            Join<Object, Object> workerJoin = root.join("worker", JoinType.LEFT);

            for (String field : fields) {
                switch (field) {
                    case "name" -> predicates.add(cb.like(cb.lower(root.get("stationCode")), pattern));
                    case "workerEmail" -> predicates.add(cb.like(cb.lower(workerJoin.get("email")), pattern));
                    case "workerName" -> predicates.add(cb.like(cb.lower(workerJoin.get("name")), pattern));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
