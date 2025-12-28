package bg.tuvarna.devicebackend;

import org.junit.jupiter.api.Test;
import bg.tuvarna.devicebackend.services.UserService;
import bg.tuvarna.devicebackend.repositories.UserRepository;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DeviceBackendApplicationTests {

    @MockBean
    private UserRepository userRepository;
        
    @Autowire
    private UserService userService;

    @Test
    public void 
    
}
