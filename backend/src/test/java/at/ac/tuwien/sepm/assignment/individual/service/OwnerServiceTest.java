package at.ac.tuwien.sepm.assignment.individual.service;

import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@ActiveProfiles({"test", "datagen"}) // enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
public class OwnerServiceTest {
  @Autowired
  OwnerService ownerService;

  @Test
  public void searchWhenGivenNameShouldContainCorrectOwner() throws NotFoundException {
    List<OwnerDto> searchedOwners = ownerService.search(new OwnerSearchDto("Jojo", null))
        .toList();
    assertThat(searchedOwners.size()).isGreaterThanOrEqualTo(1);
    assertThat(searchedOwners)
        .map(OwnerDto::id, OwnerDto::firstName, OwnerDto::lastName, OwnerDto::email)
        .contains(tuple(-10L, "Jojo", "Joestar", "jojo.joestar@gmail.at"));
  }
}
