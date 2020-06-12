package com.mcmcg.ico.bluefin.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.ApplicationProperty;
import com.mcmcg.ico.bluefin.model.Property;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class PropertyDAOImpl implements PropertyDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(PropertyDAOImpl.class);

	@Qualifier(BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_JDBC_TEMPLATE)
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public Property findByName(String name) {
		Property property;
		ArrayList<Property> list = (ArrayList<Property>) jdbcTemplate.query(Queries.FINDPROPERTYBYNAME,
				new Object[] { name }, new RowMapperResultSetExtractor<Property>(new PropertyRowMapper()));
		LOGGER.debug("Property size = {}",list.size());
		property = DataAccessUtils.singleResult(list);

		if (property != null) {
			LOGGER.debug("Found property - name={}/value={}", property.getApplicationPropertyName(), 
					property.getApplicationPropertyValue());
		} else {
			LOGGER.debug("Property not found for name ={} ", name);
		}

		return property;
	}
	
	@Override
	public String getPropertyValue(String propertyName) {
		Property property = findByName(propertyName);
		LOGGER.debug("property1 ={}", property);
		return property == null ? "" : property.getApplicationPropertyValue();
	}

	@Override
	public List<ApplicationProperty> findAll() {
		ArrayList<ApplicationProperty> propertylist = (ArrayList<ApplicationProperty>) jdbcTemplate.query(Queries.FINDALLPROPERTY, new RowMapperResultSetExtractor<ApplicationProperty>(new ApplicationPropertyRowMapper()));
		LOGGER.debug("Property size = {}",propertylist.size());
		if (propertylist.isEmpty()) {
			LOGGER.debug("Property not found");
		} else {
			LOGGER.debug("Found property - name={}");
		}

		return propertylist;
	}

	@Override
	public List<ApplicationProperty> getAllProperty() {
		List<ApplicationProperty> propertylist = findAll();
		LOGGER.debug("property2 ={}", propertylist.size());
		return propertylist;
	}

	@Override
	public ApplicationProperty update(ApplicationProperty applicationProperty, String modifiedBy) {
		/**java.util.Date dt = new java.util.Date();
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
		String currentTime = sdf.format(dt);*/
		DateTime utc1 = applicationProperty.getDateModified().withZone(DateTimeZone.UTC);
		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc1));

		long noOfRecordsUpdated = jdbcTemplate.update(Queries.UPDATEAPPLICATIONPROPERTY,
				applicationProperty.getPropertyName(),applicationProperty.getPropertyValue(),
				applicationProperty.getApplicationDescription(),modifiedBy,dateModified,applicationProperty.getPropertyId());
		LOGGER.info("Number of updated ApplicationProperty = {}", noOfRecordsUpdated);

		if (noOfRecordsUpdated>0) {
			LOGGER.debug("Property updated successfully");
			return applicationProperty;
		} else {
			LOGGER.debug("Property not updated");
		}
		
		return null;
	}

	@Override
	public ApplicationProperty updateProperty(ApplicationProperty applicationProperty, String modifiedBy) {
		ApplicationProperty propertyUpdate = update(applicationProperty, modifiedBy);
		LOGGER.debug("property ={}", propertyUpdate==null ? null : propertyUpdate.getPropertyName());
		return applicationProperty;
	}

	@Override
	public ApplicationProperty save(ApplicationProperty applicationProperty) {
		KeyHolder holder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection->{
			PreparedStatement ps = connection.prepareStatement(Queries.INSERTAPPLICATIONPROPERTY,
					Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, applicationProperty.getPropertyName()); 
			ps.setString(2, applicationProperty.getPropertyValue()); 
			ps.setString(3, BluefinWebPortalConstants.VARCHAR); 
			ps.setString(4, applicationProperty.getApplicationDescription()); 
			ps.setString(5, "Name of the person in charge of applying the changes in the db");
			return ps;
		}, holder);
		long noOfRecordsInserted = holder.getKey().longValue();
		applicationProperty.setPropertyId(noOfRecordsInserted);
		LOGGER.debug("Saved UserPreference - id ={} ", noOfRecordsInserted);
		
		if(noOfRecordsInserted>0){
			LOGGER.debug("Property inserted successfully");
			return applicationProperty;
		}
		else {
			LOGGER.debug("Property not inserted");
		}
		return null;
	}

	@Override
	public ApplicationProperty saveApplicationProperty(ApplicationProperty applicationProperty) {
		ApplicationProperty propertyUpdate = save(applicationProperty);
		LOGGER.debug("save property ={}", propertyUpdate==null ? null : propertyUpdate.getPropertyName());
		return applicationProperty;
	}

	@Override
	public String delete(String applicationPropertyId) {
		long noOfRecordsDeleted = jdbcTemplate.update(Queries.DELETEAPPLICATIONPROPERTY, applicationPropertyId);
		LOGGER.info("Number of deleted ApplicationProperty = {}", noOfRecordsDeleted);

		if (noOfRecordsDeleted>0) {
			LOGGER.debug("Property updated successfully");
			return "{ \"status\" : \"success\" }";
		} else {
			LOGGER.debug("Property not updated");
		}
		
		return "{ \"status\" : \"failure\" }";
	}

	@Override
	public String deleteApplicationProperty(String applicationPropertyId) {
		String deleteStatus = delete(applicationPropertyId);
		LOGGER.debug("delete property ={}",deleteStatus);
		return deleteStatus;
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

class ApplicationPropertyRowMapper implements RowMapper<ApplicationProperty> {

	@Override
	public ApplicationProperty mapRow(ResultSet rs, int row) throws SQLException {
		ApplicationProperty property = new ApplicationProperty();
		property.setPropertyId(rs.getLong("ApplicationpropertyID"));
		property.setPropertyName(rs.getString("ApplicationPropertyName"));
		property.setPropertyValue(rs.getString("ApplicationPropertyValue"));
		property.setApplicationDataType(rs.getString("DataType"));
		property.setApplicationDescription(rs.getString("Description"));
		property.setModifiedByUser(rs.getString("ModifiedBy"));

		return property;
	}
}
