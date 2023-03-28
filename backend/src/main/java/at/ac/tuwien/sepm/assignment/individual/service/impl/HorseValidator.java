package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;
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

  private void validateId(Long id, List<String> validationErrors) {
    if (id == null) {
      validationErrors.add("No ID given");
    }
  }

  private void validateName(String name, List<String> validationErrors) {
    if (name == null) {
      validationErrors.add("Horse name is not given");
    } else if (name.isBlank()) {
      validationErrors.add("Horse name is given but blank");
    }
  }

  private void validateDescription(String description, List<String> validationErrors) {
    if (description != null) {
      if (description.isBlank()) {
        validationErrors.add("Horse description is given but blank");
      }
      if (description.length() > 4095) {
        validationErrors.add("Horse description too long: longer than 4095 characters");
      }
    }
  }

  private void validateDateOfBirth(LocalDate dateOfBirth, List<String> validationErrors) {
    if (dateOfBirth == null) {
      validationErrors.add("Date of birth of horse is not given");
    } else if (dateOfBirth.isAfter(LocalDate.now())) {
      validationErrors.add("Date of birth is given but invalid: date of birth lies in the future");
    }
  }

  private void validateSex(Sex sex, List<String> validationErrors) {
    if (sex == null) {
      validationErrors.add("Sex of horse is not given");
    }
  }

  private void checkForConflicts(Long horseId, LocalDate horseDateOfBirth, HorseListDto mother, HorseListDto father, List<String> conflictErrors) {
    if (mother != null) {
      if (mother.id() == horseId) {
        conflictErrors.add("Horse and its mother are the same horse");
      }
      if (horseDateOfBirth.isBefore(mother.dateOfBirth())) {
        conflictErrors.add("Horse is born before its mother");
      }
      if (horseDateOfBirth.isEqual(mother.dateOfBirth())) {
        conflictErrors.add("Horse is born at the exact same date as its mother");
      }
      if (mother.sex() != Sex.FEMALE) {
        conflictErrors.add("The mother of the horse is not a female horse");
      }
    }
    if (father != null) {
      if (father.id() == horseId) {
        conflictErrors.add("Horse and its father are the same horse");
      }
      if (horseDateOfBirth.isBefore(father.dateOfBirth())) {
        conflictErrors.add("Horse is born before its father");
      }
      if (horseDateOfBirth.isEqual(father.dateOfBirth())) {
        conflictErrors.add("Horse is born at the exact same date as its father");
      }
      if (father.sex() != Sex.MALE) {
        conflictErrors.add("The father of the horse is not a male horse");
      }
    }
  }

  public void validateForUpdate(HorseDetailDto horse, HorseListDto mother, HorseListDto father, boolean isParentOfChildren, Sex sexBeforeUpdate)
      throws ValidationException, ConflictException {
    LOG.trace("validateForUpdate({}, {}, {})", horse, mother, father);
    List<String> validationErrors = new ArrayList<>();

    // call sub-methods to prevent code-duplication --> could be done in one big methode with many parameters
    // validations can't be in one whole methode, because validateForUpdate would hand over a HorseDetailDto
    // and validateForCreate would hand over a HorseCreateDto
    validateId(horse.id(), validationErrors);
    validateName(horse.name(), validationErrors);
    validateDescription(horse.description(), validationErrors);
    validateDateOfBirth(horse.dateOfBirth(), validationErrors);
    validateSex(horse.sex(), validationErrors);

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for update failed", validationErrors);
    }

    // only check for conflicts if validation is okay
    // horseId is null, because it is not existing during create
    List<String> conflictErrors = new ArrayList<>();
    checkForConflicts(null, horse.dateOfBirth(), mother, father, conflictErrors);

    // check if horse is parent of children and if sex changes
    if (isParentOfChildren && horse.sex() != sexBeforeUpdate) {
      conflictErrors.add("Sex can't be changed, because horse is a parent horse");
    }
    if (!conflictErrors.isEmpty()) {
      throw new ConflictException("Update for horse failed because of conflict(s)", conflictErrors);
    }
  }

  public void validateForCreate(HorseCreateDto newHorse, HorseListDto mother, HorseListDto father) throws ValidationException, ConflictException {
    LOG.trace("validateForCreate({}, {}, {})", newHorse, mother, father);
    List<String> validationErrors = new ArrayList<>();

    // call sub-methods to prevent code-duplication --> could be done in one big methode with many parameters
    // validations can't be in one whole methode, because validateForUpdate would hand over a HorseDetailDto
    // and validateForCreate would hand over a HorseCreateDto
    validateName(newHorse.name(), validationErrors);
    validateDescription(newHorse.description(), validationErrors);
    validateDateOfBirth(newHorse.dateOfBirth(), validationErrors);
    validateSex(newHorse.sex(), validationErrors);

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for create failed", validationErrors);
    }

    //only check for conflicts if validation is okay
    // horseId is null, because it is not existing during create
    List<String> conflictErrors = new ArrayList<>();
    checkForConflicts(null, newHorse.dateOfBirth(), mother, father, conflictErrors);
    if (!conflictErrors.isEmpty()) {
      throw new ConflictException("Create for horse failed because of conflict(s)", conflictErrors);
    }
  }

  public void validateAncestors(Long id, Long maxGenerations) throws ValidationException {
    LOG.trace("validateAncestors({}, {})", id, maxGenerations);
    List<String> validationErrors = new ArrayList<>();

    if (id == null) {
      validationErrors.add("No ID given");
    }
    if (maxGenerations == null) {
      validationErrors.add("No maximum of ancestor generations given");
    } else if (maxGenerations <= 0) {
      validationErrors.add("Maximum of ancestor generations is given but smaller than 1");
    }

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation for ancestor-tree failed", validationErrors);
    }
  }

}
