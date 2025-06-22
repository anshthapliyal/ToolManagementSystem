package com.coditas.tool.management.system.specification;

import com.coditas.tool.management.system.entity.Facility;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class FacilitySpecification {

    public static Specification<Facility> searchByFields(String keyword, List<String> fields) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty() || fields == null || fields.isEmpty()) {
                return cb.conjunction(); //predicate which is always true
            }

            String pattern = "%" + keyword.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            for (String field : fields) {
                switch (field) {
                    case "name":
                        predicates.add(cb.like(cb.lower(root.get("name")), pattern));
                        break;
                    case "address":
                        predicates.add(cb.like(cb.lower(root.get("address")), pattern));
                        break;
                    case "facilityManagerEmail":
                        predicates.add(cb.like(cb.lower(
                                root.join("facilityManager", JoinType.LEFT).get("email")
                        ), pattern));
                        break;
                }
            }

            return cb.or(predicates.toArray(new Predicate[0])); //Match if any field matches
        };
    }

    public static Specification<Facility> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }
}
