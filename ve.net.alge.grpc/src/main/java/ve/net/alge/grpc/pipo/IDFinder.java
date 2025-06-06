/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 Adempiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *
 * Copyright (C) 2005 Robert Klein. robeklein@hotmail.com
 * Contributor(s): Low Heng Sin hengsin@avantz.com
 *                 Teo Sarca, teo.sarca@gmail.com
 *****************************************************************************/
package ve.net.alge.grpc.pipo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.adempiere.exceptions.DBException;
import org.adempiere.pipo.exception.NonUniqueIDLookupException;
import org.compiere.model.I_AD_Element;
import org.compiere.model.I_AD_TreeNode;
import org.compiere.model.MTable;
import org.compiere.model.X_AD_Table;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Util;

/**
 * Utility class for the looking up of record id.
 * @author Low Heng Sin
 * @author Teo Sarca
 */
public final class IDFinder
{
	private static CLogger log = CLogger.getCLogger(IDFinder.class);
	
	private static Map<String, Integer> idCache = new HashMap<String, Integer>();
	private static Map<String, String> uUIDCache = new HashMap<String, String>();
	
	/**
	 * Get ID from Name for a table.
	 *
	 * @param tableName
	 * @param name
	 * @param clientId
	 * @param trxName
	 * @throws NonUniqueIDLookupException if more then one result found for search criteria
	 */
	public static int get_ID (String tableName, String name, int clientId, String trxName)
	{
		//construct cache key
		StringBuffer key = new StringBuffer();
		key.append(tableName)
			.append(".Name=")
			.append(name);
		if (isValidateClient(clientId, tableName))
			key.append(" and AD_Client_ID=").append(clientId);
		
		//check cache
		if (idCache.containsKey(key.toString()))
			return idCache.get(key.toString());
	
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuffer sqlB = new StringBuffer ("select ")
			.append(tableName)
			.append("_ID from ")
			.append(tableName)
			.append(" where name=?");
		params.add(name);
		
		if (isValidateClient(clientId, tableName))
		{
			sqlB = sqlB.append(" and AD_Client_ID=?");
			params.add(clientId);
		}
		
		return getID(sqlB.toString(), params, key.toString(), true, trxName);
	}
	
	/**
	 * Validate client
	 * @param clientId
	 * @param tableName
	 * @return
	 */
	private static boolean isValidateClient(int clientId, String tableName) {
		MTable table = MTable.get(Env.getCtx(), tableName);
		//	
		if(clientId == 0) {
			return true;
		}
		return !table.getAccessLevel().equals(X_AD_Table.ACCESSLEVEL_All) 
				&& !table.getAccessLevel().equals(X_AD_Table.ACCESSLEVEL_SystemPlusClient)
				&& !table.getAccessLevel().equals(X_AD_Table.ACCESSLEVEL_SystemOnly);
	}
	
	/**
	 * Get ID from UUID for a table
	 * @param ctx
	 * @param tableName
	 * @param uuid
	 * @param clientId
	 * @param trxName
	 * @return
	 */
	public static int getIdFromUUID(Properties ctx, String tableName, String uuid, int clientId, String trxName) {
		return get_IDWithColumn(tableName, I_AD_Element.COLUMNNAME_AD_Element_UU, uuid, clientId, trxName);
	}
	
	
	/**
	 * Get ID from column value for a table.
	 *
	 * @param tableName
	 * @param columName
	 * @param value
	 * @param AD_Client_ID
	 * @param trxName
	 * @throws NonUniqueIDLookupException if more then one result found for search criteria
	 */
	public static int get_IDWithColumn (String tableName, String columnName, Object value, int AD_Client_ID, String trxName)
	{
		return get_IDWithColumn(tableName, columnName, value, AD_Client_ID, true, trxName);
	}
	/**
	 * Get ID from column value for a table.
	 *
	 * @param tableName
	 * @param columName
	 * @param value
	 * @param clientId
	 * @param strict if true we throw NonUniqueIDLookupException on more then one result. Else we will return 0.
	 * @param trxName
	 * @return id or 0
	 * @throws NonUniqueIDLookupException if more then one result found for search criteria and strict is true
	 */
	public static int get_IDWithColumn (String tableName, String columnName, Object value, int clientId, boolean strict, String trxName)
	{
		if (value == null)
			return 0;
		
		//construct cache key
		StringBuffer key = new StringBuffer();
		key.append(tableName)
			.append(".")
			.append(columnName)
			.append("=")
			.append(value.toString());
		if (isValidateClient(clientId, tableName))
			key.append(" and AD_Client_ID=").append(clientId);
		
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuffer sqlB = new StringBuffer ("select ")
		 	.append(tableName)
		 	.append("_ID from ")
		 	.append(tableName)
		 	.append(" where ")
		 	.append(columnName)
		 	.append(" = ?");
		params.add(value);
		
		if (isValidateClient(clientId, tableName))
		{
			sqlB = sqlB.append(" and AD_Client_ID=?");
			params.add(clientId);
		}
		
		sqlB = sqlB.append(" Order By ")
				.append(tableName)
				.append("_ID");
		
		return getID(sqlB.toString(), params, key.toString(), strict, trxName);
	}
	
