/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 2008 SC ARHIPAC SERVICE SRL. All Rights Reserved.            *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/
package ve.net.alge.grpc.pipo.exception;

import org.adempiere.exceptions.AdempiereException;

import ve.net.alge.grpc.core.domains.models.MPPOrderNode;

/**
 * Throwed when we are trying to process/complete an Order Activity that is already processed/completed
 * @author Teo Sarca, www.arhipac.ro
 */
public class ActivityProcessedException extends AdempiereException
{
	private static final long	serialVersionUID	= 1L;

	public ActivityProcessedException(MPPOrderNode activity)
	{
		super("Order Activity Already Processed - "+activity); // TODO: translate
	}
}
