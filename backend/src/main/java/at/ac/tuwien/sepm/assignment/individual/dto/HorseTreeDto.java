package at.ac.tuwien.sepm.assignment.individual.dto;

import at.ac.tuwien.sepm.assignment.individual.type.Sex;

import java.time.LocalDate;

public record HorseTreeDto(
    Long id,
    String name,
    LocalDate dateOfBirth,
    Sex sex,
    HorseTreeDto mother,
    HorseTreeDto father
) {
  public HorseTreeDto withHorseParents(HorseTreeDto newMother, HorseTreeDto newFather) {
    return new HorseTreeDto(
        id,
        name,
        dateOfBirth,
        sex,
        newMother,
        newFather);
  }
}
