package bg.tuvarna.devicebackend.api;

import bg.tuvarna.devicebackend.controllers.exceptions.ErrorResponse;
import bg.tuvarna.devicebackend.models.dtos.AuthResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PassportApiTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper mapper;

    private static String token;
    private static Long passportId;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @Order(1)
    void userRegistrationForPassports() throws Exception {
        // Register a user that will manage passports
        MvcResult registration = mvc.perform(
                        post("/api/v1/users/registration")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "fullName": "Passport Test User",
                                          "email": "passport.api@test.bg",
                                          "phone": "123456789",
                                          "username": "passport_api_user",
                                          "password": "Az$um_PASSPORT123"
                                        }
                                        """)
                )
                .andReturn();

        assertEquals(200, registration.getResponse().getStatus());
    }

    @Test
    @Order(2)
    void userLoginForPassports() throws Exception {
        MvcResult login = mvc.perform(
                        post("/api/v1/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "passport.api@test.bg",
                                          "password": "Az$um_PASSPORT123"
                                        }
                                        """)
                )
                .andReturn();

        assertEquals(200, login.getResponse().getStatus());

        AuthResponseDTO auth = mapper.readValue(
                login.getResponse().getContentAsString(),
                AuthResponseDTO.class
        );

        token = auth.getToken();
    }

    @Test
    @Order(8)
    void getPassportUnauthorized() throws Exception {
        MvcResult result = mvc.perform(
                        get("/api/v1/passports/{id}", passportId)
                )
                .andReturn();

        assertEquals(401, result.getResponse().getStatus());
    }
}
