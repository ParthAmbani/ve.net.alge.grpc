/************************************************************************************
 * Copyright (C) 2018-2023 E.R.P. Consultores y Asociados, C.A.                     *
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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.I_C_Invoice;
import org.compiere.model.I_C_InvoiceLine;
import org.compiere.model.I_C_Order;
import org.compiere.model.I_C_OrderLine;
import org.compiere.model.I_M_AttributeInstance;
import org.compiere.model.I_M_AttributeSetInstance;
import org.compiere.model.I_M_AttributeUse;
import org.compiere.model.I_M_AttributeValue;
import org.compiere.model.I_M_Locator;
import org.compiere.model.I_M_Product;
import org.compiere.model.I_M_Requisition;
import org.compiere.model.I_M_RequisitionLine;
import org.compiere.model.I_M_Warehouse;
import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MAttributeSet;
import org.compiere.model.MAttributeSetInstance;
import org.compiere.model.MAttributeUse;
import org.compiere.model.MAttributeValue;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MLocator;
import org.compiere.model.MLookupInfo;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.model.MRequisitionLine;
import org.compiere.model.MRole;
import org.compiere.model.MTable;
import org.compiere.model.MWarehouse;
import org.compiere.model.Query;
import org.compiere.model.X_M_Attribute;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Trx;
import org.compiere.util.Util;
import org.spin.backend.grpc.common.ListEntitiesResponse;
import org.spin.backend.grpc.material_management.GetProductAttributeSetInstanceRequest;
import org.spin.backend.grpc.material_management.GetProductAttributeSetRequest;
import org.spin.backend.grpc.material_management.ListAvailableWarehousesRequest;
import org.spin.backend.grpc.material_management.ListAvailableWarehousesResponse;
import org.spin.backend.grpc.material_management.ListLocatorsRequest;
import org.spin.backend.grpc.material_management.ListLocatorsResponse;
import org.spin.backend.grpc.material_management.ListProductAttributeSetInstancesRequest;
import org.spin.backend.grpc.material_management.ListProductAttributeSetInstancesResponse;
import org.spin.backend.grpc.material_management.ListProductStorageRequest;
import org.spin.backend.grpc.material_management.Locator;
import org.spin.backend.grpc.material_management.MaterialManagementGrpc.MaterialManagementImplBase;
import org.spin.backend.grpc.material_management.ProductAttribute;
import org.spin.backend.grpc.material_management.ProductAttributeInstance;
import org.spin.backend.grpc.material_management.ProductAttributeSet;
import org.spin.backend.grpc.material_management.ProductAttributeSetInstance;
import org.spin.backend.grpc.material_management.ProductAttributeValue;
import org.spin.backend.grpc.material_management.SaveProductAttributeSetInstanceRequest;
import org.spin.backend.grpc.material_management.Warehouse;
import org.spin.service.grpc.authentication.SessionManager;
import org.spin.service.grpc.util.db.CountUtil;
import org.spin.service.grpc.util.db.LimitUtil;
import org.spin.service.grpc.util.value.NumberManager;
import org.spin.service.grpc.util.value.StringManager;
import org.spin.service.grpc.util.value.ValueManager;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import ve.net.alge.grpc.base.util.ContextManager;
import ve.net.alge.grpc.util.POUtils;
import ve.net.alge.grpc.util.QueryUtil;
import ve.net.alge.grpc.util.RecordUtil;
import ve.net.alge.grpc.util.ReferenceInfo;
import ve.net.alge.grpc.util.WhereClauseUtil;

/**
 * @author Edwin Betancourt, EdwinBetanc0urt@outlook.com, https://github.com/EdwinBetanc0urt
 * Service for Material Management
 */
