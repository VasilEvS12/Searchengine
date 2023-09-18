package searchengine.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;

@Repository
@Transactional
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    @Modifying
    @Query(value = "delete from lemma where site_id=:siteId", nativeQuery = true)
    void deleteLemmasBySiteId(@Param("siteId") Integer siteId);
}

