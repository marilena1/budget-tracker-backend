package com.budgettracker.budget_tracker_backend.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import java.time.LocalDateTime;

/**
 * Base entity with common MongoDB fields.
 * All domain entities should extend this class.
 */
@Getter
@Setter
public abstract class AbstractEntity {

    @Id
    @Field(targetType = FieldType.OBJECT_ID)
    private String id;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
}