public class MaterialManagement extends MaterialManagementImplBase {
	/**	Logger			*/
	private CLogger log = CLogger.getCLogger(MaterialManagement.class);

	
	@Override
	public void listProductStorage(ListProductStorageRequest request, StreamObserver<ListEntitiesResponse> responseObserver) {
		try {
			if(request == null) {
				throw new AdempiereException("Object Request Null");
			}
			ListEntitiesResponse.Builder entitiesList = listProductStorage(request);
			responseObserver.onNext(entitiesList.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			responseObserver.onError(Status.INTERNAL
				.withDescription(e.getLocalizedMessage())
				.withCause(e)
				.asRuntimeException());
		}
	}

	private ListEntitiesResponse.Builder listProductStorage(ListProductStorageRequest request) {
		//
		String tableName = "RV_Storage";
		MTable table = MTable.get(Env.getCtx(), tableName);
		StringBuilder sql = new StringBuilder(QueryUtil.getTableQueryWithReferences(table));
		StringBuffer whereClause = new StringBuffer(" WHERE 1=1 ");

		//	For dynamic condition
		List<Object> parametersList = new ArrayList<>(); // includes on filters criteria
		if (!Util.isEmpty(request.getTableName(), true)) {
			int recordId = request.getRecordId();
			if (recordId <= 0) {
				throw new AdempiereException("@Record_ID@ @NotFound@");
			}
			String where = getWhereClause(request.getTableName(), recordId, parametersList);
			whereClause.append(where);
		}
		sql.append(whereClause); 

		// add where with access restriction
		String parsedSQL = MRole.getDefault(Env.getCtx(), false)
			.addAccessSQL(sql.toString(),
				null,
				MRole.SQL_FULLYQUALIFIED,
				MRole.SQL_RO
			);

		//	Get page and count
		int pageNumber = LimitUtil.getPageNumber(SessionManager.getSessionUuid(), request.getPageToken());
		int limit = LimitUtil.getPageSize(request.getPageSize());
		int offset = (pageNumber - 1) * limit;
		int count = 0;
		ListEntitiesResponse.Builder builder = ListEntitiesResponse.newBuilder();

		//	Count records
		count = CountUtil.countRecords(parsedSQL, tableName, parametersList);
		//	Add Row Number
		parsedSQL = LimitUtil.getQueryWithLimit(parsedSQL, limit, offset);
		builder = RecordUtil.convertListEntitiesResult(MTable.get(Env.getCtx(), tableName), parsedSQL, parametersList);
		//	
		builder.setRecordCount(count);
		//	Set page token
		String nexPageToken = null;
		if(LimitUtil.isValidNextPageToken(count, offset, limit)) {
			nexPageToken = LimitUtil.getPagePrefix(SessionManager.getSessionUuid()) + (pageNumber + 1);
		}
		builder.setNextPageToken(
			StringManager.getValidString(nexPageToken)
		);

		return builder;
	}

	public static String getWhereClause(String tableName, int recordId, List<Object> parametersList) {
		String where = "";

		switch (tableName) {
			case I_M_Requisition.Table_Name:
			case I_M_RequisitionLine.Table_Name:
				where = getWhereClasuseByRequisition(tableName, recordId, parametersList);
				break;
			case I_C_Order.Table_Name:
			case I_C_OrderLine.Table_Name:
				where = getWhereClasuseByOrder(tableName, recordId, parametersList);
				break;
			case I_C_Invoice.Table_Name:
			case I_C_InvoiceLine.Table_Name:
			    where = getWhereClasuseByInvoice(tableName, recordId, parametersList);
				break;
			default:
			    where = " AND "
					+ " EXISTS(SELECT 1 FROM " + tableName
					+ " WHERE RV_Storage.M_Product_ID = "
					+ tableName + ".M_Product_ID"
					+ " AND "
					+ tableName + "." + tableName + "_ID = ?)"
				;
				parametersList.add(recordId);
				break;
		}

		return where;
	}


	public static String getWhereClasuseByRequisition(String tableName, int recordId, List<Object> parametersList) {
		int requisitionId = recordId;
		StringBuffer whereClause = new StringBuffer();
		if (tableName.equals(I_M_Requisition.Table_Name)) {
		} else if (tableName.equals(I_M_RequisitionLine.Table_Name)) {
			MRequisitionLine requisitionLine = new MRequisitionLine(Env.getCtx(), recordId, null);
			requisitionId = requisitionLine.getM_Requisition_ID();
		}
		whereClause.append(" AND ")
			.append("EXISTS(SELECT 1 FROM M_RequisitionLine WHERE ")
			.append("RV_Storage.M_Product_ID = M_RequisitionLine.M_Product_ID ")
			.append("AND M_RequisitionLine.M_Requisition_ID = ?) ")
		;
		parametersList.add(requisitionId);

		return whereClause.toString();
	}

	public static String getWhereClasuseByOrder(String tableName, int recordId, List<Object> parametersList) {
		int orderId = recordId;
		StringBuffer whereClause = new StringBuffer();
		if (tableName.equals(I_C_Order.Table_Name)) {
		} else if (tableName.equals(I_C_OrderLine.Table_Name)) {
			MOrderLine orderLine = new MOrderLine(Env.getCtx(), recordId, null);
			orderId = orderLine.getC_Order_ID();
		}
		whereClause.append(" AND ")
			.append("EXISTS(SELECT 1 FROM C_OrderLine WHERE ")
			.append("RV_Storage.M_Product_ID = C_OrderLine.M_Product_ID ")
			.append("AND C_OrderLine.C_Order_ID = ?) ")
		;
		parametersList.add(orderId);

		return whereClause.toString();
	}

	public static String getWhereClasuseByInvoice(String tableName, int recordId, List<Object> parametersList) {
		int invoiceId = recordId;
		StringBuffer whereClause = new StringBuffer();
		if (tableName.equals(I_C_Invoice.Table_Name)) {
		} else if (tableName.equals(I_C_InvoiceLine.Table_Name)) {
			MInvoiceLine invoiceLine = new MInvoiceLine(Env.getCtx(), recordId, null);
			invoiceId = invoiceLine.getC_Invoice_ID();
		}
		whereClause.append(" AND ")
			.append("EXISTS(SELECT 1 FROM C_InvoiceLine WHERE ")
			.append("RV_Storage.M_Product_ID = C_InvoiceLine.M_Product_ID ")
			.append("AND C_InvoiceLine.C_Invoice_ID = ?) ")
		;
		parametersList.add(invoiceId);

		return whereClause.toString();
	}

	@Override
	public void getProductAttributeSet(GetProductAttributeSetRequest request, StreamObserver<ProductAttributeSet> responseObserver) {
		try {
			if(request == null) {
				throw new AdempiereException("Object Request Null");
			}
			ProductAttributeSet.Builder productAttributeSetBuilder = getProductAttributeSet(request);
			responseObserver.onNext(productAttributeSetBuilder.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			responseObserver.onError(Status.INTERNAL
				.withDescription(e.getLocalizedMessage())
				.withCause(e)
				.asRuntimeException());
		}
	}

	private ProductAttributeSet.Builder getProductAttributeSet(GetProductAttributeSetRequest request) {
		int productAttributeSetId = request.getId();
		if (productAttributeSetId <= 0) {
			if (request.getProductId() > 0) {
				// get with product
				MProduct product = (MProduct) RecordUtil.getEntity(
					Env.getCtx(),
					I_M_Product.Table_Name,
					request.getProductId(),
					null
				);
				if (product == null || product.getM_Product_ID() <= 0) {
					throw new AdempiereException("@M_Product_ID@ @NotFound@");
				}
				if (product.getM_AttributeSet_ID() <= 0) {
					throw new AdempiereException("@PAttributeNoAttributeSet@");
				}
				productAttributeSetId = product.getM_AttributeSet_ID();
			} else if (request.getProductAttributeSetInstanceId() > 0) {
				// get with attribute set instance
				MAttributeSetInstance attributeSetInstance = (MAttributeSetInstance) RecordUtil.getEntity(
					Env.getCtx(),
					I_M_AttributeSetInstance.Table_Name,
					request.getProductAttributeSetInstanceId(),
					null
				);
				if (attributeSetInstance == null || attributeSetInstance.getM_AttributeSetInstance_ID() <= 0) {
					throw new AdempiereException("@M_AttributeSetInstance_ID@ @NotFound@");
				}
				if (attributeSetInstance.getM_AttributeSet_ID() <= 0) {
					throw new AdempiereException("@PAttributeNoAttributeSet@");
				}
				productAttributeSetId = attributeSetInstance.getM_AttributeSet_ID();
			}
		}

		if (productAttributeSetId <= 0) {
			throw new AdempiereException("@M_AttributeSet_ID@ @NotFound@");
		}

		ProductAttributeSet.Builder builder = convertProductAttributeSet(productAttributeSetId);

		return builder;
	}

	@Override
	public void getProductAttributeSetInstance(GetProductAttributeSetInstanceRequest request, StreamObserver<ProductAttributeSetInstance> responseObserver) {
		try {
			if (request == null) {
				throw new AdempiereException("Object Request Null");
			}
			ProductAttributeSetInstance.Builder builder = getProductAttributeSetInstance(request);
			responseObserver.onNext(builder.build());
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

	private ProductAttributeSetInstance.Builder getProductAttributeSetInstance(GetProductAttributeSetInstanceRequest request) {
		int productAttributeSetInstanceId = request.getId();
		if (productAttributeSetInstanceId <= 0) {
			if (request.getProductId() > 0) {
				// get with product
				MProduct product = (MProduct) RecordUtil.getEntity(
					Env.getCtx(),
					I_M_Product.Table_Name,
					request.getProductId(),
					null
				);
				if (product == null || product.getM_Product_ID() <= 0) {
					throw new AdempiereException("@M_Product_ID@ @NotFound@");
				}
				if (product.getM_AttributeSetInstance_ID() <= 0) {
					throw new AdempiereException("@PAttributeNoAttributeSetInstance@");
				}
				productAttributeSetInstanceId = product.getM_AttributeSetInstance_ID();
			}
		}

		if (productAttributeSetInstanceId <= 0) {
			throw new AdempiereException("@M_AttributeSetInstance_ID@ @NotFound@");
		}

		ProductAttributeSetInstance.Builder builder = convertProductAttributeSetInstance(productAttributeSetInstanceId);
		return builder;
	}

	@Override
	public void listProductAttributeSetInstances(ListProductAttributeSetInstancesRequest request, StreamObserver<ListProductAttributeSetInstancesResponse> responseObserver) {
		try {
			if(request == null) {
				throw new AdempiereException("Object Request Null");
			}
			ListProductAttributeSetInstancesResponse.Builder recordsList = listProductAttributeSetInstances(request);
			responseObserver.onNext(recordsList.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			responseObserver.onError(Status.INTERNAL
				.withDescription(e.getLocalizedMessage())
				.withCause(e)
				.asRuntimeException());
		}
	}

	private ListProductAttributeSetInstancesResponse.Builder listProductAttributeSetInstances(ListProductAttributeSetInstancesRequest request) {
		int productAttributeSetId = request.getProductAttributeSetId();
		// get with product
		if (productAttributeSetId <= 0 && request.getProductId() > 0) {
			MProduct product = (MProduct) RecordUtil.getEntity(
				Env.getCtx(),
				I_M_Product.Table_Name,
				request.getProductId(),
				null
			);
			if (product == null || product.getM_Product_ID() <= 0) {
				throw new AdempiereException("@M_Product_ID@ @NotFound@");
			}
			if (product.getM_AttributeSet_ID() <= 0) {
				throw new AdempiereException("@PAttributeNoAttributeSet@");
			}
			productAttributeSetId = product.getM_AttributeSet_ID();
		}
			
		if (productAttributeSetId <= 0) {
			throw new AdempiereException("@M_AttributeSet_ID@ @NotFound@");
		}

		String whereClause = "M_AttributeSet_ID = ?";
		List<Object> parameters = new ArrayList<Object>();
		parameters.add(productAttributeSetId);

		// Add search value to filter
		final String searchValue = ValueManager.getDecodeUrl(
			request.getSearchValue()
		);
		if (!Util.isEmpty(searchValue, true)) {
			whereClause += " AND (UPPER(Description) LIKE '%' || UPPER(?) || '%')";
			parameters.add(searchValue);
		}

		Query query =  new Query(
			Env.getCtx(),
			I_M_AttributeSetInstance.Table_Name,
			whereClause,
			null
		)
			.setClient_ID()
			.setParameters(parameters)
			.setOnlyActiveRecords(true)
			.setApplyAccessFilter(MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO)
		;

		int count = query.count();

		String nexPageToken = null;
		int pageNumber = LimitUtil.getPageNumber(SessionManager.getSessionUuid(), request.getPageToken());
		int limit = LimitUtil.getPageSize(request.getPageSize());
		int offset = (pageNumber - 1) * limit;

		List<MAttributeSetInstance> productAttributeSetInstancesList = query
			.setOrderBy("M_AttributeSet_ID, UPPER(Description) ASC")
//			.setLimit(limit, offset)
			.<MAttributeSetInstance>list();
		ListProductAttributeSetInstancesResponse.Builder builderList = ListProductAttributeSetInstancesResponse.newBuilder()
			.setRecordCount(count);

		productAttributeSetInstancesList.forEach(attributeSetInstance -> {
			ProductAttributeSetInstance.Builder attributeSetInstanceBuilder = convertProductAttributeSetInstance(attributeSetInstance);
			builderList.addRecords(attributeSetInstanceBuilder);
		});

		//  Set page token
		if (LimitUtil.isValidNextPageToken(count, offset, limit)) {
			nexPageToken = LimitUtil.getPagePrefix(SessionManager.getSessionUuid()) + (pageNumber + 1);
		}
		builderList.setNextPageToken(
			StringManager.getValidString(nexPageToken)
		);

		return builderList;
	}


	private ProductAttributeSetInstance.Builder convertProductAttributeSetInstance(int attributeSetInstanceId) {
		MAttributeSetInstance attributeSetInstance = new MAttributeSetInstance(Env.getCtx(), attributeSetInstanceId, null);
		return convertProductAttributeSetInstance(attributeSetInstance);
	}
	private ProductAttributeSetInstance.Builder convertProductAttributeSetInstance(MAttributeSetInstance attributeSetInstance) {
		ProductAttributeSetInstance.Builder builder = ProductAttributeSetInstance.newBuilder();
		if (attributeSetInstance == null) {
			return builder;
		}
		builder.setId(attributeSetInstance.getM_AttributeSetInstance_ID())
			.setDescription(
				StringManager.getValidString(
					attributeSetInstance.getDescription()
				)
			)
			.setLot(
				StringManager.getValidString(
					attributeSetInstance.getLot()
				)
			)
			.setLotId(attributeSetInstance.getM_Lot_ID())
			.setSerial(
				StringManager.getValidString(
					attributeSetInstance.getSerNo())
				)
			.setProductAttributeSet(
				convertProductAttributeSet(attributeSetInstance.getM_AttributeSet_ID())
			)
		;
		if (attributeSetInstance.getGuaranteeDate() != null) {
			builder.setGuaranteeDate(
				ValueManager.getTimestampFromDate(
					attributeSetInstance.getGuaranteeDate()
				)
			);
		}

		final String whereClause = I_M_AttributeInstance.COLUMNNAME_M_AttributeSetInstance_ID + " = ? ";
		List<MAttributeInstance> attributeInstancesList = new Query(
			Env.getCtx(),
			I_M_AttributeInstance.Table_Name,
			whereClause,
			null
		)
			.setParameters(attributeSetInstance.getM_AttributeSetInstance_ID())
			.list();
		if (attributeInstancesList != null) {
			attributeInstancesList.forEach(attributeInstance -> {
				ProductAttributeInstance.Builder attributeInstanceBuilder = convertProductAttributeInstance(
					attributeInstance
				);
				builder.addProductAttributeInstances(attributeInstanceBuilder);
			});
		}

		return builder;
	}

	private ProductAttributeInstance.Builder convertProductAttributeInstance(MAttributeInstance attributeInstance) {
		ProductAttributeInstance.Builder builder = ProductAttributeInstance.newBuilder();
		if (attributeInstance == null) {
			return builder;
		}

		BigDecimal valueNumber = attributeInstance.getValueNumber();
		// setMAttributeInstance doesn't work without decimal point
		if (valueNumber != null && valueNumber.scale() == 0) {
			valueNumber = valueNumber.setScale(1, RoundingMode.HALF_UP);
		}

		builder.setId(0)
			.setValue(
				StringManager.getValidString(
					attributeInstance.getValue()
				)
			)
			.setValueNumber(
				NumberManager.getBigDecimalToString(
					valueNumber
				)
			)
			.setProductAttributeId(attributeInstance.getM_Attribute_ID())
			.setProductAttributeValueId(attributeInstance.getM_AttributeValue_ID())
			.setProductAttributeSetInstanceId(attributeInstance.getM_AttributeSetInstance_ID())
		;
		return builder;
	}

	private ProductAttributeSet.Builder convertProductAttributeSet(int attributeSetId) {
		MAttributeSet attributeSet = MAttributeSet.get(Env.getCtx(), attributeSetId);
		return convertProductAttributeSet(attributeSet);
	}
	private ProductAttributeSet.Builder convertProductAttributeSet(MAttributeSet attributeSet) {
		ProductAttributeSet.Builder builder = ProductAttributeSet.newBuilder();
		if (attributeSet == null) {
			return builder;
		}
		builder.setId(attributeSet.getM_AttributeSet_ID())
			.setName(
				StringManager.getValidString(
					attributeSet.getName()
				)
			)
			.setDescription(
				StringManager.getValidString(
					attributeSet.getDescription()
				)
			)
			.setIsInstanceAttribute(attributeSet.isInstanceAttribute())
			.setIsLot(attributeSet.isLot())
			.setIsLotMandatory(attributeSet.isLotMandatory())
			.setLotControlId(attributeSet.getM_LotCtl_ID())
			.setLotCharStartOverwrite(
				StringManager.getValidString(
					attributeSet.getLotCharSOverwrite()
				)
			)
			.setLotCharEndOverwrite(
				StringManager.getValidString(
					attributeSet.getLotCharEOverwrite()
				)
			)
			.setIsSerial(attributeSet.isSerNo())
			.setIsSerialMandatory(attributeSet.isSerNoMandatory())
			.setSerialControlId(attributeSet.getM_SerNoCtl_ID())
			.setSerialCharStartOverwrite(
				StringManager.getValidString(
					attributeSet.getSerNoCharSOverwrite()
				)
			)
			.setSerialCharEndOverwrite(
				StringManager.getValidString(
					attributeSet.getSerNoCharEOverwrite()
				)
			)
			.setIsGuaranteeDate(attributeSet.isGuaranteeDate())
			.setIsGuaranteeDateMandatory(attributeSet.isGuaranteeDateMandatory())
			.setGuaranteeDays(attributeSet.getGuaranteeDays())
			.setMandatoryType(
				StringManager.getValidString(
					attributeSet.getMandatoryType()
				)
			)
		;
		
		new Query(
			Env.getCtx(),
			I_M_AttributeUse.Table_Name,
			"M_AttributeSet_ID = ?",
			null
		)
			.setParameters(attributeSet.getM_AttributeSet_ID())
			.setOnlyActiveRecords(true)
			.setApplyAccessFilter(MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO)
			.<MAttributeUse>list()
			.forEach(attributeUse -> {
				ProductAttribute.Builder productAttributeBuilder = convertProductAttribute(attributeUse.getM_Attribute_ID());
				productAttributeBuilder.setSequence(attributeUse.getSeqNo());

				builder.addProductAttributes(productAttributeBuilder);
			});
		
		return builder;
	}

	private ProductAttribute.Builder convertProductAttribute(int attributeId) {
		MAttribute attributeSet = new MAttribute(Env.getCtx(), attributeId, null);
		return convertProductAttribute(attributeSet);
	}
	private ProductAttribute.Builder convertProductAttribute(MAttribute attribute) {
		ProductAttribute.Builder builder = ProductAttribute.newBuilder();
		if (attribute == null) {
			return builder;
		}
		builder.setId(attribute.getM_Attribute_ID())
			.setName(
				StringManager.getValidString(
					attribute.getName()
				)
			)
			.setDescription(
				StringManager.getValidString(
					attribute.getDescription()
				)
			)
			.setValueType(
				StringManager.getValidString(
					attribute.getAttributeValueType()
				)
			)
			.setIsInstanceAttribute(attribute.isInstanceAttribute())
			.setIsMandatory(attribute.isMandatory())
		;
		
		if (X_M_Attribute.ATTRIBUTEVALUETYPE_List.equals(attribute.getAttributeValueType())) {
			new Query(
				Env.getCtx(),
				I_M_AttributeValue.Table_Name,
				"M_Attribute_ID = ?",
				null
			)
				.setParameters(attribute.getM_Attribute_ID())
				.setOnlyActiveRecords(true)
				.setApplyAccessFilter(MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO)
				.<MAttributeValue>list()
				.forEach(attributeValue -> {
					ProductAttributeValue.Builder productAttributeValueBuilder = convertProductAttributeValue(attributeValue.getM_AttributeValue_ID());
					builder.addProductAttributeValues(productAttributeValueBuilder);
				});
		}
		
		return builder;
	}

	private ProductAttributeValue.Builder convertProductAttributeValue(int productAttributeValueId) {
		MAttributeValue productAttributeValue = new MAttributeValue(Env.getCtx(), productAttributeValueId, null);
		return convertProductAttributeValue(productAttributeValue);
	}
	private ProductAttributeValue.Builder convertProductAttributeValue(MAttributeValue productAttributeValue) {
		ProductAttributeValue.Builder builder = ProductAttributeValue.newBuilder();
		if (productAttributeValue == null) {
			return builder;
		}
		builder.setId(productAttributeValue.getM_AttributeValue_ID())
			.setName(
				StringManager.getValidString(
					productAttributeValue.getName()
				)
			)
			.setDescription(
				StringManager.getValidString(
					productAttributeValue.getDescription()
				)
			)
			.setValue(
				StringManager.getValidString(
					productAttributeValue.getValue()
				)
			)
		;
		
		return builder;
	}

	@Override
	public void saveProductAttributeSetInstance(SaveProductAttributeSetInstanceRequest request, StreamObserver<ProductAttributeSetInstance> responseObserver) {
		try {
			if(request == null) {
				throw new AdempiereException("Object Request Null");
			}
			ProductAttributeSetInstance.Builder builder = saveProductAttributeSetInstance(request);
			responseObserver.onNext(builder.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			responseObserver.onError(Status.INTERNAL
				.withDescription(e.getLocalizedMessage())
				.withCause(e)
				.asRuntimeException());
		}
	}

	private ProductAttributeSetInstance.Builder saveProductAttributeSetInstance(SaveProductAttributeSetInstanceRequest request) {
		AtomicReference<MAttributeSetInstance> attributeSetInstaceAtomic = new AtomicReference<MAttributeSetInstance>();

		Trx.run(transactionName -> {
			int attributeSetInstanceId = request.getId();
			MProduct product = (MProduct) RecordUtil.getEntity(Env.getCtx(), I_M_Product.Table_Name, request.getProductId(), transactionName);
			if (product.getM_AttributeSet_ID() < 1) {
				throw new AdempiereException("@PAttributeNoAttributeSet@");
			}
			boolean isProductASI = product.getM_AttributeSetInstance_ID() > 0;

			MAttributeSetInstance attributeSetInstace = MAttributeSetInstance.get(Env.getCtx(), attributeSetInstanceId, product.getM_Product_ID());

			MAttributeSet atttibuteSet = MAttributeSet.get(Env.getCtx(), product.getM_AttributeSet_ID());
			attributeSetInstace.setM_AttributeSet_ID(product.getM_AttributeSet_ID());
			attributeSetInstace.saveEx();

			Map<String, Object> attributesValues = ValueManager.convertValuesMapToObjects(
				request.getAttributes().getFieldsMap()
			);
			List<MAttribute> attributes = Arrays.asList(atttibuteSet.getMAttributes(isProductASI));
			if (attributes == null || attributes.size() <= 0) {
				attributes = Arrays.asList(atttibuteSet.getMAttributes(!isProductASI));
			}

			// Save Instance Attributes
			attributes.stream().forEach(attribute -> {
				Object currentValue = attributesValues.get(attribute.get_UUID());
				if (MAttribute.ATTRIBUTEVALUETYPE_List.equals(attribute.getAttributeValueType())) {
					int attributeId = 0;
					if (currentValue != null) {
						attributeId = (Integer) currentValue;
					}
					MAttributeValue attributeValue = new MAttributeValue(Env.getCtx(), attributeId, transactionName);
					if (attribute.isMandatory() && (attributeValue == null || attributeValue.getM_AttributeValue_ID() <= 0)) {
						throw new AdempiereException("@M_Attribute_ID@: " + attribute.getName() + " @IsMandatory@");
					}
					attribute.setMAttributeInstance(attributeSetInstace.getM_AttributeSetInstance_ID(), attributeValue);
				}
				else if (MAttribute.ATTRIBUTEVALUETYPE_Number.equals(attribute.getAttributeValueType())) {
					BigDecimal value = null;
					if (currentValue != null) {
						if (currentValue instanceof Integer) {
							value = BigDecimal.valueOf((Integer) currentValue);
						} else {
							value = (BigDecimal) currentValue;
						}
					}
					if (attribute.isMandatory() && value == null) {
						throw new AdempiereException("@M_Attribute_ID@: " + attribute.getName() + " @IsMandatory@");
					}
					// setMAttributeInstance doesn't work without decimal point
					if (value != null && value.scale() == 0) {
						value = value.setScale(1, RoundingMode.HALF_UP);
					}
					attribute.setMAttributeInstance(attributeSetInstace.getM_AttributeSetInstance_ID(), value);
				}
				else {
					String value = null;
					if (currentValue != null) {
						value = currentValue.toString();
					}
					if (attribute.isMandatory() && Util.isEmpty(value, true)) {
						throw new AdempiereException("@M_Attribute_ID@: " + attribute.getName() + " @IsMandatory@");
					}
					attribute.setMAttributeInstance(attributeSetInstace.getM_AttributeSetInstance_ID(), value);
				}
			});

			attributeSetInstace.setDescription();
			attributeSetInstace.saveEx();
			attributeSetInstaceAtomic.set(attributeSetInstace);
		});

		ProductAttributeSetInstance.Builder builder = convertProductAttributeSetInstance(attributeSetInstaceAtomic.get());

		return builder;
	}


	@Override
	public void listAvailableWarehouses(ListAvailableWarehousesRequest request, StreamObserver<ListAvailableWarehousesResponse> responseObserver) {
		try {
			if(request == null) {
				throw new AdempiereException("Object Request Null");
			}
			ListAvailableWarehousesResponse.Builder recordsList = listAvailableWarehouses(request);
			responseObserver.onNext(recordsList.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			responseObserver.onError(Status.INTERNAL
				.withDescription(e.getLocalizedMessage())
				.withCause(e)
				.asRuntimeException());
		}
	}

	private ListAvailableWarehousesResponse.Builder listAvailableWarehouses(ListAvailableWarehousesRequest request) {
		String whereClause = "1 = 1";
		List<Object> parameters = new ArrayList<Object>();

		// Add warehouse to filter
		if (request.getWarehouseId() > 0) {
			whereClause += " AND M_Warehouse_ID = ?";
			int warehouseId = request.getWarehouseId();
			parameters.add(warehouseId);
		}

		// Add search value to filter
		final String searchValue = ValueManager.getDecodeUrl(
			request.getSearchValue()
		);
		if (!Util.isEmpty(searchValue, true)) {
			whereClause += " AND ("
				+ "UPPER(Value) LIKE '%' || UPPER(?) || '%'"
				+ "OR UPPER(Name) LIKE '%' || UPPER(?) || '%' "
				+ "OR UPPER(Description) LIKE '%' || UPPER(?) || '%' "
			+ ")";
			parameters.add(searchValue);
			parameters.add(searchValue);
			parameters.add(searchValue);
		}

		Query query = new Query(
			Env.getCtx(),
			I_M_Warehouse.Table_Name,
			whereClause,
			null
		)
			.setParameters(parameters)
			.setOnlyActiveRecords(true)
			.setApplyAccessFilter(MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO)
		;

		int pageNumber = LimitUtil.getPageNumber(SessionManager.getSessionUuid(), request.getPageToken());
		int limit = LimitUtil.getPageSize(request.getPageSize());
		int offset = (pageNumber - 1) * limit;
		int count = query.count();

		ListAvailableWarehousesResponse.Builder builderList = ListAvailableWarehousesResponse.newBuilder()
			.setRecordCount(count)
		;

		List<MWarehouse> warehousesList = query
			.setOrderBy("M_Warehouse_ID, UPPER(Value)")
//			.setLimit(limit, offset)
			.<MWarehouse>list()
		;
		if (warehousesList != null) {
			warehousesList.forEach(warehouse -> {
				Warehouse.Builder warehouseBuilder = convertAvailableWarehouse(
					warehouse
				);
				builderList.addRecords(warehouseBuilder);
			});
		}

		// Set page token
		String nexPageToken = null;
		if (LimitUtil.isValidNextPageToken(count, offset, limit)) {
			nexPageToken = LimitUtil.getPagePrefix(SessionManager.getSessionUuid()) + (pageNumber + 1);
		}
		builderList.setNextPageToken(
			StringManager.getValidString(nexPageToken)
		);

		return builderList;
	}

	private Warehouse.Builder convertAvailableWarehouse(int warehouseId) {
		MWarehouse warehouse = MWarehouse.get(Env.getCtx(), warehouseId);
		if (warehouseId == 0) {
			warehouse = new Query(
				Env.getCtx(),
				I_M_Warehouse.Table_Name,
				"M_Warehouse_ID = 0",
				null
			)
				.first()
			;
		}
		return convertAvailableWarehouse(
			warehouse
		);
	}
	private Warehouse.Builder convertAvailableWarehouse(MWarehouse warehouse) {
		Warehouse.Builder builder = Warehouse.newBuilder();
		if (warehouse == null) {
			return builder;
		}

		builder.setId(
				warehouse.getM_Warehouse_ID()
			)
			.setValue(
				StringManager.getValidString(
					warehouse.getValue()
				)
			)
			.setName(
				StringManager.getValidString(
					warehouse.getName()
				)
			)
			.setDescription(
				StringManager.getValidString(
					warehouse.getDescription()
				)
			)
			.setIsInTransit(
				warehouse.isInTransit()
			)
		;
		if (warehouse.getM_WarehouseSource_ID() > 0) {
			Warehouse.Builder builderSource = convertAvailableWarehouse(
				warehouse.getM_WarehouseSource_ID()
			);
			builder.setWarehouseSource(builderSource);
		}

		return builder;
	}


	@Override
	public void listLocators(ListLocatorsRequest request, StreamObserver<ListLocatorsResponse> responseObserver) {
		try {
			if(request == null) {
				throw new AdempiereException("Object Request Null");
			}
			ListLocatorsResponse.Builder recordsList = listLocators(request);
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

	private ListLocatorsResponse.Builder listLocators(ListLocatorsRequest request) {
		// Fill Env.getCtx()
		int windowNo = ThreadLocalRandom.current().nextInt(1, 8996 + 1);
		ContextManager.setContextWithAttributesFromStruct(windowNo, Env.getCtx(), request.getContextAttributes());

		String whereClause = "1 = 1";
		List<Object> parameters = new ArrayList<Object>();

		// Add warehouse to filter
		if (request.getWarehouseId() > 0) {
			whereClause += " AND M_Warehouse_ID = ?";
			int warehouseId = request.getWarehouseId();
			parameters.add(warehouseId);
		}

		// Add search value to filter
		final String searchValue = ValueManager.getDecodeUrl(
			request.getSearchValue()
		);
		if (!Util.isEmpty(searchValue, true)) {
			whereClause += " AND ("
				+ "(UPPER(Value) LIKE '%' || UPPER(?) || '%')"
				+ "OR EXISTS("
					+ "SELECT 1 FROM M_Warehouse AS wh "
					+ "WHERE wh.M_Warehouse_ID = M_Locator.M_Warehouse_ID "
					+ "AND (UPPER(wh.Name) LIKE '%' || UPPER(?) || '%') "
				+ ") "
				+ ") "
			;
			parameters.add(searchValue);
			parameters.add(searchValue);
		}

		// Add dynamic validation to filter
		MLookupInfo reference = ReferenceInfo.getInfoFromRequest(
			request.getReferenceId(),
			request.getFieldId(),
			request.getProcessParameterId(),
			request.getBrowseFieldId(),
			request.getColumnId(),
			request.getColumnName(),
			I_M_Locator.Table_Name
		);
		if (reference != null) {
			// validation code of field
			String validationCode = WhereClauseUtil.getWhereRestrictionsWithAlias(I_M_Locator.Table_Name, reference.ValidationCode);
			String parsedValidationCode = Env.parseContext(Env.getCtx(), windowNo, validationCode, false);
			if (!Util.isEmpty(reference.ValidationCode, true)) {
				if (Util.isEmpty(parsedValidationCode, true)) {
					throw new AdempiereException("@WhereClause@ @Unparseable@");
				}
				if (!Util.isEmpty(whereClause, true)) {
					whereClause += " AND ";
				}
				whereClause += parsedValidationCode;
			}
		}

		Query query = new Query(
			Env.getCtx(),
			I_M_Locator.Table_Name,
			whereClause,
			null
		)
			.setParameters(parameters)
			.setOnlyActiveRecords(true)
			.setApplyAccessFilter(MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO)
		;

		int pageNumber = LimitUtil.getPageNumber(SessionManager.getSessionUuid(), request.getPageToken());
		int limit = LimitUtil.getPageSize(request.getPageSize());
		int offset = (pageNumber - 1) * limit;
		int count = query.count();

		ListLocatorsResponse.Builder builderList = ListLocatorsResponse.newBuilder()
			.setRecordCount(count)
		;

		List<MLocator> locatorsList = query
			.setOrderBy("M_Warehouse_ID, UPPER(Value)")
//			.setLimit(limit, offset)
			.<MLocator>list()
		;
		if (locatorsList != null) {
			locatorsList.forEach(locator -> {
				Locator.Builder locatorBuilder = convertLocator(
					locator
				);
				builderList.addRecords(locatorBuilder);
			});
		}

		// Set page token
		String nexPageToken = null;
		if (LimitUtil.isValidNextPageToken(count, offset, limit)) {
			nexPageToken = LimitUtil.getPagePrefix(SessionManager.getSessionUuid()) + (pageNumber + 1);
		}
		builderList.setNextPageToken(
			StringManager.getValidString(nexPageToken)
		);

		return builderList;
	}

	private Locator.Builder convertLocator(MLocator locator) {
		Locator.Builder builder = Locator.newBuilder();
		if (locator == null || locator.getM_Locator_ID() <= 0) {
			return builder;
		}

		builder.setId(locator.getM_Locator_ID())
			.setUuid(
				StringManager.getValidString(
					locator.get_UUID()
				)
			)
			.setValue(
				StringManager.getValidString(
					locator.getValue()
				)
			)
			.setDisplayValue(
				StringManager.getValidString(
					POUtils.getDisplayValue(locator)
				)
			)
			.setIsDefault(locator.isDefault())
			.setAisle(
				StringManager.getValidString(
					locator.getX()
				)
			)
			.setBin(
				StringManager.getValidString(
					locator.getX()
				)
			)
			.setLevel(
				StringManager.getValidString(
					locator.getZ()
				)
			)
		;
		if (locator.getM_Warehouse_ID() >= 0) {
			Warehouse.Builder builderWarehouse = convertAvailableWarehouse(
				locator.getM_Warehouse_ID()
			);
			builder.setWarehouse(builderWarehouse);
		}

		return builder;
	}

}
