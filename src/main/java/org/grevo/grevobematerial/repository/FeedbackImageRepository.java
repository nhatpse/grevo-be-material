package org.grevo.grevobematerial.repository;

import org.grevo.grevobematerial.entity.Feedback;
import org.grevo.grevobematerial.entity.FeedbackImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackImageRepository extends JpaRepository<FeedbackImage, Integer> {

    List<FeedbackImage> findByFeedback(Feedback feedback);
}
