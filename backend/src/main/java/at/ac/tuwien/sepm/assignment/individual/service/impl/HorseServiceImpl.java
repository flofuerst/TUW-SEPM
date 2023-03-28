package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseTreeDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepm.assignment.individual.mapper.HorseMapper;
import at.ac.tuwien.sepm.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepm.assignment.individual.service.HorseService;
import at.ac.tuwien.sepm.assignment.individual.service.OwnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class HorseServiceImpl implements HorseService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final HorseDao dao;
  private final HorseMapper mapper;
  private final HorseValidator validator;
  private final OwnerService ownerService;

  public HorseServiceImpl(HorseDao dao, HorseMapper mapper, HorseValidator validator, OwnerService ownerService) {
    this.dao = dao;
    this.mapper = mapper;
    this.validator = validator;
    this.ownerService = ownerService;
  }

  @Override
  public Stream<HorseListDto> allHorses() {
    LOG.trace("allHorses()");
    var horses = dao.getAll();
    var ownerIds = horses.stream()
        .map(Horse::getOwnerId)
        .filter(Objects::nonNull)
        .collect(Collectors.toUnmodifiableSet());
    Map<Long, OwnerDto> ownerMap;
    try {
      ownerMap = ownerService.getAllById(ownerIds);
    } catch (NotFoundException e) {
      throw new FatalException("Horse, that is already persisted, refers to non-existing owner", e);
    }
    return horses.stream()
        .map(horse -> mapper.entityToListDto(horse, ownerMap));
  }

  @Override
  public Stream<HorseListDto> getSpecifiedHorses(HorseSearchDto horseSearchDto) {
    LOG.trace("getSpecifiedHorses({})", horseSearchDto);
    var horses = dao.searchHorses(horseSearchDto);
    var ownerIds = horses.stream()
        .map(Horse::getOwnerId)
        .filter(Objects::nonNull)
        .collect(Collectors.toUnmodifiableSet());
    Map<Long, OwnerDto> ownerMap;
    try {
      ownerMap = ownerService.getAllById(ownerIds);
    } catch (NotFoundException e) {
      throw new FatalException("Horse, that is already persisted, refers to non-existing owner", e);
    }
    return horses.stream()
        .map(horse -> mapper.entityToListDto(horse, ownerMap));
  }


  @Override
  public HorseDetailDto update(HorseDetailDto horse) throws NotFoundException, ValidationException, ConflictException {
    LOG.trace("update({})", horse);

    // get parents from database with id of parents
    // this is used for validation if something is not okay in frontend and values of parents in request and database differ
    // also wrap exception to ConflictException
    Horse mother = null;
    Horse father = null;
    List<String> conflictErrors = new ArrayList<>();
    try {
      mother = horse.mother() != null ? dao.getById(horse.mother().id()) : null;
    } catch (NotFoundException e) {
      conflictErrors.add("Mother of horse does not exist");
    }
    try {
      father = horse.father() != null ? dao.getById(horse.father().id()) : null;
    } catch (NotFoundException e) {
      conflictErrors.add("Father of horse does not exist");
    }
    if (!conflictErrors.isEmpty()) {
      throw new ConflictException("Update for horse failed because of conflict(s)", conflictErrors);
    }

    Horse horseBeforeUpdate = dao.getById(horse.id());
    validator.validateForUpdate(
        horse,
        mother != null ? mapper.entityToListDto(mother, ownerMapForSingleId(mother.getOwnerId())) : null,
        father != null ? mapper.entityToListDto(father, ownerMapForSingleId(father.getOwnerId())) : null,
        dao.isParentOfChildren(horse.id()),
        horseBeforeUpdate.getSex(),
        dao.isOlderThanChildren(horse.id(), horse.dateOfBirth())
    );
    Horse updatedHorse = dao.update(horse);
    Map<Long, OwnerDto> owners = ownerMapWithParents(updatedHorse.getOwnerId(), mother, father);

    // convert to dto again (with correct parents)
    return mapper.entityToDetailDto(
        updatedHorse,
        owners,
        mother,
        father
    );
  }

  @Override
  public HorseDetailDto create(HorseCreateDto newHorse)
      throws NotFoundException, ValidationException, ConflictException {
    LOG.trace("create({})", newHorse);

    // get parents from database with id of parents
    // this is used for validation if something is not okay in frontend and values of parents in request and database differ
    // also wrap exception to ConflictException
    Horse mother = null;
    Horse father = null;
    List<String> conflictErrors = new ArrayList<>();
    try {
      mother = newHorse.mother() != null ? dao.getById(newHorse.mother().id()) : null;
    } catch (NotFoundException e) {
      conflictErrors.add("Mother of horse does not exist");
    }
    try {
      father = newHorse.father() != null ? dao.getById(newHorse.father().id()) : null;
    } catch (NotFoundException e) {
      conflictErrors.add("Father of horse does not exist");
    }
    if (!conflictErrors.isEmpty()) {
      throw new ConflictException("Create for horse failed because of conflict(s)", conflictErrors);
    }

    validator.validateForCreate(
        newHorse,
        mother != null ? mapper.entityToListDto(mother, ownerMapForSingleId(mother.getOwnerId())) : null,
        father != null ? mapper.entityToListDto(father, ownerMapForSingleId(father.getOwnerId())) : null
    );
    Horse createdHorse = dao.create(newHorse);
    Map<Long, OwnerDto> owners = ownerMapWithParents(createdHorse.getOwnerId(), mother, father);

    // convert to dto again (with correct parents)
    return mapper.entityToDetailDto(
        createdHorse,
        owners,
        mother,
        father
    );
  }

  @Override
  public void delete(Long id) throws NotFoundException {
    LOG.trace("delete({})", id);
    dao.delete(id);
  }

  @Override
  public HorseDetailDto getById(long id) throws NotFoundException {
    LOG.trace("getById({})", id);
    Horse horse = dao.getById(id);

    // get parents from database with id of parents
    // this is used for validation if something is not okay in frontend and values of parents in request and database differ
    Horse mother = horse.getMotherId() != null ? dao.getById(horse.getMotherId()) : null;
    Horse father = horse.getFatherId() != null ? dao.getById(horse.getFatherId()) : null;
    Map<Long, OwnerDto> owners = ownerMapWithParents(horse.getOwnerId(), mother, father);

    return mapper.entityToDetailDto(
        horse,
        owners,
        mother,
        father);
  }

  @Override
  public HorseTreeDto getAncestorHorses(long id, long maxGenerations) throws NotFoundException, ValidationException {
    LOG.trace("getAncestorHorses({}, {})", id, maxGenerations);

    validator.validateAncestors(id, maxGenerations);

    List<Horse> ancestors = dao.searchAncestorHorses(id, maxGenerations);
    Horse horse = null;

    // get horse from ancestors-list
    for (Horse h : ancestors) {
      if (id == h.getId()) {
        horse = h;
      }
    }

    if (horse == null) {
      throw new FatalException("Horse is not included in ancestors-list");
    }

    return mapper.entityToTreeDto(horse, ancestors);
  }

  private Map<Long, OwnerDto> ownerMapWithParents(Long ownerId, Horse mother, Horse father) throws NotFoundException {
    LOG.trace("ownerMapWithParents({}, {}, {})", ownerId, mother, father);
    if (ownerId == null) {
      return null;
    }

    Map<Long, OwnerDto> owners = new HashMap<>();
    try {
      owners.put(ownerId, ownerService.getById(ownerId));
    } catch (NotFoundException e) {
      throw new FatalException("Owner %d referenced by horse not found".formatted(ownerId));
    }

    if (mother != null) {
      Long motherOwnerId = mother.getOwnerId();
      if (motherOwnerId != null) {
        try {
          owners.put(motherOwnerId, ownerService.getById(motherOwnerId));
        } catch (NotFoundException e) {
          throw new FatalException("Owner %d referenced by mother horse not found".formatted(motherOwnerId));
        }
      }
    }

    if (father != null) {
      Long fatherOwnerId = father.getOwnerId();
      if (fatherOwnerId != null) {
        try {
          owners.put(fatherOwnerId, ownerService.getById(fatherOwnerId));
        } catch (NotFoundException e) {
          throw new FatalException("Owner %d referenced by father horse not found".formatted(fatherOwnerId));
        }
      }
    }

    return owners;
  }

  private Map<Long, OwnerDto> ownerMapForSingleId(Long ownerId) {
    LOG.trace("ownerMapForSingleId({})", ownerId);
    try {
      return ownerId == null
          ? null
          : Collections.singletonMap(ownerId, ownerService.getById(ownerId));
    } catch (NotFoundException e) {
      throw new FatalException("Owner %d referenced by horse not found".formatted(ownerId));
    }
  }

}
