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
package ve.net.alge.grpc.service.field.business_partner;

import org.adempiere.core.domains.models.X_C_Greeting;
import org.compiere.model.MBPGroup;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MLocation;
import org.compiere.model.MUser;
import org.compiere.util.Env;
import org.spin.backend.grpc.field.business_partner.BusinessPartnerAddressLocation;
import org.spin.backend.grpc.field.business_partner.BusinessPartnerContact;
import org.spin.backend.grpc.field.business_partner.BusinessPartnerInfo;
import org.spin.service.grpc.util.value.NumberManager;
import org.spin.service.grpc.util.value.StringManager;
import org.spin.service.grpc.util.value.ValueManager;

import ve.net.alge.grpc.util.POUtils;

public class BusinessPartnerConvert {

	public static BusinessPartnerInfo.Builder convertBusinessPartner(int businessPartnerId) {
		BusinessPartnerInfo.Builder builder = BusinessPartnerInfo.newBuilder();
		if (businessPartnerId <= 0) {
			return builder;
		}
		MBPartner businessPartner = MBPartner.get(Env.getCtx(), businessPartnerId);
		return convertBusinessPartner(
			businessPartner
		);
	}

	public static BusinessPartnerInfo.Builder convertBusinessPartner(MBPartner businessPartner) {
		BusinessPartnerInfo.Builder builder = BusinessPartnerInfo.newBuilder();
		if (businessPartner == null || businessPartner.getC_BPartner_ID() <= 0) {
			return builder;
		}
		MBPGroup businessPartnerGroup = MBPGroup.get(Env.getCtx(), businessPartner.getC_BP_Group_ID());

		builder.setId(
				businessPartner.getC_BPartner_ID()
			)
			.setUuid(
				StringManager.getValidString(
					businessPartner.get_UUID()
				)
			)
			.setDisplayValue(
				StringManager.getValidString(
					POUtils.getDisplayValue(businessPartner)
				)
			)
			.setValue(
				StringManager.getValidString(
					businessPartner.getValue()
				)
			)
			.setTaxId(
				StringManager.getValidString(
					businessPartner.getTaxID()
				)
			)
			.setName(
				StringManager.getValidString(
					businessPartner.getName()
				)
			)
			.setName2(
				StringManager.getValidString(
					businessPartner.getName2()
				)
			)
			.setDescription(
				StringManager.getValidString(
					businessPartner.getDescription()
				)
			)
			.setBusinessPartnerGroup(
				StringManager.getValidString(
					businessPartnerGroup.getName()
				)
			)
			.setOpenBalanceAmount(
				NumberManager.getBigDecimalToString(
					businessPartner.getTotalOpenBalance()
				)
			)
			.setCreditAvailableAmount(
				NumberManager.getBigDecimalToString(
					businessPartner.getSO_CreditLimit().subtract(
						businessPartner.getSO_CreditUsed()
					)
				)
			)
			.setCreditUsedAmount(
				NumberManager.getBigDecimalToString(
					businessPartner.getSO_CreditUsed()
				)
			)
			.setRevenueAmount(
				NumberManager.getBigDecimalToString(
					businessPartner.getActualLifeTimeValue()
				)
			)
			.setIsActive(
				businessPartner.isActive()
			)
			.setIsCustomer(
				businessPartner.isCustomer()
			)
			.setIsVendor(
				businessPartner.isVendor()
			)
		;
		return builder;
	}



	public static BusinessPartnerContact.Builder convertBusinessPartnerContact(int businessPartnerContactId) {
		BusinessPartnerContact.Builder builder = BusinessPartnerContact.newBuilder();
		if (businessPartnerContactId <= 0) {
			return builder;
		}

		MUser businessPartnerContact = MUser.get(Env.getCtx(), businessPartnerContactId);
		return convertBusinessPartnerContact(
			businessPartnerContact
		);
	}

