/************************************************************************************
 * Copyright (C) 2012-present E.R.P. Consultores y Asociados, C.A.                  *
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
package ve.net.alge.grpc.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.I_AD_PInstance;
import org.compiere.model.I_AD_Process_Para;
import org.compiere.model.I_C_Order;
import org.compiere.model.MMenu;
import org.compiere.model.MPInstance;
import org.compiere.model.MProcess;
import org.compiere.model.MProcessPara;
import org.compiere.model.MTable;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.process.ProcessInfo;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Trx;
import org.compiere.util.Util;
// import org.spin.service.grpc.util.db.CountUtil;
import org.eevolution.services.dsl.ProcessBuilder;
import org.spin.backend.grpc.common.BusinessDataGrpc.BusinessDataImplBase;
import org.spin.backend.grpc.common.CreateEntityRequest;
import org.spin.backend.grpc.common.DeleteEntitiesBatchRequest;
import org.spin.backend.grpc.common.DeleteEntityRequest;
import org.spin.backend.grpc.common.Entity;
import org.spin.backend.grpc.common.GetEntityRequest;
import org.spin.backend.grpc.common.KeyValueSelection;
import org.spin.backend.grpc.common.ListEntitiesRequest;
import org.spin.backend.grpc.common.ListEntitiesResponse;
import org.spin.backend.grpc.common.ProcessInfoLog;
import org.spin.backend.grpc.common.ProcessLog;
import org.spin.backend.grpc.common.RunBusinessProcessRequest;
import org.spin.backend.grpc.common.UpdateEntityRequest;
import org.spin.service.grpc.authentication.SessionManager;
import org.spin.service.grpc.util.db.LimitUtil;
import org.spin.service.grpc.util.query.SortingManager;
import org.spin.service.grpc.util.value.StringManager;
import org.spin.service.grpc.util.value.ValueManager;

import com.google.protobuf.Empty;
import com.google.protobuf.Value;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import ve.net.alge.grpc.util.AccessUtil;
import ve.net.alge.grpc.util.ConvertUtil;
import ve.net.alge.grpc.util.DictionaryUtil;
import ve.net.alge.grpc.util.RecordUtil;
import ve.net.alge.grpc.util.WhereClauseUtil;
import ve.net.alge.grpc.util.WindowUtil;

/**
 * https://itnext.io/customizing-grpc-generated-code-5909a2551ca1
 * @author Yamel Senih, ysenih@erpya.com, ERPCyA http://www.erpya.com
 * Business data service
 */
