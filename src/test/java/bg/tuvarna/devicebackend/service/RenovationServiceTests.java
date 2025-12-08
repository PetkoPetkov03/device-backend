package bg.tuvarna.devicebackend.service;

import bg.tuvarna.devicebackend.models.dtos.RenovationCreateVO;
import bg.tuvarna.devicebackend.models.entities.Device;
import bg.tuvarna.devicebackend.models.entities.Renovation;
import bg.tuvarna.devicebackend.repositories.RenovationRepository;
import bg.tuvarna.devicebackend.services.DeviceService;
import bg.tuvarna.devicebackend.services.RenovationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class RenovationServiceTests {

    @MockBean
    private RenovationRepository renovationRepository;

    @MockBean
    private DeviceService deviceService;

    @Autowired
    private RenovationService renovationService;

    @Test
    public void saveRenovationSuccess() {

        String deviceId = "ABC123";

        RenovationCreateVO vo = new RenovationCreateVO(
                deviceId,
                "Changed compressor",
                LocalDate.now()
        );

        Device device = new Device();
        device.setSerialNumber(deviceId);

        when(deviceService.isDeviceExists(anyString())).thenReturn(device);

        Renovation savedRenovation = new Renovation();
        savedRenovation.setDevice(device);
        savedRenovation.setDescription("Changed compressor");
        savedRenovation.setRenovationDate(LocalDate.now());

        when(renovationRepository.save(any(Renovation.class)))
                .thenReturn(savedRenovation);

        Renovation result = renovationService.save(vo);

        assertEquals(deviceId, result.getDevice().getSerialNumber());
        assertEquals("Changed compressor", result.getDescription());
    }
}
