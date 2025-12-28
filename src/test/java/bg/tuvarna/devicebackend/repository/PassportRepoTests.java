package bg.tuvarna.devicebackend.repository;

import bg.tuvarna.devicebackend.models.dtos.PassportCreateVO;
import bg.tuvarna.devicebackend.models.entities.Passport;
import bg.tuvarna.devicebackend.models.mappers.PassportMapper;
import bg.tuvarna.devicebackend.repositories.PassportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class PassportRepoTests {

    @Autowired
    private PassportRepository passportRepository;

    @BeforeEach
    void setUp() {
        passportRepository.deleteAll();

        PassportCreateVO passport1VO = new PassportCreateVO(
                "Model A", "A1", "SB",
                1000, 2000, 24
        );

        PassportCreateVO passport2VO = new PassportCreateVO(
                "Model B", "B1", "XY",
                500, 800, 12
        );

        Passport passport1 = PassportMapper.toEntity(passport1VO);
        Passport passport2 = PassportMapper.toEntity(passport2VO);

        passportRepository.save(passport1);
        passportRepository.save(passport2);
    }


    @Test
    void testFindByFromSerialNumberBetween_ShouldReturnEmptyForNonMatchingPrefix() {
        List<Passport> results = passportRepository.findByFromSerialNumberBetween("ZZ", 1, 5000);
        assertTrue(results.isEmpty(), "Should not find any passports for unknown prefix");
    }

    @Test
    void testFindByFromSerial_ShouldReturnMatchingPassport() {
        List<Passport> results = passportRepository.findByFromSerial("SB12345");

        assertNotNull(results);
        assertEquals(1, results.size(), "Should find passport with matching serial prefix SB");
        assertEquals("SB", results.get(0).getSerialPrefix());
        assertEquals("Model A", results.get(0).getName());
    }

    @Test
    void testFindByFromSerial_ShouldReturnEmptyForNonMatchingSerial() {
        List<Passport> results = passportRepository.findByFromSerial("ZZ9999");
        assertTrue(results.isEmpty(), "Should return empty for non-matching serial prefix");
    }
}
