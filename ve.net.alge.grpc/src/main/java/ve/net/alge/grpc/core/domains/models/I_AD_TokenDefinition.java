/******************************************************************************
 * Product: ADempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 2006-2017 ADempiere Foundation, All Rights Reserved.         *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * or (at your option) any later version.                                     *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * or via info@adempiere.net                                                  *
 * or https://github.com/adempiere/adempiere/blob/develop/license.html        *
 *****************************************************************************/
package ve.net.alge.grpc.core.domains.models;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.compiere.model.MTable;
import org.compiere.util.KeyNamePair;

/** Generated Interface for AD_TokenDefinition
 *  @author Adempiere (generated) 
 *  @version Release 3.9.4
 */
public interface I_AD_TokenDefinition 
{

    /** TableName=AD_TokenDefinition */
    public static final String Table_Name = "AD_TokenDefinition";

    /** AD_Table_ID=54529 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 6 - System - Client 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(6);

    /** Load Meta Data */

    /** Column name AD_Client_ID */
    public static final String COLUMNNAME_AD_Client_ID = "AD_Client_ID";

	/** Get Client.
	  * Client/Tenant for this installation.
	  */
	public int getAD_Client_ID();

    /** Column name AD_Org_ID */
    public static final String COLUMNNAME_AD_Org_ID = "AD_Org_ID";

	/** Set Organization.
	  * Organizational entity within client
	  */
	public void setAD_Org_ID (int AD_Org_ID);

	/** Get Organization.
	  * Organizational entity within client
	  */
	public int getAD_Org_ID();

    /** Column name AD_TokenDefinition_ID */
    public static final String COLUMNNAME_AD_TokenDefinition_ID = "AD_TokenDefinition_ID";

	/** Set Token Definition.
	  * Token Definition, used for define generator class for token
	  */
	public void setAD_TokenDefinition_ID (int AD_TokenDefinition_ID);

	/** Get Token Definition.
	  * Token Definition, used for define generator class for token
	  */
	public int getAD_TokenDefinition_ID();

    /** Column name Classname */
    public static final String COLUMNNAME_Classname = "Classname";

	/** Set Classname.
	  * Java Classname
	  */
	public void setClassname (String Classname);

	/** Get Classname.
	  * Java Classname
	  */
	public String getClassname();

    /** Column name Created */
    public static final String COLUMNNAME_Created = "Created";

	/** Get Created.
	  * Date this record was created
	  */
	public Timestamp getCreated();

    /** Column name CreatedBy */
    public static final String COLUMNNAME_CreatedBy = "CreatedBy";

	/** Get Created By.
	  * User who created this records
	  */
	public int getCreatedBy();

    /** Column name Description */
    public static final String COLUMNNAME_Description = "Description";

	/** Set Description.
	  * Optional short description of the record
	  */
	public void setDescription (String Description);

	/** Get Description.
	  * Optional short description of the record
	  */
	public String getDescription();

    /** Column name ExpirationTime */
    public static final String COLUMNNAME_ExpirationTime = "ExpirationTime";

	/** Set Expiration Time.
	  * Expiration Time for Token in milliseconds, default 5 minutes
	  */
	public void setExpirationTime (BigDecimal ExpirationTime);

	/** Get Expiration Time.
	  * Expiration Time for Token in milliseconds, default 5 minutes
	  */
	public BigDecimal getExpirationTime();

    /** Column name IsActive */
    public static final String COLUMNNAME_IsActive = "IsActive";

	/** Set Active.
	  * The record is active in the system
	  */
	public void setIsActive (boolean IsActive);

	/** Get Active.
	  * The record is active in the system
	  */
	public boolean isActive();

    /** Column name IsHasExpireDate */
    public static final String COLUMNNAME_IsHasExpireDate = "IsHasExpireDate";

	/** Set Has Expire Date.
	  * Has Expire Date for generated token
	  */
	public void setIsHasExpireDate (boolean IsHasExpireDate);

	/** Get Has Expire Date.
	  * Has Expire Date for generated token
	  */
	public boolean isHasExpireDate();

    /** Column name Name */
    public static final String COLUMNNAME_Name = "Name";

	/** Set Name.
	  * Alphanumeric identifier of the entity
	  */
	public void setName (String Name);

	/** Get Name.
	  * Alphanumeric identifier of the entity
	  */
	public String getName();

    /** Column name TokenType */
    public static final String COLUMNNAME_TokenType = "TokenType";

	/** Set TokenType.
	  * Wiki Token Type
	  */
	public void setTokenType (String TokenType);

	/** Get TokenType.
	  * Wiki Token Type
	  */
	public String getTokenType();

    /** Column name Updated */
    public static final String COLUMNNAME_Updated = "Updated";

	/** Get Updated.
	  * Date this record was updated
	  */
	public Timestamp getUpdated();

    /** Column name UpdatedBy */
    public static final String COLUMNNAME_UpdatedBy = "UpdatedBy";

	/** Get Updated By.
	  * User who updated this records
	  */
	public int getUpdatedBy();

    /** Column name UUID */
    public static final String COLUMNNAME_UUID = "UUID";

	/** Set Immutable Universally Unique Identifier.
	  * Immutable Universally Unique Identifier
	  */
	public void setUUID (String UUID);

	/** Get Immutable Universally Unique Identifier.
	  * Immutable Universally Unique Identifier
	  */
	public String getUUID();

    /** Column name Value */
    public static final String COLUMNNAME_Value = "Value";

	/** Set Search Key.
	  * Search key for the record in the format required - must be unique
	  */
	public void setValue (String Value);

	/** Get Search Key.
	  * Search key for the record in the format required - must be unique
	  */
	public String getValue();
}
