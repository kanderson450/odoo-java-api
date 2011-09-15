/*
 *   This file is part of OpenERPJavaAPI.
 *
 *   OpenERPJavaAPI is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenERPJavaAPI is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with OpenERPJavaAPI.  If not, see <http://www.gnu.org/licenses/>.
 *
 *   Copyright 2011 De Bortoli Wines Pty Limited (Australia)
 */

package com.debortoliwines.openerp.api;

import java.util.HashMap;

import com.debortoliwines.openerp.api.Field.FieldType;

/***
 * Encapsulates the HashMap row object that is returned by OpenERP
 * @author Pieter van der Merwe
 *
 */
public class Row {

	private static final long serialVersionUID = -4426823347416670117L;
	private final HashMap<String, Object> openERPResult;
	private final FieldCollection fields;

	/**
	 * Default constructor
	 * @param openERPResult The HashMap object returned from readObject. containing all data for this row
	 * @param fields FieldCollection that this row holds data for.
	 * @throws OpeneERPApiException
	 */
	public Row (HashMap<String, Object> openERPResult, FieldCollection fields) throws OpeneERPApiException{
		this.openERPResult = openERPResult;
		this.fields = fields;
		
		// This is a new row, add entries for every field
		if (openERPResult.isEmpty()){
			this.put("id", null);
			for (int i = 0; i < fields.size(); i++)
				this.put("id",null);
		}
	}
	
	/**
	 * Copy constructor to create a new row from a template row
	 */
	@SuppressWarnings("unchecked")
	public Row (Row templateRow) {
		this.openERPResult = (HashMap<String, Object>) templateRow.openERPResult.clone();
		this.fields = (FieldCollection) templateRow.fields.clone();
	}

	/**
	 * Get the field information this row is holding data for.
	 * @return
	 */
	public FieldCollection getFields() {
		return fields;
	}

	/**
	 * Fetch the data for a specific field
	 * @param field Field to fetch data for
	 * @return A data object that can be of any type, or null if the field was not found.
	 */
	public Object get(Field field)
	{
		return this.get(field.getName());
	}
	
	/**
	 * Fetch the data for a specific field
	 * @param fieldName Field name to fetch data for
	 * @return A data object.  It could be of any type, or null if the field was not found.
	 */
	public Object get(String fieldName) {

		// ID is a special case.  It is always returned in a query
		if (fieldName != null && fieldName.equals("id"))
			return openERPResult.get(fieldName);
		
		Field fieldMeta = getField(fieldName);
		if (fieldMeta == null)
			return null;

		Object value = openERPResult.get(fieldName);
		Field.FieldType fieldType = fieldMeta.getType();

		if (fieldType != Field.FieldType.BOOLEAN && value instanceof Boolean)
			return null;

		if (value instanceof Object[] && ((Object []) value).length == 0)
			return null;

		return value;
	}
	
	private Field getField(String fieldName){
		for (Field fld : fields)
			if (fld.getName().equals(fieldName))
				return fld;
		return null;
	}
	
	/**
	 * Updates row field values
	 * @param fieldName Name of the field to update
	 * @param value New value that must be associated with the field
	 * @throws OpeneERPApiException
	 */
	public void put(String fieldName, Object value) throws OpeneERPApiException {
		if (fieldName.equals("id")){
			openERPResult.put(fieldName, value);
			return;
		}
		
		Field fld = getField(fieldName);
		if (fld == null)
			throw new OpeneERPApiException("Field '" + fieldName + "' was not found in row");
		
		Field.FieldType fieldType = fld.getType();
		
		if (fieldType == FieldType.ONE2MANY)
			value = new Object[]{value,null}; 
		
		if (openERPResult.containsKey(fieldName))
			openERPResult.remove(fieldName);
		
		openERPResult.put(fieldName, value);
	}
}