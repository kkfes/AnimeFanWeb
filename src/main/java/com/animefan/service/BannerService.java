package com.animefan.service;

import com.animefan.model.Banner;
import com.animefan.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for Banner management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;

    public List<Banner> getActiveBanners() {
        return bannerRepository.findByActiveTrueOrderByOrderAsc();
    }

    public List<Banner> getAllBanners() {
        return bannerRepository.findAllByOrderByOrderAsc();
    }

    public Optional<Banner> getBannerById(String id) {
        return bannerRepository.findById(id);
    }

    public Banner createBanner(Banner banner) {
        log.info("Creating new banner: {}", banner.getTitle());
        if (banner.getOrder() == null) {
            banner.setOrder(getAllBanners().size() + 1);
        }
        if (banner.getActive() == null) {
            banner.setActive(true);
        }
        return bannerRepository.save(banner);
    }

    public Banner updateBanner(String id, Banner bannerData) {
        log.info("Updating banner: {}", id);
        return bannerRepository.findById(id)
                .map(banner -> {
                    banner.setTitle(bannerData.getTitle());
                    banner.setDescription(bannerData.getDescription());
                    banner.setImageUrl(bannerData.getImageUrl());
                    banner.setButtonText(bannerData.getButtonText());
                    banner.setButtonUrl(bannerData.getButtonUrl());
                    banner.setOrder(bannerData.getOrder());
                    banner.setActive(bannerData.getActive());
                    return bannerRepository.save(banner);
                })
                .orElseThrow(() -> new RuntimeException("Banner not found: " + id));
    }

    public void deleteBanner(String id) {
        log.info("Deleting banner: {}", id);
        bannerRepository.deleteById(id);
    }

    public void toggleActive(String id, boolean active) {
        bannerRepository.findById(id).ifPresent(banner -> {
            banner.setActive(active);
            bannerRepository.save(banner);
        });
    }
}
