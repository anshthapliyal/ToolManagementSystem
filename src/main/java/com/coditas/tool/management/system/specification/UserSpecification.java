package com.coditas.tool.management.system.specification;

import com.coditas.tool.management.system.entity.Role;
import com.coditas.tool.management.system.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> hasRole(String roleName) {
        return (root, query, cb) -> {
            Join<User, Role> roleJoin = root.join("roles", JoinType.INNER);
            return cb.equal(roleJoin.get("role"), roleName);
        };
    }

    public static Specification<User> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<User> searchByFields(String keyword, List<String> fields) {
        return (root, query, cb) -> {
            if (keyword == null || fields == null || fields.isEmpty()) return null;

            String pattern = "%" + keyword.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            if (fields.contains("name")) {
                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }

            if (fields.contains("email")) {
                predicates.add(cb.like(cb.lower(root.get("email")), pattern));
            }

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
