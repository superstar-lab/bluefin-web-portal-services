package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = { "rolePermissions" })
@ToString(exclude = { "rolePermissions" })
@Entity
@Table(name = "Permission_Lookup")
public class Permission implements Serializable {
	private static final long serialVersionUID = 730005903939744601L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PermissionID")
	private long permissionId;

	@Column(name = "PermissionName")
	private String permissionName;

	@Column(name = "Description")
	private String description;

	@JsonIgnore
	@OneToMany(mappedBy = "permission")
	private Collection<RolePermission> rolePermissions;

	@JsonIgnore
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	@Column(name = "DateCreated", insertable = false, updatable = false)
	private DateTime dateCreated;

	public Permission() {
	}

	public Permission(String permissionName) {
		this.permissionName = permissionName;
	}
}
