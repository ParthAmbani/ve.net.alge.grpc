/************************************************************************************
 * Copyright (C) 2018-present E.R.P. Consultores y Asociados, C.A.                  *
 * Contributor(s): Edwin Betancourt, EdwinBetanc0urt@outlook.com                    *
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
package ve.net.alge.grpc.service.form.issue_management;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.adempiere.core.domains.models.I_R_RequestAction;
import org.adempiere.core.domains.models.I_R_RequestUpdate;
import org.adempiere.core.domains.models.X_R_RequestUpdate;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MRequest;
import org.compiere.model.MRequestAction;
import org.compiere.model.MRole;
import org.compiere.model.MTable;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Trx;
import org.compiere.util.Util;
import org.spin.backend.grpc.issue_management.CreateIssueCommentRequest;
import org.spin.backend.grpc.issue_management.CreateIssueRequest;
import org.spin.backend.grpc.issue_management.DeleteIssueCommentRequest;
import org.spin.backend.grpc.issue_management.DeleteIssueRequest;
import org.spin.backend.grpc.issue_management.ExistsIssuesRequest;
import org.spin.backend.grpc.issue_management.ExistsIssuesResponse;
import org.spin.backend.grpc.issue_management.Issue;
import org.spin.backend.grpc.issue_management.IssueComment;
import org.spin.backend.grpc.issue_management.IssueManagementGrpc.IssueManagementImplBase;
import org.spin.backend.grpc.issue_management.ListBusinessPartnersRequest;
import org.spin.backend.grpc.issue_management.ListBusinessPartnersResponse;
import org.spin.backend.grpc.issue_management.ListCategoriesRequest;
import org.spin.backend.grpc.issue_management.ListCategoriesResponse;
import org.spin.backend.grpc.issue_management.ListGroupsRequest;
import org.spin.backend.grpc.issue_management.ListGroupsResponse;
import org.spin.backend.grpc.issue_management.ListIssueCommentsReponse;
import org.spin.backend.grpc.issue_management.ListIssueCommentsRequest;
import org.spin.backend.grpc.issue_management.ListIssuesReponse;
import org.spin.backend.grpc.issue_management.ListIssuesRequest;
import org.spin.backend.grpc.issue_management.ListPrioritiesRequest;
import org.spin.backend.grpc.issue_management.ListPrioritiesResponse;
import org.spin.backend.grpc.issue_management.ListProjectsRequest;
import org.spin.backend.grpc.issue_management.ListProjectsResponse;
import org.spin.backend.grpc.issue_management.ListRequestTypesRequest;
import org.spin.backend.grpc.issue_management.ListRequestTypesResponse;
import org.spin.backend.grpc.issue_management.ListSalesRepresentativesRequest;
import org.spin.backend.grpc.issue_management.ListSalesRepresentativesResponse;
import org.spin.backend.grpc.issue_management.ListStatusCategoriesRequest;
import org.spin.backend.grpc.issue_management.ListStatusCategoriesResponse;
import org.spin.backend.grpc.issue_management.ListStatusesRequest;
import org.spin.backend.grpc.issue_management.ListStatusesResponse;
import org.spin.backend.grpc.issue_management.ListTaskStatusesRequest;
import org.spin.backend.grpc.issue_management.ListTaskStatusesResponse;
import org.spin.backend.grpc.issue_management.UpdateIssueCommentRequest;
import org.spin.backend.grpc.issue_management.UpdateIssueRequest;
import org.spin.service.grpc.authentication.SessionManager;
import org.spin.service.grpc.util.db.LimitUtil;
import org.spin.service.grpc.util.value.StringManager;
import org.spin.service.grpc.util.value.TimeManager;
import org.spin.service.grpc.util.value.ValueManager;

import com.google.protobuf.Empty;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import ve.net.alge.grpc.core.domains.models.MRequestUpdate;
import ve.net.alge.grpc.util.RecordUtil;

/**
 * @author Edwin Betancourt, EdwinBetanc0urt@outlook.com, https://github.com/EdwinBetanc0urt
 * Service for backend of Update Center
 */
