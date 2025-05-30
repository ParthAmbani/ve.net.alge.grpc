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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.I_AD_ChangeLog;
import org.compiere.model.I_AD_Element;
import org.compiere.model.I_AD_Table;
import org.compiere.model.I_C_Order;
import org.compiere.model.MClient;
import org.compiere.model.MColumn;
import org.compiere.model.MConversionRate;
import org.compiere.model.MConversionType;
import org.compiere.model.MTable;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.model.X_AD_Table;
import org.compiere.util.CCache;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;
import org.compiere.util.Util;
import org.spin.backend.grpc.common.Entity;
import org.spin.backend.grpc.common.ListEntitiesResponse;
import org.spin.service.grpc.util.db.FromUtil;
import org.spin.service.grpc.util.db.OrderByUtil;
import org.spin.service.grpc.util.db.ParameterUtil;
import org.spin.service.grpc.util.value.NumberManager;
import org.spin.service.grpc.util.value.StringManager;
import org.spin.service.grpc.util.value.ValueManager;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;

import ve.net.alge.grpc.base.interim.ContextTemporaryWorkaround;
import ve.net.alge.grpc.pipo.IDFinder;

/**
 * Class for handle records utils values
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 */
public class RecordUtil {
	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(RecordUtil.class);


	/**	Reference cache	*/
	public static CCache<String, String> referenceWhereClauseCache = new CCache<String, String>("Record_Reference_WhereClause", 30, 0);	//	no time-out


	/** Table Allows Records with Zero Identifier */
	public static final List<String> ALLOW_ZERO_ID = Arrays.asList(
		X_AD_Table.ACCESSLEVEL_All,
		X_AD_Table.ACCESSLEVEL_SystemPlusClient,
		X_AD_Table.ACCESSLEVEL_ClientPlusOrganization
	);


	/** Column names on records to client validations */
	public static final List<String> RECORDS_COLUMN_NAMES = Arrays.asList(
		I_AD_Element.COLUMNNAME_AD_Client_ID,
		I_AD_Element.COLUMNNAME_IsActive,
		I_C_Order.COLUMNNAME_Processed,
		I_C_Order.COLUMNNAME_Processing
	);

	/**
	 * get Entity from Table ID and (Record UUID / Record ID)
	 * @param context
	 * @param tableId
	 * @param uuid
	 * @param recordId
	 * @return
	 */
	public static PO getEntity(Properties context, int tableId, String uuid, int recordId, String transactionName) {
		if (tableId <= 0) {
			throw new AdempiereException("@AD_Table_ID@ @NotFound@");
		}

		String tableName = MTable.getTableName(context, tableId);
		return getEntity(context, tableName, uuid, recordId, transactionName);
	}
	
	public static PO getEntity(Properties context, String tableName, int recordId, String transactionName) {
		return getEntity(context, tableName, null, recordId, transactionName);
	}

	/**
	 * get Entity from Table and (UUID / Record ID)
	 * @param context
	 * @param tableName
	 * @param uuid
	 * @param recordId
	 * @return
	 */
	public static PO getEntity(Properties context, String tableName, String uuid, int recordId, String transactionName) {
		// Vaidate and get Table
		final MTable table = RecordUtil.validateAndGetTable(tableName);
		//	Validate UUID/ID
		boolean isId = isValidId(recordId, table.getAccessLevel());
		if (Util.isEmpty(uuid, true) && !isId) {
			throw new AdempiereException("@FillMandatory@ @Record_ID@ / @UUID@");
		}

		StringBuffer whereClause = new StringBuffer();
		List<Object> filtersList = new ArrayList<>();
		if (!Util.isEmpty(uuid, true)) {
			whereClause.append(I_AD_Element.COLUMNNAME_AD_Element_UU + " = ?");
			filtersList.add(uuid);
		} else if (isId) {
			for (final String keyColumnName: table.getKeyColumns()) {
				MColumn column = table.getColumn(keyColumnName);
				if (DisplayType.isID(column.getAD_Reference_ID())) {
					if (whereClause.length() > 0) {
						whereClause.append(" OR ");
					}
					whereClause.append(keyColumnName + " = ?");
					filtersList.add(recordId);
				}
			}
		} else {
			throw new AdempiereException("@Record_ID@ / @UUID@ @NotFound@");
		}
		//	Default
		return new Query(
			context,
			table,
			whereClause.toString(),
			transactionName
		)
			.setParameters(filtersList)
			.first()
		;
	}
	
