package com.mcmcg.ico.bluefin.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.model.UserRole;
import com.mcmcg.ico.bluefin.repository.sql.Queries;
import com.mcmcg.ico.bluefin.service.util.QueryBuilderHelper;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysql.jdbc.StringUtils;

@Repository
public class UserDAOImpl implements UserDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserDAOImpl.class);

	DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
	
	@Autowired
	private UserRoleDAO userRoleDAO;
	
	@Autowired
	private LegalEntityAppDAO legalEntityAppDAO;

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private NamedParameterJdbcTemplate namedJDBCTemplate;

	@Override
	public List<User> findAll() {
		List<User> list = jdbcTemplate.query(Queries.findAllUsers, new UserRowMapper());

		LOGGER.debug("UserDAOImpl :: findAll() : Number of rows: " + list.size());

		return list;
	}
	
	public int countUserRecords(String query,Map<String,String> filterMap){
		Integer count = namedJDBCTemplate.queryForObject(query, filterMap,Integer.class);
		LOGGER.debug("UserDAOImpl :: countUserRecords() : counts are : " + count);
		return count;
	}
	
	@Override
	public Page<User> findAllWithDynamicFilter(List<String> search, PageRequest pageRequest,Map<String,String> filterMap ) {
		StringBuilder queryBuffer = QueryBuilderHelper.buildQuery(filterMap,pageRequest.getSort());
		
		int pageNumber = pageRequest.getPageNumber();
		int pageSize = pageRequest.getPageSize();
		int offset=pageNumber*pageSize;
		String query =  queryBuffer.toString();
		String queryForTotalCount = queryBuffer.replace( queryBuffer.indexOf("SELECT",0)+7,queryBuffer.indexOf("FROM",160)-1, "count(ul.UserID)").toString();
		LOGGER.debug("UserDAOImpl :: findAllWithDynamicFilter() : Query for result:"+query);
		LOGGER.debug("UserDAOImpl :: findAllWithDynamicFilter() : Query for count:"+queryForTotalCount);
		query  =  QueryBuilderHelper.appendLimit(query, offset, pageSize);
		List<User> searchResultlist = namedJDBCTemplate.query(query, filterMap, new UserRowMapper());
		
		LOGGER.debug("UserDAOImpl :: findAllWithDynamicFilter() : Number of rows: " + searchResultlist.size());

		int countResult = namedJDBCTemplate.queryForObject(queryForTotalCount,filterMap, Integer.class);
		LOGGER.debug("UserDAOImpl :: findAllWithDynamicFilter() : Search result count:"+countResult);
		
		List<User> onePage = searchResultlist;
		Page<User> pageList = new PageImpl<>(onePage, pageRequest, countResult);

		return pageList;
	}
	
	@Override
	public User findByUserId(long userId) {
		ArrayList<User> list = (ArrayList<User>) jdbcTemplate.query(Queries.findUserByUserId, new Object[] { userId },
				new RowMapperResultSetExtractor<User>(new UserRowMapper()));
		LOGGER.debug("UserDAOImpl :: findByUserId() : Number of User: " + list.size());
		User user = DataAccessUtils.singleResult(list);

		if (user != null) {
			LOGGER.debug("UserDAOImpl :: findByUserId() : Found User for userId: " + userId);
		} else {
			LOGGER.debug("UserDAOImpl :: findByUserId() : User not found for userId: " + userId);
		}

		return user;
	}

	@Override
	public User findByUsername(String username) {
		ArrayList<User> list = (ArrayList<User>) jdbcTemplate.query(Queries.findUserByUsername,
				new Object[] { username }, new RowMapperResultSetExtractor<User>(new UserRowMapper()));
		LOGGER.debug("UserDAOImpl :: findByUsername() : Number of User: " + list.size());
		User user = DataAccessUtils.singleResult(list);

		if (user != null) {
			LOGGER.debug("UserDAOImpl :: findByUsername() : Found User for username: " + username);
		} else {
			LOGGER.debug("UserDAOImpl :: findByUsername() : User not found for username: " + username);
		}

		return user;
	}

	@Override
	public long saveUser(User user) {
		KeyHolder holder = new GeneratedKeyHolder();

		// The Java class uses Joda DateTime, which isn't supported by
		// PreparedStatement.
		// Convert Joda DateTime to UTC (the format for the database).
		// Remove the 'T' and 'Z' from the format, because it's not in the
		// database.
		// Convert this string to Timestamp, which is supported by
		// PreparedStatement.
		DateTime utc1 = user.getLastLogin().withZone(DateTimeZone.UTC);
		DateTime utc2 = user.getDateCreated().withZone(DateTimeZone.UTC);
		DateTime utc3 = user.getDateUpdated().withZone(DateTimeZone.UTC);
		DateTime utc4 = user.getDateModified().withZone(DateTimeZone.UTC);
		Timestamp lastLogin = Timestamp.valueOf(dtf.print(utc1));
		Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc2));
		Timestamp dateUpdated = Timestamp.valueOf(dtf.print(utc3));
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc4));

		jdbcTemplate.update(connection->{
				PreparedStatement ps = connection.prepareStatement(Queries.saveUser, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, user.getUsername()); // UserName
				ps.setString(2, user.getFirstName()); // FirstName
				ps.setString(3, user.getLastName()); // LastName
				ps.setShort(4, user.getIsActive()); // IsActive
				ps.setTimestamp(5, lastLogin); // LastLogin
				ps.setTimestamp(6, dateCreated); // DateCreated
				ps.setTimestamp(7, dateUpdated); // DateUpdated
				ps.setString(8, user.getEmail()); // Email
				ps.setString(9, user.getPassword()); // UserPassword
				ps.setTimestamp(10, dateModified); // DateModified
				ps.setString(11, user.getModifiedBy()); // ModifiedBy
				ps.setString(12, user.getStatus()); // Status
				return ps;
		}, holder);

		Long id = holder.getKey().longValue();
		user.setUserId(id);
		LOGGER.debug("UserDAOImpl :: saveUser() :  Saved user - id: " + id);
		createUserRoles(user);
		createLegalEntityApp(user);
		return user.getUserId();
	}

	/**
	 * @param user
	 */
	private void createLegalEntityApp(User user) {
		if (user.getLegalEntities() != null && !user.getLegalEntities().isEmpty()) {
			LOGGER.debug("UserDAOImpl :: createLegalEntityApp() : Number of childs LegalEntityApp items associated with User {}"+user.getLegalEntities().size());
			// in this case we need to create child items also.
			legalEntityAppDAO.createLegalEntityApps(user.getLegalEntities());
		}
	}

	/**
	 * @param user
	 */
	private void createUserRoles(User user) {
		if (user.getRoles() != null && !user.getRoles().isEmpty()) {
			LOGGER.debug("UserDAOImpl :: createUserRoles() : Number of childs Role items associated with User {}"+user.getRoles().size());
			// in this case we need to create child items also.
			for (UserRole userRole : user.getRoles()) {
				userRole.setUserId(user.getUserId());
			}
			userRoleDAO.saveRoles(user.getRoles());
		}
	}

	@Override
	public int updateUser(User user, String modifiedBy) {
		// The Java class uses Joda DateTime, which isn't supported by
		// PreparedStatement.
		// Convert Joda DateTime to UTC (the format for the database).
		// Remove the 'T' and 'Z' from the format, because it's not in the
		// database.
		// Convert this string to Timestamp, which is supported by
		// PreparedStatement.
		DateTime utc1 = user.getLastLogin().withZone(DateTimeZone.UTC);
		DateTime utc2 = user.getDateCreated().withZone(DateTimeZone.UTC);
		DateTime utc3 = new DateTime(DateTimeZone.UTC);
		DateTime utc4 = new DateTime(DateTimeZone.UTC);
		Timestamp lastLogin = Timestamp.valueOf(dtf.print(utc1));
		Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc2));
		Timestamp dateUpdated = Timestamp.valueOf(dtf.print(utc3));
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc4));

		int rows = jdbcTemplate.update(Queries.updateUser,
				new Object[] { user.getUsername(), user.getFirstName(), user.getLastName(), user.getIsActive(),
						lastLogin, dateCreated, dateUpdated, user.getEmail(), user.getPassword(), dateModified,
						modifiedBy, user.getStatus(), user.getUserId() });

		LOGGER.debug("UserDAOImpl :: updateUser() : Updated user with ID: " + user.getUserId() + ", rows affected = " + rows);
		
		createUserRoles(user);
		createLegalEntityApp(user);
		return rows;
	}
	
	@Override
	public Page<User> findAll(BooleanExpression expression, PageRequest pageRequest) {
		List<User> list = jdbcTemplate.query(Queries.findAllUsers, new UserRowMapper());

		LOGGER.debug("UserDAOImpl :: findAll(pageRequest) :  Number of rows: " + list.size());

		int countResult = list.size();
		int pageNumber = pageRequest.getPageNumber();
		int pageSize = pageRequest.getPageSize();

		List<User> onePage = new ArrayList<>();
		int index = pageSize * pageNumber;
		int increment = pageSize;
		// Check upper bound to avoid IndexOutOfBoundsException
		if ((index + increment) > countResult) {
			int adjustment = (index + increment) - countResult;
			increment -= adjustment;
		}
		for (int i = index; i < (index + increment); i++) {
			onePage.add(list.get(i));
		}

		Page<User> pageList = new PageImpl<>(onePage, pageRequest, countResult);

		return pageList;
	}
}

