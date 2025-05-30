/*************************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                              *
 * This program is free software; you can redistribute it and/or modify it           *
 * under the terms version 2 or later of the GNU General Public License as published *
 * by the Free Software Foundation. This program is distributed in the hope          *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied        *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                  *
 * See the GNU General Public License for more details.                              *
 * You should have received a copy of the GNU General Public License along           *
 * with this program; if not, write to the Free Software Foundation, Inc.,           *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                            *
 * For the text or an alternative of this public license, you may reach us           *
 * Copyright (C) 2012-2018 E.R.P. Consultores y Asociados, S.A. All Rights Reserved. *
 * Contributor(s): Yamel Senih www.erpya.com                                         *
 *************************************************************************************/
package ve.net.alge.grpc.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.compiere.model.I_AD_Image;
import org.compiere.model.I_AD_Reference;
import org.compiere.model.I_C_Location;
import org.compiere.model.I_C_ValidCombination;
import org.compiere.model.I_M_AttributeSetInstance;
import org.compiere.model.MColumn;
import org.compiere.model.MCountry;
import org.compiere.model.MLookupFactory;
import org.compiere.model.MLookupInfo;
import org.compiere.model.MTable;
import org.compiere.model.MValRule;
import org.compiere.model.X_AD_Reference;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Language;
import org.compiere.util.Util;


/**
 * Class for handle reference for reports, Smart Browsers and other values with Table, List or Table Direct
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 */
public class ReferenceUtil {
	/**	Instance	*/
	private static ReferenceUtil instance = null;
	/**	Context	*/
	private Properties context;
	/**	Local cache	*/
	private Map<String, ReferenceInfo> referenceInfoMap;
	
	public static ReferenceUtil getInstance(Properties context) {
		if(instance == null) {
			instance = new ReferenceUtil(context);
		}
		return instance;
	}
	
	/**
	 * Private constructor
	 * @param context
	 */
	private ReferenceUtil(Properties context) {
		this.context = context;
		referenceInfoMap = new HashMap<String, ReferenceInfo>();
	}

	/**
	 * Validate reference
	 * TODO: Add support to Resource Assigment reference to get display column
	 * @param displayTypeId
	 * @return
	 */
	public static boolean validateReference(int displayTypeId) {
		if (
			DisplayType.isLookup(displayTypeId)
			|| DisplayType.Account == displayTypeId
			|| DisplayType.ID == displayTypeId
			|| DisplayType.Location == displayTypeId
			|| DisplayType.Locator == displayTypeId
			|| DisplayType.PAttribute == displayTypeId
		) {
			return true;
		}
		if(DisplayType.Image == displayTypeId) {
			return AttachmentUtil.getInstance()
				.isValidForClient(
					Env.getAD_Client_ID(Env.getCtx())
				);
		}

		return false;
	}

	public static int overwriteDisplayType(int displayTypeId, int referenceValueId) {
		int newDisplayType = displayTypeId;
		if (DisplayType.Button == displayTypeId) {
			//	Reference Value
			if (referenceValueId > 0) {
				X_AD_Reference reference = new X_AD_Reference(
					Env.getCtx(),
					referenceValueId,
					null
				);
				if (reference != null && reference.getAD_Reference_ID() > 0) {
					// overwrite display type to Table or List
					if (X_AD_Reference.VALIDATIONTYPE_TableValidation.equals(reference.getValidationType())) {
						newDisplayType = DisplayType.Table;
					} else {
						newDisplayType = DisplayType.List;
					}
				}
			}
		} else if (DisplayType.TableDir == displayTypeId) {
			if (referenceValueId > 0) {
				// overwrite display type to Table (as C_DocTypeTarget_ID > C_DocType_ID)
				newDisplayType = DisplayType.Table;
			}
		}
		return newDisplayType;
	}

