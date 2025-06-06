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
package ve.net.alge.grpc.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.I_C_Project;
import org.compiere.model.I_R_Request;
import org.compiere.model.MProject;
import org.compiere.model.MRequest;
import org.compiere.model.MResource;
import org.compiere.model.MResourceAssignment;
import org.compiere.model.MResourceType;
import org.compiere.model.MRole;
import org.compiere.model.MUOM;
import org.compiere.model.MUser;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.spin.backend.grpc.form.time_record.CreateTimeRecordRequest;
import org.spin.backend.grpc.form.time_record.Issue;
import org.spin.backend.grpc.form.time_record.ListIssuesRequest;
import org.spin.backend.grpc.form.time_record.ListIssuesResponse;
import org.spin.backend.grpc.form.time_record.ListProjectsRequest;
import org.spin.backend.grpc.form.time_record.ListProjectsResponse;
import org.spin.backend.grpc.form.time_record.ListTimeRecordRequest;
import org.spin.backend.grpc.form.time_record.ListTimeRecordResponse;
import org.spin.backend.grpc.form.time_record.Project;
import org.spin.backend.grpc.form.time_record.Resource;
import org.spin.backend.grpc.form.time_record.ResourceAssignment;
import org.spin.backend.grpc.form.time_record.ResourceType;
import org.spin.backend.grpc.form.time_record.TimeRecordGrpc.TimeRecordImplBase;
import org.spin.backend.grpc.form.time_record.User;
import org.spin.service.grpc.authentication.SessionManager;
import org.spin.service.grpc.util.db.LimitUtil;
import org.spin.service.grpc.util.value.NumberManager;
import org.spin.service.grpc.util.value.ValueManager;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import ve.net.alge.grpc.service.core_functionality.CoreFunctionalityConvert;

/**
 * @author Edwin Betancourt, EdwinBetanc0urt@outlook.com, https://github.com/EdwinBetanc0urt
 * Service for Time Record
 */
public class TimeRecord extends TimeRecordImplBase {
	/**	Logger			*/
	private CLogger log = CLogger.getCLogger(TimeRecord.class);

	/**
	 * Convert MRequest to gRPC
	 * @param resourceType
	 * @return
	 */
	public static Issue.Builder convertRequest(org.compiere.model.MRequest request) {
		Issue.Builder builder = Issue.newBuilder();
		if (request == null) {
			return builder;
		}
		builder.setId(request.getR_Request_ID())
			.setDocumentNo(
				ValueManager.validateNull(
					request.getDocumentNo()
				)
			)
			// TODO core
//			.setSubject(
//				ValueManager.validateNull(
//					request.getSubject()
//				)
//			)
			.setSummary(
				ValueManager.validateNull(
					request.getSummary()
				)
			)
		;

		return builder;
	}

	@Override
	public void listIssues(ListIssuesRequest request, StreamObserver<ListIssuesResponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Request Null");
			}
			ListIssuesResponse.Builder recordsList = listIssues(request);
			responseObserver.onNext(recordsList.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			responseObserver.onError(Status.INTERNAL
				.withDescription(e.getLocalizedMessage())
				.withCause(e)
				.asRuntimeException()
			);
		}
	}

	ListIssuesResponse.Builder listIssues(ListIssuesRequest request) {
		List<Object> parametersList = new ArrayList<>();
		int userId = Env.getAD_User_ID(Env.getCtx());
		parametersList.add(userId);

		int roleId = Env.getAD_Role_ID(Env.getCtx());
		parametersList.add(roleId);

		final String whereClause = "Processed='N' "
			+ "AND (SalesRep_ID=? OR AD_Role_ID = ?) "
			+ "AND (DateNextAction IS NULL OR TRUNC(DateNextAction, 'DD') <= TRUNC(SysDate, 'DD'))"
			+ "AND (R_Status_ID IS NULL OR R_Status_ID IN (SELECT R_Status_ID FROM R_Status WHERE IsClosed='N'))"
		;

		Query queryRequests = new Query(
				Env.getCtx(),
			I_R_Request.Table_Name,
			whereClause,
			null
		)
			// .setClient_ID()
			.setOnlyActiveRecords(true)
			.setParameters(parametersList)
			.setApplyAccessFilter(MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO)
			.setOrderBy(I_R_Request.COLUMNNAME_Created)
		;

		int recordCount = queryRequests.count();

		ListIssuesResponse.Builder builderList = ListIssuesResponse.newBuilder();
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
			ValueManager.validateNull(nexPageToken)
		);

		queryRequests
