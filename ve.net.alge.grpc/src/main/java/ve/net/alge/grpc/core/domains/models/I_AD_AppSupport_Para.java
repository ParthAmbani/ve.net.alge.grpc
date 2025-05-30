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

import org.compiere.model.I_AD_Reference;
import org.compiere.model.MTable;
import org.compiere.util.KeyNamePair;

/** Generated Interface for AD_AppSupport_Para
 *  @author Adempiere (generated) 
 *  @version Release 3.9.4
 */
public interface I_AD_AppSupport_Para 
{

    /** TableName=AD_AppSupport_Para */
    public static final String Table_Name = "AD_AppSupport_Para";

    /** AD_Table_ID=54544 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 4 - System 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(4);

    /** Load Meta Data */

    /** Column name AD_AppSupport_ID */
    public static final String COLUMNNAME_AD_AppSupport_ID = "AD_AppSupport_ID";

	/** Set App Support.
	  * App Support for External Connection
	  */
	public void setAD_AppSupport_ID (int AD_AppSupport_ID);

	/** Get App Support.
	  * App Support for External Connection
	  */
	public int getAD_AppSupport_ID();

	public org.adempiere.core.domains.models.I_AD_AppSupport getAD_AppSupport() throws RuntimeException;

    /** Column name AD_AppSupport_Para_ID */
    public static final String COLUMNNAME_AD_AppSupport_Para_ID = "AD_AppSupport_Para_ID";

	/** Set App Support Default Parameter.
	  * Default parameter for App Supported
	  */
	public void setAD_AppSupport_Para_ID (int AD_AppSupport_Para_ID);

	/** Get App Support Default Parameter.
	  * Default parameter for App Supported
	  */
	public int getAD_AppSupport_Para_ID();

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

    /** Column name AD_Reference_ID */
    public static final String COLUMNNAME_AD_Reference_ID = "AD_Reference_ID";

	/** Set Reference.
	  * System Reference and Validation
	  */
	public void setAD_Reference_ID (int AD_Reference_ID);

	/** Get Reference.
	  * System Reference and Validation
	  */
	public int getAD_Reference_ID();

	public 	I_AD_Reference getAD_Reference() throws RuntimeException;

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

    /** Column name IsMandatory */
    public static final String COLUMNNAME_IsMandatory = "IsMandatory";

	/** Set Mandatory.
	  * Data entry is required in this column
	  */
	public void setIsMandatory (boolean IsMandatory);

	/** Get Mandatory.
	  * Data entry is required in this column
	  */
	public boolean isMandatory();

    /** Column name ParameterDefault */
    public static final String COLUMNNAME_ParameterDefault = "ParameterDefault";

	/** Set Default Parameter.
	  * Default value of the parameter
	  */
	public void setParameterDefault (String ParameterDefault);

	/** Get Default Parameter.
	  * Default value of the parameter
	  */
	public String getParameterDefault();

    /** Column name ParameterName */
    public static final String COLUMNNAME_ParameterName = "ParameterName";

	/** Set Parameter Name	  */
	public void setParameterName (String ParameterName);

	/** Get Parameter Name	  */
	public String getParameterName();

    /** Column name ParameterType */
    public static final String COLUMNNAME_ParameterType = "ParameterType";

	/** Set Parameter Type	  */
	public void setParameterType (String ParameterType);

	/** Get Parameter Type	  */
	public String getParameterType();

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
}
