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
 * Contributor(s): Yamel Senih www.erpya.com                                         *
 *************************************************************************************/
package ve.net.alge.grpc.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.I_AD_ChangeLog;
import org.compiere.model.I_AD_Element;
import org.compiere.model.I_AD_Ref_List;
import org.compiere.model.I_AD_Table;
import org.compiere.model.MBPBankAccount;
import org.compiere.model.MBPartner;
import org.compiere.model.MChatEntry;
import org.compiere.model.MClient;
import org.compiere.model.MColumn;
import org.compiere.model.MConversionRate;
import org.compiere.model.MConversionType;
import org.compiere.model.MCurrency;
import org.compiere.model.MInOut;
import org.compiere.model.MInvoice;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPOS;
import org.compiere.model.MPOSKey;
import org.compiere.model.MPOSKeyLayout;
import org.compiere.model.MPayment;
import org.compiere.model.MPriceList;
import org.compiere.model.MProduct;
import org.compiere.model.MRefList;
import org.compiere.model.MRefTable;
import org.compiere.model.MStorage;
import org.compiere.model.MTable;
import org.compiere.model.MTax;
import org.compiere.model.MUOMConversion;
import org.compiere.model.MUser;
import org.compiere.model.PO;
import org.compiere.model.POInfo;
import org.compiere.model.Query;
import org.compiere.model.SystemIDs;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Util;
import org.spin.backend.grpc.common.DocumentAction;
import org.spin.backend.grpc.common.DocumentStatus;
import org.spin.backend.grpc.common.Entity;
import org.spin.backend.grpc.common.ProcessInfoLog;
import org.spin.backend.grpc.pos.AvailableSeller;
import org.spin.backend.grpc.pos.CustomerBankAccount;
import org.spin.backend.grpc.pos.Key;
import org.spin.backend.grpc.pos.KeyLayout;
import org.spin.backend.grpc.pos.RMA;
import org.spin.backend.grpc.pos.RMALine;
import org.spin.backend.grpc.pos.Shipment;
import org.spin.backend.grpc.user_interface.ChatEntry;
import org.spin.backend.grpc.user_interface.ModeratorStatus;
import org.spin.service.grpc.util.value.NumberManager;
import org.spin.service.grpc.util.value.StringManager;
import org.spin.service.grpc.util.value.ValueManager;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;

import ve.net.alge.grpc.base.interim.ContextTemporaryWorkaround;
import ve.net.alge.grpc.pos.service.order.OrderUtil;
import ve.net.alge.grpc.pos.util.ColumnsAdded;
import ve.net.alge.grpc.pos.util.POSConvertUtil;
import ve.net.alge.grpc.service.FileManagement;
import ve.net.alge.grpc.service.core_functionality.CoreFunctionalityConvert;

/**
 * Class for convert any document
 * 
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 */
public class ConvertUtil {
	
	private static CLogger		s_log = CLogger.getCLogger (ConvertUtil.class);

	/**
	 * Convert User entity
	 * 
	 * @param user
	 * @return
	 */
	public static AvailableSeller.Builder convertSeller(MUser user) {
		AvailableSeller.Builder sellerInfo = AvailableSeller.newBuilder();
		if (user == null) {
			return sellerInfo;
		}
		sellerInfo.setId(user.getAD_User_ID()).setName(StringManager.getValidString(user.getName()))
				.setDescription(StringManager.getValidString(user.getDescription()))
				.setComments(StringManager.getValidString(user.getComments()));

		int clientId = Env.getAD_Client_ID(Env.getCtx());
		// TODO core
//		if(user.getLogo_ID() > 0 && AttachmentUtil.getInstance().isValidForClient(clientId)) {
//			MClientInfo clientInfo = MClientInfo.get(Env.getCtx(), clientId);
//			MADAttachmentReference attachmentReference = MADAttachmentReference.getByImageId(
//				Env.getCtx(),
//				clientInfo.getFileHandler_ID(),
//				user.getLogo_ID(),
//				null
//			);
//			if(attachmentReference != null
//					&& attachmentReference.getAD_AttachmentReference_ID() > 0) {
//				sellerInfo.setImage(
//					StringManager.getValidString(
//						attachmentReference.getValidFileName()
//					)
//				);
//			}
//		}
		return sellerInfo;
	}

	/**
	 * Convert ProcessInfoLog to gRPC
	 * 
	 * @param log
	 * @return
	 */
	public static ProcessInfoLog.Builder convertProcessInfoLog(org.compiere.process.ProcessInfoLog log) {
		ProcessInfoLog.Builder processLog = ProcessInfoLog.newBuilder();
		if (log == null) {
			return processLog;
		}
		processLog.setRecordId(log.getP_ID())
				.setLog(StringManager.getValidString(Msg.parseTranslation(Env.getCtx(), log.getP_Msg())));
		return processLog;
	}

