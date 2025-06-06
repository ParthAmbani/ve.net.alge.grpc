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

/** Generated Interface for AD_AppRegistration_Para
 *  @author Adempiere (generated) 
 *  @version Release 3.9.4
 */
public interface I_AD_AppRegistration_Para 
{

    /** TableName=AD_AppRegistration_Para */
    public static final String Table_Name = "AD_AppRegistration_Para";

    /** AD_Table_ID=54541 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 6 - System - Client 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(6);

    /** Load Meta Data */

    /** Column name AD_AppRegistration_ID */
    public static final String COLUMNNAME_AD_AppRegistration_ID = "AD_AppRegistration_ID";

	/** Set Application Registration.
	  * External Application Registration
	  */
	public void setAD_AppRegistration_ID (int AD_AppRegistration_ID);

	/** Get Application Registration.
	  * External Application Registration
	  */
	public int getAD_AppRegistration_ID();

	public ve.net.alge.grpc.core.domains.models.I_AD_AppRegistration getAD_AppRegistration() throws RuntimeException;

    /** Column name AD_AppRegistration_Para_ID */
    public static final String COLUMNNAME_AD_AppRegistration_Para_ID = "AD_AppRegistration_Para_ID";

	/** Set App Registration Parameter.
	  * App Registration Parameter used for connect with External App
	  */
	public void setAD_AppRegistration_Para_ID (int AD_AppRegistration_Para_ID);

	/** Get App Registration Parameter.
	  * App Registration Parameter used for connect with External App
	  */
	public int getAD_AppRegistration_Para_ID();

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

	public ve.net.alge.grpc.core.domains.models.I_AD_AppSupport_Para getAD_AppSupport_Para() throws RuntimeException;

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

    /** Column name ParameterValue */
    public static final String COLUMNNAME_ParameterValue = "ParameterValue";

	/** Set Parameter Value	  */
	public void setParameterValue (String ParameterValue);

	/** Get Parameter Value	  */
	public String getParameterValue();

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
