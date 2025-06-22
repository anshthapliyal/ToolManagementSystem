package com.coditas.tool.management.system.specification;

import com.coditas.tool.management.system.constant.ToolCategory;
import com.coditas.tool.management.system.entity.ToolInventory;
import com.coditas.tool.management.system.entity.ToolCrib;
import com.coditas.tool.management.system.entity.Workplace;
import com.coditas.tool.management.system.entity.Facility;
import com.coditas.tool.management.system.entity.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ToolInventorySpecification {

    public static Specification<ToolInventory> belongsToToolCrib(Long toolCribId) {
        return (root, query, cb)
                -> cb.equal(root.get("toolCrib").get("id"), toolCribId);
    }

    public static Specification<ToolInventory> belongsToManagerEmail(String email) {
        return (root, query, cb) -> {
            // Join ToolInventory -> ToolCrib -> Workplace -> Facility -> FacilityManager (User) -> email
            Join<Object, ToolCrib> toolCribJoin = root.join("toolCrib");
            Join<Object, Workplace> workplaceJoin = toolCribJoin.join("workplace");
            Join<Object, Facility> facilityJoin = workplaceJoin.join("facility");
            Join<Object, User> managerJoin = facilityJoin.join("facilityManager");

            return cb.equal(managerJoin.get("email"), email);
        };
    }

    public static Specification<ToolInventory> availableLessThanMinimumThreshold() {
        return (root, query, cb) ->
                cb.lessThan(root.get("availableQuantity"), root.get("minimumThreshold"));
    }

    public static Specification<ToolInventory> filterInventory(Long toolCribId, String name, Boolean isPerishable,
                                                               List<ToolCategory> categories,
                                                               Double minPrice, Double maxPrice) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("toolCrib").get("id"), toolCribId));

            Join<Object, Object> tool = root.join("tool");

            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(tool.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (isPerishable != null) {
                predicates.add(cb.equal(tool.get("isPerishable"), isPerishable));
            }

            if (categories != null && !categories.isEmpty()) {
                predicates.add(tool.get("category").in(categories));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(tool.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(tool.get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<ToolInventory> filterInventoryByToolProperties(
            Long toolCribId,
            String name,
            List<ToolCategory> category,
            Boolean isPerishable,
            Double minPrice,
            Double maxPrice
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("toolCrib").get("id"), toolCribId));

            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("tool").get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (isPerishable != null) {
                predicates.add(cb.equal(root.get("tool").get("isPerishable"), isPerishable));
            }

            if (category != null && !category.isEmpty()) {
                predicates.add(root.get("tool").get("category").in(category));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("tool").get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("tool").get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
