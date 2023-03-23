package at.ac.tuwien.sepm.assignment.individual.service;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;

import java.util.stream.Stream;

/**
 * Service for working with horses.
 */
public interface HorseService {

  /**
   * Lists all horses stored in the system.
   *
   * @return list of all stored horses
   */
  Stream<HorseListDto> allHorses();

  /**
   * Lists all horses which match with the specified HorseSearchDto stored in the system.
   *
   * @return a list of all matching stored horses
   */
  Stream<HorseListDto> getSpecifiedHorses(HorseSearchDto horseSearchDto);


  /**
   * Updates the horse with the ID given in {@code horse}
   * with the data given in {@code horse}
   * in the persistent data store.
   *
   * @param horse the horse to update
   * @return the updated horse
   * @throws NotFoundException   if the horse with given ID does not exist in the persistent data store
   * @throws ValidationException if the update data given for the horse is in itself incorrect (description too long, no name, …)
   * @throws ConflictException   if the update data given for the horse is in conflict with the data currently in the system (owner does not exist, …)
   */
  HorseDetailDto update(HorseDetailDto horse) throws NotFoundException, ValidationException, ConflictException;


  /**
   * Get the horse with given ID, with more detail information.
   * This includes the owner of the horse, and its parents.
   * The parents of the parents are not included.
   *
   * @param id the ID of the horse to get
   * @return the horse with ID {@code id}
   * @throws NotFoundException if the horse with the given ID does not exist in the persistent data store
   */
  HorseDetailDto getById(long id) throws NotFoundException;

  /**
   * Create a new horse in the persistent data store.
   *
   * @param newHorse the data for the new horse
   * @return the horse, that was just newly created in the persistent data store
   * @throws NotFoundException   if the horse with given ID does not exist in the persistent data store
   * @throws ValidationException if the data given for the horse creation is in itself incorrect (description too long, no name, …)
   * @throws ConflictException   if the data given for the horse creation is in conflict with the data currently in the system (owner does not exist, …)
   */
  HorseDetailDto create(HorseCreateDto newHorse) throws NotFoundException, ValidationException, ConflictException;

  /**
   * Delete an existing horse from the persistent data store
   *
   * @param id the id of the horse which gets deleted
   * @throws NotFoundException if the horse with the given ID does not exist in the persistent data store
   */
  void delete(Long id) throws NotFoundException;
}
