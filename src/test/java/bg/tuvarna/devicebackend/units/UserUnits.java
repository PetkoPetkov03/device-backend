package bg.tuvarna.devicebackend.units;

import bg.tuvarna.devicebackend.models.dtos.UserCreateVO;
import bg.tuvarna.devicebackend.models.dtos.UserListing;
import bg.tuvarna.devicebackend.models.entities.Device;
import bg.tuvarna.devicebackend.models.entities.User;
import bg.tuvarna.devicebackend.repositories.UserRepository;
import bg.tuvarna.devicebackend.services.DeviceService;
import bg.tuvarna.devicebackend.services.UserService;
import bg.tuvarna.devicebackend.utils.CustomPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserUnits {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @InjectMocks
    private DeviceService deviceService;

    @Mock
    PasswordEncoder passwordEncoder;

    @Test
    public void throwErrorOnPhoneNumberExistsTest() {
        String phone = "0878654432";

        when(userRepository.getByPhone(phone)).thenReturn(new User());

        boolean taken = userService.isPhoneTaken(phone);

        assertTrue(taken);
    }

    @Test
    public void throwErrorOnEmailExistsTest() {
        String email = "test@test.test";

        when(userRepository.findByEmailOrPhone(email)).thenReturn(Optional.of(new User()));

        boolean taken = userService.isEmailTaken(email);

        assertTrue(taken);
    }

    @Test
    public void getUserByUsernameTest() {
        String username = "test";

        when(userRepository.findByEmailOrPhone(username)).thenReturn(Optional.of(new User()));

        User user = userService.getUserByUsername(username);

        assertNotNull(user);
    }

    @Test
    public void getUserByIdTest() {
        Long id = 12L;

        when(userRepository.findById(id)).thenReturn(Optional.of(new User()));

        User user = userService.getUserById(id);

        assertNotNull(user);
    }

    @Test
    public void getUsersTest() {
        Pageable pageable = PageRequest.of(0, 10);

        List<User> users = List.of(new User(), new User());

        Page<User> userPage = new PageImpl<>(users, pageable, users.size());

        when(userRepository.getAllUsers(pageable)).thenReturn(userPage);

        CustomPage<UserListing> userCustomPage = userService.getUsers(null, 1, 10);

        userCustomPage.getItems().forEach(Assertions::assertNotNull);
    }

    @Test
    public void registerUserTest() {
        UserCreateVO userCreateVO = new UserCreateVO("test testing",
                "test1234", "test@test.test",
                "08675984", "Varna", LocalDate.now(), "225234");


        when(userRepository.findByEmailOrPhone(userCreateVO.email())).thenReturn(Optional.empty());
        when(userService.isEmailTaken(userCreateVO.email())).thenReturn(false);
        when(userService.isPhoneTaken(userCreateVO.phone())).thenReturn(false);

        User user = new User(userCreateVO);
        when(passwordEncoder.encode(userCreateVO.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        userService.register(userCreateVO);

        verify(userRepository).save(any(User.class));
        verify(deviceService).alreadyExist(userCreateVO.deviceSerialNumber());
        verify(deviceService).registerDevice(userCreateVO.deviceSerialNumber(), LocalDate.now(), new User(userCreateVO));
    }
}
