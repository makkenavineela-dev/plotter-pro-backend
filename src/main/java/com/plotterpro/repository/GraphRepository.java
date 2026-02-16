package com.plotterpro.repository;

import com.plotterpro.entity.GraphEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GraphRepository extends JpaRepository<GraphEntity, Long> {

    List<GraphEntity> findByUser_Id(Long userId);

    java.util.Optional<GraphEntity> findByShareToken(String shareToken);

}
