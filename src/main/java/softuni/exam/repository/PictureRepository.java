package softuni.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import softuni.exam.models.entities.Picture;

import java.util.List;

@Repository
public interface PictureRepository extends JpaRepository<Picture, Long> {
    Picture findByName(String name);

}
