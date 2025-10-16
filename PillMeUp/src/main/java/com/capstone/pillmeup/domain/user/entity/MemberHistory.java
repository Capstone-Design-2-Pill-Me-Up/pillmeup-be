package com.capstone.pillmeup.domain.user.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.capstone.pillmeup.domain.drug.entity.Drug;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "member_history",
    indexes = {
        @Index(name = "idx_history_member_created", columnList = "member_id, created_at"),
        @Index(name = "idx_history_item_seq", columnList = "item_seq")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemberHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long history_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_history_member"))
    private Member member_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_seq", referencedColumnName = "item_seq", nullable = false,
        foreignKey = @ForeignKey(name = "fk_history_drug"))
    private Drug item_seq;

    @Lob
    @Column(name = "gpt_caution_summary")
    private String gpt_caution_summary;

    @Column(name = "created_at", updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime created_at;

    /** 1:N 연관관계 (member_photo.history_id 에 매핑) */
    @OneToMany(mappedBy = "history_id", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MemberPhoto> member_photos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.created_at == null) {
            this.created_at = LocalDateTime.now();
        }
    }
    
}