	/**
	 * get Entity from Table and where clause
	 * @param context
	 * @param tableName
	 * @param whereClause
	 * @param parameters
	 * @return
	 */
	public static PO getEntity(Properties context, String tableName, String whereClause, List<Object> parameters, String transactionName) {
		//	Validate ID
		if(Util.isEmpty(whereClause)) {
			throw new AdempiereException("@WhereClause@ @NotFound@");
		}
		
		MTable table = validateAndGetTable(tableName);
		//	Default
		return new Query(
			context,
			table,
			whereClause,
			transactionName
		)
			.setParameters(parameters)
			.first();
	}
	
	/**
	 * Get ID for record from table name and uuid
	 * @param tableName
	 * @param uuid
	 * @return
	 */
	public static int getIdFromUuid(String tableName, String uuid, String transactionName) {
		if (Util.isEmpty(tableName, true) || Util.isEmpty(uuid, true)) {
			return -1;
		}
		//	Get
		return IDFinder.getIdFromUUID(Env.getCtx(), tableName, uuid, Env.getAD_Client_ID(Env.getCtx()), transactionName);
	}



	/**
	 * Validate tableName and MTable, and get instance
	 * @param tableName
	 * @return
	 */
	public static MTable validateAndGetTable(String tableName) {
		if (Util.isEmpty(tableName, true)) {
			throw new AdempiereException("@FillMandatory@ @AD_Table_ID@");
		}
		MTable table = MTable.get(Env.getCtx(), tableName);
		if (table == null || table.getAD_Table_ID() <= 0) {
			throw new AdempiereException("@AD_Table_ID@ @NotFound@");
		}
		return table;
	}
	/**
	 * Validate tableName and MTable, and get instance
	 * @param tableId
	 * @return
	 */
	public static MTable validateAndGetTable(int tableId) {
		if (tableId <= 0) {
			throw new AdempiereException("@FillMandatory@ @AD_Table_ID@");
		}
		MTable table = MTable.get(Env.getCtx(), tableId);
		if (table == null || table.getAD_Table_ID() <= 0) {
			throw new AdempiereException("@AD_Table_ID@ @NotFound@");
		}
		return table;
	}



	/**
	 * Evaluate if is valid identifier
	 * @param id
	 * @param accesLevel
	 * @return
	 */
	public static boolean isValidId(int id, String accesLevel) {
		if (id < 0) {
			return false;
		}

		if (id == 0 && !ALLOW_ZERO_ID.contains(accesLevel)) {
			return false;
		}

		return true;
	}


	/**
	 * Evaluate if is valid identifier
	 * @param id
	 * @param accesLevel
	 * @return
	 */
	public static boolean validateRecordId(int id, String accesLevel) {
		if (!isValidId(id, accesLevel)) {
			throw new AdempiereException("@FillMandatory@ @Record_ID@ / @UUID@");
		}
		return true;
	}


	/**
	 * Get UUID from record id
	 * @param tableName
	 * @param id
	 * @return
	 */
	public static String getUuidFromId(String tableName, int id) {
		//	Get
		return getUuidFromId(tableName, id, null);
	}

