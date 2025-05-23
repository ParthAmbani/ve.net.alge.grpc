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
package ve.net.alge.grpc.service.dictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.compiere.model.MForm;
import org.compiere.model.MLookupInfo;
import org.compiere.model.MMenu;
import org.compiere.model.MProcess;
import org.compiere.model.MProcessPara;
import org.compiere.model.MProcessParaCustom;
import org.compiere.model.MReportView;
import org.compiere.model.MValRule;
import org.compiere.util.Util;
import org.compiere.wf.MWorkflow;
import org.spin.backend.grpc.dictionary.DependentField;
import org.spin.backend.grpc.dictionary.Field;
import org.spin.backend.grpc.dictionary.Process;
import org.spin.backend.grpc.dictionary.Reference;
import org.spin.backend.grpc.dictionary.ReportExportType;
import org.spin.service.grpc.util.value.StringManager;
import org.spin.service.grpc.util.value.ValueManager;
import org.spin.util.AbstractExportFormat;
import org.spin.util.ReportExportHandler;

import ve.net.alge.grpc.base.util.ContextManager;
import ve.net.alge.grpc.dictionary.custom.ProcessParaCustomUtil;
import ve.net.alge.grpc.util.ReferenceUtil;

public class ProcessConvertUtil {

	/**
	 * Convert process to builder
	 * @param processId
	 * @return
	 */
	public static Process.Builder convertProcess(Properties context, int processId, boolean withParams) {
		if (processId <= 0) {
			return Process.newBuilder();
		}
		MProcess process = MProcess.get(context, processId);
		return convertProcess(context, process, withParams);
	}

	/**
	 * Convert process to builder
	 * @param process
	 * @return
	 */
	public static Process.Builder convertProcess(Properties context, MProcess process, boolean withParams) {
		if (process == null) {
			return Process.newBuilder();
		}

		// TODO: Remove with fix the issue https://github.com/solop-develop/adempiere-grpc-server/issues/28
		DictionaryConvertUtil.translateEntity(context, process);

		List<MProcessPara> parametersList = Arrays.asList(process.getParameters());

		Process.Builder builder = Process.newBuilder()
			.setId(
				StringManager.getValidString(
					process.get_UUID()
				))
			.setUuid(
				StringManager.getValidString(
					process.get_UUID()
				)
			)
			.setInternalId(
				process.getAD_Process_ID()
			)
			.setCode(
				StringManager.getValidString(
					process.getValue()
				)
			)
			.setName(
				StringManager.getValidString(
					process.getName()
				)
			)
			.setDescription(
				StringManager.getValidString(
					process.getDescription()
				)
			)
			.setHelp(
				StringManager.getValidString(
					process.getHelp()
				)
			)
			.setIsReport(process.isReport())
			.setIsActive(
				process.isActive()
			)
			.setShowHelp(
				process.getShowHelp()
			)
			.setIsBetaFunctionality(
				process.isBetaFunctionality()
			)
			.setHasParameters(
				parametersList != null && parametersList.size() > 0
			)
		;

		boolean isMultiSelection = false;
		if (process.get_ColumnIndex("SP003_IsMultiSelection") >= 0) {
			isMultiSelection = process.get_ValueAsBoolean("SP003_IsMultiSelection");
		}
		builder.setIsMultiSelection(isMultiSelection);

		//	Report Types
		if(process.isReport()) {
			builder.setIsProcessBeforeLaunch(
					!Util.isEmpty(process.getClassname(), true)
				)
				.setIsJasperReport(
					!Util.isEmpty(process.getJasperReport(), true)
				)
			;
			if (process.getAD_PrintFormat_ID() > 0) {
				builder.setPrintFormatId(
					process.getAD_PrintFormat_ID()
				);
			}
			MReportView reportView = null;
			if(process.getAD_ReportView_ID() > 0) {
				builder.setReportViewId(
					process.getAD_ReportView_ID()
				);
				reportView = MReportView.get(context, process.getAD_ReportView_ID());
			}
			ReportExportHandler exportHandler = new ReportExportHandler(context, reportView);
			for(AbstractExportFormat reportType : exportHandler.getExportFormatList()) {
				ReportExportType.Builder reportExportType = ReportExportType.newBuilder()
					.setName(
						StringManager.getValidString(
							reportType.getName()
						)
					)
					.setType(
						StringManager.getValidString(
							reportType.getExtension()
						)
					)
				;
				builder.addReportExportTypes(reportExportType.build());
			}
		} 
		else {
			// TODO core
//			if (process.getAD_Browse_ID() > 0) {
//				MBrowse browse = MBrowse.get(
//					context,
//					process.getAD_Browse_ID()
//				);
//				builder.setBrowserId(
//						process.getAD_Browse_ID()
//					)
//					.setBrowser(
//						DictionaryConvertUtil.getDictionaryEntity(
//							browse
//						)
//					)
//				;
//			} else
			if (process.getAD_Form_ID() > 0) {
				MForm form = new MForm(context, process.getAD_Workflow_ID(), null);
				builder.setFormId(
						process.getAD_Form_ID()
					)
					.setForm(
						DictionaryConvertUtil.getDictionaryEntity(
							form
						)
					)
				;
			} else if (process.getAD_Workflow_ID() > 0) {
				MWorkflow workflow = MWorkflow.get(context, process.getAD_Workflow_ID());
				builder.setWorkflowId(
						process.getAD_Workflow_ID()
					)
					.setWorkflow(
						DictionaryConvertUtil.getDictionaryEntity(
							workflow
						)
					)
				;
			}
		}

		//	For parameters
		if(withParams && parametersList != null && parametersList.size() > 0) {
			for(MProcessPara parameter : parametersList) {
				if (parameter == null) {
					continue;
				}
				Field.Builder fieldBuilder = ProcessConvertUtil.convertProcessParameter(
					context,
					parameter
				);
				builder.addParameters(fieldBuilder.build());
			}
		}

		//	Add to recent Item
		if (process.isReport()) {
			ve.net.alge.grpc.dictionary.util.DictionaryUtil.addToRecentItem(
				MMenu.ACTION_Report,
				process.getAD_Process_ID()
			);
		} else {
			ve.net.alge.grpc.dictionary.util.DictionaryUtil.addToRecentItem(
				MMenu.ACTION_Process,
				process.getAD_Process_ID()
			);
		}

		return builder;
	}


