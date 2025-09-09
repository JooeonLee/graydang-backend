package com.graydang.app.domain.search.repository;

import com.graydang.app.domain.common.Yn;
import com.graydang.app.domain.search.model.SearchKeyword;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {

    Optional<SearchKeyword> findByIdAndDelYn(Long id, Yn delYn);

    List<SearchKeyword> findAllByDisplayYnAndDelYnOrderByPriorityAscIdDesc(Yn displayYn, Yn delYn);

    Page<SearchKeyword> findAllByDelYn(Yn delYn, Pageable pageable);

    @Query("SELECT sk FROM SearchKeyword sk " +
           "WHERE sk.delYn = :delYn " +
           "AND (:displayYn IS NULL OR sk.displayYn = :displayYn) " +
           "AND (:keyword IS NULL OR sk.text LIKE %:keyword%) " +
           "ORDER BY sk.priority ASC, sk.id DESC")
    Page<SearchKeyword> searchKeywords(@Param("delYn") Yn delYn, 
                                       @Param("displayYn") Yn displayYn, 
                                       @Param("keyword") String keyword, 
                                       Pageable pageable);

    boolean existsByTextAndDelYn(String text, Yn delYn);
    
    long countByDisplayYnAndDelYn(Yn displayYn, Yn delYn);
}