package softuni.exam.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.constants.GlobalConstants;
import softuni.exam.models.dtos.SellerSeedRootDto;
import softuni.exam.models.entities.Seller;
import softuni.exam.repository.SellerRepository;
import softuni.exam.service.SellerService;
import softuni.exam.util.ValidationUtil;
import softuni.exam.util.XmlParser;

import javax.transaction.Transactional;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static softuni.exam.constants.GlobalConstants.IN_DB_MESSAGE;
import static softuni.exam.constants.GlobalConstants.SELLERS_FILE_PATH;

@Service
@Transactional
public class SellerServiceImpl implements SellerService {
    private final SellerRepository sellerRepository;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final XmlParser xmlParser;

    public SellerServiceImpl(SellerRepository sellerRepository, ModelMapper modelMapper, ValidationUtil validationUtil, XmlParser xmlParser) {
        this.sellerRepository = sellerRepository;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.xmlParser = xmlParser;
    }

    @Override
    public boolean areImported() {
        return this.sellerRepository.count()>0;
    }

    @Override
    public String readSellersFromFile() throws IOException {
        return Files.readString(Path.of(SELLERS_FILE_PATH));
    }

    @Override
    public String importSellers() throws IOException, JAXBException {
        StringBuilder resultInfo = new StringBuilder();
        SellerSeedRootDto sellerSeedRootDto = this.xmlParser
                .convertFromFile(SELLERS_FILE_PATH, SellerSeedRootDto.class);
        sellerSeedRootDto.getSellerSeedDtos().stream()
                .forEach(sellerSeedDto -> {
                    if(this.validationUtil.isValid(sellerSeedDto)){
                        if(this.sellerRepository.findByFirstNameAndLastNameAndEmail(
                                sellerSeedDto.getFirstName(), sellerSeedDto.getLastName(),
                                sellerSeedDto.getEmail())==null){
                            Seller seller = this.modelMapper.map(sellerSeedDto, Seller.class);
                            this.sellerRepository.saveAndFlush(seller);
                            resultInfo.append(String.format("Successfully imported seller %s %s.",
                                    seller.getFirstName(), seller.getEmail()));
                        } else {
                            resultInfo.append(IN_DB_MESSAGE);
                        }
                    } else {
                       resultInfo.append("Invalid seller");
                    }
                    resultInfo.append(System.lineSeparator());
                });
        return resultInfo.toString();
    }
}
