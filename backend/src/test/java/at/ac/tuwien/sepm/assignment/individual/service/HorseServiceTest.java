package at.ac.tuwien.sepm.assignment.individual.service;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles({"test", "datagen"}) // enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
public class HorseServiceTest {
  @Autowired
  HorseService horseService;

  @Test
  public void getAllReturnsAllStoredHorses() {
    List<HorseListDto> horses = horseService.allHorses()
        .toList();
    assertThat(horses.size()).isGreaterThanOrEqualTo(13);
    assertThat(horses)
        .map(HorseListDto::id, HorseListDto::sex)
        .contains(tuple(-1L, Sex.FEMALE));
  }

  @Test
  @DirtiesContext
  public void createWhenGivenValidDataShouldReturnCorrectHorseDetailDto() throws ValidationException, ConflictException, NotFoundException {
    HorseCreateDto validHorse = new HorseCreateDto("ValidTestHorse", "TestDesc", LocalDate.now(), Sex.FEMALE,
        new OwnerDto(-1, "Hans", "Wurst", null), null, null);
    HorseDetailDto createdHorse = horseService.create(validHorse);
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
  public void createWhenGivenInvalidDataShouldThrow() throws ValidationException, ConflictException, NotFoundException {
    HorseCreateDto invalidHorse = new HorseCreateDto("InvalidTestHorse", "TestDesc", null, Sex.FEMALE, null, null, null);
    Assertions.assertThrows(ValidationException.class,
        () -> horseService.create(invalidHorse));
  }

  @Test
  @DirtiesContext
  public void updateShouldChangeName() throws NotFoundException, ValidationException, ConflictException {
    HorseDetailDto horse = horseService.getById(-7);
    assertThat(horse.name()).isEqualTo("Garfield");
    HorseDetailDto horseWithNewName = horseService.update(new HorseDetailDto(
        horse.id(), "NewRandomTestName", horse.description(), horse.dateOfBirth(), horse.sex(), horse.owner(), horse.mother(), horse.father()));
    assertNotNull(horseWithNewName);
    assertAll(
        () -> assertNotNull(horseWithNewName.name()),
        () -> assertEquals(horseWithNewName.name(), ("NewRandomTestName")),
        () -> assertNotEquals(horseWithNewName.name(), horse.name())
    );
  }
}
