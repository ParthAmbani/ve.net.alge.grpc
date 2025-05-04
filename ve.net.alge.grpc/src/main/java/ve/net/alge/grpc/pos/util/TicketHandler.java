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

import java.lang.reflect.Constructor;

import org.compiere.model.MPOS;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Util;

/**
 * 	A util class for manage print ticket from Point Of Sales, you can load 
 * 	@author Yamel Senih, ysenih@erpya.com, ERPCyA http://www.erpya.com
 */
public class TicketHandler {
	
	/**	Log					*/
	private static CLogger log = CLogger.getCLogger (TicketHandler.class);
	private static TicketHandler instance;
	private int posId;
	private int recordId;
	private String tableName;
	private String transactionName;
	
	private TicketHandler() {
		
	}
	
	/**
	 * Singleton instance
	 * @return
	 */
	public static TicketHandler getInstance() {
		if(instance == null) {
			instance = new TicketHandler();
		} else {
			
		}
		return instance.clearValues();
	}
	
	private TicketHandler clearValues() {
		posId = 0;
		recordId = 0;
		tableName = null;
		return this;
	}
	
	public int getPosId() {
		return posId;
	}

	public TicketHandler withPosId(int posId) {
		this.posId = posId;
		return this;
	}

	public int getRecordId() {
		return recordId;
	}

	public TicketHandler withRecordId(int recordId) {
		this.recordId = recordId;
		return this;
	}

	public String getTableName() {
		return tableName;
	}

	public TicketHandler withTableName(String tableName) {
		this.tableName = tableName;
		return this;
	}
	
	public String getTransactionName() {
		return transactionName;
	}

	public TicketHandler withTransactionName(String transactionName) {
		this.transactionName = transactionName;
		return this;
	}

	/**
	 * Get Instance for ticket
	 * @param pos
	 * @return
	 */
	private IPrintTicket getTicketHandler() {
		if(posId <= 0) {
			log.severe("Not have POS");
			return null;
		}
		if(recordId <= 0) {
			log.severe("Not have Record ID");
			return null;
		}
		if(Util.isEmpty(tableName)) {
			log.severe("Not have Table");
			return null;
		}
		MPOS pos = new MPOS(Env.getCtx(), posId, null);
		//	Get class from parent
		String className = null; //pos.getTicketClassName();// TODO core
		if(className == null
				|| className.trim().length() == 0) {
			log.fine("Get from GenericTicketHandlerClass");
			IPrintTicket ticketHandler = new GenericPrintTicket();
			ticketHandler.setHandler(this);
			return ticketHandler;
		}
		//	Handler
		IPrintTicket ticketHandler = null;
		//	Reload
		try {
			Class<?> clazz = Class.forName(className);
			//	Make sure that it is a PO class
			Class<?> superClazz = clazz.getSuperclass();
			//	Validate super class
			while (superClazz != null) {
				if (superClazz == IPrintTicket.class) {
					break;
				}
				//	Get Supert Class
				superClazz = superClazz.getSuperclass();
			}
			//	When exists
			Constructor<?> constructor = null;
			constructor = clazz.getDeclaredConstructor();
			ticketHandler = (IPrintTicket) constructor.newInstance();
			ticketHandler.setHandler(this);
		} catch (Exception e) {
			log.severe(e.getMessage());
		}
		//	Default Return
		return ticketHandler;
	}
	
	/**
	 * Print Ticket
	 * @return
	 */
	public TicketResult printTicket() {
		IPrintTicket ticketImplementation = getTicketHandler();
		if(ticketImplementation != null) {
			return ticketImplementation.printTicket();
		}
		return null;
	}
}