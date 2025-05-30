/************************************************************************************
 * Copyright (C) 2018-2023 E.R.P. Consultores y Asociados, C.A.                     *
 * Contributor(s): Edwin Betancourt, EdwinBetanc0urt@outlook.com                    *
 * This program is free software: you can redistribute it and/or modify             *
 * it under the terms of the GNU General Public License as published by             *
 * the Free Software Foundation, either version 2 of the License, or                *
 * (at your option) any later version.                                              *
 * This program is distributed in the hope that it will be useful,                  *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of                   *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the                     *
 * GNU General Public License for more details.                                     *
 * You should have received a copy of the GNU General Public License                *
 * along with this program. If not, see <https://www.gnu.org/licenses/>.            *
 ************************************************************************************/
package ve.net.alge.grpc.service;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.pdf.IText7Document;
import org.compiere.model.I_AD_Ref_List;
import org.compiere.model.I_C_BankAccount;
import org.compiere.model.I_C_BankAccountDoc;
import org.compiere.model.I_C_PaySelection;
import org.compiere.model.I_C_PaySelectionCheck;
import org.compiere.model.I_C_PaySelectionLine;
import org.compiere.model.MBank;
import org.compiere.model.MBankAccount;
import org.compiere.model.MBankAccountDoc;
import org.compiere.model.MCurrency;
import org.compiere.model.MInvoice;
import org.compiere.model.MPaySelection;
import org.compiere.model.MPaySelectionCheck;
import org.compiere.model.MPaySelectionLine;
import org.compiere.model.MPaymentBatch;
import org.compiere.model.MRefList;
import org.compiere.model.MRole;
import org.compiere.model.Query;
import org.compiere.model.X_C_PaySelectionLine;
import org.compiere.print.ReportEngine;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.MimeType;
import org.compiere.util.PaymentExport;
import org.compiere.util.PaymentExportList;
import org.compiere.util.Util;
import org.spin.backend.grpc.common.ListLookupItemsResponse;
import org.spin.backend.grpc.common.LookupItem;
import org.spin.backend.grpc.common.ReportOutput;
import org.spin.backend.grpc.payment_print_export.BankAccount;
import org.spin.backend.grpc.payment_print_export.ConfirmPrintRequest;
import org.spin.backend.grpc.payment_print_export.ConfirmPrintResponse;
import org.spin.backend.grpc.payment_print_export.ExportRequest;
import org.spin.backend.grpc.payment_print_export.ExportResponse;
import org.spin.backend.grpc.payment_print_export.GetDocumentNoRequest;
import org.spin.backend.grpc.payment_print_export.GetDocumentNoResponse;
import org.spin.backend.grpc.payment_print_export.GetPaymentSelectionRequest;
import org.spin.backend.grpc.payment_print_export.ListPaymentRulesRequest;
import org.spin.backend.grpc.payment_print_export.ListPaymentSelectionsRequest;
import org.spin.backend.grpc.payment_print_export.ListPaymentsRequest;
import org.spin.backend.grpc.payment_print_export.ListPaymentsResponse;
import org.spin.backend.grpc.payment_print_export.Payment;
import org.spin.backend.grpc.payment_print_export.PaymentPrintExportGrpc.PaymentPrintExportImplBase;
import org.spin.backend.grpc.payment_print_export.PaymentSelection;
import org.spin.backend.grpc.payment_print_export.PrintRemittanceRequest;
import org.spin.backend.grpc.payment_print_export.PrintRemittanceResponse;
import org.spin.backend.grpc.payment_print_export.PrintRequest;
import org.spin.backend.grpc.payment_print_export.PrintResponse;
import org.spin.backend.grpc.payment_print_export.ProcessRequest;
import org.spin.backend.grpc.payment_print_export.ProcessResponse;
import org.spin.service.grpc.authentication.SessionManager;
import org.spin.service.grpc.util.db.LimitUtil;
import org.spin.service.grpc.util.value.NumberManager;
import org.spin.service.grpc.util.value.ValueManager;

import com.google.protobuf.ByteString;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import ve.net.alge.grpc.service.core_functionality.CoreFunctionalityConvert;
import ve.net.alge.grpc.util.LookupUtil;

/**
 * @author Edwin Betancourt, EdwinBetanc0urt@outlook.com, https://github.com/EdwinBetanc0urt
 * Service for backend of Update Center
 */
public class PaymentPrintExport extends PaymentPrintExportImplBase {

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(PaymentPrintExport.class);
	
