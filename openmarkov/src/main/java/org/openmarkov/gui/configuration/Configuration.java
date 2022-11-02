/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.configuration;

public interface Configuration extends DefaultConfiguration {

	/**
	 * @return Component name. <code>String</code>
	 */
	String getComponentName();

	/**
	 * @param name <code>String</code>. Property name.
	 * @return An <code>Object</code> whose name = <code>name</code>.
	 */
	Object getProperty(String name);

	/**
	 * Creates or modifies a property.
	 *
	 * @param name  <code>String</code>. Property name.
	 * @param value <code>Object</code>.
	 */
	void setProperty(String name, Object value);

}
