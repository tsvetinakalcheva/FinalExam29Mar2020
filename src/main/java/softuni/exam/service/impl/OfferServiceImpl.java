package softuni.exam.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dtos.OfferSeedRootDto;
import softuni.exam.models.entities.Offer;
import softuni.exam.repository.OfferRepository;
import softuni.exam.service.OfferService;
import softuni.exam.service.PictureService;
import softuni.exam.util.ValidationUtil;
import softuni.exam.util.XmlParser;

import javax.transaction.Transactional;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


import static softuni.exam.constants.GlobalConstants.*;

@Service
@Transactional
public class OfferServiceImpl implements OfferService {
    private final OfferRepository offerRepository;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final XmlParser xmlParser;
    private final PictureService pictureService;

    public OfferServiceImpl(OfferRepository offerRepository, ModelMapper modelMapper, ValidationUtil validationUtil, XmlParser xmlParser, PictureService pictureService) {
        this.offerRepository = offerRepository;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.xmlParser = xmlParser;
        this.pictureService = pictureService;
    }

    @Override
    public boolean areImported() {
        return this.offerRepository.count()>0;
    }

    @Override
    public String readOffersFileContent() throws IOException {
        return Files.readString(Path.of(OFFERS_FILE_PATH));
    }

    @Override
    public String importOffers() throws IOException, JAXBException {
        StringBuilder resultInfo = new StringBuilder();
        OfferSeedRootDto offerSeedRootDto = this.xmlParser.convertFromFile(OFFERS_FILE_PATH, OfferSeedRootDto.class);
        offerSeedRootDto.getOfferSeedDtos().stream()
                .forEach(offerSeedDto -> {
                    if(this.validationUtil.isValid(offerSeedDto)){
                        if(this.offerRepository.findByDescriptionAndPrice(offerSeedDto.getDescription(),offerSeedDto.getPrice())==null){
                            Offer offer = this.modelMapper.map(offerSeedDto, Offer.class);
                           offer.setPictures(this.pictureService.getPictureForCar(offerSeedDto.getCar().getId()));
                            this.offerRepository.saveAndFlush(offer);

                            resultInfo.append("Successfully imported offers.")
                            .append(offer.getAddedOn())
                            .append(" - ")
                            .append(offer.isHasGoldStatus());
                        } else {
                            resultInfo.append(IN_DB_MESSAGE);
                        }
                    } else {
                        resultInfo.append("Invalid offerSeedDto");
                    }
                    resultInfo.append(System.lineSeparator());
                });

        return resultInfo.toString();
    }


}