	/**
	 * Convert PO class from Chat Entry process to builder
	 * 
	 * @param chatEntry
	 * @return
	 */
	public static ChatEntry.Builder convertChatEntry(MChatEntry chatEntry) {
		ChatEntry.Builder builder = ChatEntry.newBuilder();
		if (chatEntry == null) {
			return builder;
		}
		builder.setId(chatEntry.getCM_ChatEntry_ID()).setChatId(chatEntry.getCM_Chat_ID())
				.setSubject(StringManager.getValidString(chatEntry.getSubject()))
				.setCharacterData(StringManager.getValidString(chatEntry.getCharacterData()));

		if (chatEntry.getAD_User_ID() > 0) {
			MUser user = MUser.get(chatEntry.getCtx(), chatEntry.getAD_User_ID());
			builder.setUserId(chatEntry.getAD_User_ID()).setUserName(StringManager.getValidString(user.getName()));
		}

		builder.setLogDate(ValueManager.getTimestampFromDate(chatEntry.getCreated()));
		// Confidential Type
		if (!Util.isEmpty(chatEntry.getConfidentialType())) {
			if (chatEntry.getConfidentialType().equals(MChatEntry.CONFIDENTIALTYPE_PublicInformation)) {
				builder.setConfidentialType(org.spin.backend.grpc.user_interface.ConfidentialType.PUBLIC);
			} else if (chatEntry.getConfidentialType().equals(MChatEntry.CONFIDENTIALTYPE_PartnerConfidential)) {
				builder.setConfidentialType(org.spin.backend.grpc.user_interface.ConfidentialType.PARTER);
			} else if (chatEntry.getConfidentialType().equals(MChatEntry.CONFIDENTIALTYPE_Internal)) {
				builder.setConfidentialType(org.spin.backend.grpc.user_interface.ConfidentialType.INTERNAL);
			}
		}
		// Moderator Status
		if (!Util.isEmpty(chatEntry.getModeratorStatus())) {
			if (chatEntry.getModeratorStatus().equals(MChatEntry.MODERATORSTATUS_NotDisplayed)) {
				builder.setModeratorStatus(ModeratorStatus.NOT_DISPLAYED);
			} else if (chatEntry.getModeratorStatus().equals(MChatEntry.MODERATORSTATUS_Published)) {
				builder.setModeratorStatus(ModeratorStatus.PUBLISHED);
			} else if (chatEntry.getModeratorStatus().equals(MChatEntry.MODERATORSTATUS_Suspicious)) {
				builder.setModeratorStatus(ModeratorStatus.SUSPICIUS);
			} else if (chatEntry.getModeratorStatus().equals(MChatEntry.MODERATORSTATUS_ToBeReviewed)) {
				builder.setModeratorStatus(ModeratorStatus.TO_BE_REVIEWED);
			}
		}
		// Chat entry type
		if (!Util.isEmpty(chatEntry.getChatEntryType())) {
			if (chatEntry.getChatEntryType().equals(MChatEntry.CHATENTRYTYPE_NoteFlat)) {
				builder.setChatEntryType(org.spin.backend.grpc.user_interface.ChatEntryType.NOTE_FLAT);
			} else if (chatEntry.getChatEntryType().equals(MChatEntry.CHATENTRYTYPE_ForumThreaded)) {
				builder.setChatEntryType(org.spin.backend.grpc.user_interface.ChatEntryType.NOTE_FLAT);
			} else if (chatEntry.getChatEntryType().equals(MChatEntry.CHATENTRYTYPE_Wiki)) {
				builder.setChatEntryType(org.spin.backend.grpc.user_interface.ChatEntryType.NOTE_FLAT);
			}
		}
		return builder;
	}

	/**
	 * Convert PO to Value Object
	 * 
	 * @param entity
	 * @return
	 */
	public static Entity.Builder convertEntity(PO entity) {
		Entity.Builder entityBuilder = Entity.newBuilder();
		if (entity == null) {
			return entityBuilder;
		}

		final int identifier = entity.get_ID();
		entityBuilder.setId(identifier);

		final String uuid = entity.get_UUID();
		entityBuilder.setUuid(StringManager.getValidString(uuid));

		// Convert attributes
		POInfo poInfo = POInfo.getPOInfo(Env.getCtx(), entity.get_Table_ID());
		entityBuilder.setTableName(StringManager.getValidString(poInfo.getTableName()));

		Struct.Builder rowValues = Struct.newBuilder();
		for (int index = 0; index < poInfo.getColumnCount(); index++) {
			String columnName = poInfo.getColumnName(index);
			int displayTypeId = poInfo.getColumnDisplayType(index);
			Object value = entity.get_Value(index);
			Value.Builder builderValue = ValueManager.getValueFromReference(value, displayTypeId);
			// add value
			rowValues.putFields(columnName, builderValue.build());

			// add display value
			if (value != null) {
				String displayValue = null;
				if (columnName.equals(poInfo.getTableName() + "_ID")) {
					displayValue = POUtils.getDisplayValue(entity);
				} else if (ReferenceUtil.validateReference(displayTypeId) || displayTypeId == DisplayType.Button) {
					int referenceValueId = MColumn.get(Env.getCtx(), poInfo.getTableName(), columnName).getAD_Reference_Value_ID();
					displayTypeId = ReferenceUtil.overwriteDisplayType(
						displayTypeId,
						referenceValueId
					);
					String tableName = null;
					if(displayTypeId == DisplayType.TableDir) {
						tableName = columnName.replace("_ID", "");
					} else if(displayTypeId == DisplayType.Table || displayTypeId == DisplayType.Search) {
						if(referenceValueId <= 0) {
							tableName = columnName.replace("_ID", "");
						} else {
							MRefTable referenceTable = MRefTable.get(Env.getCtx(), referenceValueId);
							tableName = MTable.getTableName(Env.getCtx(), referenceTable.getAD_Table_ID());
						}
					}
					if (!Util.isEmpty(tableName, true)) {
						int id = NumberManager.getIntegerFromObject(value);
						MTable referenceTable = MTable.get(Env.getCtx(), tableName);
						PO referenceEntity = referenceTable.getPO(id, null);
						if(referenceEntity != null) {
							displayValue = POUtils.getDisplayValue(referenceEntity);
						}
					}
				}
				Value.Builder builderDisplayValue = ValueManager.getValueFromString(displayValue);
				rowValues.putFields(
					LookupUtil.DISPLAY_COLUMN_KEY + "_" + columnName,
					builderDisplayValue.build()
				);
			}

			// to add client uuid by record
			if (columnName.equals(I_AD_Element.COLUMNNAME_AD_Client_ID)) {
				final int clientId = NumberManager.getIntegerFromObject(value);
				MClient clientEntity = MClient.get(entity.getCtx(), clientId);
				if (clientEntity != null) {
					final String clientUuid = clientEntity.get_UUID();
					Value.Builder valueUuidBuilder = ValueManager.getValueFromString(clientUuid);
					rowValues.putFields(LookupUtil.get_UUIDColumnName(I_AD_Element.COLUMNNAME_AD_Client_ID),
							valueUuidBuilder.build());
				}
			} else if (columnName.equals(I_AD_ChangeLog.COLUMNNAME_Record_ID)) {
				if (entity.get_ColumnIndex(I_AD_Table.COLUMNNAME_AD_Table_ID) >= 0) {
					MTable tableRow = MTable.get(entity.getCtx(),
							entity.get_ValueAsInt(I_AD_Table.COLUMNNAME_AD_Table_ID));
					if (tableRow != null) {
						PO entityRow = tableRow.getPO(entity.get_ValueAsInt(I_AD_ChangeLog.COLUMNNAME_Record_ID), null);
						if (entityRow != null) {
							final String recordIdDisplayValue = POUtils.getDisplayValue(entityRow);
							Value.Builder recordIdDisplayBuilder = ValueManager.getValueFromString(
								recordIdDisplayValue
							);
							rowValues.putFields(
								LookupUtil.getDisplayColumnName(
									I_AD_ChangeLog.COLUMNNAME_Record_ID
								),
								recordIdDisplayBuilder.build()
							);
						}

					}
				}
			}
		}

		// TODO: Temporary Workaround
		rowValues = ContextTemporaryWorkaround.setContextAsUnknowColumn(poInfo.getTableName(), rowValues);

		entityBuilder.setValues(rowValues);
		//
		return entityBuilder;
	}