public class IssueManagement extends IssueManagementImplBase {
	/**	Logger			*/
	private CLogger log = CLogger.getCLogger(IssueManagement.class);


	@Override
	public void listRequestTypes(ListRequestTypesRequest request, StreamObserver<ListRequestTypesResponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Requested is Null");
			}
			ListRequestTypesResponse.Builder entityValueList = IssueManagementServiceLogic.listRequestTypes(request);
			responseObserver.onNext(entityValueList.build());
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
	public void listSalesRepresentatives(ListSalesRepresentativesRequest request, StreamObserver<ListSalesRepresentativesResponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Requested is Null");
			}
			ListSalesRepresentativesResponse.Builder entityValueList = IssueManagementServiceLogic.listSalesRepresentatives(request);
			responseObserver.onNext(entityValueList.build());
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
	public void listPriorities(ListPrioritiesRequest request, StreamObserver<ListPrioritiesResponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Requested is Null");
			}
			ListPrioritiesResponse.Builder entityValueList = IssueManagementServiceLogic.listPriorities(request);
			responseObserver.onNext(entityValueList.build());
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
	public void listStatusCategories(ListStatusCategoriesRequest request, StreamObserver<ListStatusCategoriesResponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Requested is Null");
			}
			ListStatusCategoriesResponse.Builder entityValueList = IssueManagementServiceLogic.listStatusCategories(request);
			responseObserver.onNext(entityValueList.build());
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
	public void listStatuses(ListStatusesRequest request, StreamObserver<ListStatusesResponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Requested is Null");
			}
			ListStatusesResponse.Builder entityValueList = IssueManagementServiceLogic.listStatuses(request);
			responseObserver.onNext(entityValueList.build());
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
	public void listCategories(ListCategoriesRequest request, StreamObserver<ListCategoriesResponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Requested is Null");
			}
			ListCategoriesResponse.Builder entityValueList = IssueManagementServiceLogic.listCategories(request);
			responseObserver.onNext(entityValueList.build());
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
	public void listGroups(ListGroupsRequest request, StreamObserver<ListGroupsResponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Requested is Null");
			}
			ListGroupsResponse.Builder entityValueList = IssueManagementServiceLogic.listGroups(request);
			responseObserver.onNext(entityValueList.build());
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
	public void listTaskStatuses(ListTaskStatusesRequest request, StreamObserver<ListTaskStatusesResponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Requested is Null");
			}
			ListTaskStatusesResponse.Builder entityValueList = IssueManagementServiceLogic.listTaskStatuses(request);
			responseObserver.onNext(entityValueList.build());
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
	public void listBusinessPartners(ListBusinessPartnersRequest request, StreamObserver<ListBusinessPartnersResponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Requested is Null");
			}
			ListBusinessPartnersResponse.Builder entityValueList = IssueManagementServiceLogic.listBusinessPartners(request);
			responseObserver.onNext(entityValueList.build());
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
	public void listProjects(ListProjectsRequest request, StreamObserver<ListProjectsResponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Requested is Null");
			}
			ListProjectsResponse.Builder entityValueList = IssueManagementServiceLogic.listProjects(request);
			responseObserver.onNext(entityValueList.build());
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
	public void existsIssues(ExistsIssuesRequest request, StreamObserver<ExistsIssuesResponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Requested is Null");
			}
			ExistsIssuesResponse.Builder entityValueList = IssueManagementServiceLogic.existsIssues(request);
			responseObserver.onNext(entityValueList.build());
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
	public void listMyIssues(ListIssuesRequest request, StreamObserver<ListIssuesReponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Process Activity Requested is Null");
			}
			ListIssuesReponse.Builder entityValueList = IssueManagementServiceLogic.listMyIssues(request);
			responseObserver.onNext(entityValueList.build());
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
	public void listIssues(ListIssuesRequest request, StreamObserver<ListIssuesReponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("List Issues Requested is Null");
			}
			ListIssuesReponse.Builder builderList = IssueManagementServiceLogic.listIssues(request);
			responseObserver.onNext(builderList.build());
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
	public void createIssue(CreateIssueRequest request, StreamObserver<Issue> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Requested is Null");
			}
			Issue.Builder builder = createIssue(request);
			responseObserver.onNext(builder.build());
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

