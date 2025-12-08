package bg.tuvarna.devicebackend.repository;

import bg.tuvarna.devicebackend.models.entities.Device;
import bg.tuvarna.devicebackend.repositories.DeviceRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DeviceRepoTests {

    @Autowired
    private DeviceRepository deviceRepository;

    @BeforeEach
    public void setUp() {
        deviceRepository.deleteAll(); // clear before each test

        Device device = new Device();
        device.setSerialNumber("SB15FDPSF");
        device.setPurchaseDate(LocalDate.now());
        device.setWarrantyExpirationDate(LocalDate.now().plusMonths(12));

        deviceRepository.save(device);
    }

    @Test
    @Order(1)
    public void testFindById_ShouldReturnDevice() {
        Optional<Device> deviceOpt = deviceRepository.findById("SB15FDPSF");

        assertTrue(deviceOpt.isPresent(), "Device should be found by serial number");
        Device device = deviceOpt.get();
        assertEquals("SB15FDPSF", device.getSerialNumber());
        assertNotNull(device.getPurchaseDate());
        assertNotNull(device.getWarrantyExpirationDate());
    }

    @Test
    @Order(2)
    public void testFindById_ShouldReturnEmptyForInvalidSerial() {
        Optional<Device> deviceOpt = deviceRepository.findById("INVALID_SN");
        assertTrue(deviceOpt.isEmpty(), "Invalid serial number should return empty Optional");
    }

    @Test
    @Order(3)
    public void testGetAllDevices_ShouldReturnPagedList() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Device> devicePage = deviceRepository.getAllDevices(pageable);

        assertNotNull(devicePage);
        assertFalse(devicePage.isEmpty(), "Page should not be empty");
        assertEquals(1, devicePage.getTotalElements());
        assertEquals("SB15FDPSF", devicePage.getContent().get(0).getSerialNumber());
    }

    @Test
    @Order(4)
    public void testSaveDevice_ShouldPersistNewDevice() {
        Device device = new Device();
        device.setSerialNumber("NEW12345");
        device.setPurchaseDate(LocalDate.now().minusDays(5));
        device.setWarrantyExpirationDate(LocalDate.now().plusMonths(24));

        Device saved = deviceRepository.save(device);

        assertNotNull(saved);
        assertEquals("NEW12345", saved.getSerialNumber());
        assertEquals(2, deviceRepository.count());
    }

    @Test
    @Order(5)
    public void testDeleteBySerialNumber_ShouldRemoveDevice() {
        assertEquals(1, deviceRepository.count());

        deviceRepository.deleteBySerialNumber("SB15FDPSF");

        assertEquals(0, deviceRepository.count(), "Device should be deleted");
    }

    @Test
    @Order(6)
    public void testExistsById_ShouldReturnTrueIfExists() {
        boolean exists = deviceRepository.existsById("SB15FDPSF");
        assertTrue(exists);
    }

    @Test
    @Order(7)
    public void testExistsById_ShouldReturnFalseIfNotExists() {
        boolean exists = deviceRepository.existsById("NOT_EXIST");
        assertFalse(exists);
    }
}
