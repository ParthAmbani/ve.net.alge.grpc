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

import java.util.Enumeration;

import org.adempiere.core.domains.models.I_AD_Ref_List;
import org.adempiere.core.domains.models.X_AD_Tree;
import org.compiere.model.MRefList;
import org.compiere.model.MTable;
import org.compiere.model.MTreeNode;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.spin.backend.grpc.user_interface.TreeNode;
import org.spin.backend.grpc.user_interface.TreeType;
import org.spin.service.grpc.util.value.ValueManager;

/**
 * This class was created for add all convert methods for User Interface service
 * @author Edwin Betancourt, EdwinBetanc0urt@outlook.com , https://github.com/EdwinBetanc0urt
 */
public class UserInterfaceConvertUtil {

	public static TreeType.Builder convertTreeType(String value) {
		TreeType.Builder builder = TreeType.newBuilder();
		if (Util.isEmpty(value, true)) {
			return builder;
		}
		MRefList treeType = MRefList.get(
			Env.getCtx(),
			X_AD_Tree.TREETYPE_AD_Reference_ID,
			value,
			null
		);
		return convertTreeType(treeType);
	}

	public static TreeType.Builder convertTreeType(MRefList treeType) {
		TreeType.Builder builder = TreeType.newBuilder();
		if (treeType == null || treeType.getAD_Ref_List_ID() <= 0) {
			return builder;
		}

		String name = treeType.getName();
		String description = treeType.getDescription();

		// set translated values
		if (!Env.isBaseLanguage(Env.getCtx(), "")) {
			name = treeType.get_Translation(I_AD_Ref_List.COLUMNNAME_Name);
			description = treeType.get_Translation(I_AD_Ref_List.COLUMNNAME_Description);
		}

		builder.setId(treeType.getAD_Ref_List_ID())
			.setValue(ValueManager.validateNull(treeType.getValue()))
			.setName(
				ValueManager.validateNull(name)
			)
			.setDescription(
				ValueManager.validateNull(description)
			)
		;

		return builder;
	}

	public static TreeNode.Builder convertTreeNode(MTable table, MTreeNode treeNode, boolean isWithChildrens) {
		TreeNode.Builder builder = TreeNode.newBuilder();
		builder.setId(treeNode.getNode_ID())
			.setRecordId(treeNode.getNode_ID())
			.setSequence(treeNode.getSeqNo())
			.setName(
				ValueManager.validateNull(
					treeNode.getName()
				)
			)
			.setDescription(
				ValueManager.validateNull(
					treeNode.getDescription()
				)
			)
			.setParentId(treeNode.getParent_ID())
			.setIsSummary(treeNode.isSummary())
			.setIsActive(true)
		;

		if (isWithChildrens) {
			Enumeration<?> childrens = treeNode.children();
			while (childrens.hasMoreElements()) {
				MTreeNode child = (MTreeNode) childrens.nextElement();
				TreeNode.Builder childBuilder = convertTreeNode(table, child, isWithChildrens);
				builder.addChilds(childBuilder.build());
			}
		}

		return builder;
	}

}
