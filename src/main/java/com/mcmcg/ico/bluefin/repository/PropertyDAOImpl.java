package com.mcmcg.ico.bluefin.repository;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.model.Property;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class PropertyDAOImpl implements PropertyDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(PropertyDAOImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public Property findByName(String name) {
		Property property = jdbcTemplate.queryForObject(Queries.findPropertyByName, new Object[] { name },
				new PropertyRowMapper());

		LOGGER.debug("Find property - name/value: " + property.getApplicationPropertyName() + "/"
				+ property.getApplicationPropertyValue());

		return property;
	}
}

class PropertyRowMapper implements RowMapper<Property> {

	@Override
	public Property mapRow(ResultSet rs, int row) throws SQLException {
		Property property = new Property();
		property.setApplicationPropertyId(rs.getLong("ApplicationpropertyID"));
		property.setApplicationPropertyName(rs.getString("ApplicationPropertyName"));
		property.setApplicationPropertyValue(rs.getString("ApplicationPropertyValue"));
		property.setDataType(rs.getString("DataType"));
		property.setDescription(rs.getString("Description"));
		property.setDateCreated(new DateTime(rs.getTimestamp("DateCreated")));
		property.setDateModified(new DateTime(rs.getTimestamp("DateModified")));
		property.setModifiedBy(rs.getString("ModifiedBy"));

		return property;
	}
}
