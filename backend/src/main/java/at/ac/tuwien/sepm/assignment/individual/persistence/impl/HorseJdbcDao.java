package at.ac.tuwien.sepm.assignment.individual.persistence.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.lang.invoke.MethodHandles;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Repository
public class HorseJdbcDao implements HorseDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String TABLE_NAME = "horse";
  private static final String SQL_SELECT_ALL = "SELECT * FROM " + TABLE_NAME;
  private static final String SQL_SELECT_BY_ID = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
  private static final String SQL_SELECT_ANCESTORS = "SELECT *  FROM " + TABLE_NAME + " WHERE id IN"
      + " (WITH ancestors (id, name, mother_id, father_id, max_generation_amount)"
      + " AS (SELECT id, name, mother_id, father_id, 1 AS max_generation_amount FROM " + TABLE_NAME + " WHERE id = ?"
      + " UNION ALL"
      + " SELECT horse.id, horse.name, horse.mother_id, horse.father_id, ancestors.max_generation_amount + 1 FROM ancestors"
      + " JOIN horse ON horse.id = ancestors.mother_id OR horse.id = ancestors.father_id WHERE ancestors.max_generation_amount < ?)"
      + " SELECT DISTINCT id FROM ancestors);";
  private static final String SQL_UPDATE = "UPDATE " + TABLE_NAME
      + " SET name = ?"
      + "  , description = ?"
      + "  , date_of_birth = ?"
      + "  , sex = ?"
      + "  , owner_id = ?"
      + "  , mother_id = ?"
      + "  , father_id = ?"
      + " WHERE id = ?";

  private static final String SQL_CREATE =
      "INSERT INTO " + TABLE_NAME + " (name, description, date_of_birth, sex, owner_id, mother_id, father_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
  private static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
  private final JdbcTemplate jdbcTemplate;

  public HorseJdbcDao(
      JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public List<Horse> getAll() {
    LOG.trace("getAll()");
    return jdbcTemplate.query(SQL_SELECT_ALL, this::mapRow);
  }

  @Override
  public List<Horse> searchHorses(HorseSearchDto horseSearchDto) {
    LOG.trace("searchHorses({})", horseSearchDto);
    MapSqlParameterSource namedParameters = new MapSqlParameterSource();

    // base of search String
    // 1=1 is needed to append 'AND' keywords to string
    // the base String without any 'AND' keywords
    // is the same operation as the SELECT statement without the WHERE clause
    String search = "SELECT * FROM " + TABLE_NAME + " WHERE 1=1 ";

    // add specified parts of search to SQL_SEARCH String
    // NamedParameterJdbcTemplate and MapSqlParameterSource is used to add parameters easier
    // --> query build up with '?' would be more confusing
    if (horseSearchDto.name() != null) {
      search += " AND UPPER(name) LIKE :name";
      namedParameters.addValue("name", "%" + horseSearchDto.name().toUpperCase() + "%");
    }

    if (horseSearchDto.description() != null) {
      search += " AND UPPER(description) LIKE :description";
      namedParameters.addValue("description", "%" + horseSearchDto.description().toUpperCase() + "%");
    }

    if (horseSearchDto.bornBefore() != null) {
      search += " AND date_of_birth < :bornBefore";
      namedParameters.addValue("bornBefore", horseSearchDto.bornBefore());
    }

    if (horseSearchDto.sex() != null) {
      search += " AND sex = :sex";
      namedParameters.addValue("sex", horseSearchDto.sex().name());
    }

    if (horseSearchDto.ownerName() != null) {
      search += " AND owner_id IN (SELECT id FROM owner WHERE UPPER(first_name) LIKE :ownerName OR "
          + "UPPER(last_name) LIKE :ownerName)";
      namedParameters.addValue("ownerName", "%" + horseSearchDto.ownerName().toUpperCase() + "%");
    }

    NamedParameterJdbcTemplate searchTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    return searchTemplate.query(
        search,
        namedParameters,
        this::mapRow
    );
  }

  @Override
  public Horse getById(long id) throws NotFoundException {
    LOG.trace("getById({})", id);
    List<Horse> horses;
    horses = jdbcTemplate.query(SQL_SELECT_BY_ID, this::mapRow, id);

    if (horses.isEmpty()) {
      throw new NotFoundException("No horse with ID %d found".formatted(id));
    }
    if (horses.size() > 1) {
      // This should never happen!!
      throw new FatalException("Too many horses with ID %d found".formatted(id));
    }

    return horses.get(0);
  }


  @Override
  public Horse update(HorseDetailDto horse) throws NotFoundException {
    LOG.trace("update({})", horse);
    int updated = jdbcTemplate.update(SQL_UPDATE,
        horse.name(),
        horse.description(),
        horse.dateOfBirth(),
        horse.sex().toString(),
        horse.ownerId(),
        horse.motherId(),
        horse.fatherId(),
        horse.id());

    if (updated == 0) {
      throw new NotFoundException("Could not update horse with ID " + horse.id() + ", because it does not exist");
    }

    return new Horse()
        .setId(horse.id())
        .setName(horse.name())
        .setDescription(horse.description())
        .setDateOfBirth(horse.dateOfBirth())
        .setSex(horse.sex())
        .setOwnerId(horse.ownerId())
        .setMotherId(horse.motherId())
        .setFatherId(horse.fatherId())
        ;
  }

  @Override
  public Horse create(HorseCreateDto newHorse) {
    LOG.trace("create({})", newHorse);

    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(con -> {
      PreparedStatement stmt = con.prepareStatement(SQL_CREATE, Statement.RETURN_GENERATED_KEYS);
      stmt.setString(1, newHorse.name());
      stmt.setString(2, newHorse.description());
      stmt.setDate(3, Date.valueOf(newHorse.dateOfBirth()));
      stmt.setString(4, String.valueOf(newHorse.sex()));
      stmt.setObject(5, newHorse.ownerId());
      stmt.setObject(6, newHorse.motherId());
      stmt.setObject(7, newHorse.fatherId());
      return stmt;
    }, keyHolder);

    Number key = keyHolder.getKey();
    if (key == null) {
      // This should never happen. If it does, something is wrong with the DB or the way the prepared statement is set up.
      throw new FatalException("Could not extract key for newly created horse. There is probably a programming error…");
    }

    return new Horse()
        .setId(key.longValue())
        .setName(newHorse.name())
        .setDescription(newHorse.description())
        .setDateOfBirth(newHorse.dateOfBirth())
        .setSex(newHorse.sex())
        .setOwnerId(newHorse.ownerId())
        .setMotherId(newHorse.motherId())
        .setFatherId(newHorse.fatherId())
        ;
  }

  @Override
  public void delete(Long id) throws NotFoundException {
    LOG.trace("delete({})", id);

    int deleted = jdbcTemplate.update(SQL_DELETE, id);
    if (deleted == 0) {
      throw new NotFoundException("Could not delete horse with ID " + id + ", because it does not exist");
    }
  }

  @Override
  public List<Horse> searchAncestorHorses(long id, long maxGenerations) throws NotFoundException {
    LOG.trace("searchAncestorHorses({}, {})", id, maxGenerations);
    List<Horse> ancestors;
    ancestors = jdbcTemplate.query(SQL_SELECT_ANCESTORS, this::mapRow, id, maxGenerations);

    if (ancestors.isEmpty()) {
      throw new NotFoundException("No horse ancestors with ID %d found".formatted(id));
    }

    return ancestors;
  }

  private Horse mapRow(ResultSet result, int rownum) throws SQLException {
    return new Horse()
        .setId(result.getLong("id"))
        .setName(result.getString("name"))
        .setDescription(result.getString("description"))
        .setDateOfBirth(result.getDate("date_of_birth").toLocalDate())
        .setSex(Sex.valueOf(result.getString("sex")))
        .setOwnerId(result.getObject("owner_id", Long.class))
        .setMotherId(result.getObject("mother_id", Long.class))
        .setFatherId(result.getObject("father_id", Long.class))
        ;
  }
}
