package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dtos.CarSeedDto;
import softuni.exam.models.entities.Car;
import softuni.exam.repository.CarRepository;
import softuni.exam.service.CarService;
import softuni.exam.util.ValidationUtil;

import javax.transaction.Transactional;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static softuni.exam.constants.GlobalConstants.*;

@Service
@Transactional
public class CarServiceImpl implements CarService {
   private final CarRepository carRepository;
   private final ModelMapper modelMapper;
   private final ValidationUtil validationUtil;
   private final Gson gson;

    public CarServiceImpl(CarRepository carRepository, ModelMapper modelMapper, ValidationUtil validationUtil, Gson gson) {
        this.carRepository = carRepository;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.gson = gson;
    }

    @Override
    public boolean areImported() {
        return this.carRepository.count()>0;
    }

    @Override
    public String readCarsFileContent() throws IOException {
        return Files.readString(Path.of(CARS_FILE_PATH));
    }

    @Override
    public String importCars() throws IOException {
        StringBuilder resultInfo = new StringBuilder();
        CarSeedDto[] dtos = this.gson.fromJson(new FileReader(CARS_FILE_PATH), CarSeedDto[].class);
        Arrays.stream(dtos).forEach(carSeedDto -> {
            if (this.validationUtil.isValid(carSeedDto)) {
                if (this.carRepository.findByMakeAndModelAndKilometers(carSeedDto.getMake(), carSeedDto.getModel(), carSeedDto.getKilometers()) == null) {
                    Car car = this.modelMapper.map(carSeedDto, Car.class);
                    resultInfo.append(String.format("Successfully imported car %s %s", car.getMake(), car.getModel()));
                    this.carRepository.saveAndFlush(car);
                } else {
                    resultInfo.append(IN_DB_MESSAGE);
                }
            } else {
                resultInfo.append("Invalid car");
            }
            resultInfo.append(System.lineSeparator());
        });
        return resultInfo.toString();
    }

    @Override
    public String getCarsOrderByPicturesCountThenByMake() {
        StringBuilder result = new StringBuilder();

        List<Car> cars = carRepository.findAllSorted();

        for (Car car : cars) {

            String carInfo = String.format("Car make - %s, model - %s\n" +
                            "\tKilometers - %d\n" +
                            "\tRegistered on - %s\n" +
                            "\tNumber of pictures - %d\n",
                    car.getMake(),
                    car.getModel(),
                    car.getKilometers(),
                    car.getRegisteredOn(),
                    car.getPictures().size()
            );

            result.append(carInfo).append(System.lineSeparator());
        }

        return result.toString();
    }

    @Override
    public Car getByMakeAndModelAndKilometers(String make, String model, Integer kilometers) {
        return this.carRepository.findByMakeAndModelAndKilometers(make, model, kilometers);
    }

    @Override
    public Car getById(Long id) {
        return this.carRepository.findById(id).orElse(null);
    }


}
