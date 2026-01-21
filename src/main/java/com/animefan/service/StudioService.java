package com.animefan.service;

import com.animefan.model.Studio;
import com.animefan.exception.ResourceNotFoundException;
import com.animefan.exception.ValidationException;
import com.animefan.repository.StudioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for Studio business logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudioService {

    private final StudioRepository studioRepository;

    /**
     * Get all studios with pagination
     */
    public Page<Studio> getAllStudios(int page, int size) {
        log.info("Getting all studios, page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return studioRepository.findAll(pageable);
    }

    /**
     * Get studio by ID
     */
    public Studio getStudioById(String id) {
        log.info("Getting studio by ID: {}", id);
        return studioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Studio", "id", id));
    }

    /**
     * Get studio by name
     */
    public Studio getStudioByName(String name) {
        log.info("Getting studio by name: {}", name);
        return studioRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Studio", "name", name));
    }

    /**
     * Create new studio
     */
    @Transactional
    public Studio createStudio(Studio studio) {
        log.info("Creating new studio: {}", studio.getName());

        if (studioRepository.existsByName(studio.getName())) {
            throw new ValidationException("Studio with this name already exists");
        }

        studio.setAnimeCount(0);
        return studioRepository.save(studio);
    }

    /**
     * Update studio
     */
    @Transactional
    public Studio updateStudio(String id, Studio studioDetails) {
        log.info("Updating studio: {}", id);

        Studio studio = getStudioById(id);

        studio.setName(studioDetails.getName());
        studio.setDescription(studioDetails.getDescription());
        studio.setCountry(studioDetails.getCountry());
        studio.setFoundedYear(studioDetails.getFoundedYear());
        studio.setLogoUrl(studioDetails.getLogoUrl());
        studio.setWebsite(studioDetails.getWebsite());

        return studioRepository.save(studio);
    }

    /**
     * Delete studio
     */
    @Transactional
    public void deleteStudio(String id) {
        log.info("Deleting studio: {}", id);

        if (!studioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Studio", "id", id);
        }

        studioRepository.deleteById(id);
    }

    /**
     * Search studios by name
     */
    public Page<Studio> searchStudios(String query, int page, int size) {
        log.info("Searching studios by: {}", query);
        Pageable pageable = PageRequest.of(page, size);
        return studioRepository.searchByName(query, pageable);
    }

    /**
     * Get studios by country
     */
    public Page<Studio> getStudiosByCountry(String country, int page, int size) {
        log.info("Getting studios by country: {}", country);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return studioRepository.findByCountry(country, pageable);
    }

    /**
     * Get all countries
     */
    public List<String> getAllCountries() {
        return studioRepository.findAllCountries();
    }

    /**
     * Get top studios
     */
    public List<Studio> getTopStudios(int limit) {
        log.info("Getting top {} studios", limit);
        return studioRepository.findTopStudios(limit);
    }

    /**
     * Get studio count
     */
    public long getStudioCount() {
        return studioRepository.count();
    }
}
