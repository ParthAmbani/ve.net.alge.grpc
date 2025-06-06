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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Optional;

import org.adempiere.core.domains.models.I_C_ConversionType;
import org.adempiere.core.domains.models.I_C_PaymentMethod;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBank;
import org.compiere.model.MInvoice;
import org.compiere.model.MOrder;
import org.compiere.model.MPOS;
import org.compiere.model.MPayment;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Util;
import org.spin.backend.grpc.pos.CreatePaymentRequest;
import org.spin.service.grpc.util.value.NumberManager;
import org.spin.service.grpc.util.value.ValueManager;

import ve.net.alge.grpc.pos.service.order.OrderManagement;
import ve.net.alge.grpc.pos.service.order.OrderUtil;
import ve.net.alge.grpc.pos.util.ColumnsAdded;
import ve.net.alge.grpc.util.ConvertUtil;
import ve.net.alge.grpc.util.RecordUtil;

/**
 * This class manage all related to collecting and payment methods
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 */
public class CollectingManagement {
	
	/**
	 * Create Payment based on request, transaction name and pos
	 * @param request
	 * @param pointOfSalesDefinition
	 * @param transactionName
	 * @return
	 */
	public static  MPayment createPaymentFromOrder(MOrder salesOrder, CreatePaymentRequest request, MPOS pointOfSalesDefinition, String transactionName) {
		OrderManagement.validateOrderReleased(salesOrder);
		OrderUtil.setCurrentDate(salesOrder);
		String tenderType = request.getTenderTypeCode();
		if(Util.isEmpty(tenderType)) {
			tenderType = MPayment.TENDERTYPE_Cash;
		}
		if(!OrderUtil.isValidOrder(salesOrder)) {
			throw new AdempiereException("@ActionNotAllowedHere@");
		}
		if(pointOfSalesDefinition.getC_BankAccount_ID() <= 0) {
			throw new AdempiereException("@NoCashBook@");
		}
		//	Amount
		BigDecimal paymentAmount = NumberManager.getBigDecimalFromString(
			request.getAmount()
		);
		if(paymentAmount == null || paymentAmount.compareTo(Env.ZERO) == 0) {
			throw new AdempiereException("@PayAmt@ @NotFound@");
		}
		//	Order
		int currencyId = request.getCurrencyId();
		if(currencyId <= 0) {
			currencyId = salesOrder.getC_Currency_ID();
		}
		//	Throw if not exist conversion
		ConvertUtil.validateConversion(salesOrder, currencyId, pointOfSalesDefinition.get_ValueAsInt(I_C_ConversionType.COLUMNNAME_C_ConversionType_ID), RecordUtil.getDate());
		//	
		MPayment payment = new MPayment(Env.getCtx(), 0, transactionName);
		payment.setC_BankAccount_ID(pointOfSalesDefinition.getC_BankAccount_ID());
		//	Get from POS
		int documentTypeId;
		if(!request.getIsRefund()) {
			documentTypeId = pointOfSalesDefinition.get_ValueAsInt("POSCollectingDocumentType_ID");
		} else {
			documentTypeId = pointOfSalesDefinition.get_ValueAsInt("POSRefundDocumentType_ID");
		}
		if(documentTypeId > 0) {
			payment.setC_DocType_ID(documentTypeId);
		} else {
			payment.setC_DocType_ID(!request.getIsRefund());
		}
		payment.setAD_Org_ID(salesOrder.getAD_Org_ID());
		Timestamp date = ValueManager.getDateFromTimestampDate(
			request.getPaymentDate()
		);
    	if(date != null) {
    		payment.setDateTrx(date);
    		payment.setDateAcct(date);
    	}
		Timestamp dateValue = ValueManager.getDateFromTimestampDate(
			request.getPaymentAccountDate()
		);
    	if(dateValue != null) {
    		payment.setDateAcct(dateValue);
    	}
        payment.setTenderType(tenderType);
        payment.setDescription(Optional.ofNullable(request.getDescription()).orElse(salesOrder.getDescription()));
        payment.setC_BPartner_ID (salesOrder.getC_BPartner_ID());
        payment.setC_Currency_ID(currencyId);
//        payment.setC_POS_ID(pointOfSalesDefinition.getC_POS_ID());// TODO core
        if(salesOrder.getSalesRep_ID() > 0) {
        	payment.set_ValueOfColumn("CollectingAgent_ID", salesOrder.getSalesRep_ID());
        }
        if(pointOfSalesDefinition.get_ValueAsInt(I_C_ConversionType.COLUMNNAME_C_ConversionType_ID) > 0) {
        	payment.setC_ConversionType_ID(pointOfSalesDefinition.get_ValueAsInt(I_C_ConversionType.COLUMNNAME_C_ConversionType_ID));
        }
        payment.setPayAmt(paymentAmount);
        //	Order Reference
        payment.setC_Order_ID(salesOrder.getC_Order_ID());
        payment.setDocStatus(MPayment.DOCSTATUS_Drafted);
		if(!Util.isEmpty(request.getDescription())) {
			payment.setDescription(request.getDescription());
		} else {
			int invoiceId = salesOrder.getC_Invoice_ID();
			if(invoiceId > 0) {
				MInvoice invoice = new MInvoice(Env.getCtx(), payment.getC_Invoice_ID(), transactionName);
				payment.setDescription(Msg.getMsg(Env.getCtx(), "Invoice No ") + invoice.getDocumentNo());
			} else {
				payment.setDescription(Msg.getMsg(Env.getCtx(), "Order No ") + salesOrder.getDocumentNo());
			}
		}
		switch (tenderType) {
			case MPayment.TENDERTYPE_Check:
				payment.setCheckNo(request.getReferenceNo());
				break;
			case MPayment.TENDERTYPE_DirectDebit:
				break;
			case MPayment.TENDERTYPE_CreditCard:
				break;
//			case MPayment.TENDERTYPE_MobilePaymentInterbank:// TODO core
//				payment.setR_PnRef(request.getReferenceNo());
//				break;
//			case MPayment.TENDERTYPE_Zelle:
//				payment.setR_PnRef(request.getReferenceNo());
//				break;
//			case MPayment.TENDERTYPE_CreditMemo:
//				payment.setR_PnRef(request.getReferenceNo());
//				payment.setDocumentNo(request.getReferenceNo());
//				payment.setCheckNo(request.getReferenceNo());
//				break;
			default:
				payment.setDescription(request.getDescription());
				break;
		}
		//	Payment Method
		if(request.getPaymentMethodId() > 0) {
			payment.set_ValueOfColumn(I_C_PaymentMethod.COLUMNNAME_C_PaymentMethod_ID, request.getPaymentMethodId());
		}
		//	Set Bank Id
		if(request.getBankId() > 0) {
			payment.set_ValueOfColumn(MBank.COLUMNNAME_C_Bank_ID, request.getBankId());
		}
		//	Customer Bank Account
		if(request.getCustomerBankAccountId() > 0) {
			payment.setC_BP_BankAccount_ID(request.getCustomerBankAccountId());
		}
		//	Validate reference
		if(!Util.isEmpty(request.getReferenceNo())) {
			payment.setDocumentNo(request.getReferenceNo());
			payment.addDescription(request.getReferenceNo());
		}
		CashUtil.setCurrentDate(payment);
		if(Util.isEmpty(payment.getDocumentNo())) {
			String value = DB.getDocumentNo(payment.getC_DocType_ID(), transactionName, false,  payment);
	        payment.setDocumentNo(value);
		}
		//	
		if(request.getInvoiceReferenceId() > 0) {
			payment.set_ValueOfColumn(ColumnsAdded.COLUMNNAME_ECA14_Invoice_Reference_ID, request.getInvoiceReferenceId());
		}
		payment.saveEx(transactionName);
		return payment;
	}
}