	/**
	 * Get Reference information, can return null if reference is invalid or not exists
	 * @param referenceId
	 * @param referenceValueId
	 * @param columnName
	 * @param language
	 * @return
	 */
	public ReferenceInfo getReferenceInfo(int referenceId, int referenceValueId, String columnName, String language, String tableName) {
		if(!validateReference(referenceId)) {
			return null;
		}
		final String key = referenceId + "|" + referenceValueId + "|" + columnName + "|" + language;
		ReferenceInfo referenceInfo = referenceInfoMap.get(key);
		Language languageValue = Language.getLanguage(Env.getAD_Language(Env.getCtx()));
		if (referenceInfo != null) {
			// get from cache
			return referenceInfo;
		}

		// new instance generated
		referenceInfo = new ReferenceInfo();
		referenceInfo.setColumnName(columnName);

		if (DisplayType.ID == referenceId) {
			MTable table = MTable.get(context, tableName);
			if (table == null) {
				return null;
			}
			String[] keyColumns = table.getKeyColumns();
			if (keyColumns == null || keyColumns.length > 1) {
				// traslated or accouting table
				return null;
			}

			final String baseColumnToReplace = "REPLACE_COLUMN_TO_REPLACE";
			String displayColumn = MLookupFactory.getLookup_TableDirEmbed(languageValue, columnName, tableName, baseColumnToReplace);
			// No Identifier records
			if (Util.isEmpty(displayColumn, true)) {
				// displayColumn = tableName + "." + columnName;
				return null;
			}
			final String tableAlias = tableName + "_" + columnName;

			String regexMainTable = "(?<from>\\bFROM\\b)\\s+(?<table>" + tableName + "\\b)\\s+"
				+ "(?<restriction>\\bWHERE\\b|\\bORDER\\s+BY\\b|((LEFT|INNER|RIGHT|FULL|SELF|CROSS)\\s+(OUTER\\s+){0,1}){0,1}JOIN)"
			;
			Pattern patternMainTable = Pattern.compile(
				regexMainTable,
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL
			);
			Matcher matcherMainTable = patternMainTable
				.matcher(displayColumn);
			if (matcherMainTable.find()) {
				displayColumn = displayColumn.replaceAll(
					regexMainTable,
					// "FROM " + tableName + " AS " + tableAlias + " " + matcherMainTable.group(3)
					"FROM " + tableName + " AS " + tableAlias + " " + matcherMainTable.group("restriction")
				);
			} else {
				displayColumn = displayColumn.replace(
					"FROM " + tableName,
					"FROM " + tableName + " AS " + tableAlias
				);
			}
			displayColumn = displayColumn.replace(
				tableName + "." + baseColumnToReplace,
				tableAlias + "." + columnName
			);

			referenceInfo.setTableName(tableName);
			referenceInfo.setTableAlias(tableAlias);
			referenceInfo.setDisplayColumnValue("(" + displayColumn + ")");
			referenceInfo.setHasJoinValue(false);
		} else if (DisplayType.Account == referenceId) {
			//	Add Display
			referenceInfo.setTableName(I_C_ValidCombination.Table_Name);
			referenceInfo.setDisplayColumnValue(I_C_ValidCombination.COLUMNNAME_Combination);
			referenceInfo.setTableAlias(I_C_ValidCombination.Table_Name + "_" + columnName);
			referenceInfo.setJoinColumnName(I_C_ValidCombination.COLUMNNAME_C_ValidCombination_ID);
		} else if (DisplayType.PAttribute == referenceId) {
			//  Add Display
			referenceInfo.setTableName(I_M_AttributeSetInstance.Table_Name);
			referenceInfo.setDisplayColumnValue(I_M_AttributeSetInstance.COLUMNNAME_Description);
			referenceInfo.setTableAlias(I_M_AttributeSetInstance.Table_Name + "_" + columnName);
			referenceInfo.setJoinColumnName(I_M_AttributeSetInstance.COLUMNNAME_M_AttributeSetInstance_ID);
		} else if (DisplayType.Image == referenceId) {
			final String displaColumn = getDisplayColumnSQLImage(tableName, columnName);
			referenceInfo.setDisplayColumnValue("(" + displaColumn + ")");
			referenceInfo.setHasJoinValue(false);
		} else if (DisplayType.Location == referenceId) {
			//  Add Display
			referenceInfo.setTableName(I_C_Location.Table_Name);
			final String displaColumn = getDisplayColumnSQLLocation(tableName, columnName);
			referenceInfo.setDisplayColumnValue("(" + displaColumn + ")");
			referenceInfo.setHasJoinValue(false);
		} else if((DisplayType.TableDir == referenceId || (DisplayType.Table == referenceId || DisplayType.Search == referenceId)
			&& columnName.endsWith("_ID")) && referenceValueId <= 0) {
			//	Add Display
			final String displayColumn = MLookupFactory.getLookup_TableDirEmbed(languageValue, columnName, tableName);
			referenceInfo.setDisplayColumnValue("(" + displayColumn + ")");
			referenceInfo.setHasJoinValue(false);
		} else {
			//	Get info
			// TODO needToAddSecurity is not in Idempiere core 
			MLookupInfo lookupInfo = MLookupFactory.getLookupInfo(
				context, 0, 0,
				referenceId, languageValue,
				columnName, referenceValueId,
				false, null
			);
			if(lookupInfo == null) {
				// Table Reference without table validation
				return null;
			}

			String displayColumn = "";
			if (!Util.isEmpty(lookupInfo.DisplayColumn, true)) {
				displayColumn = (lookupInfo.DisplayColumn).replace(lookupInfo.TableName + ".", "");
			} else {
				// Parent recursive columns
				displayColumn = "(" + MLookupFactory.getLookup_TableEmbed(languageValue, columnName, tableName, referenceValueId) + ")";
				referenceInfo.setDisplayColumnValue(displayColumn);
				referenceInfo.setHasJoinValue(false);
				return referenceInfo;
			}
			if (!Util.isEmpty(displayColumn)) {
				referenceInfo.setDisplayColumnValue(displayColumn);
			}
			referenceInfo.setJoinColumnName((lookupInfo.KeyColumn == null ? "" : lookupInfo.KeyColumn).replace(lookupInfo.TableName + ".", ""));
			referenceInfo.setTableName(lookupInfo.TableName);
			if(DisplayType.List == referenceId
					&& referenceValueId != 0) {
				referenceInfo.setReferenceId(referenceValueId);
			}
			//	Translate
			if (!Util.isEmpty(displayColumn, true) 
//					&& MTable.hasTranslation(lookupInfo.TableName)// TODO core
					) {
				// display column exists on translation table
				int columnId = MColumn.getColumn_ID(lookupInfo.TableName + DictionaryUtil.TRANSLATION_SUFFIX, displayColumn);
				if (columnId > 0) {
					referenceInfo.setLanguage(language);
				}
			}
		}

		return referenceInfo;
	}



