package com.coditas.tool.management.system.specification;

import com.coditas.tool.management.system.constant.ToolCategory;
import com.coditas.tool.management.system.entity.ToolRequestItem;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class ToolRequestItemSpecifications {

    public static Specification<ToolRequestItem> hasSpecialCategory() {
        return (root, query, cb) -> cb.equal(root.get("tool")
                .get("category"), ToolCategory.SPECIAL);
    }

    public static Specification<ToolRequestItem> hasWorkplace(Long workplaceId) {
        return (root, query, cb) -> cb.equal(root.get("toolRequest").get("workplace").get("id"), workplaceId);
    }


    public static Specification<ToolRequestItem> isApprovedByWpmOrNull(Long managerId) {
        return (root, query, cb) -> cb.or(
                cb.isNull(root.get("approvedByWpm")),
                cb.equal(root.get("approvedByWpm").get("id"), managerId)
        );
    }

    public static Specification<ToolRequestItem> toolNameLike(String toolName) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("tool").get("name")), "%" + toolName.toLowerCase() + "%");
    }

    public static Specification<ToolRequestItem> workerEmailLike(String workerEmail) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("toolRequest").get("worker").get("email")), "%" + workerEmail.toLowerCase() + "%");
    }

    public static Specification<ToolRequestItem> requestDateAfter(LocalDateTime startDateTime) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("toolRequest").get("requestDate"), startDateTime);
    }

    public static Specification<ToolRequestItem> requestDateBefore(LocalDateTime endDateTime) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("toolRequest").get("requestDate"), endDateTime);
    }
}