//			.setLimit(limit, offset)
			.<MRequest>list()
			.forEach(requestRecord -> {
				Issue.Builder builder = convertRequest(requestRecord);
				builderList.addRecords(builder);
			});

		return builderList;
	}



	/**
	 * Convert MProject to gRPC
	 * @param project
	 * @return
	 */
	public static Project.Builder convertProject(org.compiere.model.MProject project) {
		Project.Builder builder = Project.newBuilder();
		if (project == null) {
			return builder;
		}
		builder.setId(project.getC_Project_ID())
			.setValue(
				ValueManager.validateNull(
					project.getValue()
				)
			)
			.setName(
				ValueManager.validateNull(
					project.getName()
				)
			)
		;

		return builder;
	}

	@Override
	public void listProjects(ListProjectsRequest request, StreamObserver<ListProjectsResponse> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Request Null");
			}
			ListProjectsResponse.Builder recordsList = listProjects(request);
			responseObserver.onNext(recordsList.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			responseObserver.onError(Status.INTERNAL
				.withDescription(e.getLocalizedMessage())
				.withCause(e)
				.asRuntimeException()
			);
		}
	}

	private ListProjectsResponse.Builder listProjects(ListProjectsRequest request) {
		List<Object> parametersList = new ArrayList<>();
		parametersList.add(false); // N

		final String whereClause = "Processed = ?";

		Query queryRequests = new Query(
				Env.getCtx(),
			I_C_Project.Table_Name,
			whereClause,
			null
		)
			.setParameters(parametersList)
			.setApplyAccessFilter(MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO)
			.setOnlyActiveRecords(true)
			.setOrderBy(I_C_Project.COLUMNNAME_Created)
		;

		int recordCount = queryRequests.count();

		ListProjectsResponse.Builder builderList = ListProjectsResponse.newBuilder();
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
			ValueManager.validateNull(nexPageToken)
		);

		queryRequests