	public static String getDisplayColumnSQLImage(String tableName, String columnName) {
		StringBuffer query = new StringBuffer()
			.append("SELECT ")
			.append("NVL(UUID, '')")
			.append(" || '-' || ")
			.append("NVL(FileName, '') ")
			.append("FROM AD_AttachmentReference ")
			.append("WHERE AD_Image_ID = ")
			.append(tableName + "." + columnName)
			// .append(" LIMIT 1")
			.append(" AND ROWNUM = 1 ")
		;
		return query.toString();
	}

	public static String getQueryColumnSQLImage() {
		StringBuffer query = new StringBuffer()
			.append("SELECT ")
			.append("AD_AttachmentReference.AD_AttachmentReference_ID, NULL, ")
			.append("NVL(AD_AttachmentReference.UUID, '')")
			.append(" || '-' || ")
			.append("NVL(AD_AttachmentReference.FileName, ''), ")
			.append("NVL(AD_AttachmentReference.UUID, '') AS UUID ")
			.append("FROM AD_AttachmentReference ")
			.append("INNER JOIN AD_Image AD_Image ")
			.append("ON AD_Image.AD_Image_ID = AD_AttachmentReference.AD_Image_ID ")
		;
		return query.toString();
	}

	public static String getDirectQueryColumnSQLImage() {
		String query = getQueryColumnSQLImage();
		StringBuffer directQuery = new StringBuffer()
			.append(query)
			.append("WHERE AD_AttachmentReference.AD_Image_ID = ? ")
			.append("AND ROWNUM = 1 ")
		;
		return directQuery.toString();
	}



