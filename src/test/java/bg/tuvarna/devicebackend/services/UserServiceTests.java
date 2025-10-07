package bg.tuvarna.devicebackend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class UserServiceTests {

    @MockBean
    @Autowired
    private UserService userService;
}
