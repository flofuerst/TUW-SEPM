package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class OwnerValidator {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void validateForCreate(OwnerCreateDto newOwner, boolean emailExists) throws ValidationException, ConflictException {
    LOG.trace("validateForCreate({}, {})", newOwner, emailExists);
    List<String> validationErrors = new ArrayList<>();


    if (newOwner.firstName() == null) {
      validationErrors.add("First name of owner is not given");
    } else if (newOwner.firstName().isBlank()) {
      validationErrors.add("First name of owner is given but blank");
    }

    if (newOwner.lastName() == null) {
      validationErrors.add("First name of owner is not given");
    } else if (newOwner.lastName().isBlank()) {
      validationErrors.add("First name of owner is given but blank");
    }

    List<String> conflictErrors = new ArrayList<>();
    if (newOwner.email() != null) {
      if (newOwner.email().isBlank()) {
        validationErrors.add("Email of owner is given but blank");
      }

      if (emailExists) {
        conflictErrors.add("Email already exists");
      }

      Pattern pattern = Pattern.compile("^(\\w+[\\w-\\.])+@([\\w-]+\\.)+[\\w-]{2,4}$");
      Matcher matcher = pattern.matcher(newOwner.email());
      if (!matcher.matches()) {
        validationErrors.add("Email is invalid");
      }
    }

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of owner for create failed", validationErrors);
    }
    if (!conflictErrors.isEmpty()) {
      throw new ConflictException("Create for owner failed because of conflict(s)", conflictErrors);
    }
  }
}