	public static String getColumnsOrderLocation(String displaySequence, boolean isAddressReverse) {
		StringBuffer cityPostalRegion = new StringBuffer();

		String inStr = displaySequence.replace(",", "|| ', ' ");
		String token;
		int index = inStr.indexOf('@');
		while (index != -1) {
			cityPostalRegion.append(inStr.substring(0, index));          // up to @
			inStr = inStr.substring(index + 1, inStr.length());   // from first @

			int endIndex = inStr.indexOf('@');                     // next @
			if (endIndex < 0) {
				token = "";                                 //  no second tag
				endIndex = index + 1;
			} else {
				token = inStr.substring(0, endIndex);
			}
			//  Tokens
			if (token.equals("C")) {
				cityPostalRegion.append("|| NVL(C_Location.City, ")
					.append("(SELECT NVL(C_City.Name, '') FROM C_City WHERE ")
					.append("C_City.C_City_ID = C_Location.C_City_ID")
					.append("), '') ");
			} else if (token.equals("R")) {
				// String regionName = "";
				//  local region name
				// if (!Util.isEmpty(country.getRegionName(), true)) {
				//     regionName = " || ' " + country.getRegionName() + "'";
				// }
				cityPostalRegion.append("|| NVL((SELECT NVL(C_Region.Name, '') FROM C_Region ")
					.append("WHERE C_Region.C_Region_ID = C_Location.C_Region_ID")
					.append("), '') ");
			} else if (token.equals("P")) {
				cityPostalRegion.append("|| NVL(" + I_C_Location.Table_Name + "." + I_C_Location.COLUMNNAME_Postal + ", '') ");
			} else if (token.equals("A")) {
				cityPostalRegion.append("|| NVL(" + I_C_Location.Table_Name + "." + I_C_Location.COLUMNNAME_Postal_Add + ", '') ");
			} else {
				cityPostalRegion.append("@").append(token).append("@");
			}

			inStr = inStr.substring(endIndex + 1, inStr.length());   // from second @
			index = inStr.indexOf('@');
		}
		cityPostalRegion.append(inStr); // add the rest of the string 

		StringBuffer query = new StringBuffer();
		if (isAddressReverse) {
			query.append("'' ")
				.append(cityPostalRegion)
				.append("|| ', ' ")
				.append("|| NVL(" + I_C_Location.Table_Name + "." + I_C_Location.COLUMNNAME_Address4 + " || ', ' , '') ")
				.append("|| NVL(" + I_C_Location.Table_Name + "." + I_C_Location.COLUMNNAME_Address3 + " || ', ' , '') ")
				.append("|| NVL(" + I_C_Location.Table_Name + "." + I_C_Location.COLUMNNAME_Address2 + " || ', ' , '') ")
				.append("|| NVL(" + I_C_Location.Table_Name + "." + I_C_Location.COLUMNNAME_Address1 + ", '')")
			;
		} else {
			query.append("'' ")
				.append("|| NVL(" + I_C_Location.Table_Name + "." + I_C_Location.COLUMNNAME_Address1 + " || ', ' , '') ")
				.append("|| NVL(" + I_C_Location.Table_Name + "." + I_C_Location.COLUMNNAME_Address2 + " || ', ' , '')  ")
				.append("|| NVL(" + I_C_Location.Table_Name + "." + I_C_Location.COLUMNNAME_Address3 + " || ', ' , '')  ")
				.append("|| NVL(" + I_C_Location.Table_Name + "." + I_C_Location.COLUMNNAME_Address4 + " || ', ' , '') ")
				.append(cityPostalRegion)
			;
		}

		return query.toString();
	}

	public static String getDisplayColumnSQLLocation(String tableName, String columnName) {
		MCountry country = MCountry.getDefault(Env.getCtx());
		boolean isAddressReverse = country.isAddressLinesLocalReverse() || country.isAddressLinesReverse();
		String displaySequence = country.getDisplaySequenceLocal();
		String columnsOrder = getColumnsOrderLocation(displaySequence, isAddressReverse);

		StringBuffer query = new StringBuffer()
			.append("SELECT ")
			.append("NVL(" + columnsOrder + ", '') ")
			.append("FROM C_Location ")
			.append("WHERE C_Location.C_Location_ID = ")
			.append(tableName + "." + columnName)
		;

		return query.toString();
	}

	public static String getQueryLocation() {
		MCountry country = MCountry.getDefault(Env.getCtx());
		boolean isAddressReverse = country.isAddressLinesLocalReverse() || country.isAddressLinesReverse();
		String displaySequence = country.getDisplaySequenceLocal();
		String columnsOrder = getColumnsOrderLocation(displaySequence, isAddressReverse);

		StringBuffer query = new StringBuffer()
			.append("SELECT ")
			.append("C_Location.C_Location_ID, NULL, ")
			.append("NVL(" + columnsOrder + ", '-1'), ")
			.append("C_Location.IsActive ")
			.append("FROM C_Location ")
		;

		return query.toString();
	}

	public static String getDirectQueryLocation() {
		String query = getQueryLocation();
		StringBuffer directQuery = new StringBuffer()
			.append(query)
			.append("WHERE C_Location.C_Location_ID = ? ")
		;

		return directQuery.toString();
	}

