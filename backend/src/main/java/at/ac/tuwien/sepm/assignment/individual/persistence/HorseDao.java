package at.ac.tuwien.sepm.assignment.individual.persistence;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object for horses.
 * Implements access functionality to the application's persistent data store regarding horses.
 */
public interface HorseDao {

  /**
   * Get all horses stored in the persistent data store.
   *
   * @return a list of all stored horses
   */
  List<Horse> getAll();

  /**
   * Get all horses which match with the specified HorseSearchDto stored in the persistent data store.
   *
   * @return a list of all matching stored horses
   */
  List<Horse> searchHorses(HorseSearchDto horseSearchDto);

  /**
   * Update the horse with the ID given in {@code horse}
   * with the data given in {@code horse}
   * in the persistent data store.
   *
   * @param horse the horse to update
   * @return the updated horse
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  Horse update(HorseDetailDto horse) throws NotFoundException;

  /**
   * Get a horse by its ID from the persistent data store.
   *
   * @param id the ID of the horse to get
   * @return the horse
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  Horse getById(long id) throws NotFoundException;

  /**
   * Create a new horse in the persistent data store.
   *
   * @param newHorse the data to create the new horse from
   * @return the newly created horse
   */
  Horse create(HorseCreateDto newHorse);

  /**
   * Delete a horse by its ID from the persistent data store.
   *
   * @param id the ID of the horse to delete
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  void delete(Long id) throws NotFoundException;

  /**
   * Get all ancestors of specified horse stored in the persistent data store until the specified generation.
   *
   * @param id             the ID of the horse to get the ancestors of
   * @param maxGenerations the maximum amount of generations which should get searched
   * @return a list of all matching stored ancestor-horses
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  List<Horse> searchAncestorHorses(long id, long maxGenerations) throws NotFoundException;

  /**
   * Checks if horse is a parent of children
   *
   * @param id the ID of the horse which gets checked
   * @return the boolean which indicates if the horse is a parent horse; true if it is parent horse and false if it is not a parent horse
   */
  boolean isParentOfChildren(long id);

  /**
   * Checks if horse is older than its children
   *
   * @param id          the ID of the horse which gets checked
   * @param dateOfBirth the date of the horse which gets checked
   * @return the boolean which indicates if the horse is older than all of its children; true if parent is older and false if parent is younger or equal old
   */
  boolean isOlderThanChildren(long id, LocalDate dateOfBirth);
}
