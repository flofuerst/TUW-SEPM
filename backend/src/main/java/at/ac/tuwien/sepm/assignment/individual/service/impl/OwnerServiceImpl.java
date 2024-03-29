package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Owner;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepm.assignment.individual.mapper.OwnerMapper;
import at.ac.tuwien.sepm.assignment.individual.persistence.OwnerDao;
import at.ac.tuwien.sepm.assignment.individual.service.OwnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OwnerServiceImpl implements OwnerService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final OwnerDao dao;
  private final OwnerMapper mapper;
  private final OwnerValidator validator;

  public OwnerServiceImpl(
      OwnerDao dao,
      OwnerMapper mapper,
      OwnerValidator validator) {
    this.dao = dao;
    this.mapper = mapper;
    this.validator = validator;
  }

  @Override
  public OwnerDto getById(long id) throws NotFoundException {
    LOG.trace("getById({})", id);
    return mapper.entityToDto(dao.getById(id));
  }

  @Override
  public Map<Long, OwnerDto> getAllById(Collection<Long> ids) throws NotFoundException {
    LOG.trace("getAllById({})", ids);
    Map<Long, OwnerDto> owners =
        dao.getAllById(ids).stream()
            .map(mapper::entityToDto)
            .collect(Collectors.toUnmodifiableMap(OwnerDto::id, Function.identity()));
    for (final var id : ids) {
      if (!owners.containsKey(id)) {
        throw new NotFoundException("Owner with ID %d not found".formatted(id));
      }
    }
    return owners;
  }

  @Override
  public Stream<OwnerDto> search(OwnerSearchDto searchParameters) {
    LOG.trace("search({})", searchParameters);
    return dao.search(searchParameters).stream()
        .map(mapper::entityToDto);
  }

  @Override
  public OwnerDto create(OwnerCreateDto newOwner) throws NotFoundException, ValidationException, ConflictException {
    LOG.trace("create({})", newOwner);

    validator.validateForCreate(
        newOwner,
        dao.emailExists(newOwner.email())
    );
    Owner createdOwner = dao.create(newOwner);

    // convert to dto again
    return mapper.entityToDto(
        createdOwner
    );
  }
}
