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
package ve.net.alge.grpc.service.field.field_management;

import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

import org.adempiere.core.domains.models.I_AD_Tab;
import org.adempiere.core.domains.models.I_AD_Window;
import org.compiere.model.MTab;
import org.compiere.model.MTable;
import org.compiere.model.MWindow;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.spin.backend.grpc.field.DefaultValue;
import org.spin.backend.grpc.field.ZoomWindow;
import org.spin.service.grpc.util.value.NumberManager;
import org.spin.service.grpc.util.value.StringManager;
import org.spin.service.grpc.util.value.ValueManager;

import com.google.protobuf.Struct;

import ve.net.alge.grpc.util.LookupUtil;

public class FieldManagementConvert {
	/**
	 * Convert Values from result
	 * @param keyValue
	 * @param uuidValue
	 * @param value
	 * @param displayValue
	 * @return
	 */
	public static DefaultValue.Builder convertDefaultValue(Object keyValue, String uuidValue, String value, String displayValue, boolean isActive) {
		Struct.Builder values = Struct.newBuilder();
		DefaultValue.Builder builder = DefaultValue.newBuilder()
			.setValues(values)
			.setIsActive(isActive)
		;
		if(keyValue == null) {
			return builder;
		}

		// Key Column
		if(keyValue instanceof Integer) {
			Integer integerValue = NumberManager.getIntegerFromObject(
				keyValue
			);
			builder.setId(integerValue);
			values.putFields(
				LookupUtil.KEY_COLUMN_KEY,
				ValueManager.getValueFromInteger(integerValue).build()
			);
		} else {
			values.putFields(
				LookupUtil.KEY_COLUMN_KEY,
				ValueManager.getValueFromString((String) keyValue).build()
			);
		}
		//	Set Value
		if(!Util.isEmpty(value)) {
			values.putFields(
				LookupUtil.VALUE_COLUMN_KEY,
				ValueManager.getValueFromString(value).build()
			);
		}
		//	Display column
		if(!Util.isEmpty(displayValue)) {
			values.putFields(
				LookupUtil.DISPLAY_COLUMN_KEY,
				ValueManager.getValueFromString(displayValue).build()
			);
		}
		// UUID Value
		values.putFields(
			LookupUtil.UUID_COLUMN_KEY,
			ValueManager.getValueFromString(uuidValue).build()
		);

		builder.setValues(values);
		return builder;
	}



	/**
	 * Convert Zoom Window from ID
	 * @param context
	 * @param windowId
	 * @param tableName
	 * @param isPurchase
	 * @return
	 */
	public static ZoomWindow.Builder convertZoomWindow(Properties context, int windowId, String tableName, boolean isPurchase) {
		String language = Env.getAD_Language(context);
		boolean isBaseLanguage = Env.isBaseLanguage(context, null);

		MWindow window = MWindow.get(context, windowId);
		ZoomWindow.Builder builder = ZoomWindow.newBuilder()
			.setId(
				window.getAD_Window_ID()
			)
			.setUuid(
				StringManager.getValidString(
					window.get_UUID()
				)
			)
			.setName(
				StringManager.getValidString(
					window.getName()
				)
			)
			.setDescription(
				StringManager.getValidString(
					window.getDescription()
				)
			)
			.setIsSalesTransaction(
				window.isSOTrx()
			)
			.setIsPurchase(
				isPurchase
			)
		;
		if (!isBaseLanguage) {
			builder.setName(
				StringManager.getValidString(
						window.get_Translation(
							I_AD_Window.COLUMNNAME_Name,
							language
						)
					)
				)
				.setDescription(
					StringManager.getValidString(
						window.get_Translation(
							I_AD_Window.COLUMNNAME_Description,
							language
						)
					)
				)
			;
		}

		MTable table = MTable.get(context, tableName);
		Optional<MTab> maybeTab = Arrays.asList(
			window.getTabs(false, null)
		)
			.stream().filter(currentTab -> {
				if (!currentTab.isActive()) {
					return false;
				}
				return currentTab.getAD_Table_ID() == table.getAD_Table_ID();
			})
			.findFirst()
		;
		if (maybeTab.isPresent()) {
			MTab tab = maybeTab.get();
			builder.setTabId(
					tab.getAD_Tab_ID()
				)
				.setTabUuid(
					StringManager.getValidString(
						tab.get_UUID()
					)
				)
				.setTabName(
					StringManager.getValidString(
						tab.getName()
					)
				)
				.setIsParentTab(
					tab.getTabLevel() == 0
				)
			;
			if (!isBaseLanguage) {
				builder.setTabName(
					StringManager.getValidString(
						window.get_Translation(
							I_AD_Tab.COLUMNNAME_Name,
							language
						)
					)
				);
			}
		}

		//	Return
		return builder;
	}

}