	@Override
	public void getPaymentSelection(GetPaymentSelectionRequest request, StreamObserver<PaymentSelection> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Request Null");
			}
			PaymentSelection.Builder builder = getPaymentSelection(request);
			responseObserver.onNext(builder.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			e.printStackTrace();
			responseObserver.onError(
				Status.INTERNAL
					.withDescription(e.getLocalizedMessage())
					.withCause(e)
					.asRuntimeException()
			);
		}
	}

	private PaymentSelection.Builder getPaymentSelection(GetPaymentSelectionRequest request) {
		// validate key values
		int paymentSelectionId = request.getId();
		if (paymentSelectionId <= 0) {
			throw new AdempiereException("@FillMandatory@ @C_PaySelection_ID@");
		}

		MPaySelection paymentSelection = new Query(
			Env.getCtx(),
			I_C_PaySelection.Table_Name,
			" C_PaySelection_ID = ? ",
			null
		)
			.setParameters(paymentSelectionId)
			.first();
		if (paymentSelection == null || paymentSelection.getC_PaySelection_ID() <= 0) {
			throw new AdempiereException("@C_PaySelection_ID@ @NotFound@");
		}

		return convertPaymentSelection(paymentSelection);
	}

	private PaymentSelection.Builder convertPaymentSelection(MPaySelection paymentSelection) {
		PaymentSelection.Builder builder = PaymentSelection.newBuilder();
		if (paymentSelection == null || paymentSelection.getC_PaySelection_ID() <= 0) {
			return builder;
		}
		builder.setId(paymentSelection.getC_PaySelection_ID())
//			.setDocumentNo(// TODO core
//				ValueManager.validateNull(
//					paymentSelection.getDocumentNo()
//				)
//			)
			.setCurrency(
				CoreFunctionalityConvert.convertCurrency(
					paymentSelection.getC_Currency_ID()
				)
			)
			.setBankAccount(
				convertBankAccount(paymentSelection.getC_BankAccount_ID())
			)
		;
		Integer paymentCount = new Query(
			Env.getCtx(),
			I_C_PaySelectionCheck.Table_Name,
			I_C_PaySelectionCheck.COLUMNNAME_C_PaySelection_ID + "=?",
			null
		)
			.setClient_ID()
			.setParameters(paymentSelection.getC_PaySelection_ID())
			.count()
		;
		builder.setPaymentQuantity(paymentCount);

		return builder;
	}

	public static MPaySelection validateAndGetPaySelection(int paymentSelectionId) {
		// validate payment selection
		if (paymentSelectionId <= 0) {
			throw new AdempiereException("@FillMandatory@ @C_PaySelection_ID@");
		}
		MPaySelection paymentSelection = new Query(
			Env.getCtx(),
			I_C_PaySelection.Table_Name,
			" C_PaySelection_ID = ? ",
			null
		)
			.setParameters(paymentSelectionId)
			.first();
		if (paymentSelection == null || paymentSelection.getC_PaySelection_ID() <= 0) {
			throw new AdempiereException("@C_PaySelection_ID@ @NotFound@");
		}
		return paymentSelection;
	}



	private BankAccount.Builder convertBankAccount(int bankAccountId) {
		if (bankAccountId > 0) {
			MBankAccount bankAccount = MBankAccount.get(Env.getCtx(), bankAccountId);
			return convertBankAccount(bankAccount);
		}
		return BankAccount.newBuilder();
	}
	private BankAccount.Builder convertBankAccount(MBankAccount bankAccount) {
		BankAccount.Builder builder = BankAccount.newBuilder();
		if (bankAccount == null || bankAccount.getC_BankAccount_ID() <= 0) {
			return builder;
		}
		
		MBank bank = MBank.get(Env.getCtx(), bankAccount.getC_Bank_ID());

		String accountNo = ValueManager.validateNull(
			bankAccount.getAccountNo()
		);
		int accountNoLength = accountNo.length();
		if (accountNoLength > 4) {
			accountNo = accountNo.substring(accountNoLength - 4);
		}
		accountNo = String.format("%1$" + 20 + "s", accountNo).replace(" ", "*");

		builder.setId(bankAccount.getC_BankAccount_ID())
			.setAccountNo(accountNo)
			.setAccountName(
				ValueManager.validateNull(
					bankAccount.getName()
				)
			)
			.setBankName(
				ValueManager.validateNull(
					bank.getName()
				)
			)
			.setCurrentBalance(
				NumberManager.getBigDecimalToString(
					bankAccount.getCurrentBalance()
				)
			)
		;

		return builder;
	}

	public static MBankAccount validateAndGetBankAccount(int bankAccountId) {
		if (bankAccountId <= 0) {
			throw new AdempiereException("@FillMandatory@ @C_BankAccount_ID@");
		}
		MBankAccount bankAccount = new Query(
			Env.getCtx(),
			I_C_BankAccount.Table_Name,
			" C_BankAccount_ID = ? ",
			null
		)
			.setParameters(bankAccountId)
			.setClient_ID()
			.first();
		if (bankAccount == null || bankAccount.getC_BankAccount_ID() <= 0) {
			throw new AdempiereException("@C_BankAccount_ID@ @NotFound@");
		}
		return bankAccount;
	}



	@Override
	public void listPaymentSelections(ListPaymentSelectionsRequest request, StreamObserver<ListLookupItemsResponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Request Null");
			}
			ListLookupItemsResponse.Builder builder = listPaymentSelections(request);
			responseObserver.onNext(builder.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			e.printStackTrace();
			responseObserver.onError(
				Status.INTERNAL
					.withDescription(e.getLocalizedMessage())
					.withCause(e)
					.asRuntimeException()
			);
		}
	}
	
	private ListLookupItemsResponse.Builder listPaymentSelections(ListPaymentSelectionsRequest request) {
		//	Add DocStatus for validation
		final String validationCode = " C_PaySelection.C_BankAccount_ID IS NOT NULL "
			+ "AND C_PaySelection.DocStatus = 'CO' "
			+ "AND EXISTS("
			+ "		SELECT 1 FROM C_PaySelectionCheck psc "
			+ "		LEFT JOIN C_Payment p ON(p.C_Payment_ID = psc.C_Payment_ID) "
			+ "		WHERE psc.C_PaySelection_ID = C_PaySelection.C_PaySelection_ID "
			+ "		AND (psc.C_Payment_ID IS NULL OR p.DocStatus NOT IN('CO', 'CL'))"
			+ ")";
		Query query = new Query(
			Env.getCtx(),
			I_C_PaySelection.Table_Name,
			validationCode,
			null
		)
			.setOnlyActiveRecords(true)
			.setApplyAccessFilter(MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO)
		;

		int count = query.count();
		ListLookupItemsResponse.Builder builderList = ListLookupItemsResponse.newBuilder()
			.setRecordCount(count);

		List<MPaySelection> paymentSelectionChekList = query.list();
		paymentSelectionChekList.stream().forEach(paymentSelection -> {
			BigDecimal totalAmount = Env.ZERO;
			if (paymentSelection.getTotalAmt() != null) {
				totalAmount = paymentSelection.getTotalAmt();
			}

			//	Display column
			String displayedValue = new StringBuffer()
				.append(paymentSelection.get_ID())// TODO core using ID for now
//				.append(Util.isEmpty(paymentSelection.getDocumentNo(), true) ? "-1" : paymentSelection.getDocumentNo())
				.append("_")
				.append(MCurrency.getISO_Code(Env.getCtx(), paymentSelection.getC_Currency_ID()))
				.append("_")
				.append(DisplayType.getNumberFormat(DisplayType.Amount).format(totalAmount))
				.toString();
	
			LookupItem.Builder builderItem = LookupUtil.convertObjectFromResult(
				paymentSelection.getC_PaySelection_ID(),
				paymentSelection.get_UUID(),
				String.valueOf(paymentSelection.get_ID()),
//				paymentSelection.getDocumentNo(),// TODO core now use id
				displayedValue,
				paymentSelection.isActive()
			);

			builderItem.setTableName(I_C_PaySelection.Table_Name);
			builderItem.setId(paymentSelection.getC_PaySelection_ID());
			
			builderList.addRecords(builderItem.build());
		});

		return builderList;
	}


	@Override
	public void listPaymentRules(ListPaymentRulesRequest request, StreamObserver<ListLookupItemsResponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Request Null");
			}
			ListLookupItemsResponse.Builder builder = listPaymentRules(request);
			responseObserver.onNext(builder.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			e.printStackTrace();
			responseObserver.onError(
				Status.INTERNAL
					.withDescription(e.getLocalizedMessage())
					.withCause(e)
					.asRuntimeException()
			);
		}
	}

	private ListLookupItemsResponse.Builder listPaymentRules(ListPaymentRulesRequest request) {
		// validate and get Pay Selection
		MPaySelection paymentSelection = validateAndGetPaySelection(request.getPaymentSelectionId());

		ListLookupItemsResponse.Builder builderList = ListLookupItemsResponse.newBuilder();
		final String whereClause = "EXISTS ("
			+ "	SELECT 1 FROM C_PaySelectionLine "
			+ "	WHERE "
			+ "	AD_Ref_List.Value = C_PaySelectionLine.PaymentRule"
			+ "	AND C_PaySelectionLine.C_PaySelection_ID = ?"
			+ "	AND AD_Ref_List.AD_Reference_ID = ?"
			+ ")"
		;
		List<MRefList> paymemntRulesList = new Query(
			Env.getCtx(),
			I_AD_Ref_List.Table_Name,
			whereClause,
			null
		).setParameters(paymentSelection.getC_PaySelection_ID(), X_C_PaySelectionLine.PAYMENTRULE_AD_Reference_ID)
		.<MRefList>list();

		paymemntRulesList.stream().forEach(paymentRule -> {
			LookupItem.Builder builderItem = LookupUtil.convertObjectFromResult(
				paymentRule.getAD_Ref_List_ID(),
				paymentRule.get_UUID(),
				paymentRule.getValue(),
				paymentRule.getName(),
				paymentRule.isActive()
			);

			builderItem.setTableName(I_AD_Ref_List.Table_Name);
			builderItem.setId(paymentRule.getAD_Ref_List_ID());
			
			builderList.addRecords(builderItem.build());
		});
		
		return builderList;
	}

	public static MRefList validateAndGetPaymentRule(String value) {
		if (Util.isEmpty(value, true)) {
			throw new AdempiereException("@FillMandatory@ @PaymentRule@");
		}
		MRefList paymentRule = MRefList.get(
			Env.getCtx(),
			X_C_PaySelectionLine.PAYMENTRULE_AD_Reference_ID,
			value,
			null
		);
		if (paymentRule == null || paymentRule.getAD_Ref_List_ID() <= 0) {
			throw new AdempiereException("@PaymentRule@ @NotFound@");
		}
		return paymentRule;
	}



	@Override
	public void getDocumentNo(GetDocumentNoRequest request, StreamObserver<GetDocumentNoResponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Request Null");
			}
			GetDocumentNoResponse.Builder builder = getDocumentNo(request);
			responseObserver.onNext(builder.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			e.printStackTrace();
			responseObserver.onError(
				Status.INTERNAL
					.withDescription(e.getLocalizedMessage())
					.withCause(e)
					.asRuntimeException()
			);
		}
	}

	private GetDocumentNoResponse.Builder getDocumentNo(GetDocumentNoRequest request) {
		// validate and get Bank Account
		MBankAccount bankAccount = validateAndGetBankAccount(request.getBankAccountId());

		// validate and Payment Rule
		MRefList paymentRule = validateAndGetPaymentRule(request.getPaymentRule());

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ")
			.append(I_C_BankAccountDoc.COLUMNNAME_CurrentNext)
			.append(" FROM ")
			.append(I_C_BankAccountDoc.Table_Name)
			.append(" WHERE ")
			.append(I_C_BankAccountDoc.COLUMNNAME_C_BankAccount_ID).append("=? AND ")
			.append(I_C_BankAccountDoc.COLUMNNAME_PaymentRule).append("=? AND ")
			.append(I_C_BankAccountDoc.COLUMNNAME_IsActive).append("=?")
		;

		List<Object> parameters = new ArrayList<>();
		parameters.add(bankAccount.getC_BankAccount_ID());
		parameters.add(paymentRule.getValue());
		parameters.add(true);

		int documentNo = DB.getSQLValueEx(null , sql.toString(), parameters.toArray());
		return GetDocumentNoResponse.newBuilder()
			.setDocumentNo(documentNo);
	}

	private Payment.Builder convertPayment(MPaySelectionLine paySelectionLine) {
		Payment.Builder builder = Payment.newBuilder();
		if (paySelectionLine == null || paySelectionLine.getC_PaySelectionLine_ID() <= 0) {
			return builder;
		}

		String documentNo = "";
		if (paySelectionLine.getC_Invoice_ID() > 0) {
			MInvoice invoice = MInvoice.get(Env.getCtx(), paySelectionLine.getC_Invoice_ID());
			documentNo = invoice.getDocumentNo();
		} 
		// TODO core
//		else if (paySelectionLine.getC_Order_ID() > 0) {
//			MOrder order = new MOrder(Env.getCtx(), paySelectionLine.getC_Order_ID(), null);
//			documentNo = order.getDocumentNo();
//		}

//		BigDecimal grandTotal = paySelectionLine.getAmtSource();// TODO core
		BigDecimal openAmount = paySelectionLine.getOpenAmt();
		BigDecimal paymentAmount = paySelectionLine.getPayAmt();
//		BigDecimal overUnderAmount = grandTotal.subtract(openAmount);// TODO core
		BigDecimal finalBalance = paySelectionLine.getOpenAmt().subtract(paymentAmount);

		// TODO core
//		MBPartner vendor = MBPartner.get(Env.getCtx(), paySelectionLine.getC_BPartner_ID());

		builder.setDocumentNo(documentNo)
		// TODO core
//			.setVendorId(vendor.getC_BPartner_ID())
//			// .setVendorTaxId(ValueUtil.validateNull(vendor.get_UUID()))
//			.setVendorTaxId(
//				ValueManager.validateNull(
//					vendor.getTaxID()
//				)
//			)
//			.setVendorName(
//				ValueManager.validateNull(
//					vendor.getName()
//				)
//			)
//			.setGrandTotal(
//				NumberManager.getBigDecimalToString(
//					grandTotal
//				)
//			)
			.setPaymentAmount(
				NumberManager.getBigDecimalToString(
					paymentAmount
				)
			)
			.setOpenAmount(
				NumberManager.getBigDecimalToString(
					openAmount
				)
			)
//			.setOverUnderAmount(
//				NumberManager.getBigDecimalToString(
//					overUnderAmount
//				)
//			)
			.setFinalBalance(
				NumberManager.getBigDecimalToString(
					finalBalance
				)
			)
		;

		return builder;
	}



	@Override
	public void listPayments(ListPaymentsRequest request, StreamObserver<ListPaymentsResponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Request Null");
			}
			ListPaymentsResponse.Builder builder = listPayments(request);
			responseObserver.onNext(builder.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			e.printStackTrace();
			responseObserver.onError(
				Status.INTERNAL
					.withDescription(e.getLocalizedMessage())
					.withCause(e)
					.asRuntimeException()
			);
		}
	}

	private ListPaymentsResponse.Builder listPayments(ListPaymentsRequest request) {
		// validate and get Pay Selection
		MPaySelection paymentSelection = validateAndGetPaySelection(
			request.getPaymentSelectionId()
		);

		// validate payment rule
		MRefList paymentRule = validateAndGetPaymentRule(
			request.getPaymentRule()
		);

		String nexPageToken = null;
		int pageNumber = LimitUtil.getPageNumber(SessionManager.getSessionUuid(), request.getPageToken());
		int limit = LimitUtil.getPageSize(request.getPageSize());
		int offset = (pageNumber - 1) * limit;

		ListPaymentsResponse.Builder builderList = ListPaymentsResponse.newBuilder();
		final String whereClause = " C_PaySelectionLine.C_PaySelection_ID = ?" 
			+ " AND C_PaySelectionLine.PaymentRule = ? "
			+ "	AND EXISTS ("
			+ "	SELECT 1 FROM C_PaySelectionCheck "
			+ " INNER JOIN C_PaySelection ON C_PaySelection.C_PaySelection_ID = C_PaySelectionLine.C_PaySelection_ID"
			+ "	WHERE "
			+ "	C_PaySelectionCheck.C_PaySelectionCheck_ID = C_PaySelectionLine.C_PaySelectionCheck_ID"
			+ " AND C_PaySelectionCheck.IsPrinted = 'N' "
			+ ")"
		;
		Query paymentSelectionLine = new Query(
			Env.getCtx(),
			I_C_PaySelectionLine.Table_Name,
			whereClause,
			null
		)
			.setParameters(paymentSelection.getC_PaySelection_ID(), paymentRule.getValue())
			.setApplyAccessFilter(MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO)
		;

		int count = paymentSelectionLine.count();

		paymentSelectionLine
//			.setLimit(limit, offset)
			.<MPaySelectionLine>list()
			.stream()
			.forEach(selectionLine -> {
				Payment.Builder builder = convertPayment(selectionLine);
				builderList.addRecords(builder);
			});

		builderList.setRecordCount(count);
		//	Set page token
		if(LimitUtil.isValidNextPageToken(count, offset, limit)) {
			nexPageToken = LimitUtil.getPagePrefix(SessionManager.getSessionUuid()) + (pageNumber + 1);
		}
		builderList.setNextPageToken(
			ValueManager.validateNull(nexPageToken)
		);
	
		return builderList;
	}



	public static List<MPaySelectionCheck> validateAndListPaySelectionCheck(int paymentSelectionId, String paymentRuleValue, int documentNo) {
		//	get list Pay Selections Check
		List<MPaySelectionCheck> paySelectionChecksList = Arrays.asList(MPaySelectionCheck.get(
			paymentSelectionId,
			paymentRuleValue,
			documentNo,
			null
		))
			.stream()
			.filter(paySelectionCheck -> {
				return paySelectionCheck != null &&
					paymentRuleValue.equals(paySelectionCheck.getPaymentRule());
			})
			.collect(Collectors.toList())
		;
		if (paySelectionChecksList == null || paySelectionChecksList.size() <= 0) {
			throw new AdempiereException("@VPayPrintNoRecords@");
		}
		return paySelectionChecksList;
	}


	@Override
	public void process(ProcessRequest request, StreamObserver<ProcessResponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Request Null");
			}
			ProcessResponse.Builder builder = process(request);
			responseObserver.onNext(builder.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			e.printStackTrace();
			responseObserver.onError(
				Status.INTERNAL
					.withDescription(e.getLocalizedMessage())
					.withCause(e)
					.asRuntimeException()
			);
		}
	}

	private ProcessResponse.Builder process(ProcessRequest request) {
		createEFT(
			request.getPaymentSelectionId(),
			request.getPaymentRule(),
			request.getBankAccountId(),
			request.getDocumentNo()
		);
		//	document No not updated
		return ProcessResponse.newBuilder();
	}


	public static boolean createEFT(int paySelectiontId, String paymentRuleValue, int bankAccountId, int documentNo) {
		// validate and get Pay Selection
		MPaySelection paymentSelection = validateAndGetPaySelection(paySelectiontId);

		// validate and get Payment Rule
		MRefList paymentRule = validateAndGetPaymentRule(paymentRuleValue);

		// validate and get Bank Account
		MBankAccount bankAccount = validateAndGetBankAccount(bankAccountId);

		// validate and get list Pay Selections Check
		List<MPaySelectionCheck> paySelectionChecks = validateAndListPaySelectionCheck(
			paymentSelection.getC_PaySelection_ID(),
			paymentRule.getValue(),
			documentNo
		);

		MPaymentBatch paymentBatch = MPaymentBatch.getForPaySelection(
			Env.getCtx(),
			paymentSelection.getC_PaySelection_ID(),
			null
		);

		int lastDocumentNo = confirmPrint(paySelectionChecks, paymentBatch);
		if (lastDocumentNo != 0) {
			setBankAccountNextSequence(bankAccount.getC_BankAccount_ID(), paymentRule.getValue(), ++lastDocumentNo);
		}

		return true;
	}
	

	/**************************************************************************
	 * 	Confirm Print.
	 * 	Create Payments the first time 
	 * 	@param paySelectionChecks checks
	 * 	@param batch batch
	 * 	@return last Document number or 0 if nothing printed
	 */
	public static int confirmPrint (List<MPaySelectionCheck> paySelectionChecks, MPaymentBatch batch)
	{
		AtomicInteger lastDocumentNo = new AtomicInteger();
		paySelectionChecks.stream().filter(psc -> psc != null).forEach(paySelectionCheck ->
		{
			try
			{
				MPaySelectionCheck.confirmPrint(paySelectionCheck, batch);
				int no = Integer.parseInt(paySelectionCheck.getDocumentNo());
				if (lastDocumentNo.get() < no)
					lastDocumentNo.set(no);
			}
			catch (NumberFormatException ex)
			{
				log.log(Level.SEVERE, "DocumentNo=" + paySelectionCheck.getDocumentNo(), ex);
			}
		});	//	all checks

		log.fine("Last Document No = " + lastDocumentNo.get());
		return lastDocumentNo.get();
	}	//	confirmPrint



	@Override
	public void export(ExportRequest request, StreamObserver<ExportResponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Request Null");
			}
			ExportResponse.Builder builder = export(request);
			responseObserver.onNext(builder.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			e.printStackTrace();
			responseObserver.onError(
				Status.INTERNAL
					.withDescription(e.getLocalizedMessage())
					.withCause(e)
					.asRuntimeException()
			);
		}
	}

	private ExportResponse.Builder export(ExportRequest request) {
		// validate and get Pay Selection
		MPaySelection paymentSelection = validateAndGetPaySelection(request.getPaymentSelectionId());
//		MDocType doctType = MDocType.get(Env.getCtx(), paymentSelection.getC_DocType_ID());

		// Payment Rule
		MRefList paymentRule = validateAndGetPaymentRule(request.getPaymentRule());

		// validate and get Bank Account
		MBankAccount bankAccount = validateAndGetBankAccount(request.getBankAccountId());

		String paymentExportClass = bankAccount.getPaymentExportClass();
//		if (doctType.isPayrollPayment()) {// TODO Core
//			paymentExportClass = bankAccount.getPayrollPaymentExportClass();
//		}
		if (Util.isEmpty(paymentExportClass, true)) {
			paymentExportClass = "org.compiere.util.GenericPaymentExport";
		}

		int startDocumentNo = request.getDocumentNo();

		ExportResponse.Builder builder = ExportResponse.newBuilder();
		try {
			// Get File Info
			File tempFile = File.createTempFile("paymentExport", ".txt");

			// validate and get list Pay Selections Check
			List<MPaySelectionCheck> paySelectionChecks = validateAndListPaySelectionCheck(
				paymentSelection.getC_PaySelection_ID(),
				paymentRule.getValue(),
				startDocumentNo
			);

			// Create File
			int no = 0;
			StringBuffer error = new StringBuffer("");
			//	Get Payment Export Class
			try {
				Class<?> clazz = Class.forName(paymentExportClass);
				if (PaymentExportList.class.isAssignableFrom(clazz)) {
					PaymentExportList custom = (PaymentExportList) clazz.getDeclaredConstructor().newInstance();
					no = custom.exportToFile(paySelectionChecks, tempFile, error);
					if(custom.getFile() != null) {
						tempFile = custom.getFile();
					}
				}
				else if (PaymentExport.class.isAssignableFrom(clazz)) {
					PaymentExport custom = (PaymentExport) clazz.getDeclaredConstructor().newInstance();
					no = custom.exportToFile(paySelectionChecks.toArray(new MPaySelectionCheck[paySelectionChecks.size()]), tempFile, error);
				}
			}
			catch (ClassNotFoundException e) {
				no = -1;
				error.append("No custom PaymentExport class " + paymentExportClass + " - " + e.toString());
				log.log(Level.SEVERE, error.toString(), e);
				e.printStackTrace();
			}
			catch (Exception e) {
				no = -1;
				error.append("Error in " + paymentExportClass + " check log, " + e.toString());
				log.log(Level.SEVERE, error.toString(), e);
				e.printStackTrace();
			}
			if (no >= 0) {
				// confirm print
			} else {
				throw new Exception(error.toString());
			}

			ByteString resultFile = ByteString.readFrom(new FileInputStream(tempFile));
			ReportOutput.Builder output = ReportOutput.newBuilder()
				.setOutputStream(resultFile)
				.setOutputBytes(resultFile)
			;

			builder.setReportOutput(output);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new AdempiereException(e.getLocalizedMessage());
		}

		return builder;
	}



	@Override
	public void print(PrintRequest request, StreamObserver<PrintResponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Request Null");
			}
			PrintResponse.Builder builder = print(request);
			responseObserver.onNext(builder.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			e.printStackTrace();
			responseObserver.onError(
				Status.INTERNAL
					.withDescription(e.getLocalizedMessage())
					.withCause(e)
					.asRuntimeException()
			);
		}
	}



	public static void validatePrintFormatByCheck(int paySelectionCheckId) {
		String sql = "SELECT bad.Check_PrintFormat_ID " //	1
			+ "FROM C_PaySelectionCheck d"
			+ " INNER JOIN C_PaySelection ps ON (d.C_PaySelection_ID = ps.C_PaySelection_ID)"
			+ " INNER JOIN C_BankAccountDoc bad ON (ps.C_BankAccount_ID = bad.C_BankAccount_ID AND d.PaymentRule = bad.PaymentRule)"
			+ "WHERE d.C_PaySelectionCheck_ID = ?"
		; //	info from BankAccount
		int value = DB.getSQLValue(null, sql, paySelectionCheckId);
		if (value <= 0) {
			throw new AdempiereException("@NoDocPrintFormat@ @C_PaySelectionCheck_ID@=" + paySelectionCheckId);
		}
	}

	private PrintResponse.Builder print(PrintRequest request) {
		// validate and get Pay Selection
		MPaySelection paymentSelection = validateAndGetPaySelection(request.getPaymentSelectionId());

		// validate and get Payment Rule
		final MRefList paymentRule = validateAndGetPaymentRule(request.getPaymentRule());

		// validate and get list Pay Selections Check
		List<MPaySelectionCheck> paySelectionChecks = validateAndListPaySelectionCheck(
			paymentSelection.getC_PaySelection_ID(),
			paymentRule.getValue(),
			request.getDocumentNo()
		);

		//	for all checks
		List<File> pdfList = new ArrayList<>();
		paySelectionChecks.stream()
			.forEach(paySelectionCheck -> {
				//	ReportCtrl will check BankAccountDoc for PrintFormat
				ReportEngine reportEngine = ReportEngine.get(
					Env.getCtx(),
					ReportEngine.CHECK,
					paySelectionCheck.get_ID()
				);
				if (reportEngine == null) {
					validatePrintFormatByCheck(paySelectionCheck.get_ID());
					return;
				}
				try {
					File file = File.createTempFile("WPayPrint", null);
					File pdfFile = reportEngine.getPDF(file);
					pdfList.add(pdfFile);
				}
				catch (Exception e) {
					log.log(Level.SEVERE, e.getLocalizedMessage(), e);
					return;
				}
			});

		PrintResponse.Builder builder = PrintResponse.newBuilder();
		ReportOutput.Builder reportOutputBuilder = ReportOutput.newBuilder();
		reportOutputBuilder.setReportType(
			"pdf"
		);
		try {
			File outFile = File.createTempFile("WPayPrint", null);
			String validFileName = ValueManager.validateNull(
				outFile.getName()
			);
			reportOutputBuilder.setName(
				validFileName
			);
			reportOutputBuilder.setMimeType("application/pdf");

			IText7Document.mergePdf(pdfList, outFile);
			ByteString resultFile = ByteString.readFrom(new FileInputStream(outFile));
			reportOutputBuilder.setOutputStream(resultFile);

			builder.setReportOutput(reportOutputBuilder);
		}
		catch (Exception e) {
			log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new AdempiereException(e.getLocalizedMessage());
		}

		return builder;
	}


	@Override
	public void confirmPrint(ConfirmPrintRequest request, StreamObserver<ConfirmPrintResponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Request Null");
			}
			ConfirmPrintResponse.Builder builder = confirmPrint(request);
			responseObserver.onNext(builder.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			e.printStackTrace();
			responseObserver.onError(
				Status.INTERNAL
					.withDescription(e.getLocalizedMessage())
					.withCause(e)
					.asRuntimeException()
			);
		}
	}

	private ConfirmPrintResponse.Builder confirmPrint(ConfirmPrintRequest request) {
		// validate and get Pay Selection
		MPaySelection paymentSelection = validateAndGetPaySelection(request.getPaymentSelectionId());

		// validate and get Payment Rule
		MRefList paymentRule = validateAndGetPaymentRule(request.getPaymentRule());

		// validate and get Bank Account
		MBankAccount bankAccount = validateAndGetBankAccount(request.getBankAccountId());

		// validate and get list Pay Selections Check
		List<MPaySelectionCheck> paySelectionChecks = validateAndListPaySelectionCheck(
			paymentSelection.getC_PaySelection_ID(),
			paymentRule.getValue(),
			request.getDocumentNo()
		);

		MPaymentBatch paymentBatch = MPaymentBatch.getForPaySelection(
			Env.getCtx(),
			paymentSelection.getC_PaySelection_ID(),
			null
		);

		int lastDocumentNo = confirmPrint(paySelectionChecks, paymentBatch);
		if (lastDocumentNo != 0) {
			setBankAccountNextSequence(bankAccount.getC_BankAccount_ID(), paymentRule.getValue(), ++lastDocumentNo);
		}
		//	document No not updated
		return ConfirmPrintResponse.newBuilder();
	}

	/**
	 * Set Next Sequence for Bank Account Document 
	 * @param bankAccountId
	 * @param paymentRule
	 * @param lastDocumentNo
	 */
	public static void setBankAccountNextSequence(int bankAccountId, String paymentRule, int lastDocumentNo) {
		if (bankAccountId > 0) {
			MBankAccount bankAccount = MBankAccount.get(Env.getCtx(), bankAccountId);
			setDocumentCurrentNext(bankAccount, paymentRule, lastDocumentNo);
		}
	}

	/**
	 * Set Current Next for Bank Account Document
	 * @param bankAccount 
	 * @param paymentRule
	 * @param currentNext
	 */
	public static void setDocumentCurrentNext(MBankAccount bankAccount, String paymentRule, int currentNext) {
		Optional<MBankAccountDoc> maybeBankAccountDoc = Optional.ofNullable(MBankAccountDoc.get(bankAccount.getC_BankAccount_ID(), paymentRule));
		maybeBankAccountDoc.ifPresent(bankAccountDoc ->{
			bankAccountDoc.setCurrentNext(currentNext);
			bankAccountDoc.save();
		});
	}


	public static void validatePrintFormatByRemittance(int paySelectionCheckId) {
		final String sql = "pf.Remittance_PrintFormat_ID " //	1
			+ "FROM C_PaySelectionCheck d"
			+ " INNER JOIN AD_PrintForm pf ON (d.AD_Client_ID=pf.AD_Client_ID)"
			+ "WHERE d.C_PaySelectionCheck_ID = ?"
			+ " AND pf.AD_Org_ID IN (0, d.AD_Org_ID) "
			+ " ORDER BY pf.AD_Org_ID DESC"
		; //	info from BankAccount
		int value = DB.getSQLValue(null, sql, paySelectionCheckId);
		if (value <= 0) {
			throw new AdempiereException("@NoDocPrintFormat@ @C_PaySelectionCheck_ID@=" + paySelectionCheckId);
		}
	}



	@Override
	public void printRemittance(PrintRemittanceRequest request, StreamObserver<PrintRemittanceResponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Request Null");
			}
			PrintRemittanceResponse.Builder builder = printRemittance(request);
			responseObserver.onNext(builder.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			e.printStackTrace();
			responseObserver.onError(
				Status.INTERNAL
					.withDescription(e.getLocalizedMessage())
					.withCause(e)
					.asRuntimeException()
			);
		}
	}

	private PrintRemittanceResponse.Builder printRemittance(PrintRemittanceRequest request) {
		// validate and get Pay Selection
		MPaySelection paymentSelection = validateAndGetPaySelection(request.getPaymentSelectionId());

		// validate and get Payment Rule
		MRefList paymentRule = validateAndGetPaymentRule(request.getPaymentRule());

		// validate and get list Pay Selections Check
		List<MPaySelectionCheck> paySelectionChecks = validateAndListPaySelectionCheck(
			paymentSelection.getC_PaySelection_ID(),
			paymentRule.getValue(),
			request.getDocumentNo()
		);

		List<File> pdfList = new ArrayList<>();
		paySelectionChecks.stream()
			.forEach(paySelectionCheck -> {
				ReportEngine reportEngine = ReportEngine.get(
					Env.getCtx(),
					ReportEngine.REMITTANCE,
					paySelectionCheck.get_ID()
				);
				if (reportEngine == null ) {
					validatePrintFormatByRemittance(paySelectionCheck.get_ID());
					return;
				}
				try {
					File file = File.createTempFile("WPayPrint", null);
					File pdfFile = reportEngine.getPDF(file);
					pdfList.add(pdfFile);
				}
				catch (Exception e) {
					log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			});

		PrintRemittanceResponse.Builder builder = PrintRemittanceResponse.newBuilder();
		ReportOutput.Builder reportOutputBuilder = ReportOutput.newBuilder();
		reportOutputBuilder.setReportType(
			"pdf"
		);

		try {
			File outFile = File.createTempFile("WPayPrint", null);
			String validFileName = ValueManager.validateNull(
				outFile.getName()
			);
			reportOutputBuilder.setName(
				validFileName
			);
			reportOutputBuilder.setMimeType(
				ValueManager.validateNull(
					MimeType.getMimeType(validFileName)
				)
			);

			IText7Document.mergePdf(pdfList, outFile);
			ByteString resultFile = ByteString.readFrom(new FileInputStream(outFile));
			reportOutputBuilder.setOutputStream(resultFile);

			builder.setReportOutput(reportOutputBuilder);
		}
		catch (Exception e) {
			log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}

		return builder;
	}

}