	/**
	 * Convert Document Action
	 * 
	 * @param value
	 * @param name
	 * @param description
	 * @return
	 */
	public static DocumentAction.Builder convertDocumentAction(String value, String name, String description) {
		return DocumentAction.newBuilder().setValue(StringManager.getValidString(value))
				.setName(StringManager.getValidString(name)).setDescription(StringManager.getValidString(description));
	}

	/**
	 * Convert Document Status
	 * 
	 * @param value
	 * @param name
	 * @param description
	 * @return
	 */
	public static DocumentStatus.Builder convertDocumentStatus(String value, String name, String description) {
		return DocumentStatus.newBuilder().setValue(StringManager.getValidString(value))
				.setName(StringManager.getValidString(name)).setDescription(StringManager.getValidString(description));
	}

	/**
	 * Convert RMA
	 * 
	 * @param order
	 * @return
	 */
	public static RMA.Builder convertRMA(MOrder order) {
		RMA.Builder builder = RMA.newBuilder();
		if (order == null) {
			return builder;
		}
		MPOS pos = new MPOS(Env.getCtx(), order.getC_POS_ID(), order.get_TrxName());
		int defaultDiscountChargeId = pos.get_ValueAsInt("DefaultDiscountCharge_ID");
		MRefList reference = MRefList.get(Env.getCtx(), SystemIDs.REFERENCE_DOCUMENTSTATUS, order.getDocStatus(), null);
		MPriceList priceList = MPriceList.get(Env.getCtx(), order.getM_PriceList_ID(), order.get_TrxName());
		List<MOrderLine> orderLines = Arrays.asList(order.getLines());
		BigDecimal totalLines = orderLines.stream().filter(
				orderLine -> orderLine.getC_Charge_ID() != defaultDiscountChargeId || defaultDiscountChargeId == 0)
				.map(orderLine -> Optional.ofNullable(orderLine.getLineNetAmt()).orElse(Env.ZERO))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal discountAmount = orderLines.stream().filter(
				orderLine -> orderLine.getC_Charge_ID() > 0 && orderLine.getC_Charge_ID() == defaultDiscountChargeId)
				.map(orderLine -> Optional.ofNullable(orderLine.getLineNetAmt()).orElse(Env.ZERO))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal lineDiscountAmount = orderLines.stream().filter(
				orderLine -> orderLine.getC_Charge_ID() != defaultDiscountChargeId || defaultDiscountChargeId == 0)
				.map(orderLine -> {
					BigDecimal priceActualAmount = Optional.ofNullable(orderLine.getPriceActual()).orElse(Env.ZERO);
					BigDecimal priceListAmount = Optional.ofNullable(orderLine.getPriceList()).orElse(Env.ZERO);
					BigDecimal discountLine = priceListAmount.subtract(priceActualAmount)
							.multiply(Optional.ofNullable(orderLine.getQtyOrdered()).orElse(Env.ZERO));
					return discountLine;
				}).reduce(BigDecimal.ZERO, BigDecimal::add);
		//
		BigDecimal totalDiscountAmount = discountAmount.add(lineDiscountAmount);

		//
		Optional<BigDecimal> paidAmount = new Query(order.getCtx(), MPayment.Table_Name,
				MOrder.COLUMNNAME_C_Order_ID + "=?", order.get_TrxName()).setClient_ID()
				.setParameters(order.getC_Order_ID()).list().stream().map(pay -> {
					MPayment payment = (MPayment) pay;
					BigDecimal paymentAmount = payment.getPayAmt();
					if (paymentAmount.compareTo(Env.ZERO) == 0
//							&& payment.getTenderType().equals(MPayment.TENDERTYPE_CreditMemo)// TODO core
							) {
						MInvoice creditMemo = new Query(payment.getCtx(), MInvoice.Table_Name, "C_Payment_ID = ?",
								payment.get_TrxName()).setParameters(payment.getC_Payment_ID()).first();
						if (creditMemo != null) {
							paymentAmount = creditMemo.getGrandTotal();
						}
					}
					if (!payment.isReceipt()) {
						paymentAmount = payment.getPayAmt().negate();
					}
					return getConvetedAmount(order, payment, paymentAmount);
				}).collect(Collectors.reducing(BigDecimal::add));

		List<PO> paymentReferencesList = OrderUtil.getPaymentReferencesList(order);
		Optional<BigDecimal> paymentReferenceAmount = paymentReferencesList.stream().map(paymentReference -> {
			BigDecimal amount = ((BigDecimal) paymentReference.get_Value("Amount"));
			if (paymentReference.get_ValueAsBoolean("IsReceipt")) {
				amount = amount.negate();
			}
			return getConvetedAmount(order, paymentReference, amount);
		}).collect(Collectors.reducing(BigDecimal::add));
		BigDecimal grandTotal = order.getGrandTotal();
		BigDecimal paymentAmount = Env.ZERO;
		if (paidAmount.isPresent()) {
			paymentAmount = paidAmount.get();
		}

		int standardPrecision = priceList.getStandardPrecision();

		BigDecimal creditAmt = BigDecimal.ZERO.setScale(standardPrecision, RoundingMode.HALF_UP);
		Optional<BigDecimal> maybeCreditAmt = OrderUtil.getPaymentChageOrCredit(order, true);
		if (maybeCreditAmt.isPresent()) {
			creditAmt = maybeCreditAmt.get().setScale(standardPrecision, RoundingMode.HALF_UP);
		}
		BigDecimal chargeAmt = BigDecimal.ZERO.setScale(standardPrecision, RoundingMode.HALF_UP);
		Optional<BigDecimal> maybeChargeAmt = OrderUtil.getPaymentChageOrCredit(order, false);
		if (maybeChargeAmt.isPresent()) {
			chargeAmt = maybeChargeAmt.get().setScale(standardPrecision, RoundingMode.HALF_UP);
		}

		BigDecimal totalPaymentAmount = paymentAmount;
		if (paymentReferenceAmount.isPresent()) {
			totalPaymentAmount = totalPaymentAmount.subtract(paymentReferenceAmount.get());
		}

		BigDecimal openAmount = (grandTotal.subtract(totalPaymentAmount).compareTo(Env.ZERO) < 0 ? Env.ZERO
				: grandTotal.subtract(totalPaymentAmount));
		BigDecimal refundAmount = (grandTotal.subtract(totalPaymentAmount).compareTo(Env.ZERO) > 0 ? Env.ZERO
				: grandTotal.subtract(totalPaymentAmount).negate());
		BigDecimal displayCurrencyRate = getDisplayConversionRateFromOrder(order);
		// Convert
		return builder.setId(order.getC_Order_ID()).setSourceOrderId(order.getRef_Order_ID())
				.setDocumentType(CoreFunctionalityConvert.convertDocumentType(order.getC_DocTypeTarget_ID()))
				.setDocumentNo(StringManager.getValidString(order.getDocumentNo()))
				.setSalesRepresentative(CoreFunctionalityConvert
						.convertSalesRepresentative(MUser.get(Env.getCtx(), order.getSalesRep_ID())))
				.setDescription(StringManager.getValidString(order.getDescription()))
				.setOrderReference(StringManager.getValidString(order.getPOReference()))
				.setDocumentStatus(ConvertUtil.convertDocumentStatus(StringManager.getValidString(order.getDocStatus()),
						StringManager
								.getValidString(ValueManager.getTranslation(reference, I_AD_Ref_List.COLUMNNAME_Name)),
						StringManager.getValidString(
								ValueManager.getTranslation(reference, I_AD_Ref_List.COLUMNNAME_Description))))
				.setPriceList(CoreFunctionalityConvert
						.convertPriceList(MPriceList.get(Env.getCtx(), order.getM_PriceList_ID(), order.get_TrxName())))
				.setWarehouse(CoreFunctionalityConvert.convertWarehouse(order.getM_Warehouse_ID()))
				.setIsDelivered(order.isDelivered())
				.setDiscountAmount(NumberManager.getBigDecimalToString(Optional.ofNullable(totalDiscountAmount)
						.orElse(Env.ZERO).setScale(priceList.getStandardPrecision(), RoundingMode.HALF_UP)))
				.setTaxAmount(NumberManager.getBigDecimalToString(grandTotal.subtract(totalLines.add(discountAmount))
						.setScale(priceList.getStandardPrecision(), RoundingMode.HALF_UP)))
				.setTotalLines(NumberManager.getBigDecimalToString(totalLines.add(totalDiscountAmount)
						.setScale(priceList.getStandardPrecision(), RoundingMode.HALF_UP)))
				.setGrandTotal(NumberManager.getBigDecimalToString(
						grandTotal.setScale(priceList.getStandardPrecision(), RoundingMode.HALF_UP)))
				.setDisplayCurrencyRate(NumberManager.getBigDecimalToString(
						displayCurrencyRate.setScale(priceList.getStandardPrecision(), RoundingMode.HALF_UP)))
				.setPaymentAmount(NumberManager.getBigDecimalToString(
						paymentAmount.setScale(priceList.getStandardPrecision(), RoundingMode.HALF_UP)))
				.setOpenAmount(NumberManager.getBigDecimalToString(
						openAmount.setScale(priceList.getStandardPrecision(), RoundingMode.HALF_UP)))
				.setRefundAmount(NumberManager.getBigDecimalToString(
						refundAmount.setScale(priceList.getStandardPrecision(), RoundingMode.HALF_UP)))
				.setDateOrdered(ValueManager.getTimestampFromDate(order.getDateOrdered()))
				.setCustomer(POSConvertUtil.convertCustomer((MBPartner) order.getC_BPartner()))
				.setCampaign(POSConvertUtil.convertCampaign(order.getC_Campaign_ID()))
				.setChargeAmount(NumberManager.getBigDecimalToString(chargeAmt))
				.setCreditAmount(NumberManager.getBigDecimalToString(creditAmt));
	}

