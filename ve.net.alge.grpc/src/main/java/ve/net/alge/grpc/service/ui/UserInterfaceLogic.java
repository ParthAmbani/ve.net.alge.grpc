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
 * Copyright (C) 2018-2023 E.R.P. Consultores y Asociados, S.A. All Rights Reserved. *
 * Contributor(s): Edwin Betancourt, EdwinBetanc0urt@outlook.com                     *
 *************************************************************************************/

package ve.net.alge.grpc.service.ui;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MTab;
import org.compiere.model.MTable;
import org.compiere.model.MTree;
import org.compiere.model.MTreeNode;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.spin.backend.grpc.user_interface.ListTreeNodesRequest;
import org.spin.backend.grpc.user_interface.ListTreeNodesResponse;
import org.spin.backend.grpc.user_interface.TreeNode;
import org.spin.backend.grpc.user_interface.TreeType;

import ve.net.alge.grpc.base.db.WhereClauseUtil;
import ve.net.alge.grpc.base.util.ContextManager;
import ve.net.alge.grpc.util.RecordUtil;

/**
 * This class was created for add all logic methods for User Interface service
 * @author Edwin Betancourt, EdwinBetanc0urt@outlook.com , https://github.com/EdwinBetanc0urt
 */
public class UserInterfaceLogic {



	public static ListTreeNodesResponse.Builder listTreeNodes(ListTreeNodesRequest request) {
		if (Util.isEmpty(request.getTableName(), true) && request.getTabId() <= 0) {
			throw new AdempiereException("@FillMandatory@ @AD_Table_ID@");
		}
		Properties context = Env.getCtx();

		// get element id
		int elementId = request.getElementId();
		MTable table = null;
		// tab where clause
		String whereClause = null;
		if (request.getTabId() > 0) {
			MTab tab = MTab.get(request.getTabId());
			if (tab == null || tab.getAD_Tab_ID() <= 0) {
				throw new AdempiereException(
					"@AD_Tab_ID@ " + request.getTabId() + " @NotFound@"
				);
			}

			table = MTable.get(context, tab.getAD_Table_ID());
			final String whereTab = WhereClauseUtil.getWhereClauseFromTab(tab.getAD_Tab_ID());
			//	Fill context
			int windowNo = ThreadLocalRandom.current().nextInt(1, 8996 + 1);
			ContextManager.setContextWithAttributesFromStruct(windowNo, context, null);
			String parsedWhereClause = Env.parseContext(context, windowNo, whereTab, false);
			if (Util.isEmpty(parsedWhereClause, true) && !Util.isEmpty(whereTab, true)) {
				throw new AdempiereException(
					"@AD_Tab_ID@ " + tab.getName() + " (" + tab.getAD_Tab_ID() + "), @WhereClause@ @Unparseable@"
				);
			}
			whereClause = parsedWhereClause;
		} else {
			// validate and get table
			table = RecordUtil.validateAndGetTable(
				request.getTableName()
			);
		}
		if (table == null || table.getAD_Table_ID() <= 0) {
			throw new AdempiereException(
				"@AD_Table_ID@ " + request.getTableName() + " @NotFound@"
			);
		}
//		if (!MTree.hasTree(table.getAD_Table_ID())) {// TODO core
//			throw new AdempiereException(
//				"@AD_Table_ID@ " + table.getName() + " (" + table.getAD_Table_ID() + "), @AD_Tree_ID@ @NotFound@"
//			);
//		}

		final int clientId = Env.getAD_Client_ID(context);
		int treeId = getDefaultTreeIdFromTableName(clientId, table.getTableName(), elementId);
		MTree tree = new MTree(context, treeId, false, true, null);
//				new MTree(context, treeId, false, true, whereClause, null);// TODO core 

		MTreeNode treeNode = tree.getRoot();

		int treeNodeId = request.getId();
		ListTreeNodesResponse.Builder builder = ListTreeNodesResponse.newBuilder();

		TreeType.Builder treeTypeBuilder = UserInterfaceConvertUtil.convertTreeType(tree.getTreeType());
		builder.setTreeType(treeTypeBuilder);

		// list child nodes
		Enumeration<?> childrens = Collections.emptyEnumeration();
		if (treeNodeId <= 0) {
			// get root children's
			childrens = treeNode.children();
			builder.setRecordCount(treeNode.getChildCount());
		} else {
			// get current node
			MTreeNode currentNode = treeNode.findNode(treeNodeId);
			if (currentNode == null) {
				throw new AdempiereException(
					"@Node_ID@ " + treeNodeId + " @NotFound@"
				);
			}
			childrens = currentNode.children();
			builder.setRecordCount(currentNode.getChildCount());
		}

		final boolean isWhitChilds = true;
		while (childrens.hasMoreElements()) {
			MTreeNode child = (MTreeNode) childrens.nextElement();
			TreeNode.Builder childBuilder = UserInterfaceConvertUtil.convertTreeNode(table, child, isWhitChilds);
			builder.addRecords(childBuilder.build());
		}

		return builder;
	}

	public static int getDefaultTreeIdFromTableName(int clientId, String tableName, int elementId) {
		if(Util.isEmpty(tableName)) {
			return -1;
		}
		//
		Integer treeId = null;
		String whereClause = new String();
		//	Valid Accouting Element
		if (elementId > 0) {
			whereClause = " AND EXISTS ("
				+ "SELECT 1 FROM C_Element ae "
				+ "WHERE ae.C_Element_ID=" + elementId
				+ " AND tr.AD_Tree_ID=ae.AD_Tree_ID) "
			;
		}
		if(treeId == null || treeId == 0) {
			String sql = "SELECT tr.AD_Tree_ID "
				+ "FROM AD_Tree tr "
				+ "INNER JOIN AD_Table tb ON (tr.AD_Table_ID=tb.AD_Table_ID) "
				+ "WHERE tr.AD_Client_ID IN(0, ?) "
				+ "AND tb.TableName=? "
				+ "AND tr.IsActive='Y' "
				+ "AND tr.IsAllNodes='Y' "
				+ whereClause
				+ "ORDER BY tr.AD_Client_ID DESC, tr.IsDefault DESC, tr.AD_Tree_ID"
			;
			//	Get Tree
			treeId = DB.getSQLValue(null, sql, clientId, tableName);
		}
		//	Default Return
		return treeId;
	}

}
