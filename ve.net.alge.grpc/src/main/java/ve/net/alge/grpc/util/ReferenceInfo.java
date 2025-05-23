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
 * Copyright (C) 2012-2023 E.R.P. Consultores y Asociados, S.A. All Rights Reserved. *
 * Contributor(s): Yamel Senih www.erpya.com                                         *
 *************************************************************************************/
package ve.net.alge.grpc.util;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.MBrowseField;
import org.compiere.model.I_AD_Column;
import org.compiere.model.MColumn;
import org.compiere.model.MField;
import org.compiere.model.MLookupInfo;
import org.compiere.model.MProcessPara;
import org.compiere.model.MTable;
import org.compiere.model.MViewColumn;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.model.X_AD_Reference;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Language;
import org.compiere.util.Util;

/**
 * Class for store information about reference
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 */
public class ReferenceInfo {
	
	public ReferenceInfo() {
		displayColumnValue = "";
		displayColumnAlias = "";
		joinColumnName = "";
		tableName = "";
		tableAlias = "";
		columnName = "";
		referenceId = 0;
		hasJoinValue = false;
		language = Language.getBaseAD_Language();// TODO core  AD_Language_en_US;
	}
	/**	Display column Value: Test.Name	*/
	private String displayColumnValue;
	/**	Display column Alias: TestName	*/
	private String displayColumnAlias;
	/**	Column Name	*/
	private String columnName;
	/**	Join Column Name	*/
	private String joinColumnName;
	/**	Table Name	*/
	private String tableName;
	/**	Table Alias	*/
	private String tableAlias;
	/**	Language	*/
	private String language;
	/**	Reference ID	*/
	private int referenceId;
	/**	Has Join value	*/
	private boolean hasJoinValue;
	/**	Default Column And Table Alias	*/
	private final String DISPLAY_COLUMN_ALIAS = LookupUtil.DISPLAY_COLUMN_KEY;
	
	public boolean isHasJoinValue() {
		return hasJoinValue;
	}

	public void setHasJoinValue(boolean hasJoinValue) {
		this.hasJoinValue = hasJoinValue;
	}

	public int getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(int referenceId) {
		this.referenceId = referenceId;
	}
	
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public boolean isTranslated() {
		return !language.equals(Language.getBaseAD_Language());// TODO coreLanguage.AD_Language_en_US);
	}

	public String getDisplayColumnValue() {
		return getDisplayColumnValue(false);
	}
	
	private String getDisplayColumnValue(boolean withTable) {
		if(withTable) {
			return getTableAlias(isTranslated()) + "." + displayColumnValue;
		}
		return displayColumnValue;
	}
	
	public void setDisplayColumnValue(String displayColumnValue) {
		this.displayColumnValue = displayColumnValue;
	}
	
	private String getDisplayColumnAlias() {
		return displayColumnAlias;
	}
	
	public void setDisplayColumnAlias(String displayColumnAlias) {
		this.displayColumnAlias = displayColumnAlias;
	}
	
	public String getJoinColumnName() {
		return getJoinColumnName(false);
	}
	
	private String getJoinColumnName(boolean withTable) {
		if(withTable) {
			return getTableAlias() + "." + joinColumnName;
		}
		return joinColumnName;
	}
	
	public void setJoinColumnName(String joinColumnName) {
		this.joinColumnName = joinColumnName;
		setHasJoinValue(true);
	}

	public String getTableName() {
		return getTableName(false);
	}