	private Issue.Builder createIssue(CreateIssueRequest request) {
		MRequest requestRecord = new MRequest(Env.getCtx(), 0, null);

		// create issue with record on window
		if (!Util.isEmpty(request.getTableName(), true) || request.getRecordId() > 0) {
			// validate and get table
			final MTable table = RecordUtil.validateAndGetTable(
				request.getTableName()
			);

			// validate record
			if (request.getRecordId() < 0) {
				throw new AdempiereException("@Record_ID@ / @NotFound@");
			}
			PO entity = RecordUtil.getEntity(Env.getCtx(), table.getTableName(), request.getRecordId(), null);
			if (entity == null) {
				throw new AdempiereException("@PO@ @NotFound@");
			}
			PO.copyValues(entity, requestRecord);

			// validate if entity key column exists on request to set
			String keyColumn = entity.get_TableName() + "_ID";
			if (requestRecord.get_ColumnIndex(keyColumn) >= 0) {
				requestRecord.set_ValueOfColumn(keyColumn, entity.get_ID());
			}
			requestRecord.setRecord_ID(entity.get_ID());
			requestRecord.setAD_Table_ID(table.getAD_Table_ID());
		}

		if (Util.isEmpty(request.getSubject(), true)) {
			throw new AdempiereException("@FillMandatory@ @Subject@");
		}

		if (Util.isEmpty(request.getSummary(), true)) {
			throw new AdempiereException("@FillMandatory@ @Summary@");
		}

		int requestTypeId = request.getRequestTypeId();
		if (requestTypeId <= 0) {
			throw new AdempiereException("@R_RequestType_ID@ @NotFound@");
		}

		int salesRepresentativeId = request.getSalesRepresentativeId();
		if (salesRepresentativeId <= 0) {
			throw new AdempiereException("@SalesRep_ID@ @NotFound@");
		}

		// fill values
		requestRecord.setR_RequestType_ID(requestTypeId);
		requestRecord.setR_Status_ID(request.getStatusId());
//		requestRecord.setSubject(request.getSubject());// TODO core
		requestRecord.setSummary(request.getSummary());
		requestRecord.setSalesRep_ID(salesRepresentativeId);
		requestRecord.setPriority(
			StringManager.getValidString(
				request.getPriorityValue()
			)
		);
		requestRecord.setDateNextAction(
			TimeManager.getTimestampFromString(request.getDateNextAction())
		);
		if (request.getCategoryId() > 0) {
			requestRecord.setR_Category_ID(
				request.getCategoryId()
			);
		}
		if (request.getGroupId() > 0) {
			requestRecord.setR_Group_ID(
				request.getGroupId()
			);
		}
		if (!Util.isEmpty(request.getTaskStatusValue(), true)) {
			requestRecord.setTaskStatus(
				request.getTaskStatusValue()
			);
		}
		if (request.getBusinessPartnerId() > 0) {
			requestRecord.setC_BPartner_ID(
				request.getBusinessPartnerId()
			);
		}
		if (request.getProjectId() > 0) {
			requestRecord.setC_Project_ID(
				request.getProjectId()
			);
		}
		requestRecord.saveEx();

		Issue.Builder builder = IssueManagementConvertUtil.convertRequest(requestRecord);

		return builder;
	}



	@Override
	public void updateIssue(UpdateIssueRequest request, StreamObserver<Issue> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Requested is Null");
			}
			Issue.Builder builder = updateIssue(request);
			responseObserver.onNext(builder.build());
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

