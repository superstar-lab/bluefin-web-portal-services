package com.mcmcg.ico.bluefin.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
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
		Property property;
		ArrayList<Property> list = (ArrayList<Property>) jdbcTemplate.query(Queries.FINDPROPERTYBYNAME,
				new Object[] { name }, new RowMapperResultSetExtractor<Property>(new PropertyRowMapper()));
		LOGGER.debug("PropertyDAOImpl :: findByName() : Property size : "+list.size());
		property = DataAccessUtils.singleResult(list);

		if (property != null) {
			LOGGER.debug("PropertyDAOImpl :: findByName() : Found property - name/value: " + property.getApplicationPropertyName() + "/"
					+ property.getApplicationPropertyValue());
		} else {
			LOGGER.debug("PropertyDAOImpl :: findByName() : Property not found for - name: " + name);
		}

		return property;
	}
	
	@Override
	public String getPropertyValue(String propertyName) {
		Property property = findByName(propertyName);
		LOGGER.debug("PropertyDAOImpl :: getPropertyValue() : property " + property);
		return property == null ? "" : property.getApplicationPropertyValue();
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
		Timestamp ts;
		if (rs.getString("DateCreated") != null) {
			ts = Timestamp.valueOf(rs.getString("DateCreated"));
			property.setDateCreated(new DateTime(ts));
		}
		if (rs.getString("DateModified") != null) {
			ts = Timestamp.valueOf(rs.getString("DateModified"));
			property.setDateModified(new DateTime(ts));
		}
		
		property.setModifiedBy(rs.getString("ModifiedBy"));

		return property;
	}
}