	/**
	 * Get Converted Amount based on Order currency
	 * 
	 * @param order
	 * @param payment
	 * @return
	 * @return BigDecimal
	 */
	public static BigDecimal getConvetedAmount(MOrder order, PO payment, BigDecimal amount) {
		if (payment.get_ValueAsInt("C_Currency_ID") == order.getC_Currency_ID() || amount == null
				|| amount.compareTo(Env.ZERO) == 0) {
			return amount;
		}
		BigDecimal convertedAmount = MConversionRate.convert(payment.getCtx(), amount,
				payment.get_ValueAsInt("C_Currency_ID"), order.getC_Currency_ID(), order.getDateAcct(),
				payment.get_ValueAsInt("C_ConversionType_ID"), payment.getAD_Client_ID(), payment.getAD_Org_ID());
		//
		return Optional.ofNullable(convertedAmount).orElse(Env.ZERO);
	}

	/**
	 * Get Converted Amount based on Order currency
	 * 
	 * @param pos
	 * @param payment
	 * @return
	 * @return BigDecimal
	 */
	public static BigDecimal getConvetedAmount(MPOS pos, MPayment payment, BigDecimal amount) {
		MPriceList priceList = MPriceList.get(pos.getCtx(), pos.getM_PriceList_ID(), null);
		if (payment.getC_Currency_ID() == priceList.getC_Currency_ID() || amount == null
				|| amount.compareTo(Env.ZERO) == 0) {
			return amount;
		}
		BigDecimal convertedAmount = MConversionRate.convert(pos.getCtx(), amount, payment.getC_Currency_ID(),
				priceList.getC_Currency_ID(), payment.getDateAcct(), payment.getC_ConversionType_ID(),
				payment.getAD_Client_ID(), payment.getAD_Org_ID());
		//
		return Optional.ofNullable(convertedAmount).orElse(Env.ZERO);
	}

