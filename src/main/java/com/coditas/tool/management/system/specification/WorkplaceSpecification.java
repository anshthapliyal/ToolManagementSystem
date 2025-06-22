package com.coditas.tool.management.system.specification;

import com.coditas.tool.management.system.entity.Facility;
import com.coditas.tool.management.system.entity.User;
import com.coditas.tool.management.system.entity.Workplace;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class WorkplaceSpecification {

    public static Specification<Workplace> belongsToFacilityManager(String email) {
        return (root, query, cb) -> {
            if (email == null) return cb.conjunction();

            Join<Workplace, Facility> facilityJoin = root.join("facility", JoinType.LEFT);
            Join<Facility, User> managerJoin = facilityJoin.join("facilityManager", JoinType.LEFT);

            return cb.equal(managerJoin.get("email"), email);
        };
    }

    public static Specification<Workplace> belongsToFacility(Long facilityId) {
        return (root, query, cb) -> {
            if (facilityId == null) return cb.conjunction();
            return cb.equal(root.get("facility").get("id"), facilityId);
        };
    }



    public static Specification<Workplace> searchByFields(String search, List<String> fields) {
        return (root, query, cb) -> {
            if (search == null || fields == null || fields.isEmpty()) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();
            String pattern = "%" + search.toLowerCase() + "%";

            for (String field : fields) {
                switch (field) {
                    case "name" -> predicates.add(cb.like(cb.lower(root.get("name")), pattern));
                    case "workplaceManagerEmail" ->
                            predicates.add(cb.like(cb.lower(root.join("workplaceManager").get("email")), pattern));
                    case "facilityName" ->
                            predicates.add(cb.like(cb.lower(root.join("facility").get("name")), pattern));
                }
            }

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
