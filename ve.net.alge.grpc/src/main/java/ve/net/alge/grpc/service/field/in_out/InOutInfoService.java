/************************************************************************************
 * Copyright (C) 2018-present E.R.P. Consultores y Asociados, C.A.                  *
 * Contributor(s): Edwin Betancourt, EdwinBetanc0urt@outlook.com                    *
 * This program is free software: you can redistribute it and/or modify             *
 * it under the terms of the GNU General Public License as published by             *
 * the Free Software Foundation, either version 2 of the License, or                *
 * (at your option) any later version.                                              *
 * This program is distributed in the hope that it will be useful,                  *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of                   *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the                     *
 * GNU General Public License for more details.                                     *
 * You should have received a copy of the GNU General Public License                *
 * along with this program. If not, see <https://www.gnu.org/licenses/>.            *
 ************************************************************************************/
package ve.net.alge.grpc.service.field.in_out;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.adempiere.core.domains.models.I_M_InOut;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MLookupInfo;
import org.compiere.model.MRole;
import org.compiere.model.MTable;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.spin.backend.grpc.common.ListEntitiesResponse;
import org.spin.backend.grpc.inout.InOutInfoServiceGrpc.InOutInfoServiceImplBase;
import org.spin.backend.grpc.inout.ListInOutInfoRequest;
import org.spin.service.grpc.authentication.SessionManager;
import org.spin.service.grpc.util.db.CountUtil;
import org.spin.service.grpc.util.db.LimitUtil;
import org.spin.service.grpc.util.value.StringManager;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import ve.net.alge.grpc.base.db.QueryUtil;
import ve.net.alge.grpc.base.db.WhereClauseUtil;
import ve.net.alge.grpc.base.util.ContextManager;
import ve.net.alge.grpc.util.RecordUtil;
import ve.net.alge.grpc.util.ReferenceInfo;

/**
 * @author Edwin Betancourt, EdwinBetanc0urt@outlook.com, https://github.com/EdwinBetanc0urt
 * Service for backend of Update Center
 */
public class InOutInfoService extends InOutInfoServiceImplBase {
	/**	Logger			*/
	private CLogger log = CLogger.getCLogger(InOutInfoService.class);
	
	public String tableName = I_M_InOut.Table_Name;

	@Override
	public void listInOutInfo(ListInOutInfoRequest request, StreamObserver<ListEntitiesResponse> responseObserver) {
		try {
			if(request == null) {
				throw new AdempiereException("Object Request Null");
			}

			ListEntitiesResponse.Builder entityValueList = listInOutInfo(request);
			responseObserver.onNext(entityValueList.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			responseObserver.onError(Status.INTERNAL
				.withDescription(e.getLocalizedMessage())
				.withCause(e)
				.asRuntimeException());
		}
	}
	
	/**
	 * Get default value base on field, process parameter, browse field or column
	 * @param request
	 * @return
	 */
	private ListEntitiesResponse.Builder listInOutInfo(ListInOutInfoRequest request) {
		MLookupInfo reference = ReferenceInfo.getInfoFromRequest(
			request.getReferenceId(),
			request.getFieldId(),
			request.getProcessParameterId(),
			request.getBrowseFieldId(),
			request.getColumnId(),
			request.getColumnName(),
			this.tableName,
			request.getIsWithoutValidation()
		);

		int windowNo = ThreadLocalRandom.current().nextInt(1, 8996 + 1);
		ContextManager.setContextWithAttributesFromString(
			windowNo,
			Env.getCtx(),
			request.getContextAttributes()
		);

		//
		MTable table = MTable.get(Env.getCtx(), this.tableName);
		StringBuilder sql = new StringBuilder(QueryUtil.getTableQueryWithReferences(table));

		// add where with access restriction
		String sqlWithRoleAccess = MRole.getDefault(Env.getCtx(), false)
			.addAccessSQL(
				sql.toString(),
				null,
				MRole.SQL_FULLYQUALIFIED,
				MRole.SQL_RO
			);

		StringBuffer whereClause = new StringBuffer();

		// validation code of field
		if (!request.getIsWithoutValidation()) {
			String validationCode = WhereClauseUtil.getWhereRestrictionsWithAlias(
				this.tableName,
				reference.ValidationCode
			);
			if (!Util.isEmpty(reference.ValidationCode, true)) {
				String parsedValidationCode = Env.parseContext(Env.getCtx(), windowNo, validationCode, false);
				if (Util.isEmpty(parsedValidationCode, true)) {
					throw new AdempiereException("@WhereClause@ @Unparseable@");
				}
				whereClause.append(" AND ").append(parsedValidationCode);
			}
		}

		//	For dynamic condition
		List<Object> params = new ArrayList<>(); // includes on filters criteria
		String dynamicWhere = WhereClauseUtil.getWhereClauseFromCriteria(request.getFilters(), this.tableName, params);
		if (!Util.isEmpty(dynamicWhere, true)) {
			//	Add includes first AND
			whereClause.append(" AND ")
				.append("(")
				.append(dynamicWhere)
				.append(")");
		}

		sqlWithRoleAccess += whereClause;
		String parsedSQL = RecordUtil.addSearchValueAndGet(sqlWithRoleAccess, this.tableName, request.getSearchValue(), params);

		//	Get page and count
		int pageNumber = LimitUtil.getPageNumber(SessionManager.getSessionUuid(), request.getPageToken());
		int limit = LimitUtil.getPageSize(request.getPageSize());
		int offset = (pageNumber - 1) * limit;
		int count = 0;

		ListEntitiesResponse.Builder builder = ListEntitiesResponse.newBuilder();
		
		//	Count records
		count = CountUtil.countRecords(parsedSQL, this.tableName, params);
		//	Add Row Number
		parsedSQL = LimitUtil.getQueryWithLimit(parsedSQL, limit, offset);
		builder = RecordUtil.convertListEntitiesResult(MTable.get(Env.getCtx(), this.tableName), parsedSQL, params);
		//	
		builder.setRecordCount(count);
		//	Set page token
		String nexPageToken = null;
		if(LimitUtil.isValidNextPageToken(count, offset, limit)) {
			nexPageToken = LimitUtil.getPagePrefix(SessionManager.getSessionUuid()) + (pageNumber + 1);
		}
		//	Set next page
		builder.setNextPageToken(
			StringManager.getValidString(
				nexPageToken
			)
		);

		return builder;
	}

}
