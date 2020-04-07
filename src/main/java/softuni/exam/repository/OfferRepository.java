package softuni.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import softuni.exam.models.entities.Offer;

import java.math.BigDecimal;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    Offer findByDescriptionAndPrice(String desc, BigDecimal price);
}