public class BusinessData extends BusinessDataImplBase {
	/**	Logger			*/
	private CLogger log = CLogger.getCLogger(BusinessData.class);
	@Override
	public void getEntity(GetEntityRequest request, StreamObserver<Entity> responseObserver) {
		try {
			if(request == null) {
				throw new AdempiereException("Object Request Null");
			}
			Entity.Builder entityValue = getEntity(request);
			responseObserver.onNext(entityValue.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			responseObserver.onError(Status.INTERNAL
					.withDescription(e.getLocalizedMessage())
					.withCause(e)
					.asRuntimeException());
		}
	}
	
	@Override
	public void createEntity(CreateEntityRequest request, StreamObserver<Entity> responseObserver) {
		try {
			if(request == null) {
				throw new AdempiereException("Object Request Null");
			}
			Entity.Builder entityValue = createEntity(Env.getCtx(), request);
			responseObserver.onNext(entityValue.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			responseObserver.onError(Status.INTERNAL
					.withDescription(e.getLocalizedMessage())
					.withCause(e)
					.asRuntimeException());
		}
	}
	
	@Override
	public void updateEntity(UpdateEntityRequest request, StreamObserver<Entity> responseObserver) {
		try {
			if(request == null) {
				throw new AdempiereException("Object Request Null");
			}
			
			Entity.Builder entityValue = updateEntity(Env.getCtx(), request);
			responseObserver.onNext(entityValue.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			responseObserver.onError(Status.INTERNAL
					.withDescription(e.getLocalizedMessage())
					.withCause(e)
					.asRuntimeException());
		}
	}



	@Override
	public void deleteEntitiesBatch(DeleteEntitiesBatchRequest request, StreamObserver<Empty> responseObserver) {
		try {
			Empty.Builder entityValue = deleteEntities(Env.getCtx(), request);
			responseObserver.onNext(entityValue.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			responseObserver.onError(Status.INTERNAL
					.withDescription(e.getLocalizedMessage())
					.withCause(e)
					.asRuntimeException());
		}
	}


	@Override
	public void runBusinessProcess(RunBusinessProcessRequest request, StreamObserver<ProcessLog> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Request Null");
			}

			ProcessLog.Builder processReponse = runBusinessProcess(request);
			responseObserver.onNext(processReponse.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			e.printStackTrace();
			responseObserver.onError(Status.INTERNAL
				.withDescription(e.getLocalizedMessage())
				.withCause(e)
				.asRuntimeException()
			);
		}
	}
	
	/**
	 * Run a process from request
	 * @param request
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static ProcessLog.Builder runBusinessProcess(RunBusinessProcessRequest request) throws FileNotFoundException, IOException {
		if(request.getId() <= 0) {
			throw new AdempiereException("@FillMandatory@ @AD_Process_ID@");
		}
		int processId = request.getId();
		//	Get Process definition
		MProcess process = MProcess.get(
			Env.getCtx(),
			processId
		);
		if(process == null || process.getAD_Process_ID() <= 0) {
			throw new AdempiereException("@AD_Process_ID@ @NotFound@");
		}

		// Record/Role access
		boolean isWithAccess = AccessUtil.isProcessAccess(process.getAD_Process_ID());
		if(!isWithAccess) {
			if (process.isReport()) {
				throw new AdempiereException("@AccessCannotReport@");
			}
			throw new AdempiereException("@AccessCannotProcess@");
		}

		if (process.isReport()) {
			return ReportManagement.generateReport(
				processId,
				request.getParameters(),
				request.getReportType(),
				request.getPrintFormatId(),
				request.getReportViewId(),
				request.getIsSummary(),
				request.getTableName(),
				request.getRecordId()
			);
		}

		ProcessLog.Builder response = ProcessLog.newBuilder()
			.setId(
				process.getAD_Process_ID()
			);

		int tableId = 0;
		MTable table = null;
		if (!Util.isEmpty(request.getTableName(), true)) {
			table = MTable.get(Env.getCtx(), request.getTableName());
			if (table != null && table.getAD_Table_ID() > 0) {
				tableId = table.getAD_Table_ID();

				if (table.getAD_Window_ID() > 0) {
					//	Add to recent Item
					DictionaryUtil.addToRecentItem(
						MMenu.ACTION_Window,
						table.getAD_Window_ID()
					);
				}
			}
		}


		//	browser/window selection by client or generate selection by server
		List<KeyValueSelection> selectionsList = request.getSelectionsList();
		boolean isMultiSelection = false;
		if (process.get_ColumnIndex("SP003_IsMultiSelection") >= 0) {
			isMultiSelection = process.get_ValueAsBoolean("SP003_IsMultiSelection");
		}

		PO entity = null;
		int recordId = request.getRecordId();
		if (isMultiSelection && selectionsList != null && !selectionsList.isEmpty() && selectionsList.size() > 1) {
			// is window multi selection
			;
		} else if (table != null && RecordUtil.isValidId(recordId, table.getAccessLevel())) {
			entity = RecordUtil.getEntity(Env.getCtx(), table.getTableName(), recordId, null);
			if(entity != null) {
				recordId = entity.get_ID();
			}
		}

		//	Call process builder
		ProcessBuilder builder = ProcessBuilder.create(Env.getCtx())
			.process(process.getAD_Process_ID())
			.withRecordId(tableId, recordId)
			.withoutPrintPreview()
			.withoutBatchMode()
			.withWindowNo(0)
			.withTitle(process.getName())
			.withoutTransactionClose()
		;

//		if(request.getBrowserId() > 0) {
//			MBrowse browse = MBrowse.get(
//				Env.getCtx(),
//				request.getBrowserId()
//			);
//			if (browse == null || browse.getAD_Browse_ID() <= 0) {
//				throw new AdempiereException("@AD_Browse_ID@ @NotFound@");
//			}
//			List<Integer> selectionKeys = new ArrayList<>();
//			LinkedHashMap<Integer, LinkedHashMap<String, Object>> selection = new LinkedHashMap<>();
//
//			if (request.getIsAllSelection()) {
//				// get all records march with browser criteria
//				selectionsList = BrowserLogic.getAllSelectionByCriteria(
//					request.getBrowserId(),
//					request.getBrowserContextAttributes(),
//					request.getCriteriaFilters()
//				);
//			}
//			if (selectionsList == null || selectionsList.isEmpty()) {
//				throw new AdempiereException("@AD_Browse_ID@ @FillMandatory@ @Selection@");
//			}
//
//			for(KeyValueSelection selectionKey : selectionsList) {
//				selectionKeys.add(selectionKey.getSelectionId());
//				if(selectionKey.getValues().getFieldsCount() > 0) {
//					Map<String, Integer> displayTypeColumns = BrowserUtil.getBrowseFieldsSelectionDisplayType(browse);
//					LinkedHashMap<String, Object> entities = new LinkedHashMap<String, Object>(
//						ValueManager.convertValuesMapToObjects(
//							selectionKey.getValues().getFieldsMap(),
//							displayTypeColumns
//						)
//					);
//					selection.put(
//						selectionKey.getSelectionId(),
//						entities
//					);
//				}
//			}
//			MBrowseField fieldKey = browse.getFieldKey();
//			int tableSelectionId = 0;
//			String tableAlias = null;
//			//	Set Selected Values
//			if (fieldKey != null && fieldKey.get_ID() > 0) {
//				MViewDefinition viewDefinition = (MViewDefinition) fieldKey.getAD_View_Column().getAD_View_Definition();
//				tableSelectionId = viewDefinition.getAD_Table_ID();
//				tableAlias = viewDefinition.getTableAlias();
//			}
//			builder.withSelectedRecordsIds(tableSelectionId, selectionKeys, selection)
//				.withSelectedRecordsIds(tableSelectionId, tableAlias, selectionKeys)
//			;
//		} else 
		if (table != null && isMultiSelection) {
			if (selectionsList == null || selectionsList.isEmpty()) {
				throw new AdempiereException("@AD_Window_ID@ @FillMandatory@ @Selection@");
			}

			List<Integer> selectionKeys = new ArrayList<>();
			LinkedHashMap<Integer, LinkedHashMap<String, Object>> selection = new LinkedHashMap<>();
			for(KeyValueSelection selectionKey : selectionsList) {
				selectionKeys.add(selectionKey.getSelectionId());
				if(selectionKey.getValues().getFieldsCount() > 0) {
					Map<String, Integer> displayTypeColumns = WindowUtil.getTableColumnsDisplayType(table);
					LinkedHashMap<String, Object> entities = new LinkedHashMap<String, Object>(
						ValueManager.convertValuesMapToObjects(
							selectionKey.getValues().getFieldsMap(),
							displayTypeColumns
						)
					);
					selection.put(
						selectionKey.getSelectionId(),
						entities
					);
				}
			}

			builder.withSelectedRecordsIds(table.getAD_Table_ID(), selectionKeys, selection)
				.withSelectedRecordsIds(table.getAD_Table_ID(), table.getTableName(), selectionKeys)
			;
		}

		//	get document action
		String documentAction = null;
		//	Parameters
		Map<String, Value> parametersList = new HashMap<String, Value>();
		parametersList.putAll(request.getParameters().getFieldsMap());
		if(request.getParameters().getFieldsCount() > 0) {
			List<Entry<String, Value>> parametersListWithoutRange = parametersList.entrySet().parallelStream()
				.filter(parameterValue -> {
					return !parameterValue.getKey().endsWith("_To");
				})
				.collect(Collectors.toList());
			for(Entry<String, Value> parameter : parametersListWithoutRange) {
				final String columnName = parameter.getKey();
				int displayTypeId = -1;
				MProcessPara processParameter = new org.compiere.model.Query(
					Env.getCtx(),
					I_AD_Process_Para.Table_Name,
					"AD_Process_ID = ? AND (ColumnName = ? OR ColumnName = ?)",
					null
				)
					.setParameters(process.getAD_Process_ID(), columnName, columnName + "_To")
					.first()
				;
				if (processParameter != null) {
					displayTypeId = processParameter.getAD_Reference_ID();
				}

				Object value = null;
				if (displayTypeId > 0) {
					value = ValueManager.getObjectFromReference(parameter.getValue(), displayTypeId);
				} else {
					value = ValueManager.getObjectFromValue(parameter.getValue());
				}
				Optional<Entry<String, Value>> maybeToParameter = parametersList.entrySet().parallelStream()
					.filter(parameterValue -> {
						return parameterValue.getKey().equals(parameter.getKey() + "_To");
					})
					.findFirst();
				if(value != null) {
					if(maybeToParameter.isPresent()) {
						Object valueTo = null;
						if (displayTypeId > 0) {
							valueTo = ValueManager.getObjectFromReference(maybeToParameter.get().getValue(), displayTypeId);
						} else {
							valueTo = ValueManager.getObjectFromValue(maybeToParameter.get().getValue());
						}
						builder.withParameter(columnName, value, valueTo);
					} else {
						builder.withParameter(columnName, value);
					}
					//	For Document Action
					if(columnName.equals(I_C_Order.COLUMNNAME_DocAction)) {
						documentAction = (String) value;
					}
				}
			}
		}
		//	For Document
		if(process.getAD_Workflow_ID() > 0 && !Util.isEmpty(documentAction, true)
			&& entity != null && DocAction.class.isAssignableFrom(entity.getClass())) {
			return ve.net.alge.grpc.workflow.util.WorkflowUtil.startWorkflow(
				request.getTableName(),
				entity.get_ID(),
				documentAction
			);
		}

		//	Execute Process
		ProcessInfo result = null;
		try {
			result = builder.execute();
		} catch (Exception e) {
			e.printStackTrace();
			// log.severe(e.getLocalizedMessage());

			result = builder.getProcessInfo();
			//	Set error message
			String summary = result.getSummary();
			if(Util.isEmpty(summary, true)) {
				summary = e.getLocalizedMessage();
			}
			result.setSummary(
				StringManager.getValidString(
					Msg.parseTranslation(
						Env.getCtx(),
						summary
					)
				)
			);
		}

		//	Get process instance from identifier
		if(result.getAD_PInstance_ID() != 0) {
			MPInstance instance = new Query(
				Env.getCtx(),
				I_AD_PInstance.Table_Name,
				I_AD_PInstance.COLUMNNAME_AD_PInstance_ID + " = ?",
				null
			)
				.setParameters(result.getAD_PInstance_ID())
				.first();
			response.setInstanceId(instance.getAD_PInstance_ID());

			response.setLastRun(
				ValueManager.getTimestampFromDate(
					instance.getUpdated()
				)
			);
		}

		//	
		response.setIsError(result.isError());
		if(!Util.isEmpty(result.getSummary(), true)) {
			response.setSummary(
				StringManager.getValidString(
					Msg.parseTranslation(
						Env.getCtx(),
						result.getSummary()
					)
				)
			);
		}
		//	
		response.setResultTableName(
			StringManager.getValidString(
				MTable.getTableName(Env.getCtx(), result.getTable_ID()) 
			)
		);
		//	Convert Log
		if(result.getLogList() != null) {
			for(org.compiere.process.ProcessInfoLog log : result.getLogList()) {
				ProcessInfoLog.Builder infoLogBuilder = ConvertUtil.convertProcessInfoLog(log);
				response.addLogs(infoLogBuilder.build());
			}
		}

		return response;
	}
	
	/**
	 * Convert a PO from query
	 * @param request
	 * @return
	 */
	private Entity.Builder getEntity(GetEntityRequest request) {
		String tableName = request.getTableName();
		PO entity = null;
		if(request.getId() != 0) {
			entity = RecordUtil.getEntity(Env.getCtx(), tableName, request.getId(), null);
		} else if(request.getFilters() != null) {
			List<Object> parameters = new ArrayList<Object>();
			String whereClause = WhereClauseUtil.getWhereClauseFromCriteria(request.getFilters(), tableName, parameters);
			entity = RecordUtil.getEntity(Env.getCtx(), tableName, whereClause, parameters, null);
		}
		//	Return
		return ConvertUtil.convertEntity(entity);
	}



	@Override
	public void deleteEntity(DeleteEntityRequest request, StreamObserver<Empty> responseObserver) {
		try {
			Empty.Builder entityValue = deleteEntity(Env.getCtx(), request);
			responseObserver.onNext(entityValue.build());
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
	 * Delete a entity
	 * @param context
	 * @param request
	 * @return
	 */
	private Empty.Builder deleteEntity(Properties context, DeleteEntityRequest request) {
		// validate and get table
		final MTable table = RecordUtil.validateAndGetTable(
			request.getTableName()
		);

		Trx.run(transactionName -> {
			PO entity = RecordUtil.getEntity(context, table.getTableName(), request.getId(), transactionName);
			if (entity != null && RecordUtil.isValidId(entity.get_ID(), table.getAccessLevel())) {
				entity.deleteEx(true);
			}
		});
		//	Return
		return Empty.newBuilder();
	}
	
	/**
	 * Delete many entities
	 * @param context
	 * @param request
	 * @return
	 */
	private Empty.Builder deleteEntities(Properties context, DeleteEntitiesBatchRequest request) {
		// validate and get table
		final MTable table = RecordUtil.validateAndGetTable(
			request.getTableName()
		);
		Trx.run(transactionName -> {
			List<Integer> ids = request.getIdsList();
			if (ids.size() > 0) {
				ids.stream().forEach(id -> {
					PO entity = table.getPO(id, transactionName);
					if (entity != null && entity.get_ID() > 0) {
						entity.deleteEx(true);
					}
				});
			}
		});
		//	Return
		return Empty.newBuilder();
	}
	
	/**
	 * Create Entity
	 * @param context
	 * @param request
	 * @return
	 */
	private Entity.Builder createEntity(Properties context, CreateEntityRequest request) {
		// validate and get table
		final MTable table = RecordUtil.validateAndGetTable(
			request.getTableName()
		);

		PO entity = table.getPO(0, null);
		if(entity == null) {
			throw new AdempiereException("@Error@ PO is null");
		}
		Map<String, Value> attributes = request.getAttributes().getFieldsMap();
		attributes.keySet().forEach(key -> {
			Value attribute = attributes.get(key);
			int referenceId = DictionaryUtil.getReferenceId(entity.get_Table_ID(), key);
			Object value = null;
			if(referenceId > 0) {
				value = ValueManager.getObjectFromReference(attribute, referenceId);
			} 
			if(value == null) {
				value = ValueManager.getObjectFromValue(attribute);
			}
			entity.set_ValueOfColumn(key, value);
		});
		//	Save entity
		entity.saveEx();
		//	Return
		return ConvertUtil.convertEntity(entity);
	}
	
	/**
	 * Update Entity
	 * @param context
	 * @param request
	 * @return
	 */
	private Entity.Builder updateEntity(Properties context, UpdateEntityRequest request) {
		// validate and get table
		final MTable table = RecordUtil.validateAndGetTable(
			request.getTableName()
		);
		
		PO entity = RecordUtil.getEntity(context, table.getTableName(), request.getId(), null);
		if(entity != null
				&& entity.get_ID() >= 0) {
			Map<String, Value> attributes = request.getAttributes().getFieldsMap();
			attributes.keySet().forEach(key -> {
				Value attribute = attributes.get(key);
				int referenceId = DictionaryUtil.getReferenceId(entity.get_Table_ID(), key);
				Object value = null;
				if(referenceId > 0) {
					value = ValueManager.getObjectFromReference(attribute, referenceId);
				} 
				if(value == null) {
					value = ValueManager.getObjectFromValue(attribute);
				}
				entity.set_ValueOfColumn(key, value);
			});
			//	Save entity
			entity.saveEx();
		}
		//	Return
		return ConvertUtil.convertEntity(entity);
	}



	@Override
	public void listEntities(ListEntitiesRequest request, StreamObserver<ListEntitiesResponse> responseObserver) {
		try {
			ListEntitiesResponse.Builder entityValueList = listEntities(Env.getCtx(), request);
			responseObserver.onNext(entityValueList.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			e.printStackTrace();
			responseObserver.onError(Status.INTERNAL
				.withDescription(e.getLocalizedMessage())
				.withCause(e)
				.asRuntimeException()
			);
		}
	}

	/**
	 * Convert Object to list
	 * @param request
	 * @return
	 */
	private ListEntitiesResponse.Builder listEntities(Properties context, ListEntitiesRequest request) {
		StringBuffer whereClause = new StringBuffer();
		List<Object> params = new ArrayList<>();
		//	For dynamic condition
		String dynamicWhere = WhereClauseUtil.getWhereClauseFromCriteria(request.getFilters(), request.getTableName(), params);
		if(!Util.isEmpty(dynamicWhere)) {
			if(whereClause.length() > 0) {
				whereClause.append(" AND ");
			}
			//	Add
			whereClause.append(dynamicWhere);
		}

		//	TODO: Add support to this functionality with a distinct scope
		//	Add from reference
		if(!Util.isEmpty(request.getRecordReferenceUuid())) {
			String referenceWhereClause = RecordUtil.referenceWhereClauseCache.get(request.getRecordReferenceUuid());
			if(!Util.isEmpty(referenceWhereClause, true)) {
				if(whereClause.length() > 0) {
					whereClause.append(" AND ");
				}
				whereClause.append("(").append(referenceWhereClause).append(")");
			}
		}

		//	Get page and count
		String nexPageToken = null;
		int pageNumber = LimitUtil.getPageNumber(SessionManager.getSessionUuid(), request.getPageToken());
		int limit = LimitUtil.getPageSize(request.getPageSize());
		int offset = (pageNumber - 1) * limit;
		int count = 0;

		ListEntitiesResponse.Builder builder = ListEntitiesResponse.newBuilder();
		//	
//		if(Util.isEmpty(criteria.getQuery())) {
//			Query query = new Query(context, criteria.getTableName(), whereClause.toString(), null)
//					.setParameters(params);
//			count = query.count();
//			if(!Util.isEmpty(criteria.getOrderByClause())) {
//				query.setOrderBy(criteria.getOrderByClause());
//			}
//			List<PO> entityList = query
//					.setLimit(limit, offset)
//					.<PO>list();
//			//	
//			for(PO entity : entityList) {
//				Entity.Builder valueObject = ConvertUtil.convertEntity(entity);
//				builder.addRecords(valueObject.build());
//			}
//		} else {
//			StringBuilder sql = new StringBuilder(criteria.getQuery());
//			if (whereClause.length() > 0) {
//				sql.append(" WHERE ").append(whereClause); // includes first AND
//			}
//			//	
//			String parsedSQL = MRole.getDefault().addAccessSQL(sql.toString(),
//					null, MRole.SQL_FULLYQUALIFIED,
//					MRole.SQL_RO);
//
//			String orderByClause = criteria.getOrderByClause();
//			if (!Util.isEmpty(orderByClause, true)) {
//				orderByClause = " ORDER BY " + orderByClause;
//			}
//
//			//	Count records
//			count = CountUtil.countRecords(parsedSQL, criteria.getTableName(), params);
//			//	Add Row Number
//			parsedSQL = LimitUtil.getQueryWithLimit(parsedSQL, limit, offset);
//			//	Add Order By
//			parsedSQL = parsedSQL + orderByClause;
//			builder = RecordUtil.convertListEntitiesResult(MTable.get(context, criteria.getTableName()), parsedSQL, params);
//		}
		Query query = new Query(
			context,
			request.getTableName(),
			whereClause.toString(),
			null
		)
			.setParameters(params)
		;
		count = query.count();
		if(!Util.isEmpty(request.getSortBy(), true)) {
			query.setOrderBy(
				SortingManager.newInstance(
					request.getSortBy()
				).getSotingAsSQL()
			);
		}
		List<PO> entityList = query
//			.setLimit(limit, offset)
			.<PO>list()
		;
		//	
		for(PO entity : entityList) {
			Entity.Builder valueObject = ConvertUtil.convertEntity(entity);
			builder.addRecords(valueObject.build());
		}
		//	
		builder.setRecordCount(count);
		//	Set page token
		if(LimitUtil.isValidNextPageToken(count, offset, limit)) {
			nexPageToken = LimitUtil.getPagePrefix(SessionManager.getSessionUuid()) + (pageNumber + 1);
		}
		//	Set netxt page
		builder.setNextPageToken(
			StringManager.getValidString(
				nexPageToken
			)
		);
		//	Return
		return builder;
	}

}
