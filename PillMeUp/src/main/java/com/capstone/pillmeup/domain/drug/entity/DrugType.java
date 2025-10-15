package com.capstone.pillmeup.domain.drug.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "drug_type",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_drug_type", columnNames = {"item_seq", "type_code"})
    },
    indexes = {
        @Index(name = "ix_drug_type_item_seq", columnList = "item_seq"),
        @Index(name = "ix_drug_type_code", columnList = "type_code")
    }
)
public class DrugType {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id")
    private Long typeId;

    /**
     * FK: drug.item_seq(UNIQUE)을 참조
     * referencedColumnName 으로 자연키(UNIQUE 컬럼) 매핑
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_seq", referencedColumnName = "item_seq", nullable = false,
                foreignKey = @ForeignKey(name = "fk_drug_type_drug_item_seq"))
    private Drug drug;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_code", length = 1, nullable = false)
    private DrugCautionType typeCode; // A~I

    @Column(name = "type_name", length = 100, nullable = false)
    private String typeName;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
	
}