	/**
	 * Get UUID from record id
	 * @param tableName
	 * @param id
	 * @param transactionName
	 * @return
	 */
	public static String getUuidFromId(String tableName, int id, String transactionName) {
		if (Util.isEmpty(tableName, true)) {
			return null;
		}
		MTable table = MTable.get(Env.getCtx(), tableName);
		if (table == null || table.getAD_Table_ID() <= 0) {
			return null;
		}

		//	Validate ID
		if (!isValidId(id, table.getAccessLevel())) {
			return null;
		}
		//	Get
		return IDFinder.getUUIDFromId(tableName, id, Env.getAD_Client_ID(Env.getCtx()), transactionName);
	}



	/**
	 * Get conversion Rate from ValidFrom, Currency From, Currency To and Conversion Type
	 * @param request
	 * @return
	 */
	public static MConversionRate getConversionRate(int organizationId, int conversionTypeId, int currencyFromId, int currencyToId, Timestamp conversionDate) {
		if(conversionTypeId <= 0
				|| currencyFromId <= 0
				|| currencyToId <= 0) {
			return null;
		}
		//	Get values
		conversionDate = TimeUtil.getDay(Optional.ofNullable(conversionDate).orElse(new Timestamp(System.currentTimeMillis())));
		if(organizationId < 0) {
			organizationId = 0;
		}
		int conversionRateId = getConversionRateId(currencyFromId, 
				currencyToId, 
				conversionDate, 
				conversionTypeId, 
				Env.getAD_Client_ID(Env.getCtx()), 
				organizationId);
		if(conversionRateId > 0) {
			return new MConversionRate(Env.getCtx(), conversionRateId, null);
		}
		//	
		return null;
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
			log.info ("getRate - not found - CurFrom=" + currencyFromId 
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
	 * Add where clause with search value and return the new complete SQL
	 * @param sql
	 * @param tableName
	 * @param searchValue
	 * @param parameters
	 * @return
	 */
	public static String addSearchValueAndGet(String sql, String tableName, String searchValue, List<Object> parameters) {
		return addSearchValueAndGet(sql, tableName, null, searchValue, true, parameters);
	}

	/**
	 * Add where clause with search value and return the new complete SQL
	 * @param sql
	 * @param tableName
	 * @param searchValue
	 * @param parameters
	 * @return
	 */
	public static String addSearchValueAndGet(String sql, String tableName, String tableAlias, String searchValue, List<Object> parameters) {
		return addSearchValueAndGet(sql, tableName, tableAlias, searchValue, true, parameters);
	}

	/**
	 * Add where clause with search value and return the new complete SQL
	 * @param sql
	 * @param tableName
	 * @param searchValue
	 * @param isTranslated
	 * @param parameters
	 * @return
	 */
	public static String addSearchValueAndGet(String sql, String tableName, String searchValue, boolean isTranslated, List<Object> parameters) {
		return addSearchValueAndGet(sql, tableName, null, searchValue, isTranslated, parameters);
	}

	/**
	 * Add where clause with search value and return the new complete SQL
	 * @param sql
	 * @param tableName
	 * @param tableAlias
	 * @param searchValue
	 * @param isTranslated
	 * @param parameters
	 * @return
	 */
	public static String addSearchValueAndGet(String sql, String table_name, String table_alias, String search_value, boolean isTranslated, List<Object> parameters) {
		if(Util.isEmpty(search_value, true)) {
			return sql;
		}
		MTable table = MTable.get(Env.getCtx(), table_name);
		if(table == null || table.getAD_Table_ID() <= 0) {
			return sql;
		}

		// URL decode to change characteres
		final String searchValue = ValueManager.getDecodeUrl(
			search_value
		);

		String lang = Env.getAD_Language(Env.getCtx());
		// search on trl table
		final MTable tableTranslation = org.compiere.util.Language.isBaseLanguage(lang) || !isTranslated ?
			null : MTable.get(Env.getCtx(), table_name + DictionaryUtil.TRANSLATION_SUFFIX);
		final String tableAlias = Util.isEmpty(table_alias, true) ? table.getTableName() : table_alias;

		List<MColumn> selectionColums = Arrays.asList(table.getColumns(true)).parallelStream()
			.filter(column -> {
				return (column.isIdentifier() || column.isSelectionColumn()
					|| column.getColumnName().equals("Name")
					|| column.getColumnName().equals("Value")
					|| column.getColumnName().equals("DocumentNo"))
					&& Util.isEmpty(column.getColumnSQL(), true)
					&& DisplayType.isText(column.getAD_Reference_ID());
			})
			.collect(Collectors.toList());

		// TODO: Add 1=1 to remove `if (where.length() > 0)` and change stream with parallelStream
		StringBuffer where = new StringBuffer();
		selectionColums.stream()
			.filter(column -> {
				// "Value" is not translated for example
				return (tableTranslation == null || (tableTranslation != null && !column.isTranslated()));
			})
			.forEach(column -> {
				if(where.length() > 0) {
					where.append(" OR ");
				}
				where.append("UPPER(")
					.append(tableAlias).append(".")
					.append(column.getColumnName())
					.append(")")
					.append(" LIKE ")
					.append("'%' || UPPER(?) || '%'");
				parameters.add(searchValue);
			});

		// Add language clause
		StringBuffer whereClause = where;
		String joinTranslation = "";
		if (tableTranslation != null) {
			// TODO: Add 1=1 to remove `if (whereTranslation.length() > 0)` and change stream with parallelStream
			StringBuffer whereTranslation = new StringBuffer();
			
			selectionColums.stream()
				.filter(column -> {
					// Name is translated for example
					return tableTranslation != null && column.isTranslated();
				})
				.forEach(column -> {
					if(whereTranslation.length() > 0) {
						whereTranslation.append(" OR ");
					}
					whereTranslation.append("UPPER(")
						.append("NVL(")
						.append(tableTranslation.getTableName()).append(".")
						.append(column.getColumnName())
						.append(", ")
						.append(tableAlias).append(".")
						.append(column.getColumnName())
						.append("))")
						.append(" LIKE ")
						.append("'%'|| UPPER(?) || '%'");
					parameters.add(searchValue);
				});

			if (!Util.isEmpty(whereTranslation.toString(), true)) {
				if (!Util.isEmpty(where.toString(), true)) {
					whereClause.append(" OR ");
				}
				whereClause
					.append("((")
					.append(whereTranslation)
					.append(") AND ")
					.append(tableTranslation.getTableName() + ".AD_Language = ? ")
					// .append(" AND ")
					// .append(tableTranslation.getTableName() + ".IsTranslated = 'Y' ")
					.append(")")
				;
				parameters.add(lang);
	
				String keyColumn = table.getTableName() + "_ID";
				String translationTableName = tableTranslation.getTableName();
				// only table, tableName tableName, tableName AS tableName
				String tableWithAliases = FromUtil.getPatternTableName(translationTableName, null);

				String joinPattern = "JOIN(\\s+)" 
					+ "(" + tableWithAliases + ")"
					+ "(\\s+ON(\\s*\\(){0,1})\\s*"
					// base_table.table_id = translation_table.table_id
					+ "((\\w+." + keyColumn + "\\s*=\\s*" + translationTableName + "." + keyColumn + ")"
					// translation_table.table_id = base_table.table_id
					+ "|(" + translationTableName + "." + keyColumn + "\\s*=\\s*\\w+." + keyColumn +"))"
					;
				Matcher matcherJoinTranslated = Pattern.compile(
					joinPattern,
					Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE
				).matcher(sql);
				// Add tranlated join
				if (!matcherJoinTranslated.find()) {
					joinTranslation = " LEFT JOIN " + translationTableName + " AS " + translationTableName
						+ " ON " + translationTableName + "." + keyColumn + " = " + tableAlias + "." + keyColumn + " "
					;
				}
			}
		}
		
		//	Validate and return
		if(whereClause.length() > 0) {
			//	Order by
			Matcher matcherOrderBy = OrderByUtil.SQL_ORDER_BY_PATTERN
				.matcher(sql)
			;
			int positionOrderBy = -1;
			String orderByClause = "";
			String queryWithoutOrderBy = sql;
			if (matcherOrderBy.find()) {
				positionOrderBy = matcherOrderBy.start();
				orderByClause = sql.substring(positionOrderBy);
				// remove order By
				queryWithoutOrderBy = sql.substring(0, positionOrderBy);
			}

			// tableName tableName, tableName AS tableName
			String patternAlias = FromUtil.getPatternTableName(table.getTableName(), tableAlias);

			String fromWherePattern = "\\s+(FROM)\\s+(" + patternAlias + ")" + "\\s+(WHERE)";
			Matcher matcher = Pattern.compile(
				fromWherePattern,
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL
			)
			.matcher(queryWithoutOrderBy);
			Matcher matcherJoin = Pattern.compile(
				"JOIN(\\w|\\s)*(\\((\\w|\\.|\\s|=|\\')*\\))(\\s*)(WHERE)",
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL
			).matcher(queryWithoutOrderBy);

			String conditional = " WHERE ";
			List<MatchResult> fromWhereParts = matcher.results()
					.collect(Collectors.toList());
			List<MatchResult> joinWhereParts = matcherJoin.results()
					.collect(Collectors.toList());
			
			String onlyQuery = queryWithoutOrderBy;
			if (joinWhereParts != null && joinWhereParts.size() > 0) {
				MatchResult lastJoinWhere = joinWhereParts.get(joinWhereParts.size() - 1);
				onlyQuery = queryWithoutOrderBy.substring(0, lastJoinWhere.end());

				int lastWhere = onlyQuery.lastIndexOf("WHERE");
				onlyQuery = onlyQuery.substring(0, lastWhere);

				conditional = queryWithoutOrderBy.substring(lastWhere, queryWithoutOrderBy.length()) + " AND ";
			} else if (fromWhereParts != null && fromWhereParts.size() > 0) {
				MatchResult lastJoinWhere = fromWhereParts.get(fromWhereParts.size() - 1);
				onlyQuery = queryWithoutOrderBy.substring(0, lastJoinWhere.end());

				int lastWhere = onlyQuery.lastIndexOf("WHERE");
				onlyQuery = onlyQuery.substring(0, lastWhere);
				
				conditional = queryWithoutOrderBy.substring(lastWhere, queryWithoutOrderBy.length()) + " AND ";
			}

			sql = onlyQuery + joinTranslation + conditional + "(" + whereClause + ")" + orderByClause;
		}
		return sql;
	}



	/**
	 * Get Date
	 * @return
	 */
	public static Timestamp getDate() {
		return TimeUtil.getDay(System.currentTimeMillis());
	}


	/**
	 * 	Return DateTime + last time
	 * 	@param dateTime Date and Time
	 * 	@param offset minute offset
	 * 	@return dateTime + offset in minutes
	 */
	static public Timestamp getDayLastTime(Timestamp dateTime)
	{
		if (dateTime == null) {
			dateTime = new Timestamp(System.currentTimeMillis());
		}
		//
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(dateTime);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 999);
		return new Timestamp (cal.getTimeInMillis());
	}	//	addMinutes


	/**
	 * Verify if exist a column
	 * @param metaData
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	public static int getColumnIndex(ResultSetMetaData metaData, String columnName) throws SQLException {
		if (metaData == null) {
			return -1;
		}
		for(int columnIndex = 1; columnIndex <= metaData.getColumnCount(); columnIndex++) {
			String metaDataColumnName = metaData.getColumnName(columnIndex);
			if(metaDataColumnName != null && metaDataColumnName.toLowerCase().equals(columnName.toLowerCase())) {
				return columnIndex;
			}
		}
		return -1;
	}



	/**
	 * Convert Entities List
	 * @param table
	 * @param sql
	 * @param params
	 * @return
	 */
	public static ListEntitiesResponse.Builder convertListEntitiesResult(MTable table, String sql, List<Object> params) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ListEntitiesResponse.Builder builder = ListEntitiesResponse.newBuilder();
		long recordCount = 0;
		try {
			LinkedHashMap<String, MColumn> columnsMap = new LinkedHashMap<>();
			//	Add field to map
			for(MColumn column: table.getColumns(true)) {
				columnsMap.put(column.getColumnName().toUpperCase(), column);
			}
			//	SELECT Key, Value, Name FROM ...
			pstmt = DB.prepareStatement(sql, null);
			ParameterUtil.setParametersFromObjectsList(pstmt, params);

			//	Get from Query
			rs = pstmt.executeQuery();
			while(rs.next()) {
				Entity.Builder entityBuilder = Entity.newBuilder()
					.setTableName(table.getTableName())
				;
				Struct.Builder rowValues = Struct.newBuilder();
				ResultSetMetaData metaData = rs.getMetaData();
				for (int index = 1; index <= metaData.getColumnCount(); index++) {
					try {
						String columnName = metaData.getColumnName(index);
						MColumn field = columnsMap.get(columnName.toUpperCase());
						//	Display Columns
						if(field == null) {
							String displayValue = rs.getString(index);
							Value.Builder displayValueBuilder = ValueManager.getValueFromString(displayValue);

							rowValues.putFields(
								columnName,
								displayValueBuilder.build()
							);
							continue;
						}
						if (field.isKey()) {
							final int identifier = rs.getInt(index);
							entityBuilder.setId(identifier);
						}
						if (I_AD_Element.COLUMNNAME_AD_Element_UU.toLowerCase().equals(columnName.toLowerCase())) {
							final String uuid = rs.getString(index);
							entityBuilder.setUuid(
								StringManager.getValidString(uuid)
							);
						}
						//	From field
						String fieldColumnName = field.getColumnName();
						Object value = rs.getObject(index);
						Value.Builder valueBuilder = ValueManager.getValueFromReference(
							value,
							field.getAD_Reference_ID()
						);
						rowValues.putFields(
							fieldColumnName,
							valueBuilder.build()
						);

						// to add client uuid by record
						if (fieldColumnName.equals(I_AD_Element.COLUMNNAME_AD_Client_ID)) {
							final int clientId = NumberManager.getIntegerFromObject(value);
							MClient clientEntity = MClient.get(
								table.getCtx(),
								clientId
							);
							if (clientEntity != null) {
								final String clientUuid = clientEntity.get_UUID();
								Value.Builder valueUuidBuilder = ValueManager.getValueFromString(
									clientUuid
								);
								rowValues.putFields(
									LookupUtil.get_UUIDColumnName(
										I_AD_Element.COLUMNNAME_AD_Client_ID
									),
									valueUuidBuilder.build()
								);
							}
						} else if (fieldColumnName.equals(I_AD_ChangeLog.COLUMNNAME_Record_ID)) {
							if (rs.getInt(I_AD_Table.COLUMNNAME_AD_Table_ID) > 0) {
								MTable tableRow = MTable.get(table.getCtx(), rs.getInt(I_AD_Table.COLUMNNAME_AD_Table_ID));
								if (tableRow != null) {
									PO entityRow = tableRow.getPO(rs.getInt(I_AD_ChangeLog.COLUMNNAME_Record_ID), null);
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
					} catch (Exception e) {
						log.severe(e.getLocalizedMessage());
					}
				}

				// TODO: Temporary Workaround
				rowValues = ContextTemporaryWorkaround.setContextAsUnknowColumn(
					table.getTableName(),
					rowValues
				);
				//	
				entityBuilder.setValues(rowValues);
				builder.addRecords(entityBuilder.build());
				recordCount++;
			}
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			DB.close(rs, pstmt);
		}
		//	Set record counts
		builder.setRecordCount(recordCount);
		//	Return
		return builder;
	}

}