	/**
	 * Get Converted Amount based on Order currency
	 * 
	 * @param order
	 * @param payment
	 * @return
	 * @return BigDecimal
	 */
	private static BigDecimal getConvetedAmount(MOrder order, MPayment payment, BigDecimal amount) {
		if (payment.getC_Currency_ID() == order.getC_Currency_ID() || amount == null
				|| amount.compareTo(Env.ZERO) == 0) {
			return amount;
		}
		BigDecimal convertedAmount = MConversionRate.convert(payment.getCtx(), amount, payment.getC_Currency_ID(),
				order.getC_Currency_ID(), payment.getDateAcct(), payment.getC_ConversionType_ID(),
				payment.getAD_Client_ID(), payment.getAD_Org_ID());
		//
		return Optional.ofNullable(convertedAmount).orElse(Env.ZERO);
	}

	/**
	 * Get Display Currency rate from Sales Order
	 * 
	 * @param order
	 * @return
	 * @return BigDecimal
	 */
	public static BigDecimal getDisplayConversionRateFromOrder(MOrder order) {
		MPOS pos = MPOS.get(order.getCtx(), order.getC_POS_ID());
		if (order.getC_Currency_ID() == pos.get_ValueAsInt("DisplayCurrency_ID")
				|| pos.get_ValueAsInt("DisplayCurrency_ID") <= 0) {
			return Env.ONE;
		}
		BigDecimal conversionRate = MConversionRate.getRate(order.getC_Currency_ID(),
				pos.get_ValueAsInt("DisplayCurrency_ID"), order.getDateAcct(), order.getC_ConversionType_ID(),
				order.getAD_Client_ID(), order.getAD_Org_ID());
		//
		return Optional.ofNullable(conversionRate).orElse(Env.ZERO);
	}

	/**
	 * Get Order conversion rate for payment
	 * 
	 * @param payment
	 * @return
	 * @return BigDecimal
	 */
	public static BigDecimal getOrderConversionRateFromPaymentReference(PO paymentReference) {
		if (paymentReference.get_ValueAsInt("C_Order_ID") <= 0) {
			return Env.ONE;
		}
		MOrder order = new MOrder(Env.getCtx(), paymentReference.get_ValueAsInt("C_Order_ID"), null);
		if (paymentReference.get_ValueAsInt("C_Currency_ID") == order.getC_Currency_ID()) {
			return Env.ONE;
		}

		Timestamp conversionDate = Timestamp.valueOf(paymentReference.get_ValueAsString("PayDate"));
		BigDecimal conversionRate = MConversionRate.getRate(paymentReference.get_ValueAsInt("C_Currency_ID"),
				order.getC_Currency_ID(), conversionDate, paymentReference.get_ValueAsInt("C_ConversionType_ID"),
				paymentReference.getAD_Client_ID(), paymentReference.getAD_Org_ID());
		//
		return Optional.ofNullable(conversionRate).orElse(Env.ZERO);
	}

	/**
	 * Validate conversion
	 * 
	 * @param order
	 * @param currencyId
	 * @param conversionTypeId
	 * @param transactionDate
	 */
	public static void validateConversion(MOrder order, int currencyId, int conversionTypeId,
			Timestamp transactionDate) {
		if (currencyId == order.getC_Currency_ID()) {
			return;
		}
		int convertionRateId = getConversionRateId(currencyId, order.getC_Currency_ID(),
				transactionDate, conversionTypeId, order.getAD_Client_ID(), order.getAD_Org_ID());
		if (convertionRateId == -1) {
			String error = getErrorMessage(order.getCtx(),
					"ErrorConvertingDocumentCurrencyToBaseCurrency", currencyId, order.getC_Currency_ID(),
					conversionTypeId, transactionDate, null);
			throw new AdempiereException(error);
		}
	}
	
