/*************************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                              *
 * This program is free software; you can redistribute it and/or modify it    		 *
 * under the terms version 2 or later of the GNU General Public License as published *
 * by the Free Software Foundation. This program is distributed in the hope   		 *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 		 *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           		 *
 * See the GNU General Public License for more details.                       		 *
 * You should have received a copy of the GNU General Public License along    		 *
 * with this program; if not, write to the Free Software Foundation, Inc.,    		 *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     		 *
 * For the text or an alternative of this public license, you may reach us    		 *
 * Copyright (C) 2012-2018 E.R.P. Consultores y Asociados, S.A. All Rights Reserved. *
 * Contributor(s): Yamel Senih www.erpya.com				  		                 *
 *************************************************************************************/
package ve.net.alge.grpc.pos.service.cash;

import org.compiere.model.MPayment;

import ve.net.alge.grpc.util.RecordUtil;

/**
 * A util class for change values for documents
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 */
public class CashUtil {
	
	/**
	 * Default set date for payment
	 * @param payment
	 */
	public static void setCurrentDate(MPayment payment) {
		setCurrentDate(payment, false);
	}
	
	/**
	 * Set current date to order
	 * @param payment
	 * @param force
	 * @return void
	 */
	public static void setCurrentDate(MPayment payment, boolean force) {
		//	Ignore if the document is processed
		if(payment.isProcessed() || payment.isProcessing() 
//				|| (payment.getTenderType().equals(MPayment.TENDERTYPE_CreditMemo) && !force) // TODO core
				) {
			return;
		}
		if(!payment.getDateTrx().equals(RecordUtil.getDate())) {
			payment.setDateTrx(RecordUtil.getDate());
			payment.saveEx();
		}
	}
}
