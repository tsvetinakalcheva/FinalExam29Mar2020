package softuni.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import softuni.exam.models.entities.Car;

import java.util.List;


@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    Car findByMakeAndModelAndKilometers(String make, String model, Integer km);
    @Query("SELECT c FROM Car c LEFT JOIN Picture p ON c.id = p.car.id GROUP BY c.id ORDER BY count(p.id) DESC, c.make")
    List<Car> findAllSorted();
}