	private Issue.Builder updateIssue(UpdateIssueRequest request) {
		// validate record
		int recordId = request.getId();
		if (recordId <= 0) {
			throw new AdempiereException("@Record_ID@ / @UUID@ @NotFound@");
		}
		if (Util.isEmpty(request.getSubject(), true)) {
			throw new AdempiereException("@FillMandatory@ @Subject@");
		}

		if (Util.isEmpty(request.getSummary(), true)) {
			throw new AdempiereException("@FillMandatory@ @Summary@");
		}

		int requestTypeId = request.getRequestTypeId();
		if (requestTypeId <= 0) {
			throw new AdempiereException("@R_RequestType_ID@ @NotFound@");
		}

		int salesRepresentativeId = request.getSalesRepresentativeId();
		if (salesRepresentativeId <= 0) {
			throw new AdempiereException("@SalesRep_ID@ @NotFound@");
		}

		MRequest requestRecord = new MRequest(Env.getCtx(), recordId, null);
		if (requestRecord == null || requestRecord.getR_Request_ID() <= 0) {
			throw new AdempiereException("@R_Request_ID@ @NotFound@");
		}
		requestRecord.setR_RequestType_ID(requestTypeId);
//		requestRecord.setSubject(request.getSubject());// TODO core
		requestRecord.setSummary(request.getSummary());
		requestRecord.setSalesRep_ID(salesRepresentativeId);
		requestRecord.setPriority(
			StringManager.getValidString(
				request.getPriorityValue()
			)
		);
		requestRecord.setDateNextAction(
			ValueManager.getDateFromTimestampDate(request.getDateNextAction())
		);
		
		requestRecord.setR_Status_ID(request.getStatusId());
		requestRecord.setR_Category_ID(
			request.getCategoryId()
		);
		requestRecord.setR_Group_ID(
			request.getGroupId()
		);
		String taskStatus = null;
		if (!Util.isEmpty(request.getTaskStatusValue(), true)) {
			taskStatus = request.getTaskStatusValue();
		}
		requestRecord.setTaskStatus(
			taskStatus
		);
		requestRecord.setC_BPartner_ID(
			request.getBusinessPartnerId()
		);
		requestRecord.setC_Project_ID(
			request.getProjectId()
		);

		requestRecord.saveEx();

		Issue.Builder builder = IssueManagementConvertUtil.convertRequest(requestRecord);
		return builder;
	}



	@Override
	public void deleteIssue(DeleteIssueRequest request, StreamObserver<Empty> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Requested is Null");
			}
			Empty.Builder builder = deleteIssue(request);
			responseObserver.onNext(builder.build());
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

	private Empty.Builder deleteIssue(DeleteIssueRequest request) {
		Trx.run(transactionName -> {
			// validate record
			int recordId = request.getId();
			if (recordId < 0) {
				throw new AdempiereException("@Record_ID@ / @NotFound@");
			}
			MRequest requestRecord = new MRequest(Env.getCtx(), recordId, transactionName);
			if (requestRecord == null || requestRecord.getR_Request_ID() <= 0) {
				throw new AdempiereException("@R_Request_ID@ @NotFound@");
			}

			final String whereClause = "R_Request_ID = ?";

			// delete actions
		 Arrays.stream(new Query(
				Env.getCtx(),
				I_R_RequestAction.Table_Name,
				whereClause,
				transactionName
			)
				.setParameters(requestRecord.getR_Request_ID())
				.getIDs())
				// .list(MRequestAction.class);
				.parallel()
				.forEach(requestActionId -> {
					MRequestAction requestAction = new MRequestAction(Env.getCtx(), requestActionId, null);
					requestAction.deleteEx(true);
				});

			// delete updates
		Arrays.stream(new Query(
				Env.getCtx(),
				I_R_RequestUpdate.Table_Name,
				whereClause,
				transactionName
			)
				.setParameters(requestRecord.getR_Request_ID())
				.getIDs())
				// .list(MRequestUpdate.class);
				.parallel()
				.forEach(requestUpdateId -> {
					MRequestUpdate requestUpdate = new MRequestUpdate(Env.getCtx(), requestUpdateId, null);
					requestUpdate.deleteEx(true);
				});

			// delete header
			requestRecord.deleteEx(true);
		});

		return Empty.newBuilder();
	}



