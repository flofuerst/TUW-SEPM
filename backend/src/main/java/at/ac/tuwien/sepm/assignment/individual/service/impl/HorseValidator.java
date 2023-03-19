package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class HorseValidator {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


  public void validateForUpdate(HorseDetailDto horse) throws ValidationException, ConflictException {
    LOG.trace("validateForUpdate({})", horse);
    List<String> validationErrors = new ArrayList<>();

    if (horse.id() == null) {
      validationErrors.add("No ID given");
    }

    if (horse.name() == null) {
      validationErrors.add("Horse name is not given");
    } else if (horse.name().isBlank()) {
      validationErrors.add("Horse name is given but blank");
    }

    if (horse.description() != null) {
      if (horse.description().isBlank()) {
        validationErrors.add("Horse description is given but blank");
      }
      if (horse.description().length() > 4095) {
        validationErrors.add("Horse description too long: longer than 4095 characters");
      }
    }

    if (horse.dateOfBirth() == null) {
      validationErrors.add("Date of birth of horse is not given");
    }

    if (horse.sex() == null) {
      validationErrors.add("Sex of horse is not given");
    }

    //TODO: Validate owner
    //    if (newHorse.owner() != null) {
    //      if (newHorse.owner().firstName().isBlank() || newHorse.owner().lastName().isBlank()) {
    //        validationErrors.add("Owner of horse is given but blank");
    //      }
    //    }

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for update failed", validationErrors);
    }
  }

  public void validateForCreate(HorseCreateDto newHorse) throws ValidationException {
    LOG.trace("validateForCreate({})", newHorse);
    List<String> validationErrors = new ArrayList<>();

    if (newHorse.name() == null) {
      validationErrors.add("Horse name is not given");
    } else if (newHorse.name().isBlank()) {
      validationErrors.add("Horse name is given but blank");
    }

    if (newHorse.description() != null) {
      if (newHorse.description().isBlank()) {
        validationErrors.add("Horse description is given but blank");
      }
      if (newHorse.description().length() > 4095) {
        validationErrors.add("Horse description too long: longer than 4095 characters");
      }
    }

    if (newHorse.dateOfBirth() == null) {
      validationErrors.add("Date of birth of horse is not given");
    } else if (newHorse.dateOfBirth().isAfter(LocalDate.now())) {
      validationErrors.add("Date of birth is given but invalid: date of birth lies in the future");
    }

    if (newHorse.sex() == null) {
      validationErrors.add("Sex of horse is not given");
    }

    //TODO: Validate owner
    //    if (newHorse.owner() != null) {
    //      if (newHorse.owner().firstName().isBlank() || newHorse.owner().lastName().isBlank()) {
    //        validationErrors.add("Owner of horse is given but blank");
    //      }
    //    }

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for create failed", validationErrors);
    }
  }

}
