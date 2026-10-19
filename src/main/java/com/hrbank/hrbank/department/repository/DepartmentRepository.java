package com.hrbank.hrbank.department.repository;

import com.hrbank.hrbank.department.entity.Department;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
    long countByNameContainingOrDescriptionContaining(String name, String description);
    @Query("""
        select d from Department d
        where (d.name like %:keyword% or d.description like %:keyword%)
          and (:cursor is null or d.name > :cursor)
        order by d.name asc
        """)
    List<Department> findNameAsc(@Param("keyword") String keyword,
                                 @Param("cursor") String cursor,
                                 Pageable pageable);
    @Query("""
        select d from Department d
        where (d.name like %:keyword% or d.description like %:keyword%)
          and (:cursor is null or d.name < :cursor)
        order by d.name desc
        """)
    List<Department> findNameDesc(@Param("keyword") String keyword,
                                  @Param("cursor") String cursor,
                                  Pageable pageable);
    @Query("""
        select d from Department d
        where (d.name like %:keyword% or d.description like %:keyword%)
          and (:cursor is null or d.establishedDate > :cursor
               or (d.establishedDate = :cursor and d.id > :idAfter))
        order by d.establishedDate asc, d.id asc
        """)
    List<Department> findDateAsc(@Param("keyword") String keyword,
                                 @Param("cursor") LocalDate cursor,
                                 @Param("idAfter") Long idAfter,
                                 Pageable pageable);
    @Query("""
        select d from Department d
        where (d.name like %:keyword% or d.description like %:keyword%)
          and (:cursor is null or d.establishedDate < :cursor
               or (d.establishedDate = :cursor and d.id < :idAfter))
        order by d.establishedDate desc, d.id desc
        """)
    List<Department> findDateDesc(@Param("keyword") String keyword,
                                  @Param("cursor") LocalDate cursor,
                                  @Param("idAfter") Long idAfter,
                                  Pageable pageable);
}
