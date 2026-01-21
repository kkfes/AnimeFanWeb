package com.animefan.repository;

import com.animefan.model.Banner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Banner entity
 */
@Repository
public interface BannerRepository extends MongoRepository<Banner, String> {

    List<Banner> findByActiveTrueOrderByOrderAsc();

    List<Banner> findAllByOrderByOrderAsc();
}
