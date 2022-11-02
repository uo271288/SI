/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.xmlbif.strings;

import java.io.Serializable;

public enum XMLBIFValues implements Serializable {
		NATURE("nature");
		
		private String name;

		XMLBIFValues(String name) {
			this.name = name;
		}
		
		public String toString() {
			return name;
		}

}