	/** Return the message to show when no exchange rate is found */
	public static String getErrorMessage(Properties ctx, String adMessage, int currencyFromID, int currencyToID, int convertionTypeId, Timestamp date, String trxName)
	{
		if (convertionTypeId == 0)
			convertionTypeId = MConversionType.getDefault(Env.getAD_Client_ID(ctx));
		String retValue = Msg.getMsg(ctx, adMessage,
				new Object[] {MCurrency.get(ctx, currencyFromID).getISO_Code(), MCurrency.get(ctx, currencyToID).getISO_Code(), new MConversionType(ctx, convertionTypeId, trxName).getName(), date});
		return retValue;
	}

	
	/**
	 * Get Conversion Rate ID from Currency
	 * @param currencyFromId
	 * @param CurencyToId
	 * @param conversionDate
	 * @param conversionTypeId
	 * @param clientId
	 * @param organizationId
	 * @return
	 */
	public static int getConversionRateId(int currencyFromId, int CurencyToId, Timestamp conversionDate, int conversionTypeId, int clientId, int organizationId) {
		if (currencyFromId == CurencyToId) {
			return 0;
		}
		//	Conversion Type
		int internalConversionTypeId = conversionTypeId;
		if (internalConversionTypeId == 0) {
			internalConversionTypeId = MConversionType.getDefault(clientId);
		}
		//	Conversion Date
		if (conversionDate == null) {
			conversionDate = new Timestamp (System.currentTimeMillis());
		}
		//	Get Rate
		String sql = "SELECT C_Conversion_Rate_ID "
				+ "FROM C_Conversion_Rate "
				+ "WHERE C_Currency_ID=?"					//	#1
				+ " AND C_Currency_ID_To=?"					//	#2
				+ " AND	C_ConversionType_ID=?"				//	#3
				+ " AND	? >= ValidFrom"						//	#4
				+ " AND	? <= ValidTo"						//	#5
				+ " AND AD_Client_ID IN (0,?)"				//	#6
				+ " AND AD_Org_ID IN (0,?) "				//	#7
				+ " AND IsActive = 'Y' "					//	#8
				+ "ORDER BY AD_Client_ID DESC, AD_Org_ID DESC, ValidFrom DESC";
		//	Get
		int conversionRateId = DB.getSQLValue(null, sql, currencyFromId, CurencyToId, internalConversionTypeId, conversionDate, conversionDate, clientId, organizationId);
		//	Show Log
		if (conversionRateId == -1) {
			s_log.info ("getRate - not found - CurFrom=" + currencyFromId 
						  + ", CurTo=" + CurencyToId
						  + ", " + conversionDate 
						  + ", Type=" + conversionTypeId + (conversionTypeId==internalConversionTypeId ? "" : "->" + internalConversionTypeId) 
						  + ", Client=" + clientId 
						  + ", Org=" + organizationId);
		}
		//	Return
		return conversionRateId;
	}	//	getConversionRateId
	/**
	 * Convert customer bank account
	 * 
	 * @param customerBankAccount
	 * @return
	 * @return CustomerBankAccount.Builder
	 */
	public static CustomerBankAccount.Builder convertCustomerBankAccount(MBPBankAccount customerBankAccount) {
		CustomerBankAccount.Builder builder = CustomerBankAccount.newBuilder();
		if (customerBankAccount == null) {
			return builder;
		}
		builder.setId(customerBankAccount.getC_BP_BankAccount_ID())
				.setCity(StringManager.getValidString(customerBankAccount.getA_City()))
				.setCountry(StringManager.getValidString(customerBankAccount.getA_Country()))
				.setEmail(StringManager.getValidString(customerBankAccount.getA_EMail()))
				.setDriverLicense(StringManager.getValidString(customerBankAccount.getA_Ident_DL()))
				.setSocialSecurityNumber(StringManager.getValidString(customerBankAccount.getA_Ident_SSN()))
				.setName(StringManager.getValidString(customerBankAccount.getA_Name()))
				.setState(StringManager.getValidString(customerBankAccount.getA_State()))
				.setStreet(StringManager.getValidString(customerBankAccount.getA_Street()))
				.setZip(StringManager.getValidString(customerBankAccount.getA_Zip()))
				.setBankAccountType(StringManager.getValidString(customerBankAccount.getBankAccountType()));
		if (customerBankAccount.getC_Bank_ID() > 0) {
			builder.setBankId(customerBankAccount.getC_Bank_ID());
		}
		MBPartner customer = MBPartner.get(Env.getCtx(), customerBankAccount.getC_BPartner_ID());
		builder.setCustomerId(customer.getC_BPartner_ID())
				.setAddressVerified(StringManager.getValidString(customerBankAccount.getR_AvsAddr()))
				.setZipVerified(StringManager.getValidString(customerBankAccount.getR_AvsZip()))
				.setRoutingNo(StringManager.getValidString(customerBankAccount.getRoutingNo()))
				.setAccountNo(StringManager.getValidString(customerBankAccount.getAccountNo()))
				.setIban(StringManager.getValidString(customerBankAccount.getIBAN()));
		return builder;
	}

	/**
	 * Convert Order from entity
	 * 
	 * @param shipment
	 * @return
	 */
	public static Shipment.Builder convertShipment(MInOut shipment) {
		Shipment.Builder builder = Shipment.newBuilder();
		if (shipment == null) {
			return builder;
		}
		MRefList reference = MRefList.get(Env.getCtx(), SystemIDs.REFERENCE_DOCUMENTSTATUS, shipment.getDocStatus(),
				null);
		MOrder order = (MOrder) shipment.getC_Order();
		// Convert
		return builder.setOrderId(order.getC_Order_ID()).setId(shipment.getM_InOut_ID())
				.setDocumentType(CoreFunctionalityConvert.convertDocumentType(shipment.getC_DocType_ID()))
				.setDocumentNo(StringManager.getValidString(shipment.getDocumentNo()))
				.setSalesRepresentative(CoreFunctionalityConvert
						.convertSalesRepresentative(MUser.get(Env.getCtx(), shipment.getSalesRep_ID())))
				.setDocumentStatus(
						ConvertUtil.convertDocumentStatus(StringManager.getValidString(shipment.getDocStatus()),
								StringManager.getValidString(
										ValueManager.getTranslation(reference, I_AD_Ref_List.COLUMNNAME_Name)),
								StringManager.getValidString(
										ValueManager.getTranslation(reference, I_AD_Ref_List.COLUMNNAME_Description))))
				.setWarehouse(CoreFunctionalityConvert.convertWarehouse(shipment.getM_Warehouse_ID()))
				.setMovementDate(ValueManager.getTimestampFromDate(shipment.getMovementDate()));
	}