	/**
	 * Get Reference information, can return null if reference is invalid or not exists
	 * @param referenceId
	 * @param referenceValueId
	 * @param columnName
	 * @param validationRuleId
	 * @return
	 */
	public static MLookupInfo getReferenceLookupInfo(int referenceId, int referenceValueId, String columnName, int validationRuleId) {
		return getReferenceLookupInfo(
			referenceId,
			referenceValueId,
			columnName,
			validationRuleId,
			null
		);
	}

	/**
	 * Get Reference information, can return null if reference is invalid or not exists
	 * @param referenceId
	 * @param referenceValueId
	 * @param columnName
	 * @param validationRuleId
	 * @param customRestriction
	 * @return
	 */
	public static MLookupInfo getReferenceLookupInfo(int referenceId, int referenceValueId, String columnName, int validationRuleId, String customRestriction) {
		if(!validateReference(referenceId)) {
			return null;
		}
		MLookupInfo lookupInformation = null;
		if (DisplayType.Account == referenceId) {
			columnName = I_C_ValidCombination.COLUMNNAME_C_ValidCombination_ID;
		}

		if (DisplayType.Location == referenceId) {
			columnName = I_C_Location.COLUMNNAME_C_Location_ID;
			lookupInformation = getLookupInfoFromColumnName(columnName);
			lookupInformation.Query = getQueryLocation();
			lookupInformation.QueryDirect = getDirectQueryLocation();
		} else if (DisplayType.PAttribute == referenceId) {
			columnName = I_M_AttributeSetInstance.COLUMNNAME_M_AttributeSetInstance_ID;
			lookupInformation = getLookupInfoFromColumnName(columnName);
			lookupInformation.DisplayType = referenceId;
			lookupInformation.Query = "SELECT M_AttributeSetInstance.M_AttributeSetInstance_ID, "
				+ "NULL, M_AttributeSetInstance.Description, M_AttributeSetInstance.IsActive "
				+ "FROM M_AttributeSetInstance ";
			lookupInformation.QueryDirect = lookupInformation.Query
				+ "WHERE M_AttributeSetInstance.M_AttributeSetInstance_ID = ? ";
		} else if(DisplayType.Image == referenceId) {
			columnName = I_AD_Image.COLUMNNAME_AD_Image_ID;
			lookupInformation = getLookupInfoFromColumnName(columnName);
			lookupInformation.DisplayType = referenceId;
			lookupInformation.Query = getQueryColumnSQLImage();
			lookupInformation.QueryDirect = getDirectQueryColumnSQLImage();
		} else if(DisplayType.TableDir == referenceId
				|| referenceValueId <= 0) {
			//	Add Display
			lookupInformation = getLookupInfoFromColumnName(columnName);
		} else {
			//	Get info
			lookupInformation = getLookupInfoFromReference(referenceValueId);
		}

		// without lookup info
		if (lookupInformation == null) {
			return null;
		}

		//	For validation
		String queryForLookup = lookupInformation.Query;
		//	Add support to UUID
		queryForLookup = getQueryWithUuid(lookupInformation.TableName, queryForLookup);
		String directQuery = getQueryWithUuid(lookupInformation.TableName, lookupInformation.QueryDirect);

		// set new query
		lookupInformation.Query = queryForLookup;
		lookupInformation.QueryDirect = directQuery;
		lookupInformation.DisplayType = referenceId;
		lookupInformation.AD_Reference_Value_ID = referenceValueId;

		MValRule validationRule = null;
		//	For validation rule
		if (validationRuleId > 0) {
			validationRule = MValRule.get(Env.getCtx(), validationRuleId);
			if (validationRule != null) {
				if (!Util.isEmpty(validationRule.getCode(), true)) {
					String dynamicValidation = validationRule.getCode();
					if (!validationRule.getCode().startsWith("(")) {
						dynamicValidation = "(" + validationRule.getCode() + ")";
					}
					// // table validation
					// if (!Util.isEmpty(lookupInformation.ValidationCode, true)) {
					// 	dynamicValidation += " AND (" + lookupInformation.ValidationCode + ")";
					// }
					// overwrite ValidationCode with table validation on reference and dynamic validation
					lookupInformation.ValidationCode = dynamicValidation;
				}
			}
		}
		if (!Util.isEmpty(customRestriction, true)) {
			if (!Util.isEmpty(lookupInformation.ValidationCode) && !customRestriction.trim().startsWith("AND")) {
				lookupInformation.ValidationCode += " AND ";
			}
			lookupInformation.ValidationCode += " (" + customRestriction + ")";
		}

		// return only code without validation rule
		if (Util.isEmpty(lookupInformation.ValidationCode, true)) {
			return lookupInformation;
		}

		// remove order by
		String queryWithoutOrder = queryForLookup;
		int positionOrder = queryForLookup.lastIndexOf(" ORDER BY ");
		String orderByQuery = "";
		if (positionOrder != -1) {
			orderByQuery = queryForLookup.substring(positionOrder);
			queryWithoutOrder = queryForLookup.substring(0, positionOrder);
		}

		// add where clause with validation code
		int positionFrom = queryForLookup.lastIndexOf(" FROM ");
		boolean hasWhereClause = queryForLookup.indexOf(" WHERE ", positionFrom) != -1;
		if (hasWhereClause) {
			queryWithoutOrder += " AND ";
		} else {
			queryWithoutOrder += " WHERE ";
		}
		queryWithoutOrder += " " + lookupInformation.ValidationCode;

		// Add new query with where clause and order By
		queryForLookup = queryWithoutOrder + orderByQuery;

		// set new query
		lookupInformation.Query = queryForLookup;

		return lookupInformation;
	}