	public static BusinessPartnerContact.Builder convertBusinessPartnerContact(MUser businessPartnerContact) {
		BusinessPartnerContact.Builder builder = BusinessPartnerContact.newBuilder();
		if (businessPartnerContact == null || businessPartnerContact.getAD_User_ID() <= 0) {
			return builder;
		}
		String greetingName = null;
		if (businessPartnerContact.getC_Greeting_ID() > 0) {
			X_C_Greeting greating = new X_C_Greeting(Env.getCtx(), businessPartnerContact.getC_Greeting_ID(), null);
			if (greating != null && greating.getC_Greeting_ID() > 0) {
				greetingName = greating.getName();
			}
		}
		String locationName = null;
		if (businessPartnerContact.getC_Location_ID() > 0) {
			MLocation location = MLocation.get(Env.getCtx(), businessPartnerContact.getC_Location_ID(), null);
			if (location != null && location.getC_Location_ID() > 0) {
				locationName = POUtils.getDisplayValue(location);
			}
		}

		builder.setId(
				businessPartnerContact.getAD_User_ID()
			)
			.setUuid(
				StringManager.getValidString(
					businessPartnerContact.get_UUID()
				)
			)
			.setGreeting(
				StringManager.getValidString(
					greetingName
				)
			)
			.setName(
				StringManager.getValidString(
					businessPartnerContact.getName()
				)
			)
			.setTitle(
				StringManager.getValidString(
					businessPartnerContact.getTitle()
				)
			)
			.setAddress(
				StringManager.getValidString(
					locationName
				)
			)
			.setPhone(
				StringManager.getValidString(
					businessPartnerContact.getPhone()
				)
			)
			.setPhone2(
				StringManager.getValidString(
					businessPartnerContact.getPhone2()
				)
			)
			.setFax(
				StringManager.getValidString(
					businessPartnerContact.getFax()
				)
			)
			.setEmail(
				StringManager.getValidString(
					businessPartnerContact.getEMail()
				)
			)
			.setLastContact(
				ValueManager.getTimestampFromDate(
					businessPartnerContact.getLastContact()
				)
			)
			.setLastResult(
				StringManager.getValidString(
					businessPartnerContact.getLastResult()
				)
			)
			.setIsActive(
				businessPartnerContact.isActive()
			)
		;
		return builder;
	}



	public static BusinessPartnerAddressLocation.Builder convertBusinessPartnerLocationAddress(int businessPartnerLocationId) {
		BusinessPartnerAddressLocation.Builder builder = BusinessPartnerAddressLocation.newBuilder();
		if (businessPartnerLocationId <= 0) {
			return builder;
		}

		MBPartnerLocation businessPartnerLocation = new MBPartnerLocation(Env.getCtx(), businessPartnerLocationId, null);
		return convertBusinessPartnerLocationAddress(
			businessPartnerLocation
		);
	}

	public static BusinessPartnerAddressLocation.Builder convertBusinessPartnerLocationAddress(MBPartnerLocation businessPartnerLocation) {
		BusinessPartnerAddressLocation.Builder builder = BusinessPartnerAddressLocation.newBuilder();
		if (businessPartnerLocation == null || businessPartnerLocation.getC_BPartner_Location_ID() <= 0) {
			return builder;
		}
		String locationName = null;
		if (businessPartnerLocation.getC_Location_ID() > 0) {
			MLocation location = MLocation.get(Env.getCtx(), businessPartnerLocation.getC_Location_ID(), null);
			if (location != null && location.getC_Location_ID() > 0) {
				locationName = location.toString();
			}
		}

		builder.setId(
				businessPartnerLocation.getC_BPartner_Location_ID()
			)
			.setUuid(
				StringManager.getValidString(
					businessPartnerLocation.get_UUID()
				)
			)
			.setName(
				StringManager.getValidString(
					businessPartnerLocation.getName()
				)
			)
			// TODO core
//			.setDescription(
//				StringManager.getValidString(
//					businessPartnerLocation.getDescription()
//				)
//			)
			.setPhone(
				StringManager.getValidString(
					businessPartnerLocation.getPhone()
				)
			)
			.setPhone2(
				StringManager.getValidString(
					businessPartnerLocation.getPhone2()
				)
			)
			.setFax(
				StringManager.getValidString(
					businessPartnerLocation.getFax()
				)
			)
			.setAddress(
				StringManager.getValidString(
					locationName
				)
			)
			// TODO core
//			.setMapUrl(
//				StringManager.getValidString(
//					businessPartnerLocation.getMapURL()
//				)
//			)
			.setIsShipToAddress(
				businessPartnerLocation.isShipTo()
			)
			.setIsBillToAddress(
				businessPartnerLocation.isBillTo()
			)
			.setIsRemitToAddress(
				businessPartnerLocation.isRemitTo()
			)
			.setIsPayFormAddress(
				businessPartnerLocation.isPayFrom()
			)
			.setIsActive(
				businessPartnerLocation.isActive()
			)
			// TODO core
//			.setIsDefaultBilling(
//				businessPartnerLocation.isDefaultBilling()
//			)
//			.setIsDefaultShipping(
//				businessPartnerLocation.isDefaultShipping()
//			)
		;
		return builder;
	}

}
