package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
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
import java.util.Collections;
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
    LOG.trace("getSpecifiedHorses()");
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
    Horse mother = horse.mother() != null ? dao.getById(horse.mother().id()) : null;
    Horse father = horse.father() != null ? dao.getById(horse.father().id()) : null;
    validator.validateForUpdate(
        horse,
        mother != null ? mapper.entityToListDto(mother, ownerMapForSingleId(mother.getOwnerId())) : null,
        father != null ? mapper.entityToListDto(father, ownerMapForSingleId(father.getOwnerId())) : null
    );
    Horse updatedHorse = dao.update(horse);

    // convert to dto again (with correct parents)
    return mapper.entityToDetailDto(
        updatedHorse,
        ownerMapForSingleId(updatedHorse.getOwnerId()),
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
    Horse mother = newHorse.mother() != null ? dao.getById(newHorse.mother().id()) : null;
    Horse father = newHorse.father() != null ? dao.getById(newHorse.father().id()) : null;

    validator.validateForCreate(
        newHorse,
        mother != null ? mapper.entityToListDto(mother, ownerMapForSingleId(mother.getOwnerId())) : null,
        father != null ? mapper.entityToListDto(father, ownerMapForSingleId(father.getOwnerId())) : null
    );
    Horse createdHorse = dao.create(newHorse);

    // convert to dto again (with correct parents)
    return mapper.entityToDetailDto(
        createdHorse,
        ownerMapForSingleId(createdHorse.getOwnerId()),
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
    LOG.trace("details({})", id);
    Horse horse = dao.getById(id);

    // get parents from database with id of parents
    // this is used for validation if something is not okay in frontend and values of parents in request and database differ
    var mother = horse.getMotherId() != null ? dao.getById(horse.getMotherId()) : null;
    var father = horse.getFatherId() != null ? dao.getById(horse.getFatherId()) : null;
    return mapper.entityToDetailDto(
        horse,
        ownerMapForSingleId(horse.getOwnerId()),
        mother,
        father);
  }

  private Map<Long, OwnerDto> ownerMapForSingleId(Long ownerId) {
    try {
      return ownerId == null
          ? null
          : Collections.singletonMap(ownerId, ownerService.getById(ownerId));
    } catch (NotFoundException e) {
      throw new FatalException("Owner %d referenced by horse not found".formatted(ownerId));
    }
  }

}
