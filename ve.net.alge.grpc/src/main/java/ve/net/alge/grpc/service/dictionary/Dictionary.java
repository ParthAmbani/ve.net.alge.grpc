/************************************************************************************
 * Copyright (C) 2012-2023 E.R.P. Consultores y Asociados, C.A.                     *
 * Contributor(s): Yamel Senih ysenih@erpya.com                                     *
 * This program is free software: you can redistribute it and/or modify             *
 * it under the terms of the GNU General Public License as published by             *
 * the Free Software Foundation, either version 2 of the License, or                *
 * (at your option) any later version.                                              *
 * This program is distributed in the hope that it will be useful,                  *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of                   *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the                     *
 * GNU General Public License for more details.                                     *
 * You should have received a copy of the GNU General Public License                *
 * along with this program. If not, see <https://www.gnu.org/licenses/>.            *
 ************************************************************************************/
package ve.net.alge.grpc.service.dictionary;

import java.util.Properties;

import org.adempiere.core.domains.models.I_AD_Browse;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.MBrowse;
import org.compiere.model.I_AD_Field;
import org.compiere.model.I_AD_Form;
import org.compiere.model.I_AD_Tab;
import org.compiere.model.I_AD_Window;
import org.compiere.model.MColumn;
import org.compiere.model.MField;
import org.compiere.model.MForm;
import org.compiere.model.MTab;
import org.compiere.model.MTable;
import org.compiere.model.MWindow;
import org.compiere.model.M_Element;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Language;
import org.compiere.util.Util;
import org.spin.backend.grpc.dictionary.Browser;
import org.spin.backend.grpc.dictionary.DictionaryGrpc.DictionaryImplBase;
import org.spin.backend.grpc.dictionary.EntityRequest;
import org.spin.backend.grpc.dictionary.Field;
import org.spin.backend.grpc.dictionary.FieldRequest;
import org.spin.backend.grpc.dictionary.Form;
import org.spin.backend.grpc.dictionary.ListIdentifierColumnsRequest;
import org.spin.backend.grpc.dictionary.ListIdentifierColumnsResponse;
import org.spin.backend.grpc.dictionary.ListProcessesRequest;
import org.spin.backend.grpc.dictionary.ListProcessesResponse;
import org.spin.backend.grpc.dictionary.ListSearchFieldsRequest;
import org.spin.backend.grpc.dictionary.ListSearchFieldsResponse;
import org.spin.backend.grpc.dictionary.Process;
import org.spin.backend.grpc.dictionary.Tab;
import org.spin.backend.grpc.dictionary.Window;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import ve.net.alge.grpc.util.RecordUtil;

/**
 * @author Yamel Senih, ysenih@erpya.com, ERPCyA http://www.erpya.com
 * Dictionary service
 * Get all dictionary meta-data
 */
public class Dictionary extends DictionaryImplBase {
	/**	Logger			*/
	private CLogger log = CLogger.getCLogger(Dictionary.class);
	