	public String getTableName(boolean translated) {
		if(translated) {
			return tableName + "_Trl";
		}
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableAlias(boolean translated) {
		if(translated) {
			return tableAlias + "_Trl";
		}
		return tableAlias;
	}

	public String getTableAlias() {
		return getTableAlias(false);
	}

	public void setTableAlias(String tableAlias) {
		this.tableAlias = tableAlias;
	}

	/**
	 * Without column name
	 */
	private void buildAlias() {
		buildAlias(null);
	}

	/**
	 * Create alias
	 */
	private void buildAlias(String columnName) {
		//	For table alias
		if(Util.isEmpty(tableAlias)) {
			setTableAlias(getColumnName() + "_" + getTableName());
		}
		//	For column alias
		if(Util.isEmpty(displayColumnAlias)) {
			if(Util.isEmpty(columnName)) {
				setDisplayColumnAlias(DISPLAY_COLUMN_ALIAS + "_" + getColumnName());
			} else {
				setDisplayColumnAlias(DISPLAY_COLUMN_ALIAS + "_" + columnName);
			}
		}
	}

	/**
	 * Get display value for Query
	 * @return
	 */
	public String getDisplayValue() {
		buildAlias();
		//	
		return getDisplayColumnValue(isHasJoinValue()) + " AS \"" + getDisplayColumnAlias() + "\"";
	}
	
	/**
	 * With custom column name
	 * @param columnName
	 * @return
	 */
	public String getDisplayValue(String columnName) {
		buildAlias(columnName);
		//	
		return getDisplayColumnValue(isHasJoinValue()) + " AS \"" + getDisplayColumnAlias() + "\"";
	}

	/**
	 * Get Join value for query
	 * @return
	 */
	public String getJoinValue(String baseColumnName) {
		return getJoinValue(baseColumnName, null);
	}
	public String getJoinValue(String baseColumnName, String baseTable) {
		buildAlias();
		if(!isHasJoinValue()) {
			return "";
		}
		StringBuffer join = new StringBuffer();
		join.append(" LEFT JOIN ")
			.append(getTableName()).append(" AS ").append(getTableAlias())
			.append(" ON(").append(getJoinColumnName(true)).append(" = ")
		;
		if (!Util.isEmpty(baseTable, hasJoinValue)) {
			join.append(baseTable).append(".");
		}
		join.append(baseColumnName);

		//	Reference
		if(getReferenceId() > 0) {
			join.append(" AND ").append(getTableAlias() + ".AD_Reference_ID = ").append(getReferenceId());
		}
		join.append(")");
		//	Language
		if(isTranslated()) {
			join.append(" LEFT JOIN ")
				.append(getTableName(true)).append(" AS ").append(getTableAlias(true))
				.append(" ON(").append(getTableAlias(true) + "." + getTableName() + "_ID").append(" = ").append(getTableAlias() + "." + getTableName() + "_ID");
				join.append(" AND ").append(getTableAlias(true)).append(".").append("AD_Language = '").append(getLanguage()).append("'").append(")");
		}
		//	Return
		return join.toString();
	}

	static public MLookupInfo getInfoFromRequest(
		int displayTypeId,
		int fieldId, int processParameterId, int browseFieldId,
		int columnId, String columnName, String tableName
	) {
		return getInfoFromRequest(
			displayTypeId,
			fieldId, processParameterId, browseFieldId,
			columnId, columnName, tableName,
			0,
			0, null, false
		);
	}

	static public MLookupInfo getInfoFromRequest(
		int displayTypeId,
		int fieldId, int processParameterId, int browseFieldId,
		int columnId, String columnName, String tableName,
		boolean isWithoutValidation
	) {
		return getInfoFromRequest(
			displayTypeId,
			fieldId, processParameterId, browseFieldId,
			columnId, columnName, tableName,
			0,
			0, null, isWithoutValidation
		);
	}

	/**
	 * Get reference Info from request
	 * @param request
	 * @return
	 */
	static public MLookupInfo getInfoFromRequest(
		int displayTypeId,
		int fieldId, int processParameterId, int browseFieldId,
		int columnId, String columnName, String tableName,
		int validationRuleId, String customRestriction
	) {
		return getInfoFromRequest(
			displayTypeId,
			fieldId, processParameterId, browseFieldId,
			columnId, columnName, tableName,
			0,
			0, null, false
		);
	}

	/**
	 * Get reference Info from request
	 * @param request
	 * @return
	 */
	static public MLookupInfo getInfoFromRequest(
		int displayTypeId,
		int fieldId, int processParameterId, int browseFieldId,
		int columnId, String columnName, String tableName,
		int validationRuleId, String customRestriction, boolean isWithoutValidation
	) {
		return getInfoFromRequest(
			displayTypeId,
			fieldId, processParameterId, browseFieldId,
			columnId, columnName, tableName,
			0,
			validationRuleId, customRestriction, isWithoutValidation
		);
	}

	/**
	 * Get reference Info from request
	 * @param request
	 * @return
	 */
	static public MLookupInfo getInfoFromRequest(
		int displayTypeId,
		int fieldId, int processParameterId, int browseFieldId,
		int columnId, String columnName, String tableName,
		int displayDefinitionFieldId,
		int validationRuleId, String customRestriction, boolean isWithoutValidation
	) {
		int referenceValueId = 0;
		if(fieldId > 0) {
			MField field = new MField(Env.getCtx(), fieldId, null);
			if(field == null || field.getAD_Field_ID() <= 0) {
				throw new AdempiereException("@AD_Field_ID@ @NotFound@");
			}
			MColumn column = MColumn.get(Env.getCtx(), field.getAD_Column_ID());
			//	Display Type
			displayTypeId = column.getAD_Reference_ID();
			referenceValueId = column.getAD_Reference_Value_ID();
			columnName = column.getColumnName();
			if(field.getAD_Reference_ID() > 0) {
				displayTypeId = field.getAD_Reference_ID();
			}
			if(field.getAD_Reference_Value_ID() > 0) {
				referenceValueId = field.getAD_Reference_Value_ID();
			}
			// not overwrite
			if (validationRuleId <= 0) {
				validationRuleId = field.getAD_Val_Rule_ID();
				if(validationRuleId <= 0) {
					validationRuleId = column.getAD_Val_Rule_ID();
				}
			}
		} else if(browseFieldId > 0) {
			MBrowseField browseField = new MBrowseField(Env.getCtx(), browseFieldId, null);
			if(browseField == null || browseField.getAD_Browse_Field_ID() <= 0) {
				throw new AdempiereException("@AD_Browse_Field_ID@ @NotFound@");
			}
			displayTypeId = browseField.getAD_Reference_ID();
			referenceValueId = browseField.getAD_Reference_Value_ID();
			// not overwrite
			if (validationRuleId <= 0) {
				validationRuleId = browseField.getAD_Val_Rule_ID();
			}
//			MViewColumn viewColumn = browseField.getAD_View_Column();// TODO core
//			if(viewColumn.getAD_Column_ID() > 0) {
//				columnName = MColumn.getColumnName(Env.getCtx(), viewColumn.getAD_Column_ID());
//			} else {
//				columnName = browseField.getAD_Element().getColumnName();
//			}
		} else if(processParameterId > 0) {
			MProcessPara processParameter = new MProcessPara(Env.getCtx(), processParameterId, null);
			if(processParameter == null || processParameter.getAD_Process_Para_ID() <= 0) {
				throw new AdempiereException("@AD_Process_Para_ID@ @NotFound@");
			}
			displayTypeId = processParameter.getAD_Reference_ID();
			referenceValueId = processParameter.getAD_Reference_Value_ID();
			// not overwrite
			if (validationRuleId <= 0) {
				validationRuleId = processParameter.getAD_Val_Rule_ID();
			}
			columnName = processParameter.getColumnName();
		} else if(columnId > 0) {
			MColumn column = MColumn.get(Env.getCtx(), columnId);
			if(column == null || column.getAD_Column_ID() <= 0) {
				throw new AdempiereException("@AD_Column_ID@ @NotFound@");
			}
			displayTypeId = column.getAD_Reference_ID();
			referenceValueId = column.getAD_Reference_Value_ID();
			// not overwrite
			if (validationRuleId <= 0) {
				validationRuleId = column.getAD_Val_Rule_ID();
			}
			columnName = column.getColumnName();
		} else if(!Util.isEmpty(tableName, true)) {
			MTable table = RecordUtil.validateAndGetTable(tableName);
			if(!Util.isEmpty(columnName, true)) {
				MColumn column = table.getColumn(columnName);
				if (column == null || column.getAD_Column_ID() <= 0) {
					throw new AdempiereException("@ColumnName@ @NotFound@");
				}
				displayTypeId = column.getAD_Reference_ID();
				if (referenceValueId <= 0) {
					referenceValueId = column.getAD_Reference_Value_ID();
				}
				if (displayTypeId == DisplayType.ID) {
					if (referenceValueId > 0) {
						//	Is force a Table
						displayTypeId = DisplayType.Table;
					} else {
						//	Is force a Table Direct
						displayTypeId = DisplayType.TableDir;
					}
				}
				// not overwrite
				if (validationRuleId <= 0) {
					validationRuleId = column.getAD_Val_Rule_ID();
				}
				columnName = column.getColumnName();
			} else {
				String[] keyColumns = table.getKeyColumns();
				if (keyColumns != null && keyColumns.length > 0) {
					if (keyColumns.length == 1) {
						//	Single key is force a Table Direct
						displayTypeId = DisplayType.TableDir;
						columnName = tableName + "_ID";
					} else {
						//	Multi keys if force a Table
						displayTypeId = DisplayType.Table;
						columnName = keyColumns[0];
					}
				}
			}
		} else if (displayDefinitionFieldId > 0) {
			PO fieldDefinition = new Query(
				Env.getCtx(),
				"SP010_Field",
				"SP010_Field_ID = ?",
				null
			)
				.setParameters(displayDefinitionFieldId)
				.first()
			;

			if (fieldDefinition == null || fieldDefinition.get_ID() <= 0) {
				throw new AdempiereException("@SP010_DisplayDefinition_ID@ @SP010_Field_ID@ @NotFound@");
			}
			MColumn column = MColumn.get(
				Env.getCtx(),
				fieldDefinition.get_ValueAsInt(
					I_AD_Column.COLUMNNAME_AD_Column_ID
				)
			);
			columnName = column.getColumnName();

			//	Display Type
			displayTypeId = fieldDefinition.get_ValueAsInt(I_AD_Column.COLUMNNAME_AD_Reference_ID);
			if (displayTypeId <= 0) {
				displayTypeId = column.getAD_Reference_ID();
			}

			referenceValueId = fieldDefinition.get_ValueAsInt(I_AD_Column.COLUMNNAME_AD_Reference_Value_ID);
			if(referenceValueId <= 0) {
				referenceValueId = column.getAD_Reference_Value_ID();
			}

			// not overwrite
			if (validationRuleId <= 0) {
				validationRuleId = fieldDefinition.get_ValueAsInt(I_AD_Column.COLUMNNAME_AD_Val_Rule_ID);
				if (validationRuleId <= 0) {
					validationRuleId = column.getAD_Val_Rule_ID();
				}
			}
		}else if(displayTypeId > 0) {
			X_AD_Reference reference = new X_AD_Reference(Env.getCtx(), displayTypeId, null);
			if(reference == null || reference.getAD_Reference_ID() <= 0) {
				throw new AdempiereException("@AD_Reference_ID@ @NotFound@");
			}
			referenceValueId = displayTypeId;
			displayTypeId = DisplayType.Search;
		} else {
			throw new AdempiereException(
				"@AD_Reference_ID@ / @AD_Column_ID@ / @AD_Table_ID@ / @AD_Field_ID@ / @AD_Process_Para_ID@ / @AD_Browse_Field_ID@ / @IsMandatory@"
			);
		}

		// wihtout dynamic validation restriction
		if (isWithoutValidation) {
			validationRuleId = 0;
		}

		// overwrite display type `Button` to `List`, example `PaymentRule` or `Posted`
		displayTypeId = ReferenceUtil.overwriteDisplayType(
			displayTypeId,
			referenceValueId
		);

		return ReferenceUtil.getReferenceLookupInfo(
			displayTypeId,
			referenceValueId,
			columnName,
			validationRuleId,
			customRestriction
		);
	}


	@Override
	public String toString() {
		return "ReferenceInfo [displayColumnValue=" + displayColumnValue + ", displayColumnAlias=" + displayColumnAlias
				+ ", columnName=" + columnName + ", joinColumnName=" + joinColumnName + ", tableName=" + tableName
				+ ", tableAlias=" + tableAlias + ", language=" + language + ", referenceId=" + referenceId + "]";
	}

}
