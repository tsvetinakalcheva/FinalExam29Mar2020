package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dtos.PictureSeedDto;
import softuni.exam.models.entities.Car;
import softuni.exam.models.entities.Picture;
import softuni.exam.repository.PictureRepository;
import softuni.exam.service.CarService;
import softuni.exam.service.OfferService;
import softuni.exam.service.PictureService;
import softuni.exam.util.ValidationUtil;

import javax.transaction.Transactional;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static softuni.exam.constants.GlobalConstants.IN_DB_MESSAGE;
import static softuni.exam.constants.GlobalConstants.PICTURES_FILE_PATH;

@Service
@Transactional
public class PictureServiceImpl implements PictureService {
    private final PictureRepository pictureRepository;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final Gson gson;
    private final CarService carService;


    public PictureServiceImpl(PictureRepository pictureRepository, ModelMapper modelMapper, ValidationUtil validationUtil, Gson gson, CarService carService) {
        this.pictureRepository = pictureRepository;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.gson = gson;
        this.carService = carService;

    }

    @Override
    public boolean areImported() {
        return this.pictureRepository.count() > 0;
    }

    @Override
    public String readPicturesFromFile() throws IOException {
        return Files.readString(Path.of(PICTURES_FILE_PATH));
    }

    @Override
    public String importPictures() throws IOException {
        StringBuilder resultInfo = new StringBuilder();
        PictureSeedDto[] dtos = this.gson
                .fromJson(new FileReader(PICTURES_FILE_PATH), PictureSeedDto[].class);
        for (PictureSeedDto pictureSeedDto : dtos) {
            if (this.validationUtil.isValid(pictureSeedDto)) {
                if (this.pictureRepository.findByName(pictureSeedDto.getName()) == null) {
                    Picture picture = this.modelMapper.map(pictureSeedDto, Picture.class);
                    Car car = this.carService.getById(pictureSeedDto.getCar());
                    picture.setCar(car);
                    resultInfo.append(String.format("Successfully imported picture %s", picture.getName()));
                    this.pictureRepository.saveAndFlush(picture);
                } else {
                    resultInfo.append(IN_DB_MESSAGE);
                }
            } else {
                resultInfo.append("Invalid picture");
            }
            resultInfo.append(System.lineSeparator());
        }
        return resultInfo.toString();
    }

    @Override
    public List<Picture> getPictureForCar(Long carId) {
        List<Picture> byCarId = new ArrayList<>();
        byCarId.add(this.pictureRepository.getOne(carId));
        return byCarId;

    }
}
