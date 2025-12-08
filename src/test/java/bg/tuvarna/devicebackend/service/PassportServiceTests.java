package bg.tuvarna.devicebackend.service;

import bg.tuvarna.devicebackend.controllers.exceptions.CustomException;
import bg.tuvarna.devicebackend.models.dtos.PassportCreateVO;
import bg.tuvarna.devicebackend.models.dtos.PassportUpdateVO;
import bg.tuvarna.devicebackend.models.entities.Passport;
import bg.tuvarna.devicebackend.repositories.PassportRepository;
import bg.tuvarna.devicebackend.services.PassportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class PassportServiceTests {

    @MockBean
    private PassportRepository passportRepository;

    @Autowired
    private PassportService passportService;

    @Test
    void createShouldThrowAlreadyExists() {
        PassportCreateVO vo = new PassportCreateVO(
                "Passport X",
                "Model A",
                "SN-",
                12,
                1,
                100
        );

        when(passportRepository.findByFromSerialNumberBetween("SN-", 1, 100))
                .thenReturn(List.of(new Passport()));

        CustomException ex = assertThrows(
                CustomException.class,
                () -> passportService.create(vo)
        );

        assertEquals("Serial number already exists", ex.getMessage());
    }

    @Test
    void createShouldSave() {
        PassportCreateVO vo = new PassportCreateVO(
                "Passport Test",
                "Model B",
                "PF-",
                6,
                10,
                20
        );

        Passport p = new Passport();
        p.setId(1L);

        when(passportRepository.findByFromSerialNumberBetween("PF-", 10, 20))
                .thenReturn(List.of());
        when(passportRepository.save(any(Passport.class)))
                .thenReturn(p);

        Passport saved = passportService.create(vo);

        assertNotNull(saved);
        assertEquals(1L, saved.getId());
    }

    @Test
    void updateShouldThrowNotFound() {
        when(passportRepository.findById(99L)).thenReturn(Optional.empty());

        PassportUpdateVO vo = new PassportUpdateVO(null, null, null, null, null, null);

        CustomException ex = assertThrows(
                CustomException.class,
                () -> passportService.update(99L, vo)
        );

        assertEquals("Passport not found", ex.getMessage());
    }

    @Test
    void updateShouldThrowAlreadyExistsWhenRangeConflict() {
        Passport existing = new Passport();
        existing.setId(5L);
        existing.setSerialPrefix("SN-");
        existing.setFromSerialNumber(1);
        existing.setToSerialNumber(100);

        Passport other = new Passport();
        other.setId(99L);

        when(passportRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(passportRepository.findByFromSerialNumberBetween("SN-", 1, 100))
                .thenReturn(List.of(existing, other));

        PassportUpdateVO vo = new PassportUpdateVO(
                null,
                null,
                "SN-",
                12,
                1,
                100
        );

        CustomException ex = assertThrows(
                CustomException.class,
                () -> passportService.update(5L, vo)
        );

        assertEquals("Serial number already exists", ex.getMessage());
    }

    @Test
    void updateShouldSave() {
        Passport p = new Passport();
        p.setId(10L);
        p.setSerialPrefix("AA-");
        p.setFromSerialNumber(10);
        p.setToSerialNumber(20);

        when(passportRepository.findById(10L)).thenReturn(Optional.of(p));
        when(passportRepository.findByFromSerialNumberBetween("AA-", 10, 20))
                .thenReturn(List.of(p));
        when(passportRepository.save(any(Passport.class))).thenReturn(p);

        PassportUpdateVO vo = new PassportUpdateVO(
                "Passport Y",
                "Model Y",
                "AA-",
                24,
                10,
                20
        );

        Passport updated = passportService.update(10L, vo);

        assertEquals(10L, updated.getId());
    }

    @Test
    void findPassportByIdShouldReturnNull() {
        when(passportRepository.findById(22L)).thenReturn(Optional.empty());

        Passport p = passportService.findPassportById(22L);

        assertNull(p);
    }

    @Test
    void findPassportBySerialIdShouldThrowWhenNotFound() {
        when(passportRepository.findByFromSerial("SN-999"))
                .thenReturn(List.of());

        CustomException ex = assertThrows(
                CustomException.class,
                () -> passportService.findPassportBySerialId("SN-999")
        );

        assertTrue(ex.getMessage().contains("Passport not found"));
    }

    @Test
    void findPassportBySerialIdShouldReturnPassport() {
        Passport p = new Passport();
        p.setSerialPrefix("SN-");
        p.setFromSerialNumber(100);
        p.setToSerialNumber(200);

        when(passportRepository.findByFromSerial("SN-150"))
                .thenReturn(List.of(p));

        Passport found = passportService.findPassportBySerialId("SN-150");

        assertNotNull(found);
    }

    @Test
    void deleteShouldThrowMappingException() {
        doThrow(new RuntimeException())
                .when(passportRepository).deleteById(99L);

        CustomException ex = assertThrows(
                CustomException.class,
                () -> passportService.delete(99L)
        );

        assertEquals("Can't delete passport", ex.getMessage());
    }
}