//			.setLimit(limit, offset)
			.<MProject>list()
			.forEach(projectRecord -> {
				Project.Builder builder = convertProject(projectRecord);
				builderList.addRecords(builder);
			});

		return builderList;
	}

	/**
	 * Convert MResourceType to gRPC
	 * @param resourceType
	 * @return
	 */
	public static ResourceType.Builder convertResourceType(org.compiere.model.MResourceType resourceType) {
		ResourceType.Builder builder = ResourceType.newBuilder();
		if (resourceType == null) {
			return builder;
		}
		builder.setId(resourceType.getS_ResourceType_ID())
			.setValue(
				ValueManager.validateNull(
					resourceType.getValue()
				)
			)
			.setName(
				ValueManager.validateNull(
					resourceType.getName()
				)
			)
			.setDescription(
				ValueManager.validateNull(
					resourceType.getDescription()
				)
			)
		;

		MUOM unitOfMeasure = MUOM.get(Env.getCtx(), resourceType.getC_UOM_ID());
		builder.setUnitOfMeasure(
			CoreFunctionalityConvert.convertUnitOfMeasure(unitOfMeasure)
		);

		return builder;
	}

	/**
	 * Convert MUser to gRPC
	 * @param user
	 * @return
	 */
	public static User.Builder convertUser(org.compiere.model.MUser user) {
		User.Builder builder = User.newBuilder();
		if (user == null || user.getAD_User_ID() <= 0) {
			return builder;
		}
		builder.setId(user.getAD_User_ID())
			.setValue(
				ValueManager.validateNull(
					user.getValue()
				)
			)
			.setName(
				ValueManager.validateNull(
					user.getName()
				)
			)
			.setDescription(
				ValueManager.validateNull(
					user.getDescription()
				)
			)
		;

		return builder;
	}
	public static User.Builder convertUser(int userId) {
		User.Builder builder = User.newBuilder();
		if (userId <= 0) {
			return builder;
		}
		MUser user = MUser.get(Env.getCtx(), userId);

		return convertUser(user);
	}

	/**
	 * Convert MResource to gRPC
	 * @param log
	 * @return
	 */
	public static Resource.Builder convertResource(MResource resource) {
		Resource.Builder builder = Resource.newBuilder();
		if (resource == null) {
			return builder;
		}
		builder.setId(resource.getS_ResourceType_ID())
			.setName(
				ValueManager.validateNull(
					resource.getName()
				)
			)
		;

		MResourceType resourceType = MResourceType.get(Env.getCtx(), resource.getS_ResourceType_ID());
		ResourceType.Builder resourceTypeBuilder = convertResourceType(resourceType);
		builder.setResourceType(resourceTypeBuilder);
		if (resource.getAD_User_ID() > 0) {
			builder.setUser(
				convertUser(resource.getAD_User_ID())
			);
		}

		return builder;
	}

	public static ResourceAssignment.Builder convertResourceAssignment(int resourceAssignmentId) {
		ResourceAssignment.Builder builder = ResourceAssignment.newBuilder();
		if (resourceAssignmentId > 0) {
			MResourceAssignment resourceAssigment = new MResourceAssignment(Env.getCtx(), resourceAssignmentId, null);
			return convertResourceAssignment(resourceAssigment);
		}
		return builder;
	}

	/**
	 * Convert MResourceAssignment to gRPC
	 * @param log
	 * @return
	 */
	public static ResourceAssignment.Builder convertResourceAssignment(MResourceAssignment resourceAssignment) {
		ResourceAssignment.Builder builder = ResourceAssignment.newBuilder();
		if (resourceAssignment == null) {
			return builder;
		}
		builder.setId(resourceAssignment.getS_ResourceAssignment_ID())
			.setName(
				ValueManager.validateNull(
					resourceAssignment.getName()
				)
			)
			.setDescription(
				ValueManager.validateNull(
					resourceAssignment.getDescription()
				)
			)
		;
		if (resourceAssignment.getAssignDateFrom() != null) {
			builder.setAssignDateFrom(
				ValueManager.getTimestampFromDate(
					resourceAssignment.getAssignDateFrom()
				)
			);
		}
		if (resourceAssignment.getAssignDateTo() != null) {
			builder.setAssignDateTo(
				ValueManager.getTimestampFromDate(
					resourceAssignment.getAssignDateTo()
				)
			);
		}
		builder.setIsConfirmed(resourceAssignment.isConfirmed());
		builder.setQuantity(
			NumberManager.getBigDecimalToString(
				resourceAssignment.getQty()
			)
		);

		MResource resourceType = MResource.get(Env.getCtx(), resourceAssignment.getS_Resource_ID());
		Resource.Builder resourceTypeBuilder = convertResource(resourceType);
		builder.setResource(resourceTypeBuilder);

		return builder;
	}

	public static ResourceAssignment.Builder convertResourceAssignmentByResource(int resourceId) {
		ResourceAssignment.Builder builder = ResourceAssignment.newBuilder();
		MResource resource = new MResource(Env.getCtx(), resourceId, null);

		if (resource == null || resource.getS_Resource_ID() <= 0) {
			return builder;
		}
		MResourceAssignment resourceAssignment = new Query(
			Env.getCtx(),
			MResourceAssignment.Table_Name,
			" S_Resource_ID = ? ",
			null
		)
			.setParameters(resource.getS_Resource_ID())
			.first();

		return convertResourceAssignment(resourceAssignment);
	}

	@Override
	public void createTimeRecord(CreateTimeRecordRequest request, StreamObserver<ResourceAssignment> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Request Null");
			}
			ResourceAssignment.Builder entity = createResourceAssignment(request);
			responseObserver.onNext(entity.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			responseObserver.onError(Status.INTERNAL
				.withDescription(e.getLocalizedMessage())
				.withCause(e)
				.asRuntimeException()
			);
		}
	}

	private ResourceAssignment.Builder createResourceAssignment(CreateTimeRecordRequest request) {
		int orgId = Env.getAD_Org_ID(Env.getCtx());
		if (orgId <= 0) {
			throw new AdempiereException("@FillMandatory@ @AD_Org_ID@");
		}

		if (Util.isEmpty(request.getName(), true)) {
			throw new AdempiereException("@FillMandatory@ @Name@");
		}
		BigDecimal quantity = NumberManager.getBigDecimalFromString(
			request.getQuantity()
		);
		if (quantity == null) {
			throw new AdempiereException("@FillMandatory@ @Qty@");
		}

		int userId = Env.getAD_User_ID(Env.getCtx());
		MResource resource = new Query(
			Env.getCtx(),
			MResource.Table_Name,
			" AD_User_ID = ? ",
			null
		)
			.setClient_ID()
			.setOnlyActiveRecords(true)
			.setParameters(userId)
			.setApplyAccessFilter(MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO)
			.first()
		;
		if (resource == null || resource.getS_Resource_ID() <= 0) {
			throw new AdempiereException("@S_Resource_ID@ @NotFound@");
		}

		MResourceAssignment resourceAssignment = new MResourceAssignment(Env.getCtx(), 0, null);
		resourceAssignment.setS_Resource_ID(resource.getS_Resource_ID());
		resourceAssignment.getAD_Org_ID();
		resourceAssignment.setName(request.getName());
		resourceAssignment.setQty(quantity);
		resourceAssignment.setDescription(
			ValueManager.validateNull(
				request.getDescription()
			)
		);

		// set date
		Timestamp dateFrom = new Timestamp(System.currentTimeMillis());
		if (ValueManager.getDateFromTimestampDate(request.getDate()) != null) {
			dateFrom = ValueManager.getDateFromTimestampDate(
				request.getDate()
			);
		}
		resourceAssignment.setAssignDateFrom(dateFrom);

		// set project
		if (request.getProjectId() > 0) {
			int projectId = request.getProjectId();
			// checks if the column exists in the database
			if (resourceAssignment.get_ColumnIndex(I_C_Project.COLUMNNAME_C_Project_ID) >= 0) {
				resourceAssignment.set_ValueOfColumn(
					I_C_Project.COLUMNNAME_C_Project_ID,
					projectId
				);
			}
		}

		// set request
		if (request.getRequestId() > 0) {
			int requestId = request.getProjectId();
			// checks if the column exists in the database
			if (resourceAssignment.get_ColumnIndex(I_R_Request.COLUMNNAME_R_Request_ID) >= 0) {
				resourceAssignment.set_ValueOfColumn(
					I_R_Request.COLUMNNAME_R_Request_ID,
					requestId
				);
			}
		}

		resourceAssignment.saveEx();

		ResourceAssignment.Builder builder = convertResourceAssignment(resourceAssignment);
		return builder;
	}

	@Override
	public void listTimeRecord(ListTimeRecordRequest request, StreamObserver<ListTimeRecordResponse> responseObserver) {
		try {
			if(request == null) {
				throw new AdempiereException("Object Request Null");
			}
			ListTimeRecordResponse.Builder entitiesList = listTimeRecord(request);
			responseObserver.onNext(entitiesList.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			responseObserver.onError(Status.INTERNAL
				.withDescription(e.getLocalizedMessage())
				.withCause(e)
				.asRuntimeException()
			);
		}
	}

	private ListTimeRecordResponse.Builder listTimeRecord(ListTimeRecordRequest request) {
		int userId = Env.getAD_User_ID(Env.getCtx());

		String nexPageToken = null;
		int pageNumber = LimitUtil.getPageNumber(SessionManager.getSessionUuid(), request.getPageToken());
		int limit = LimitUtil.getPageSize(request.getPageSize());
		int offset = (pageNumber - 1) * limit;

		List<Object> parametersList = new ArrayList<>();
		// filter by user type
		parametersList.add(userId);
		String whereClause = " EXISTS("
			+ " SELECT 1 FROM S_Resource "
			+ " WHERE S_Resource.S_Resource_ID = S_ResourceAssignment.S_Resource_ID "
			+ " AND S_Resource.AD_User_ID = ? "
			+ ") "
		;

		if (ValueManager.getDateFromTimestampDate(request.getDateFrom()) != null) {
			Timestamp dateFrom = ValueManager.getDateFromTimestampDate(
				request.getDateFrom()
			);
			parametersList.add(dateFrom);
			whereClause += " AND AssignDateFrom = ? ";
		}
		if (ValueManager.getDateFromTimestampDate(request.getDateTo()) != null) {
			Timestamp dateTo = ValueManager.getDateFromTimestampDate(
				request.getDateTo()
			);
			parametersList.add(dateTo);
			whereClause += " AND AssignDateTo = ? ";
		}

		final String searchValue = ValueManager.getDecodeUrl(
			request.getSearchValue()
		);
		if (!Util.isEmpty(searchValue, true)) {
			whereClause += " AND ("
				+ "UPPER(Name) LIKE '%' || UPPER(?) || '%' "
				+ "OR UPPER(Description) LIKE '%' || UPPER(?) || '%' "
			+ ")";
			parametersList.add(searchValue);
			parametersList.add(searchValue);
		}

		Query query = new Query(
				Env.getCtx(),
			MResourceAssignment.Table_Name,
			whereClause,
			null
		)
			.setApplyAccessFilter(MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO)
			.setParameters(parametersList)
			.setOnlyActiveRecords(true)
			.setOrderBy(MResourceAssignment.COLUMNNAME_Created)
		;

		int count = query.count();

		List<MResourceAssignment> resourceAssignmentList = query
//				.setLimit(limit, offset)
				.list();

		ListTimeRecordResponse.Builder builderList = ListTimeRecordResponse.newBuilder();
		resourceAssignmentList.forEach(resourceAssignment -> {
			ResourceAssignment.Builder resourceAssignmentBuilder = convertResourceAssignment(resourceAssignment);
			builderList.addRecords(resourceAssignmentBuilder);
		});
		builderList.setRecordCount(count);
		// Set page token
		if (LimitUtil.isValidNextPageToken(count, offset, limit)) {
			nexPageToken = LimitUtil.getPagePrefix(SessionManager.getSessionUuid()) + (pageNumber + 1);
		}
		builderList.setNextPageToken(
			ValueManager.validateNull(nexPageToken)
		);

		return builderList;
	}

}