	@Override
	public void listIssueComments(ListIssueCommentsRequest request, StreamObserver<ListIssueCommentsReponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Requested is Null");
			}
			ListIssueCommentsReponse.Builder builder = listIssueComments(request);
			responseObserver.onNext(builder.build());
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

	private ListIssueCommentsReponse.Builder listIssueComments(ListIssueCommentsRequest request) {
		// validate parent record
		int recordId = request.getIssueId();
		if (recordId < 0) {
			throw new AdempiereException("@Record_ID@ @NotFound@");
		}
		
		MRequest requestRecord = new MRequest(Env.getCtx(), recordId, null);
		if (requestRecord == null || requestRecord.getR_Request_ID() <= 0) {
			throw new AdempiereException("@Record_ID@ / @UUID@ @NotFound@");
		}

		final String whereClause = "R_Request_ID = ? ";
		Query queryRequestsUpdate = new Query(
			Env.getCtx(),
			I_R_RequestUpdate.Table_Name,
			whereClause,
			null
		)
			.setOnlyActiveRecords(true)
			.setParameters(recordId)
			.setApplyAccessFilter(MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO)
		;

		Query queryRequestsLog = new Query(
			Env.getCtx(),
			I_R_RequestAction.Table_Name,
			whereClause,
			null
		)
			.setOnlyActiveRecords(true)
			.setParameters(recordId)
			.setApplyAccessFilter(MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO)
		;

		int recordCount = queryRequestsUpdate.count() + queryRequestsLog.count();

		ListIssueCommentsReponse.Builder builderList = ListIssueCommentsReponse.newBuilder();
		builderList.setRecordCount(recordCount);

		String nexPageToken = null;
		int pageNumber = LimitUtil.getPageNumber(SessionManager.getSessionUuid(), request.getPageToken());
		int limit = LimitUtil.getPageSize(request.getPageSize());
		int offset = (pageNumber - 1) * limit;

		// Set page token
		if (LimitUtil.isValidNextPageToken(recordCount, offset, limit)) {
			nexPageToken = LimitUtil.getPagePrefix(SessionManager.getSessionUuid()) + (pageNumber + 1);
		}
		builderList.setNextPageToken(
			StringManager.getValidString(nexPageToken)
		);

		List<IssueComment.Builder> issueCommentsList = new ArrayList<>();
		Arrays.stream(queryRequestsUpdate
			// .setLimit(limit, offset)
				.getIDs())
			// .list(X_R_RequestUpdate.class)
			.forEach(requestUpdateId -> {
				IssueComment.Builder builder = IssueManagementConvertUtil.convertRequestUpdate(requestUpdateId);
				issueCommentsList.add(builder);
				// builderList.addRecords(builder);
			});

	Arrays.stream(queryRequestsLog
			// .setLimit(limit, offset)
			.getIDs())
			// .list(MRequestAction.class)
			.forEach(requestActionId -> {
				IssueComment.Builder builder = IssueManagementConvertUtil.convertRequestAction(requestActionId);
				issueCommentsList.add(builder);
				// builderList.addRecords(builder);
			});

		issueCommentsList.stream()
			.sorted((comment1, comment2) -> {
				Timestamp from = ValueManager.getDateFromTimestampDate(
					comment1.getCreated()
				);

				Timestamp to = ValueManager.getDateFromTimestampDate(
					comment2.getCreated()
				);

				if (from == null || to == null) {
					// prevent Null Pointer Exception
					if (from == null && to == null) {
						return 0;
					} else if (from == null) {
						return -1;
					} else if (to == null) {
						return 1;
					}
				}
				int compared = to.compareTo(from);
				return compared;
			})
			.forEach(issueComment -> {
				builderList.addRecords(issueComment);
			});

		return builderList;
	}



