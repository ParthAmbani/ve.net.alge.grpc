/*************************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                              *
 * Copyright (C) 2012-2018 E.R.P. Consultores y Asociados, C.A.                      *
 * Contributor(s): Yamel Senih ysenih@erpya.com                                      *
 * This program is free software: you can redistribute it and/or modify              *
 * it under the terms of the GNU General Public License as published by              *
 * the Free Software Foundation, either version 3 of the License, or                 *
 * (at your option) any later version.                                               *
 * This program is distributed in the hope that it will be useful,                   *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of                    *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                     *
 * GNU General Public License for more details.                                      *
 * You should have received a copy of the GNU General Public License                 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.            *
 ************************************************************************************/
package ve.net.alge.grpc.pos.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 	Printing Result from implementation, use this stub class for fill all result values as map, also you can return values like:
 * <li> Error flag
 * <li> Summary Data
 * <li> Report File (Automatically read if is a pdf)
 * <li> Map with data
 * 	@author Yamel Senih, ysenih@erpya.com, ERPCyA http://www.erpya.com
 */
public class TicketResult {
	
	/**
	 * Default instance or static constructor
	 * @return
	 */
	public static TicketResult newInstance() {
		return new TicketResult();
	}
	
	private TicketResult() {
		isError = false;
		summary = null;
		resultValues = new HashMap<>();
		reportFile = null;
	}
	
	private boolean isError;
	private String summary;
	private Map<String, Object> resultValues;
	private File reportFile;
	public boolean isError() {
		return isError;
	}

	public TicketResult withError(boolean isError) {
		this.isError = isError;
		return this;
	}

	public String getSummary() {
		return summary;
	}

	public TicketResult withSummary(String summary) {
		this.summary = summary;
		return this;
	}

	public Map<String, Object> getResultValues() {
		return resultValues;
	}

	public TicketResult withResultValues(Map<String, Object> resultValues) {
		this.resultValues = resultValues;
		return this;
	}

	public File getReportFile() {
		return reportFile;
	}

	public TicketResult withReportFile(File reportFile) {
		this.reportFile = reportFile;
		return this;
	}
}