package com.codecollab.chat.repository;

import com.codecollab.chat.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    Page<Room> findAllByArchivedFalse(Pageable pageable);

    @Query("""
        SELECT r FROM Room r
        WHERE r.archived = FALSE
        ORDER BY r.defaultRoom DESC, LOWER(r.name) ASC
        """)
    List<Room> findAllActiveOrdered();
}
