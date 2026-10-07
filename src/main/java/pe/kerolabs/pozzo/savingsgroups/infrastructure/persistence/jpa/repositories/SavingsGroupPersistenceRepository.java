package pe.kerolabs.pozzo.savingsgroups.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.MembershipStatus;
import pe.kerolabs.pozzo.savingsgroups.infrastructure.persistence.jpa.entities.SavingsGroupPersistenceEntity;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository for savings group persistence entities.
 */
@Repository
public interface SavingsGroupPersistenceRepository extends JpaRepository<SavingsGroupPersistenceEntity, UUID> {

    @Query("""
            select distinct g from SavingsGroupPersistenceEntity g
            join g.memberships m
            where m.memberId = :memberId and m.status in :statuses
            order by g.createdAt desc
            """)
    List<SavingsGroupPersistenceEntity> findAllByMember(@Param("memberId") UUID memberId,
                                                       @Param("statuses") Collection<MembershipStatus> statuses);
}
