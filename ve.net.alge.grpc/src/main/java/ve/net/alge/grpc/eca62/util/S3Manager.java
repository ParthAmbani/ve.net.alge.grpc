/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 or later of the                                  *
 * GNU General Public License as published                                    *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * Copyright (C) 2003-2019 E.R.P. Consultores y Asociados, C.A.               *
 * All Rights Reserved.                                                       *
 * Contributor(s): Yamel Senih www.erpya.com                                  *
 *****************************************************************************/
package ve.net.alge.grpc.eca62.util;

import java.io.File;
import java.io.FileInputStream;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MClientInfo;
import org.compiere.util.Env;

import ve.net.alge.grpc.core.domains.models.MADAppRegistration;
import ve.net.alge.grpc.eca62.support.IS3;
import ve.net.alge.grpc.eca62.support.ResourceMetadata;
import ve.net.alge.grpc.util.support.AppSupportHandler;
import ve.net.alge.grpc.util.support.IAppSupport;

/**
 * Handle S3 Util Manager
 * @author Yamel Senih, ysenih@erpya.com, ERPCyA http://www.erpya.com
 */
public class S3Manager {
	/**
	 * Put a Temporary resource to S3
	 * @param file
	 * @return
	 */
	public static String putTemporaryFile(File file) throws Exception {
		try {
		    //	Push to S3
		    MClientInfo clientInfo = MClientInfo.get(Env.getCtx());
//		    if(clientInfo.getFileHandler_ID() <= 0) {
//		    	throw new AdempiereException("@FileHandler_ID@ @NotFound@");
//		    }
//		    MADAppRegistration genericConnector = MADAppRegistration.getById(Env.getCtx(), clientInfo.getFileHandler_ID(), null);
//		    if(genericConnector == null) {
//				throw new AdempiereException("@AD_AppRegistration_ID@ @NotFound@");
//			}
			//	Load
//			IAppSupport supportedApi = AppSupportHandler.getInstance().getAppSupport(genericConnector);
//			if(supportedApi == null) {
//				throw new AdempiereException("@AD_AppSupport_ID@ @NotFound@");
//			}
//			if(!IS3.class.isAssignableFrom(supportedApi.getClass())) {
//				throw new AdempiereException("@AD_AppSupport_ID@ @Unsupported@");
//			}
//			//	Push it
//			IS3 fileHandler = (IS3) supportedApi;
//			ResourceMetadata resourceMetadata = ResourceMetadata.newInstance()
//					.withClientId(Env.getAD_Client_ID(Env.getCtx()))
//					.withUserId(Env.getAD_User_ID(Env.getCtx()))
//					.withContainerType(ResourceMetadata.ContainerType.RESOURCE)
//					.withContainerId("tmp")
//					.withName(file.getName())
//					;
//			String fileName = fileHandler.putResource(resourceMetadata, new FileInputStream(file));
//			return fileName;
		    return null;
		} catch (Exception e) {
			throw new AdempiereException(e);
		}
	}
}