	public static List<DependentField> generateDependentProcessParameters(MProcessPara processParameter) {
		List<DependentField> depenentFieldsList = new ArrayList<>();
		if (processParameter == null) {
			return depenentFieldsList;
		}

		MProcess process = MProcess.get(
			processParameter.getCtx(),
			processParameter.getAD_Process_ID()
		);
		List<MProcessPara> parametersList = Arrays.asList(process.getParameters());
		if (parametersList == null || parametersList.isEmpty()) {
			return depenentFieldsList;
		}

		final String parentColumnName = processParameter.getColumnName();
		parametersList.stream()
			.filter(currentParameter -> {
				if (currentParameter == null || !currentParameter.isActive()) {
					return false;
				}
				// Display Logic
				if (ContextManager.isUseParentColumnOnContext(parentColumnName, currentParameter.getDisplayLogic())) {
					return true;
				}
				// Default Value of Column
				if (ContextManager.isUseParentColumnOnContext(parentColumnName, currentParameter.getDefaultValue())) {
					return true;
				}
				// TODO: Validate range with `_To` suffix
				if (ContextManager.isUseParentColumnOnContext(parentColumnName, currentParameter.getDefaultValue2())) {
					return true;
				}
				// ReadOnly Logic
				if (ContextManager.isUseParentColumnOnContext(parentColumnName, currentParameter.getReadOnlyLogic())) {
					return true;
				}
				// Dynamic Validation
				if (currentParameter.getAD_Val_Rule_ID() > 0) {
					MValRule validationRule = MValRule.get(
						currentParameter.getCtx(),
						currentParameter.getAD_Val_Rule_ID()
					);
					if (ContextManager.isUseParentColumnOnContext(parentColumnName, validationRule.getCode())) {
						return true;
					}
				}
				return false;
			})
			.forEach(currentParameter -> {
				final String currentColumnName = currentParameter.getColumnName();
				DependentField.Builder builder = DependentField.newBuilder()
					.setId(
						ValueManager.validateNull(
							currentParameter.get_UUID()
						)
					)
					.setUuid(
						ValueManager.validateNull(
							currentParameter.get_UUID()
						)
					)
					.setInternalId(
						currentParameter.getAD_Process_Para_ID()
					)
					.setColumnName(
						ValueManager.validateNull(
							currentColumnName
						)
					)
					.setParentId(
						process.getAD_Process_ID()
					)
					.setParentUuid(
						ValueManager.validateNull(
							process.get_UUID()
						)
					)
					.setParentName(
						ValueManager.validateNull(
							process.getName()
						)
					)
				;

				depenentFieldsList.add(builder.build());
			});

		return depenentFieldsList;
	}


