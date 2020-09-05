/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ghost.framework.data.configuration.jdbc.support;


import ghost.framework.data.configuration.dao.DataAccessException;
import ghost.framework.util.StringUtils;

/**
 * JavaBean for holding custom JDBC error codes translation for a particular
 * database. The "exceptionClass" property defines which exception will be
 * thrown for the list of error codes specified in the errorCodes property.
 *
 * @author Thomas Risberg
 * @since 1.1
 * @see SQLErrorCodeSQLExceptionTranslator
 */
public class CustomSQLErrorCodesTranslation {

	private String[] errorCodes = new String[0];

	
	private Class<?> exceptionClass;


	/**
	 * Set the SQL error codes to match.
	 */
	public void setErrorCodes(String... errorCodes) {
		this.errorCodes = StringUtils.sortStringArray(errorCodes);
	}

	/**
	 * Return the SQL error codes to match.
	 */
	public String[] getErrorCodes() {
		return this.errorCodes;
	}

	/**
	 * Set the exception class for the specified error codes.
	 */
	public void setExceptionClass( Class<?> exceptionClass) {
		if (exceptionClass != null && !DataAccessException.class.isAssignableFrom(exceptionClass)) {
			throw new IllegalArgumentException("Invalid exception class [" + exceptionClass +
					"]: needs to be a subclass of [ghost.framework.dao.DataAccessException]");
		}
		this.exceptionClass = exceptionClass;
	}

	/**
	 * Return the exception class for the specified error codes.
	 */
	
	public Class<?> getExceptionClass() {
		return this.exceptionClass;
	}

}