	/**
	 * Get UUID from ID
	 * @param tableName
	 * @param value
	 * @param clientId
	 * @param trxName
	 * @return
	 */
	public static String getUUIDFromId(String tableName, int value, int clientId, String trxName) {
		if (value <= 0)
			return null;
		
		//construct cache key
		StringBuffer key = new StringBuffer();
		key.append(tableName)
			.append(".")
			.append(tableName)
		 	.append("_ID")
			.append("=")
			.append(value);
		if (isValidateClient(clientId, tableName)) {
			key.append(" and AD_Client_ID=").append(clientId);
		}
		
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer ("select ")
		 	.append(I_AD_Element.COLUMNNAME_AD_Element_UU)
			.append(" from ")
		 	.append(tableName)
		 	.append(" where ")
		 	.append(tableName)
		 	.append("_ID")
		 	.append(" = ?");
		params.add(value);
		
		if (isValidateClient(clientId, tableName)) {
			sql = sql.append(" and AD_Client_ID=?");
			params.add(clientId);
		}
		
		sql = sql.append(" Order By ").append(tableName).append("_ID");
		
		return getUUID(sql.toString(), params, key.toString(), trxName);
	}
	
	/**
	 * Get UUID from ID
	 * @param tableName
	 * @param nodeId
	 * @param clientId
	 * @param trxName
	 * @return
	 */
	public static String getUUIDFromNodeId(String tableName, int treeId, int nodeId, String trxName) {
		if (nodeId <= 0)
			return null;
		
		//construct cache key
		StringBuffer key = new StringBuffer();
		key.append(tableName)
			.append(".")
		 	.append("Node_ID")
			.append("=")
			.append(nodeId)
			.append(" AND ")
			.append(tableName)
			.append(".")
			.append("AD_Tree_ID")
			.append("=")
			.append(treeId);
		
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer ("select ")
		 	.append(I_AD_Element.COLUMNNAME_AD_Element_UU)
			.append(" from ")
		 	.append(tableName)
		 	.append(" where ")
		 	.append("Node_ID")
		 	.append(" = ?")
		 	.append(" and ")
		 	.append("AD_Tree_ID")
		 	.append(" = ?");
		params.add(nodeId);
		params.add(treeId);
		//	Get
		return getUUID(sql.toString(), params, key.toString(), trxName);
	}
	
	/**
	 * Get ID from Node UUID
	 * @param tableName
	 * @param value
	 * @param clientId
	 * @param trxName
	 * @return
	 */
	public static int getIdFromNodeUUID(String tableName, String value, String trxName) {
		if (Util.isEmpty(value))
			return 0;
		
		//construct cache key
		StringBuffer key = new StringBuffer();
		key.append(tableName)
			.append(".")
		 	.append(I_AD_Element.COLUMNNAME_AD_Element_UU)
			.append("=")
			.append(value);
		
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer ("select ")
		 	.append(I_AD_TreeNode.COLUMNNAME_Node_ID)
			.append(" from ")
		 	.append(tableName)
		 	.append(" where ")
		 	.append(I_AD_Element.COLUMNNAME_AD_Element_UU)
		 	.append(" = ?");
		params.add(value);
		//	Get
		return getID(sql.toString(), params, key.toString(), false, trxName);
	}
	
	/**
	 * Get UUID
	 * @param sql
	 * @param params
	 * @param key
	 * @param trxName
	 * @return
	 */
	private static String getUUID(String sql, List<Object> params, String key, String trxName) {
		if (key != null && uUIDCache.containsKey(key)) {
			return uUIDCache.get(key);
		}
		//	
		String value = DB.getSQLValueString(trxName, sql, params);
		// update cache
		if (!Util.isEmpty(value)) {
			uUIDCache.put(key, value);
		}
		
		return value;
	}
	
	/**
	 * Get ID from Name for a table with a Master reference.
	 *
	 * @param tableName
	 * @param name
	 * @param tableNameMaster
	 * @param nameMaster
	 * @param trxName
	 * @throws NonUniqueIDLookupException if more then one result found for search criteria
	 */
	public static int get_IDWithMaster (String tableName, String name, String tableNameMaster, String nameMaster, String trxName)
	{
		//construct cache key
		StringBuffer key = new StringBuffer();
		key.append(tableName)
			.append(".Name=")
			.append(name)
			.append(" and ")
			.append(tableNameMaster)
			.append(".Name=")
			.append(nameMaster);
		
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuffer sqlB = new StringBuffer ("select ")
			.append(tableName)
			.append("_ID from ")
			.append(tableName)
			.append(" where name = ? and ")
			.append(tableNameMaster)
			.append("_ID = (select ")
			.append(tableNameMaster)
			.append("_ID from ")
			.append(tableNameMaster)
			.append(" where name = ?)");
		params.add(name);
		params.add(nameMaster);
		
		return getID(sqlB.toString(), params, key.toString(), true, trxName);
	}
	