	public static RMALine.Builder convertRMALine(MOrderLine orderLine) {
		RMALine.Builder builder = RMALine.newBuilder();
		if (orderLine == null) {
			return builder;
		}
		MTax tax = MTax.get(Env.getCtx(), orderLine.getC_Tax_ID());
		MOrder order = orderLine.getParent();
		MPriceList priceList = MPriceList.get(Env.getCtx(), order.getM_PriceList_ID(), order.get_TrxName());
		boolean isTaxIncluded = priceList.isTaxIncluded();
		BigDecimal quantityEntered = orderLine.getQtyEntered();
		BigDecimal quantityOrdered = orderLine.getQtyOrdered();
		// Units
		BigDecimal priceListAmount = orderLine.getPriceList();
		BigDecimal priceBaseAmount = orderLine.getPriceActual();
		BigDecimal priceAmount = orderLine.getPriceEntered();
		// Discount
		BigDecimal discountRate = orderLine.getDiscount();
		BigDecimal discountAmount = Optional.ofNullable(orderLine.getPriceList()).orElse(Env.ZERO)
				.subtract(Optional.ofNullable(orderLine.getPriceActual()).orElse(Env.ZERO));
		// Taxes
		BigDecimal priceTaxAmount = tax.calculateTax(priceAmount, isTaxIncluded, priceList.getStandardPrecision());
		BigDecimal priceBaseTaxAmount = tax.calculateTax(priceBaseAmount, isTaxIncluded,
				priceList.getStandardPrecision());
		BigDecimal priceListTaxAmount = tax.calculateTax(priceListAmount, isTaxIncluded,
				priceList.getStandardPrecision());
		// Prices with tax
		BigDecimal priceListWithTaxAmount = isTaxIncluded ? priceListAmount : priceListAmount.add(priceListTaxAmount);
		BigDecimal priceBaseWithTaxAmount = isTaxIncluded ? priceBaseAmount : priceBaseAmount.add(priceBaseTaxAmount);
		BigDecimal priceWithTaxAmount = isTaxIncluded ? priceAmount : priceAmount.add(priceTaxAmount);
		// Totals
		BigDecimal totalDiscountAmount = discountAmount.multiply(quantityOrdered);
		BigDecimal totalAmount = orderLine.getLineNetAmt();
		BigDecimal totalBaseAmount = totalAmount.subtract(totalDiscountAmount);
		BigDecimal totalTaxAmount = tax.calculateTax(totalAmount, false, priceList.getStandardPrecision());
		BigDecimal totalBaseAmountWithTax = totalBaseAmount.add(totalTaxAmount);
		BigDecimal totalAmountWithTax = totalAmount.add(totalTaxAmount);
		// Add Tax for Include Tax
		if (isTaxIncluded) {
			totalBaseAmount = totalBaseAmountWithTax;
			totalAmount = totalAmount.add(totalTaxAmount);
		}
		MUOMConversion uom = null;
		MUOMConversion productUom = null;
		if (orderLine.getM_Product_ID() > 0) {
			MProduct product = MProduct.get(Env.getCtx(), orderLine.getM_Product_ID());
			List<MUOMConversion> productsConversion = Arrays
					.asList(MUOMConversion.getProductConversions(Env.getCtx(), product.getM_Product_ID()));
			Optional<MUOMConversion> maybeUom = productsConversion.parallelStream().filter(productConversion -> {
				return productConversion.getC_UOM_To_ID() == orderLine.getC_UOM_ID();
			}).findFirst();
			if (maybeUom.isPresent()) {
				uom = maybeUom.get();
			}

			Optional<MUOMConversion> maybeProductUom = productsConversion.parallelStream().filter(productConversion -> {
				return productConversion.getC_UOM_To_ID() == product.getC_UOM_ID();
			}).findFirst();
			if (maybeProductUom.isPresent()) {
				productUom = maybeProductUom.get();
			}
		} else {
			uom = new MUOMConversion(Env.getCtx(), 0, null);
			uom.setC_UOM_ID(orderLine.getC_UOM_ID());
			uom.setC_UOM_To_ID(orderLine.getC_UOM_ID());
			uom.setMultiplyRate(Env.ONE);
			uom.setDivideRate(Env.ONE);
			productUom = uom;
		}

		int standardPrecision = priceList.getStandardPrecision();
		BigDecimal availableQuantity = MStorage.getQtyAvailable(orderLine.getM_Warehouse_ID(), 0,
				orderLine.getM_Product_ID(), orderLine.getM_AttributeSetInstance_ID(), null);
		// Convert
		return builder.setId(orderLine.getC_OrderLine_ID())
				.setSourceOrderLineId(orderLine.get_ValueAsInt(ColumnsAdded.COLUMNNAME_ECA14_Source_OrderLine_ID))
				.setLine(orderLine.getLine()).setDescription(StringManager.getValidString(orderLine.getDescription()))
				.setLineDescription(StringManager.getValidString(orderLine.getName()))
				.setProduct(CoreFunctionalityConvert.convertProduct(orderLine.getM_Product_ID()))
				.setCharge(CoreFunctionalityConvert.convertCharge(orderLine.getC_Charge_ID()))
				.setWarehouse(CoreFunctionalityConvert.convertWarehouse(orderLine.getM_Warehouse_ID()))
				.setQuantity(NumberManager
						.getBigDecimalToString(quantityEntered.setScale(standardPrecision, RoundingMode.HALF_UP)))
				.setQuantityOrdered(NumberManager
						.getBigDecimalToString(quantityOrdered.setScale(standardPrecision, RoundingMode.HALF_UP)))
				.setAvailableQuantity(NumberManager
						.getBigDecimalToString(availableQuantity.setScale(standardPrecision, RoundingMode.HALF_UP)))
				// Prices
				.setPriceList(NumberManager
						.getBigDecimalToString(priceListAmount.setScale(standardPrecision, RoundingMode.HALF_UP)))
				.setPrice(NumberManager
						.getBigDecimalToString(priceAmount.setScale(standardPrecision, RoundingMode.HALF_UP)))
				.setPriceBase(NumberManager
						.getBigDecimalToString(priceBaseAmount.setScale(standardPrecision, RoundingMode.HALF_UP)))
				// Taxes
				.setPriceListWithTax(NumberManager.getBigDecimalToString(
						priceListWithTaxAmount.setScale(standardPrecision, RoundingMode.HALF_UP)))
				.setPriceBaseWithTax(NumberManager.getBigDecimalToString(
						priceBaseWithTaxAmount.setScale(standardPrecision, RoundingMode.HALF_UP)))
				.setPriceWithTax(NumberManager
						.getBigDecimalToString(priceWithTaxAmount.setScale(standardPrecision, RoundingMode.HALF_UP)))
				// Prices with taxes
				.setListTaxAmount(NumberManager
						.getBigDecimalToString(priceListTaxAmount.setScale(standardPrecision, RoundingMode.HALF_UP)))
				.setTaxAmount(NumberManager
						.getBigDecimalToString(priceTaxAmount.setScale(standardPrecision, RoundingMode.HALF_UP)))
				.setBaseTaxAmount(NumberManager
						.getBigDecimalToString(priceBaseTaxAmount.setScale(standardPrecision, RoundingMode.HALF_UP)))
				// Discounts
				.setDiscountAmount(NumberManager
						.getBigDecimalToString(discountAmount.setScale(standardPrecision, RoundingMode.HALF_UP)))
				.setDiscountRate(NumberManager
						.getBigDecimalToString(discountRate.setScale(standardPrecision, RoundingMode.HALF_UP)))
				.setTaxRate(CoreFunctionalityConvert.convertTaxRate(tax))
				// Totals
				.setTotalDiscountAmount(NumberManager
						.getBigDecimalToString(totalDiscountAmount.setScale(standardPrecision, RoundingMode.HALF_UP)))
				.setTotalTaxAmount(NumberManager
						.getBigDecimalToString(totalTaxAmount.setScale(standardPrecision, RoundingMode.HALF_UP)))
				.setTotalBaseAmount(NumberManager
						.getBigDecimalToString(totalBaseAmount.setScale(standardPrecision, RoundingMode.HALF_UP)))
				.setTotalBaseAmountWithTax(NumberManager.getBigDecimalToString(
						totalBaseAmountWithTax.setScale(standardPrecision, RoundingMode.HALF_UP)))
				.setTotalAmount(NumberManager
						.getBigDecimalToString(totalAmount.setScale(standardPrecision, RoundingMode.HALF_UP)))
				.setTotalAmountWithTax(NumberManager
						.getBigDecimalToString(totalAmountWithTax.setScale(standardPrecision, RoundingMode.HALF_UP)))
				.setUom(CoreFunctionalityConvert.convertProductConversion(uom))
				.setProductUom(CoreFunctionalityConvert.convertProductConversion(productUom));
	}

