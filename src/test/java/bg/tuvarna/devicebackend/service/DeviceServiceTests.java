package bg.tuvarna.devicebackend.service;

import bg.tuvarna.devicebackend.controllers.exceptions.CustomException;
import bg.tuvarna.devicebackend.models.dtos.DeviceCreateVO;
import bg.tuvarna.devicebackend.models.dtos.DeviceUpdateVO;
import bg.tuvarna.devicebackend.models.entities.Device;
import bg.tuvarna.devicebackend.models.entities.Passport;
import bg.tuvarna.devicebackend.models.entities.User;
import bg.tuvarna.devicebackend.repositories.DeviceRepository;
import bg.tuvarna.devicebackend.services.DeviceService;
import bg.tuvarna.devicebackend.services.PassportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("test")
public class DeviceServiceTests {

    @MockBean
    private DeviceRepository deviceRepository;

    @MockBean
    private PassportService passportService;

    @Autowired
    private DeviceService deviceService;

    @Test
    void alreadyExistShouldThrowDeviceAlreadyExists() {
        Device device = new Device();
        when(deviceRepository.findById("SN-123")).thenReturn(Optional.of(device));

        CustomException ex = assertThrows(
                CustomException.class,
                () -> deviceService.alreadyExist("SN-123")
        );

        assertEquals("Device already registered", ex.getMessage());
    }

    @Test
    void alreadyExistShouldPassWhenDeviceNotFound() {
        when(deviceRepository.findById("SN-123")).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> deviceService.alreadyExist("SN-123"));
    }

    @Test
    void registerNewDeviceShouldThrowUserNotFound() {
        DeviceCreateVO vo = new DeviceCreateVO(
                "SN-1",
                LocalDate.now()
        );

        when(deviceRepository.findById("SN-1")).thenReturn(Optional.empty());

        CustomException ex = assertThrows(
                CustomException.class,
                () -> deviceService.registerNewDevice(vo, null)
        );

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void registerNewDeviceShouldThrowAlreadyExists() {
        DeviceCreateVO vo = new DeviceCreateVO(
                "SN-1",
                LocalDate.now()
        );

        when(deviceRepository.findById("SN-1")).thenReturn(Optional.of(new Device()));

        CustomException ex = assertThrows(
                CustomException.class,
                () -> deviceService.registerNewDevice(vo, new User())
        );

        assertEquals("Device already registered", ex.getMessage());
    }

    @Test
    void isDeviceExistsShouldThrowNotRegistered() {
        when(deviceRepository.existsById("SN-999")).thenReturn(false);

        CustomException ex = assertThrows(
                CustomException.class,
                () -> deviceService.isDeviceExists("SN-999")
        );

        assertEquals("Device not registered", ex.getMessage());
    }

    @Test
    void isDeviceExistsShouldReturnDevice() {
        Device d = new Device();
        when(deviceRepository.existsById("SN-1")).thenReturn(true);
        when(deviceRepository.findById("SN-1")).thenReturn(Optional.of(d));

        Device result = deviceService.isDeviceExists("SN-1");
        assertNotNull(result);
    }

    @Test
    void addAnonymousDeviceShouldThrowInvalidSerial() {
        DeviceCreateVO vo = new DeviceCreateVO(
                "SN-404",
                LocalDate.now()
        );

        when(deviceRepository.findById("SN-404")).thenReturn(Optional.empty());
        when(passportService.findPassportBySerialId("SN-404")).thenThrow(new RuntimeException());

        CustomException ex = assertThrows(
                CustomException.class,
                () -> deviceService.addAnonymousDevice(vo)
        );

        assertEquals("Invalid serial number", ex.getMessage());
    }

    @Test
    void addAnonymousDeviceShouldSaveDevice() {
        LocalDate now = LocalDate.now();
        DeviceCreateVO vo = new DeviceCreateVO(
                "SN-100",
                now
        );

        Passport p = new Passport();
        p.setWarrantyMonths(12);

        Device saved = new Device();
        saved.setSerialNumber("SN-100");

        when(deviceRepository.findById("SN-100")).thenReturn(Optional.empty());
        when(passportService.findPassportBySerialId("SN-100")).thenReturn(p);
        when(deviceRepository.save(any(Device.class))).thenReturn(saved);

        Device result = deviceService.addAnonymousDevice(vo);
        assertEquals("SN-100", result.getSerialNumber());
    }

    @Test
    void updateDeviceShouldThrowNotFound() {
        when(deviceRepository.findById("SN-100")).thenReturn(Optional.empty());

        DeviceUpdateVO updateVO = new DeviceUpdateVO(
                LocalDate.now(),
                "comment"
        );

        CustomException ex = assertThrows(
                CustomException.class,
                () -> deviceService.updateDevice("SN-100", updateVO)
        );

        assertEquals("Device not found", ex.getMessage());
    }

    @Test
    void updateDeviceShouldUpdateWarranty() {
        Passport passport = new Passport();
        passport.setWarrantyMonths(6);

        Device device = new Device();
        device.setSerialNumber("SN-5");
        device.setPassport(passport);
        device.setUser(new User());
        device.setPurchaseDate(LocalDate.of(2024,1,1));

        DeviceUpdateVO vo = new DeviceUpdateVO(
                LocalDate.of(2024,2,1),
                "new comment"
        );

        when(deviceRepository.findById("SN-5")).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenReturn(device);

        Device updated = deviceService.updateDevice("SN-5", vo);

        LocalDate expected = vo.purchaseDate().plusMonths(6).plusMonths(12);
        assertEquals(expected, updated.getWarrantyExpirationDate());
    }

    @Test
    void deleteDeviceShouldThrowWhenRepositoryFails() {
        doThrow(new RuntimeException())
                .when(deviceRepository).deleteBySerialNumber("BAD");

        CustomException ex = assertThrows(
                CustomException.class,
                () -> deviceService.deleteDevice("BAD")
        );

        assertEquals("Cannot delete device: renovations exist", ex.getMessage());
    }
}