class UserRowMapper implements RowMapper<User> {

	@Override
	public User mapRow(ResultSet rs, int row) throws SQLException {
		User user = new User();
		user.setUserId(rs.getLong("UserID"));
		user.setUsername(rs.getString("UserName"));
		user.setFirstName(rs.getString("FirstName"));
		user.setLastName(rs.getString("LastName"));
		user.setIsActive(rs.getShort("IsActive"));
		Timestamp ts;
		if (rs.getString("LastLogin") != null) {
			ts = Timestamp.valueOf(rs.getString("LastLogin"));
			user.setLastLogin(new DateTime(ts));
		}
		
		if (rs.getString("DateCreated") != null) {

			ts = Timestamp.valueOf(rs.getString("DateCreated"));
			user.setDateCreated(new DateTime(ts));
		}
		if (rs.getString("DateUpdated") != null) {

			ts = Timestamp.valueOf(rs.getString("DateUpdated"));
			user.setDateUpdated(new DateTime(ts));
		}
		user.setEmail(rs.getString("Email"));
		user.setPassword(rs.getString("UserPassword"));
		if (rs.getString("DateModified") != null) {

			ts = Timestamp.valueOf(rs.getString("DateModified"));
			user.setDateModified(new DateTime(ts));
		}
		user.setEmail(rs.getString("Email"));
		user.setPassword(rs.getString("UserPassword"));
		user.setModifiedBy(rs.getString("ModifiedBy"));
		user.setStatus(rs.getString("Status"));

		return user;
	}
	
	
}