	@Override
	public void createIssueComment(CreateIssueCommentRequest request, StreamObserver<IssueComment> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Requested is Null");
			}
			IssueComment.Builder builder = createIssueComment(request);
			responseObserver.onNext(builder.build());
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

	private IssueComment.Builder createIssueComment(CreateIssueCommentRequest request) {
		// validate parent record
		int recordId = request.getIssueId();
		if (recordId < 0) {
			throw new AdempiereException("@Record_ID@ @NotFound@");
		}
		MRequest requestRecord = new MRequest(Env.getCtx(), recordId, null);
		requestRecord.setResult(
			StringManager.getValidString(
				request.getResult()
			)
		);
		requestRecord.saveEx();

		// requestRecord.request
		X_R_RequestUpdate requestUpdate = new Query(
			Env.getCtx(),
			I_R_RequestUpdate.Table_Name,
			"R_Request_ID = ?",
			requestRecord.get_TrxName()
		)
			.setParameters(
				requestRecord.getR_Request_ID()
			)
			.setOrderBy(
				I_R_RequestUpdate.COLUMNNAME_Created + " DESC "
			)
			.first()
		;
		return ve.net.alge.grpc.service.form.issue_management.IssueManagementConvertUtil.convertRequestUpdate(
			requestUpdate
		);
	}



	@Override
	public void updateIssueComment(UpdateIssueCommentRequest request, StreamObserver<IssueComment> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Requested is Null");
			}
			IssueComment.Builder builder = updateIssueComment(request);
			responseObserver.onNext(builder.build());
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

	private IssueComment.Builder updateIssueComment(UpdateIssueCommentRequest request) {
		// validate parent record
		int recordId = request.getId();
		if (recordId <= 0) {
			throw new AdempiereException("@Record_ID@ @NotFound@");
		}
		// validate entity
		MRequestUpdate requestUpdate = new MRequestUpdate(Env.getCtx(), recordId, null);
		if (requestUpdate == null || requestUpdate.getR_Request_ID() <= 0) {
			throw new AdempiereException("@R_RequestUpdate_ID@ @NotFound@");
		}
		int userId = Env.getAD_User_ID(Env.getCtx());
		if (requestUpdate.getCreatedBy() != userId) {
			throw new AdempiereException("@ActionNotAllowedHere@");
		}
		if (Util.isEmpty(request.getResult(), true)) {
			throw new AdempiereException("@Result@ @NotFound@");
		}

		requestUpdate.setResult(
			StringManager.getValidString(
				request.getResult()
			)
		);
		requestUpdate.saveEx();

		return ve.net.alge.grpc.service.form.issue_management.IssueManagementConvertUtil.convertRequestUpdate(requestUpdate);
	}



	@Override
	public void deleteIssueComment(DeleteIssueCommentRequest request, StreamObserver<Empty> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Requested is Null");
			}
			Empty.Builder builder = deleteIssueComment(request);
			responseObserver.onNext(builder.build());
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

	private Empty.Builder deleteIssueComment(DeleteIssueCommentRequest request) {
		// validate record
		int recordId = request.getId();
		if (recordId < 0) {
			throw new AdempiereException("@Record_ID@ @NotFound@");
		}
		// validate entity
		MRequestUpdate requestUpdate = new MRequestUpdate(Env.getCtx(), recordId, null);
		if (requestUpdate == null || requestUpdate.getR_Request_ID() <= 0) {
			throw new AdempiereException("@R_RequestUpdate_ID@ @NotFound@");
		}
		int userId = Env.getAD_User_ID(Env.getCtx());
		if (requestUpdate.getCreatedBy() != userId) {
			throw new AdempiereException("@ActionNotAllowedHere@");
		}

		requestUpdate.deleteEx(true);

		return Empty.newBuilder();
	}

}
