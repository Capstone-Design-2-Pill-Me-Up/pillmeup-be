package com.capstone.pillmeup.domain.drug.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
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
    name = "drug",
    indexes = {
        @Index(name = "ix_drug_item_seq", columnList = "item_seq", unique = true),
        @Index(name = "ix_drug_item_name", columnList = "item_name")
    }
)
public class Drug {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "drug_id")
    private Long drugId;

    // 공통 키
    @Column(name = "item_seq", length = 50, nullable = false, unique = true)
    private String itemSeq;

    @Column(name = "item_name", length = 500, nullable = false)
    private String itemName;

    @Column(name = "entp_name", length = 200)
    private String entpName;

    // DUR 기반 필드
    @Column(name = "etc_otc_code", length = 50)
    private String etcOtcCode;

    @Column(name = "class_no", length = 100)
    private String classNo;

    @Column(name = "chart", length = 1000)
    private String chart;

    @Lob
    @Column(name = "material_name", columnDefinition = "TEXT")
    private String materialName;

    @Column(name = "valid_term", length = 200)
    private String validTerm;

    // e약은요 상세 필드
    @Enumerated(EnumType.STRING)
    @Column(name = "detail_source", length = 20)
    private DetailSource detailSource; // NULL 허용

    @Lob
    @Column(name = "efcy_qesitm", columnDefinition = "TEXT")
    private String efcyQesitm;

    @Lob
    @Column(name = "use_method_qesitm", columnDefinition = "TEXT")
    private String useMethodQesitm;

    @Lob
    @Column(name = "atpn_warn_qesitm", columnDefinition = "TEXT")
    private String atpnWarnQesitm;

    @Lob
    @Column(name = "atpn_qesitm", columnDefinition = "TEXT")
    private String atpnQesitm;

    @Lob
    @Column(name = "intrc_qesitm", columnDefinition = "TEXT")
    private String intrcQesitm;

    @Lob
    @Column(name = "se_qesitm", columnDefinition = "TEXT")
    private String seQesitm;

    @Column(name = "deposit_method_qesitm", length = 1000)
    private String depositMethodQesitm;

    // 관리 컬럼
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 연관관계: drug_type (item_seq를 FK로 사용)
    @OneToMany(mappedBy = "drug", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("typeCode ASC")
    @Builder.Default
    private List<DrugType> drugTypes = new ArrayList<>();

    // 편의 메서드
    public void addDrugType(DrugType drugType) {
        drugType.setDrug(this);
        this.drugTypes.add(drugType);
    }
	
}