	/**
	 * Get Lookup info from reference
	 * @param referenceId
	 * @return
	 */
	private static MLookupInfo getLookupInfoFromReference(int referenceId) {
		MLookupInfo lookupInformation = null;
		X_AD_Reference reference = (X_AD_Reference) RecordUtil.getEntity(Env.getCtx(), I_AD_Reference.Table_Name, null, referenceId, null);
		if(reference.getValidationType().equals(X_AD_Reference.VALIDATIONTYPE_TableValidation)) {
			// TODO needToAddSecurity is not in Idempiere core 
			lookupInformation = MLookupFactory.getLookupInfo(Env.getCtx(), 0, 0, DisplayType.Search, Language.getLanguage(Env.getAD_Language(Env.getCtx())), null, reference.getAD_Reference_ID(), false, null);
		} else if(reference.getValidationType().equals(X_AD_Reference.VALIDATIONTYPE_ListValidation)) {
			lookupInformation = MLookupFactory.getLookup_List(Language.getLanguage(Env.getAD_Language(Env.getCtx())), reference.getAD_Reference_ID());
		}
		return lookupInformation;
	}

	/**
	 * Get Query with UUID
	 * @param tableName
	 * @param query
	 * @return
	 */
	public static String getQueryWithUuid(String tableName, String query) {
		Matcher matcherFrom = Pattern.compile(
			"\\s+(FROM)\\s+(" + tableName + ")",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL
		).matcher(query);

		List<MatchResult> fromWhereParts = matcherFrom.results()
				.collect(Collectors.toList());

		String queryWithUuid = query;
		if (fromWhereParts != null && fromWhereParts.size() > 0) {
			MatchResult lastFrom = fromWhereParts.get(fromWhereParts.size() - 1);
			queryWithUuid = query.substring(0, lastFrom.start());
			queryWithUuid += ", " + tableName + ".UUID";
			queryWithUuid += query.substring(lastFrom.start());
		}

		return queryWithUuid;
	}

	/**
	 * Get Query with UUID
	 * @param tableName
	 * @param query
	 * @return
	 */
	public static String getQueryWithActiveRestriction(String tableName, String query) {
		Matcher matcherFrom = Pattern.compile(
			"\\s+(FROM)\\s+(" + tableName + ")",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL
		).matcher(query);

		List<MatchResult> fromWhereParts = matcherFrom.results()
				.collect(Collectors.toList());

		String queryWithUuid = query;
		if (fromWhereParts != null && fromWhereParts.size() > 0) {
			MatchResult lastFrom = fromWhereParts.get(fromWhereParts.size() - 1);
			queryWithUuid = query.substring(0, lastFrom.start());
			queryWithUuid += ", " + tableName + ".UUID";
			queryWithUuid += query.substring(lastFrom.start());
		}

		return queryWithUuid;
	}

	/**
	 * Get Lookup info from column name
	 * @param columnName
	 * @return
	 */
	private static MLookupInfo getLookupInfoFromColumnName(String columnName) {
		// TODO needToAddSecurity is not in Idempiere core 
		return MLookupFactory.getLookupInfo(Env.getCtx(), 0, 0, DisplayType.TableDir, Language.getLanguage(Env.getAD_Language(Env.getCtx())), columnName, 0, false, null);
	}

}
