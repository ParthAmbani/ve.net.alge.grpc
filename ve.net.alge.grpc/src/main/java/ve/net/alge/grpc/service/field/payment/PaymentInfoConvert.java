/************************************************************************************
 * Copyright (C) 2018-present E.R.P. Consultores y Asociados, C.A.                  *
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
package ve.net.alge.grpc.service.field.payment;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.adempiere.core.domains.models.I_C_Payment;
import org.compiere.model.MPayment;
import org.compiere.util.Env;
import org.spin.backend.grpc.field.payment.PaymentInfo;
import org.spin.service.grpc.util.value.NumberManager;
import org.spin.service.grpc.util.value.StringManager;
import org.spin.service.grpc.util.value.TimeManager;

import ve.net.alge.grpc.util.POUtils;

/**
 * @author Elsio Sanchez elsiosanches@gmail.com, https://github.com/elsiosanchez
 * Service for backend of Payment Info field
 */

public class PaymentInfoConvert {
	public static PaymentInfo.Builder convertPaymentInfo(ResultSet rs) throws SQLException {
		PaymentInfo.Builder builder = PaymentInfo.newBuilder();
		if (rs == null) {
			return builder;
		}

		int paymentId = rs.getInt(
			I_C_Payment.COLUMNNAME_C_Payment_ID
		);

		MPayment payment = new MPayment(Env.getCtx(), paymentId, null);


		builder.setId(
				rs.getInt(
					I_C_Payment.COLUMNNAME_C_Payment_ID
				)
			)
			.setUuid(
				StringManager.getValidString(
					rs.getString(
						I_C_Payment.COLUMNNAME_UUID
					)
				)
			)
			.setDisplayValue(
				StringManager.getValidString(
						POUtils.getDisplayValue(payment)
				)
			)
			.setBankAccount(
				StringManager.getValidString(
					rs.getString(
						"BankAccount"
					)
				)
			)
			.setBusinessPartner(
				StringManager.getValidString(
					rs.getString("BusinessPartner")
				)
			)
			.setDatePayment(
				TimeManager.convertDateToValue(
					rs.getTimestamp(
						I_C_Payment.COLUMNNAME_DateTrx
					)
				)
			)
			.setDocumentNo(
				StringManager.getValidString(
					rs.getString(
						I_C_Payment.COLUMNNAME_DocumentNo
					)
				)
			)
			.setDocumentType(
				StringManager.getValidString(
					rs.getString("DocmentType")
				)
			)
			.setInfoTo(
				StringManager.getValidString(
					rs.getString("InfoTo")
				)
			)
			.setAccountName(
				StringManager.getValidString(
					rs.getString(
						I_C_Payment.COLUMNNAME_A_Name
					)
				)
			)
			.setIsReceipt(
				rs.getBoolean(
					I_C_Payment.COLUMNNAME_IsReceipt
				)
			)
			.setCurrency(
				StringManager.getValidString(
					rs.getString("Currency")
				)
			)
			.setPayAmt(
				NumberManager.getBigDecimalToString(
					rs.getBigDecimal(
						I_C_Payment.COLUMNNAME_PayAmt
					)
				)
			)
			.setConvertedAmount(
				NumberManager.getBigDecimalToString(
					rs.getBigDecimal("currencyBase")
				)
			)
			.setDiscountAmt(
				NumberManager.getBigDecimalToString(
					rs.getBigDecimal(
						I_C_Payment.COLUMNNAME_DiscountAmt
					)
				)
			)
			.setWriteOffAmt(
				NumberManager.getBigDecimalToString(
					rs.getBigDecimal(
						I_C_Payment.COLUMNNAME_WriteOffAmt
					)
				)
			)
			.setIsAllocated(
				rs.getBoolean(
					I_C_Payment.COLUMNNAME_IsAllocated
				)
			)
			.setDocumentStatus(
				StringManager.getValidString(
					payment.getDocStatusName()
				)
			)
		;

		return builder;
	}
}
