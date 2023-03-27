package at.ac.tuwien.sepm.assignment.individual.persistence;

import at.ac.tuwien.sepm.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Owner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles({"test", "datagen"}) // enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
public class OwnerDaoTest {

  @Autowired
  OwnerDao ownerDao;

  @Test
  @DirtiesContext
  public void createWhenGivenValidDataShouldReturnCorrectOwner() {
    OwnerCreateDto validOwner = new OwnerCreateDto("ValidTestFirstName", "ValidTestLastName", null);
    Owner createdOwner = ownerDao.create(validOwner);
    assertNotNull(createdOwner);
    assertAll(
        () -> assertNotNull(createdOwner.getFirstName()),
        () -> assertEquals(createdOwner.getFirstName(), validOwner.firstName()),
        () -> assertEquals(createdOwner.getLastName(), validOwner.lastName()),
        () -> assertEquals(createdOwner.getEmail(), validOwner.email())
    );
  }
}
