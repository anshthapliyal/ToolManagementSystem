package com.coditas.tool.management.system.specification;

import com.coditas.tool.management.system.entity.ToolCrib;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ToolCribSpecification {

    public static Specification<ToolCrib> searchWithFields(String search, List<String> fields, Long facilityManagerId) {
        return (root, query, cb) -> {
            query.distinct(true);

            Join<Object, Object> workplaceJoin = root.join("workplace", JoinType.INNER);
            Join<Object, Object> facilityJoin = workplaceJoin.join("facility", JoinType.INNER);
            Join<Object, Object> managerJoin = root.join("toolCribManagers", JoinType.LEFT);

            Predicate basePredicate = cb.equal(facilityJoin.get("facilityManager").get("id"), facilityManagerId);

            if (search == null || search.isBlank() || fields == null || fields.isEmpty()) {
                return basePredicate;
            }

            String likePattern = "%" + search.toLowerCase() + "%";
            List<Predicate> searchPredicates = new ArrayList<>();

            for (String field : fields) {
                switch (field) {
                    case "name":
                        searchPredicates.add(cb.like(cb.lower(root.get("name")), likePattern));
                        break;
                    case "workplaceName":
                        searchPredicates.add(cb.like(cb.lower(workplaceJoin.get("name")), likePattern));
                        break;
                    case "toolCribManagerEmail":
                        searchPredicates.add(cb.like(cb.lower(managerJoin.get("email")), likePattern));
                        break;
                }
            }

            Predicate andSearch = cb.and(searchPredicates.toArray(new Predicate[0]));

            return cb.and(basePredicate, andSearch);
        };
    }
}