	/**
     * Get ID from Name for a table with a Master reference.
     *
     * @param tableName
     * @param name
     * @param tableNameMaster
     * @param masterID
     * @param trxName
	 * @throws NonUniqueIDLookupException if more then one result found for search criteria
     */    
	public static int get_IDWithMasterAndColumn (String tableName, String columnName, String name, String tableNameMaster, int masterID, String trxName)
	{
		String key = tableName + "." + columnName + "=" + name + tableNameMaster + "=" + masterID;
		
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuffer sqlB = new StringBuffer ("select ")
			.append(tableName)
			.append("_ID from ")
			.append(tableName)
			.append(" where ")
			.append(columnName)
			.append(" = ? and ")
			.append(tableNameMaster+"_ID =?");
		params.add(name);
		params.add(masterID);
		
		return getID(sqlB.toString(), params, key, true, trxName);
	}

	/**
	 * Get ID from Name for a table with a Master reference ID.
	 *
	 * @param tableName
	 * @param name
	 * @param tableNameMaster
	 * @param masterID
	 * @param trxName
	 * @throws NonUniqueIDLookupException if more then one result found for search criteria
	 */    
	public static int get_IDWithMaster (String tableName, String name, String tableNameMaster, int masterID, String trxName)
	{
		//construct cache key
		StringBuffer key = new StringBuffer();
		key.append(tableName)
			.append(".Name=")
			.append(name)
			.append(" and ")
			.append(tableNameMaster)
			.append(".")
			.append(tableNameMaster)
			.append("_ID=")
			.append(masterID);
		
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuffer sqlB = new StringBuffer ("select ")
			.append(tableName)
			.append("_ID from ")
			.append(tableName)
			.append(" where name=? and ")
			.append(tableNameMaster)
			.append("_ID=?");
		params.add(name);
		params.add(masterID);
		
		return getID(sqlB.toString(), params, key.toString(), true, trxName);
	}

	/**
	 * Get ID from Column for a table.
	 *
	 * @param tableName
	 * @param column
	 * @param name
	 * @param clientId
	 * @param trxName
	 * @throws NonUniqueIDLookupException if more then one result found for search criteria
	 */
	public static int getIDbyColumn (String tableName, String column, String name, int clientId, String trxName)
	{
		//construct cache key
		StringBuffer key = new StringBuffer();
		key.append(tableName)
			.append("."+column+"=")
			.append(name);
		if (isValidateClient(clientId, tableName))
			key.append(" AND AD_Client_ID=").append(clientId);
		
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer("SELECT ")
			.append(tableName)
			.append("_ID ")
			.append("FROM ")
			.append(tableName)
			.append(" ")
			.append("WHERE "+column+"=?");
		params.add(name);
		if (isValidateClient(clientId, tableName))
		{
			sql.append(" AND AD_Client_ID=?");
			params.add(clientId);
		}
		
		return getID(sql.toString(), params, key.toString(), true, trxName);
	}

	/**
	 * 
	 * @param tableName
	 * @param name
	 * @param AD_Client_ID
	 * @param trxName
	 * @return
	 * @throws NonUniqueIDLookupException if more then one result found for search criteria
	 */
	public static int getIDbyName (String tableName, String name, int AD_Client_ID, String trxName)
	{
		return getIDbyColumn(tableName, "Name", name, AD_Client_ID, trxName);
	}

	/**
	 * 
	 * @param tableName
	 * @param name
	 * @param AD_Client_ID
	 * @param trxName
	 * @return
	 * @throws NonUniqueIDLookupException if more then one result found for search criteria
	 */
	public static int getIDbyValue (String tableName, String name, int AD_Client_ID, String trxName)
	{
		return getIDbyColumn(tableName, "Value", name, AD_Client_ID, trxName);
	}

	
	public static void clearIDCache()
	{
		idCache.clear();
	}
	
	/**
	 * 
	 * @param sql
	 * @param params
	 * @param key
	 * @param strict if true we throw NonUniqueIDLookupException on more then one result. Else we will return 0.
	 * @param trxName
	 * @return id or 0
	 * @throws NonUniqueIDLookupException if more then one result found for search criteria and strict is true
	 */
	private static int getID(String sql, List<Object> params, String key, boolean strict, String trxName)
	{
		if (key != null && idCache.containsKey(key))
			return idCache.get(key);
		
		int id = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, trxName);
			DB.setParameters(pstmt, params);
			
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				id = rs.getInt(1);
			}
			if (rs.next())
			{
				if (strict)
				{
					throw new NonUniqueIDLookupException(key);
				}
				else
				{
					log.warning("Non Unique ID Lookup found for "+key);
				}
			}
		}
		catch (SQLException e)
		{
			throw new DBException(e, sql);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		
		// update cache
		if (key != null && id > 0)
		{
			idCache.put(key, id);
		}
		
		return id;
	}
}
