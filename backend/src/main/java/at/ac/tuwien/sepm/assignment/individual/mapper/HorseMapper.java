package at.ac.tuwien.sepm.assignment.individual.mapper;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseTreeDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.exception.FatalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

@Component
public class HorseMapper {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public HorseMapper() {
  }

  /**
   * Convert a horse entity object to a {@link HorseListDto}.
   * The given map of owners needs to contain the owner of {@code horse}.
   *
   * @param horse  the horse to convert
   * @param owners a map of horse owners by their id, which needs to contain the owner referenced by {@code horse}
   * @return the converted {@link HorseListDto}
   */
  public HorseListDto entityToListDto(Horse horse, Map<Long, OwnerDto> owners) {
    LOG.trace("entityToListDto({})", horse);
    if (horse == null) {
      return null;
    }

    return new HorseListDto(
        horse.getId(),
        horse.getName(),
        horse.getDescription(),
        horse.getDateOfBirth(),
        horse.getSex(),
        getOwner(horse, owners)
    );
  }

  /**
   * Convert a horse entity object to a {@link HorseListDto}.
   * The given map of owners needs to contain the owner of {@code horse}.
   *
   * @param horse  the horse to convert
   * @param owners a map of horse owners by their id, which needs to contain the owner referenced by {@code horse}
   * @return the converted {@link HorseDetailDto}
   */
  public HorseDetailDto entityToDetailDto(
      Horse horse,
      Map<Long, OwnerDto> owners,
      Horse mother,
      Horse father) {
    LOG.trace("entityToDetailDto({})", horse);
    if (horse == null) {
      return null;
    }

    return new HorseDetailDto(
        horse.getId(),
        horse.getName(),
        horse.getDescription(),
        horse.getDateOfBirth(),
        horse.getSex(),
        getOwner(horse, owners),
        entityToListDto(mother, owners),
        entityToListDto(father, owners)
    );
  }

  /**
   * Convert a horse entity object to a {@link HorseTreeDto}.
   *
   * @param horse     the horse to convert
   * @param ancestors the ancestor list of the specified horse (including itself)
   * @return the converted {@link HorseTreeDto}
   */
  public HorseTreeDto entityToTreeDto(
      Horse horse,
      List<Horse> ancestors) {
    LOG.trace("entityToTreeDto({}, {})", horse, ancestors);
    if (horse == null) {
      return null;
    }

    // check if ancestors-list contains parents of horse
    Horse mother = null;
    for (Horse h : ancestors) {
      if (horse.getMotherId() == h.getId()) {
        mother = h;
      }
    }
    Horse father = null;
    for (Horse h : ancestors) {
      if (horse.getFatherId() == h.getId()) {
        father = h;
      }
    }

    return new HorseTreeDto(
        horse.getId(),
        horse.getName(),
        horse.getDateOfBirth(),
        horse.getSex(),
        mother != null ? entityToTreeDto(mother, ancestors) : null,
        father != null ? entityToTreeDto(father, ancestors) : null
    );
  }

  /**
   * Convert the owners of the specified horse to a {@link OwnerDto}.
   *
   * @param horse  the horse to get the owner from
   * @param owners a map of horse owners by their id, which needs to contain the owner referenced by {@code horse}
   * @return the converted {@link OwnerDto}
   */
  private OwnerDto getOwner(Horse horse, Map<Long, OwnerDto> owners) {
    OwnerDto owner = null;
    var ownerId = horse.getOwnerId();
    if (ownerId != null && owners != null) {
      if (!owners.containsKey(ownerId)) {
        throw new FatalException("Given owner map does not contain owner of this Horse (%d)".formatted(horse.getId()));
      }
      owner = owners.get(ownerId);
    }
    return owner;
  }

}