	@Override
	public void getWindow(EntityRequest request, StreamObserver<Window> responseObserver) {
		try {
			Window.Builder windowBuilder = getWindow(Env.getCtx(), request.getId(), true);
			responseObserver.onNext(windowBuilder.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			e.printStackTrace();
			responseObserver.onError(
				Status.INTERNAL
					.withDescription(e.getLocalizedMessage())
					.withCause(e)
					.asRuntimeException()
			);
		}
	}
	/**
	 * Request Window: can be only window or child
	 * @param request
	 * @param uuid
	 * @param id
	 * @param withTabs
	 */
	private Window.Builder getWindow(Properties context, String windowUuid, boolean withTabs) {
		if (Util.isEmpty(windowUuid, true)) {
			throw new AdempiereException("@FillMandatory@ @AD_Window_ID@ / @UUID@");
		}
		int windowId = RecordUtil.getIdFromUuid(I_AD_Window.Table_Name, windowUuid, null);
		if (windowId <= 0) {
			throw new AdempiereException("@FillMandatory@ @AD_Window_ID@");
		}
		MWindow window = MWindow.get(context, windowId);
		if (window == null || window.getAD_Window_ID() <= 0) {
			throw new AdempiereException("@AD_Window_ID@ @NotFound@");
		}
		return WindowConvertUtil.convertWindow(
			context,
			window,
			withTabs
		);
	}



	@Override
	public void getTab(EntityRequest request, StreamObserver<Tab> responseObserver) {
		try {
			Tab.Builder tabBuilder = getTab(Env.getCtx(), request.getId(), true);
			responseObserver.onNext(tabBuilder.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			e.printStackTrace();
			responseObserver.onError(
				Status.INTERNAL
					.withDescription(e.getLocalizedMessage())
					.withCause(e)
					.asRuntimeException()
			);
		}
	}

	/**
	 * Convert Tabs from UUID
	 * @param uuid
	 * @param withFields
	 * @return
	 */
	private Tab.Builder getTab(Properties context, String tabUuid, boolean withFields) {
		if (Util.isEmpty(tabUuid, true)) {
			throw new AdempiereException("@FillMandatory@ @AD_Tab_ID@ / @UUID@");
		}
		int tabId = RecordUtil.getIdFromUuid(I_AD_Window.Table_Name, tabUuid, null);
		if (tabId <= 0) {
			throw new AdempiereException("@FillMandatory@ @AD_Tab_ID@ ");
		}
		MTab tab = MTab.get(tabId);
		if (tab == null || tab.getAD_Tab_ID() <= 0) {
			throw new AdempiereException("@AD_Tab_ID@ @NotFound@");
		}
		//	Convert
		return WindowConvertUtil.convertTab(
			context,
			tab,
			null,
			withFields
		);
	}



	@Override
	public void getProcess(EntityRequest request, StreamObserver<Process> responseObserver) {
		try {
			Process.Builder processBuilder = DictionaryServiceLogic.getProcess(Env.getCtx(), request.getId(), true);
			responseObserver.onNext(processBuilder.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			e.printStackTrace();
			responseObserver.onError(
				Status.INTERNAL
					.withDescription(e.getLocalizedMessage())
					.withCause(e)
					.asRuntimeException()
			);
		}
	}

	@Override
	public void listProcesses(ListProcessesRequest request, StreamObserver<ListProcessesResponse> responseObserver) {
		try {
			ListProcessesResponse.Builder processBuilder = DictionaryServiceLogic.listProcesses(request);
			responseObserver.onNext(processBuilder.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			e.printStackTrace();
			responseObserver.onError(
				Status.INTERNAL
					.withDescription(e.getLocalizedMessage())
					.withCause(e)
					.asRuntimeException()
			);
		}
	}



	@Override
	public void getBrowser(EntityRequest request, StreamObserver<Browser> responseObserver) {
		try {
			Browser.Builder browserBuilder = getBrowser(Env.getCtx(), request.getId(), true);
			responseObserver.onNext(browserBuilder.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			e.printStackTrace();
			responseObserver.onError(
				Status.INTERNAL
					.withDescription(e.getLocalizedMessage())
					.withCause(e)
					.asRuntimeException()
			);
		}
	}

	/**
	 * Convert Browser from UUID
	 * @param uuid
	 * @param withFields
	 * @return
	 */
	private Browser.Builder getBrowser(Properties context, String browseUuid, boolean withFields) {
		if (Util.isEmpty(browseUuid, true)) {
			throw new AdempiereException("@FillMandatory@ @AD_Browse_ID@ / @UUID@");
		}
		int browseId = RecordUtil.getIdFromUuid(I_AD_Browse.Table_Name, browseUuid, null);
		if (browseId <= 0) {
			throw new AdempiereException("@FillMandatory@ @AD_Browse_ID@");
		}
		MBrowse browser = MBrowse.get(
			context,
			browseId
		);
		if (browser == null || browser.getAD_Browse_ID() <= 0) {
			throw new AdempiereException("@AD_Browse_ID@ @NotFound@");
		}
		//	Convert
		return BrowseConverUtil.convertBrowser(
			context,
			browser,
			withFields
		);
	}



	@Override
	public void getForm(EntityRequest request, StreamObserver<Form> responseObserver) {
		try {
			Form.Builder formBuilder = getForm(Env.getCtx(), request.getId());
			responseObserver.onNext(formBuilder.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			e.printStackTrace();
			responseObserver.onError(
				Status.INTERNAL
					.withDescription(e.getLocalizedMessage())
					.withCause(e)
					.asRuntimeException()
			);
		}
	}

	/**
	 * Request Form from uuid
	 * @param context
	 * @param uuid
	 * @param id
	 */
	private Form.Builder getForm(Properties context, String formUuid) {
		if (Util.isEmpty(formUuid, true)) {
			throw new AdempiereException("@FillMandatory@ @AD_Form_ID@ / @UUID@");
		}
		int formId = RecordUtil.getIdFromUuid(I_AD_Form.Table_Name, formUuid, null);
		if (formId <= 0) {
			throw new AdempiereException("@FillMandatory@ @AD_Form_ID@");
		}
		final String whereClause = I_AD_Form.COLUMNNAME_AD_Form_ID + " = ?";
		MForm form = new Query(
			context,
			I_AD_Form.Table_Name,
			whereClause,
			null
		)
			.setParameters(formId)
			.setOnlyActiveRecords(true)
			.first();

		if (form == null || form.getAD_Form_ID() <= 0) {
			throw new AdempiereException("@AD_Form_ID@ @NotFound@");
		}
		return DictionaryConvertUtil.convertForm(
			context,
			form
		);
	}



//	/**
//	 * Get Field group from Tab
//	 * @param tabId
//	 * @return
//	 */
//	private int[] getFieldGroupIdsFromTab(int tabId) {
//		return DB.getIDsEx(null, "SELECT f.AD_FieldGroup_ID "
//				+ "FROM AD_Field f "
//				+ "INNER JOIN AD_FieldGroup fg ON(fg.AD_FieldGroup_ID = f.AD_FieldGroup_ID) "
//				+ "WHERE f.AD_Tab_ID = ? "
//				+ "AND fg.FieldGroupType = ? "
//				+ "GROUP BY f.AD_FieldGroup_ID", tabId, X_AD_FieldGroup.FIELDGROUPTYPE_Tab);
//	}



	@Override
	public void getField(FieldRequest request, StreamObserver<Field> responseObserver) {
		try {
			Field.Builder fieldBuilder = getField(request);
			responseObserver.onNext(fieldBuilder.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			e.printStackTrace();
			responseObserver.onError(
				Status.INTERNAL
					.withDescription(e.getLocalizedMessage())
					.withCause(e)
					.asRuntimeException()
			);
		}
	}

	/**
	 * Convert field from request
	 * @param context
	 * @param request
	 * @return
	 */
	private Field.Builder getField(FieldRequest request) {
		Field.Builder builder = Field.newBuilder();
		//	For UUID
		if(request.getId() > 0) {
			builder = convertFieldById(Env.getCtx(), request.getId());
		} else if(request.getColumnId() > 0) {
			MColumn column = MColumn.get(Env.getCtx(), request.getColumnId());
			if (column == null || column.getAD_Column_ID() <= 0) {
				throw new AdempiereException("@AD_Column_ID@ @NotFound@");
			}
			builder = DictionaryConvertUtil.convertFieldByColumn(Env.getCtx(), column);
		} else if(request.getElementId() > 0) {
			M_Element element = new M_Element(Env.getCtx(), request.getElementId(), null);
			builder = DictionaryConvertUtil.convertFieldByElemnt(Env.getCtx(), element);
		} else if(!Util.isEmpty(request.getElementColumnName())) {
			M_Element element = M_Element.get(Env.getCtx(), request.getElementColumnName());
			builder = DictionaryConvertUtil.convertFieldByElemnt(Env.getCtx(), element);
		} else if(!Util.isEmpty(request.getTableName(), true)
				&& !Util.isEmpty(request.getColumnName(), true)) {
			MTable table = MTable.get(Env.getCtx(), request.getTableName());
			if (table == null || table.getAD_Table_ID() <= 0) {
				throw new AdempiereException("@AD_Table_ID@ @NotFound@");
			}
			MColumn column = table.getColumn(request.getColumnName());
			if (column == null || column.getAD_Column_ID() <= 0) {
				throw new AdempiereException("@AD_Column_ID@ @NotFound@");
			}
			builder = DictionaryConvertUtil.convertFieldByColumn(Env.getCtx(), column);
		}
		return builder;
	}
	
	/**
	 * Convert Field from UUID
	 * @param id
	 * @return
	 */
	private Field.Builder convertFieldById(Properties context, int id) {
		MField field = new Query(
			context,
			I_AD_Field.Table_Name,
			I_AD_Field.COLUMNNAME_AD_Field_ID + " = ?",
			null
		)
			.setParameters(id)
			.setOnlyActiveRecords(true)
			.first()
		;
				
		// TODO: Remove conditional with fix the issue https://github.com/solop-develop/backend/issues/28
		String language = context.getProperty(Env.LANGUAGE);
		if(!Language.isBaseLanguage(language)) {
			//	Name
			String name = field.get_Translation(I_AD_Field.COLUMNNAME_Name, language);
			if (!Util.isEmpty(name, true)) {
				field.set_ValueOfColumn(I_AD_Field.COLUMNNAME_Name, name);
			}
			//	Description
			String description = field.get_Translation(I_AD_Field.COLUMNNAME_Description, language);
			if (!Util.isEmpty(description, true)) {
				field.set_ValueOfColumn(I_AD_Field.COLUMNNAME_Description, description);
			}
			//	Help
			String help = field.get_Translation(I_AD_Tab.COLUMNNAME_Help, language);
			if (!Util.isEmpty(help, true)) {
				field.set_ValueOfColumn(I_AD_Field.COLUMNNAME_Help, help);
			}
		}
		//	Convert
		return WindowConvertUtil.convertField(
			context,
			field,
			true
		);
	}
	


	@Override
	public void listIdentifiersColumns(ListIdentifierColumnsRequest request, StreamObserver<ListIdentifierColumnsResponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Request Null");
			}
			ListIdentifierColumnsResponse.Builder fielsListBuilder = DictionaryServiceLogic.getIdentifierFields(request);
			responseObserver.onNext(fielsListBuilder.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			e.printStackTrace();
			responseObserver.onError(
				Status.INTERNAL
					.withDescription(e.getLocalizedMessage())
					.withCause(e)
					.asRuntimeException()
			);
		}
	}



	@Override
	public void listSearchFields(ListSearchFieldsRequest request, StreamObserver<ListSearchFieldsResponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Request Null");
			}
			ListSearchFieldsResponse.Builder fielsListBuilder = DictionaryServiceLogic.listSearchFields(request);
			responseObserver.onNext(fielsListBuilder.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			e.printStackTrace();
			responseObserver.onError(
				Status.INTERNAL
					.withDescription(e.getLocalizedMessage())
					.withCause(e)
					.asRuntimeException()
			);
		}
	}

}
