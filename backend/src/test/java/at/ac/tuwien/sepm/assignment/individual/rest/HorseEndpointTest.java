package at.ac.tuwien.sepm.assignment.individual.rest;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"test", "datagen"}) // enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
@EnableWebMvc
@WebAppConfiguration
public class HorseEndpointTest {

  @Autowired
  private WebApplicationContext webAppContext;
  private MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @BeforeEach
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
  }

  @Test
  public void gettingAllHorses() throws Exception {
    byte[] body = mockMvc
        .perform(MockMvcRequestBuilders
            .get("/horses")
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        .andReturn().getResponse().getContentAsByteArray();

    List<HorseListDto> horseResult = objectMapper.readerFor(HorseListDto.class).<HorseListDto>readValues(body).readAll();

    assertThat(horseResult).isNotNull();
    assertThat(horseResult.size()).isGreaterThanOrEqualTo(5); // TODO adapt this to the exact number in the test data later
    assertThat(horseResult)
        .extracting(HorseListDto::id, HorseListDto::name)
        .contains(tuple(-1L, "Wendy"));
  }

  @Test
  public void gettingNonexistentUrlReturns404() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders
            .get("/asdf123")
        ).andExpect(status().isNotFound());
  }

  @Test
  @DirtiesContext
  public void createWhenGivenValidDataShouldReturnCorrectHorse() throws Exception {
    HorseCreateDto validHorse = new HorseCreateDto("ValidTestHorse", "TestDesc", LocalDate.now(), Sex.FEMALE,
        new OwnerDto(-1, "Hans", "Wurst", null), null, null);

    byte[] putBody = mockMvc
        .perform(MockMvcRequestBuilders
            .post("/horses")
            .content(objectMapper.writeValueAsString(validHorse))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsByteArray();
    HorseDetailDto createdHorse = objectMapper.readValue(putBody, HorseDetailDto.class);

    assertNotNull(createdHorse);
    assertAll(
        () -> assertNotNull(createdHorse.name()),
        () -> assertEquals(createdHorse.name(), validHorse.name()),
        () -> assertEquals(createdHorse.description(), validHorse.description()),
        () -> assertEquals(createdHorse.dateOfBirth(), validHorse.dateOfBirth()),
        () -> assertEquals(createdHorse.sex(), validHorse.sex()),
        () -> assertEquals(createdHorse.ownerId(), validHorse.ownerId()),
        () -> assertEquals(createdHorse.mother(), validHorse.mother()),
        () -> assertEquals(createdHorse.father(), validHorse.father())
    );
  }

  @Test
  @DirtiesContext
  public void createWhenGivenInvalidDataShouldThrow() throws Exception {
    HorseCreateDto validHorse = new HorseCreateDto("InvalidTestHorse", "TestDesc", null, Sex.FEMALE,
        new OwnerDto(-1, "Hans", "Wurst", null), null, null);

    mockMvc
        .perform(MockMvcRequestBuilders
            .post("/horses")
            .content(objectMapper.writeValueAsString(validHorse))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnprocessableEntity());
  }

  @Test
  @DirtiesContext
  public void updateShouldChangeName() throws Exception {
    byte[] getBody = mockMvc
        .perform(MockMvcRequestBuilders
            .get("/horses/-7")
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        .andReturn().getResponse().getContentAsByteArray();
    HorseDetailDto horse = objectMapper.readValue(getBody, HorseDetailDto.class);

    assertThat(horse.name()).isEqualTo("Garfield");
    var updateHorse = new HorseDetailDto(
        horse.id(), "NewRandomTestName", horse.description(), horse.dateOfBirth(), horse.sex(), horse.owner(), horse.mother(), horse.father());

    byte[] putBody = mockMvc
        .perform(MockMvcRequestBuilders
            .put("/horses/-7")
            .content(objectMapper.writeValueAsString(updateHorse))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        .andReturn().getResponse().getContentAsByteArray();

    HorseDetailDto horseWithNewName = objectMapper.readValue(putBody, HorseDetailDto.class);
    assertNotNull(horseWithNewName);
    assertAll(
        () -> assertNotNull(horseWithNewName.name()),
        () -> assertEquals(horseWithNewName.name(), ("NewRandomTestName")),
        () -> assertNotEquals(horseWithNewName.name(), horse.name())
    );
  }
}