	/**
	 * Convert key layout from id
	 * 
	 * @param keyLayoutId
	 * @return
	 */
	public static KeyLayout.Builder convertKeyLayout(int keyLayoutId) {
		KeyLayout.Builder builder = KeyLayout.newBuilder();
		if (keyLayoutId <= 0) {
			return builder;
		}
		return convertKeyLayout(MPOSKeyLayout.get(Env.getCtx(), keyLayoutId));
	}

	/**
	 * Convert Key Layout from PO
	 * 
	 * @param keyLayout
	 * @return
	 */
	public static KeyLayout.Builder convertKeyLayout(MPOSKeyLayout keyLayout) {
		KeyLayout.Builder builder = KeyLayout.newBuilder();
		if (keyLayout == null) {
			return builder;
		}
		builder.setId(keyLayout.getC_POSKeyLayout_ID()).setName(StringManager.getValidString(keyLayout.getName()))
				.setDescription(StringManager.getValidString(keyLayout.getDescription()))
				.setHelp(StringManager.getValidString(keyLayout.getHelp()))
				.setLayoutType(StringManager.getValidString(keyLayout.getPOSKeyLayoutType()))
				.setColumns(keyLayout.getColumns());
		// TODO: Color
		// Add keys
		Arrays.asList(keyLayout.getKeys(false)).stream().filter(key -> key.isActive()).forEach(key -> {
			builder.addKeys(convertKey(key));
		});
		return builder;
	}

	/**
	 * Convet key for layout
	 * 
	 * @param key
	 * @return
	 */
	public static Key.Builder convertKey(MPOSKey key) {
		if (key == null) {
			return Key.newBuilder();
		}
		String productValue = null;
		if (key.getM_Product_ID() > 0) {
			productValue = MProduct.get(Env.getCtx(), key.getM_Product_ID()).getValue();
		}
		return Key.newBuilder().setId(key.getC_POSKeyLayout_ID()).setName(StringManager.getValidString(key.getName()))
				// TODO: Color
				.setSequence(key.getSeqNo()).setSpanX(key.getSpanX()).setSpanY(key.getSpanY())
				.setSubKeyLayoutId(key.getSubKeyLayout_ID())
				.setQuantity(NumberManager.getBigDecimalToString(Optional.ofNullable(key.getQty()).orElse(Env.ZERO)))
				.setProductValue(StringManager.getValidString(productValue)).setResourceReference(
						FileManagement.convertResourceReference(FileUtil.getResourceFromImageId(key.getAD_Image_ID())));
	}

}
