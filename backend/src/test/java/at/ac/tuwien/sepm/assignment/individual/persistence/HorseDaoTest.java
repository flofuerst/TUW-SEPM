package at.ac.tuwien.sepm.assignment.individual.persistence;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
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
public class HorseDaoTest {

  @Autowired
  HorseDao horseDao;

  @Test
  public void getAllReturnsAllStoredHorses() {
    List<Horse> horses = horseDao.getAll();
    assertThat(horses.size()).isGreaterThanOrEqualTo(13);
    assertThat(horses)
        .extracting(Horse::getId, Horse::getName)
        .contains(tuple(-1L, "Wendy"));
  }

  @Test
  @DirtiesContext
  public void createWhenGivenValidDataShouldReturnCorrectHorse() throws ValidationException, ConflictException, NotFoundException {
    HorseCreateDto validHorse = new HorseCreateDto("ValidTestHorse", "TestDesc", LocalDate.now(), Sex.FEMALE,
        new OwnerDto(-1, "Hans", "Wurst", null), null, null);
    Horse createdHorse = horseDao.create(validHorse);
    assertNotNull(createdHorse);
    assertAll(
        () -> assertNotNull(createdHorse.getName()),
        () -> assertEquals(createdHorse.getName(), validHorse.name()),
        () -> assertEquals(createdHorse.getDescription(), validHorse.description()),
        () -> assertEquals(createdHorse.getDateOfBirth(), validHorse.dateOfBirth()),
        () -> assertEquals(createdHorse.getSex(), validHorse.sex()),
        () -> assertEquals(createdHorse.getOwnerId(), validHorse.ownerId())
    );
  }

  @Test
  public void getByIdWhenIdIsNotExistingShouldThrow() {
    Assertions.assertThrows(NotFoundException.class,
        () -> horseDao.getById(-80));
  }

  @Test
  @DirtiesContext
  public void updateShouldChangeName() throws NotFoundException {
    Horse horse = horseDao.getById(-7);
    assertThat(horse.getName()).isEqualTo("Garfield");
    Horse horseWithNewName = horseDao.update(new HorseDetailDto(
        horse.getId(), "NewRandomTestName", horse.getDescription(), horse.getDateOfBirth(), horse.getSex(), null, null, null));
    assertNotNull(horseWithNewName);
    assertAll(
        () -> assertNotNull(horseWithNewName.getName()),
        () -> assertEquals(horseWithNewName.getName(), ("NewRandomTestName")),
        () -> assertNotEquals(horseWithNewName.getName(), horse.getName())
    );
  }
}