	/**
	 * Convert Process Parameter
	 * @param processParameter
	 * @return
	 */
	public static Field.Builder convertProcessParameter(Properties context, MProcessPara processParameter) {
		if (processParameter == null) {
			return Field.newBuilder();
		}

		// TODO: Remove with fix the issue https://github.com/solop-develop/backend/issues/28
		DictionaryConvertUtil.translateEntity(context, processParameter);

		//	Convert
		Field.Builder builder = Field.newBuilder()
			.setId(
				ValueManager.validateNull(
					processParameter.get_UUID()
				))
			.setUuid(
				ValueManager.validateNull(
					processParameter.get_UUID()
				)
			)
			.setInternalId(
				processParameter.getAD_Process_Para_ID()
			)
			.setName(
				ValueManager.validateNull(processParameter.getName())
			)
			.setDescription(
				ValueManager.validateNull(processParameter.getDescription())
			)
			.setHelp(
				ValueManager.validateNull(processParameter.getHelp())
			)
			.setColumnName(
				ValueManager.validateNull(processParameter.getColumnName())
			)
			.setElementName(
				ValueManager.validateNull(processParameter.getColumnName())
			)
			.setDefaultValue(
				ValueManager.validateNull(processParameter.getDefaultValue())
			)
			.setDefaultValueTo(
				ValueManager.validateNull(processParameter.getDefaultValue2())
			)
			.setDisplayLogic(
				ValueManager.validateNull(processParameter.getDisplayLogic())
			)
			.setDisplayType(processParameter.getAD_Reference_ID())
			.setIsDisplayed(true)
//			.setIsInfoOnly(processParameter.isInfoOnly())// TODO core
			.setIsMandatory(processParameter.isMandatory())
			.setIsRange(processParameter.isRange())
			.setReadOnlyLogic(
				ValueManager.validateNull(processParameter.getReadOnlyLogic())
			)
			.setSequence(processParameter.getSeqNo())
			.setValueMax(
				ValueManager.validateNull(processParameter.getValueMax())
			)
			.setValueMin(
				ValueManager.validateNull(processParameter.getValueMin())
			)
			.setVFormat(
				ValueManager.validateNull(processParameter.getVFormat())
			)
			.setFieldLength(processParameter.getFieldLength())
			.addAllContextColumnNames(
				ContextManager.getContextColumnNames(
					Optional.ofNullable(processParameter.getDefaultValue()).orElse("")
					+ Optional.ofNullable(processParameter.getDefaultValue2()).orElse("")
				)
			)
		;

		// overwrite display type `Button` to `List`, example `PaymentRule` or `Posted`
		int displayTypeId = ReferenceUtil.overwriteDisplayType(
			processParameter.getAD_Reference_ID(),
			processParameter.getAD_Reference_Value_ID()
		);
		if (ReferenceUtil.validateReference(displayTypeId)) {
			//	Reference Value
			int referenceValueId = processParameter.getAD_Reference_Value_ID();
			//	Validation Code
			int validationRuleId = processParameter.getAD_Val_Rule_ID();

			String columnName = processParameter.getColumnName();
			if (processParameter.getAD_Element_ID() > 0) {
				columnName = processParameter.getAD_Element().getColumnName();
			}

			MLookupInfo info = ReferenceUtil.getReferenceLookupInfo(
				displayTypeId, referenceValueId, columnName, validationRuleId
			);
			if (info != null) {
				Reference.Builder referenceBuilder = DictionaryConvertUtil.convertReference(context, info);
				builder.setReference(referenceBuilder.build());
			}
		}

		MProcessParaCustom processParaCustom = ProcessParaCustomUtil.getProcessParaCustom(processParameter.getAD_Process_Para_ID());
		if (processParaCustom != null && processParaCustom.isActive()) {
			// ASP default displayed field as panel
			if (processParaCustom.get_ColumnIndex(ve.net.alge.grpc.dictionary.util.DictionaryUtil.IS_DISPLAYED_AS_PANEL_COLUMN_NAME) >= 0) {
				builder.setIsDisplayedAsPanel(
					ValueManager.validateNull(
						processParaCustom.get_ValueAsString(
							ve.net.alge.grpc.dictionary.util.DictionaryUtil.IS_DISPLAYED_AS_PANEL_COLUMN_NAME
						)
					)
				);
			}
		}

		List<DependentField> dependentProcessParameters = ProcessConvertUtil.generateDependentProcessParameters(
			processParameter
		);
		builder.addAllDependentFields(dependentProcessParameters);

		return builder;
	}

}
