
package ve.net.alge.grpc.activator;

import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import ve.net.alge.grpc.service.BusinessData;
import ve.net.alge.grpc.service.Dashboarding;
import ve.net.alge.grpc.service.Enrollment;
import ve.net.alge.grpc.service.FileManagement;
import ve.net.alge.grpc.service.GeneralLedger;
import ve.net.alge.grpc.service.LogsInfo;
import ve.net.alge.grpc.service.MaterialManagement;
import ve.net.alge.grpc.service.NoticeManagement;
import ve.net.alge.grpc.service.PaymentPrintExport;
import ve.net.alge.grpc.service.PointOfSalesForm;
import ve.net.alge.grpc.service.PreferenceManagement;
import ve.net.alge.grpc.service.RecordManagement;
import ve.net.alge.grpc.service.ReportManagement;
import ve.net.alge.grpc.service.Security;
import ve.net.alge.grpc.service.SendNotifications;
import ve.net.alge.grpc.service.TimeControl;
import ve.net.alge.grpc.service.TimeRecord;
import ve.net.alge.grpc.service.UpdateManagement;
import ve.net.alge.grpc.service.UserCustomization;
import ve.net.alge.grpc.service.UserInterface;
import ve.net.alge.grpc.service.WebStore;
import ve.net.alge.grpc.service.Workflow;
import ve.net.alge.grpc.service.core_functionality.CoreFunctionality;
import ve.net.alge.grpc.service.dictionary.Dictionary;
import ve.net.alge.grpc.service.field.business_partner.BusinessPartnerInfo;
import ve.net.alge.grpc.service.field.field_management.FieldManagementService;
import ve.net.alge.grpc.service.field.in_out.InOutInfoService;
import ve.net.alge.grpc.service.field.invoice.InvoiceInfoService;
import ve.net.alge.grpc.service.field.location_address.LocationAddress;
import ve.net.alge.grpc.service.field.order.OrderInfoService;
import ve.net.alge.grpc.service.field.payment.PaymentInfoService;
import ve.net.alge.grpc.service.field.product.ProductInfo;
import ve.net.alge.grpc.service.form.ExpressMovement;
import ve.net.alge.grpc.service.form.ExpressReceipt;
import ve.net.alge.grpc.service.form.ExpressShipment;
import ve.net.alge.grpc.service.form.PaymentAllocation;
import ve.net.alge.grpc.service.form.bank_statement_match.BankStatementMatch;
import ve.net.alge.grpc.service.form.import_file_loader.ImportFileLoader;
import ve.net.alge.grpc.service.form.issue_management.IssueManagement;
import ve.net.alge.grpc.service.form.match_po_receipt_invoice.MatchPOReceiptInvoice;
import ve.net.alge.grpc.service.form.out_bound_order.OutBoundOrderService;
import ve.net.alge.grpc.service.form.payroll_action_notice.PayrollActionNotice;
import ve.net.alge.grpc.service.form.task_management.TaskManagement;
import ve.net.alge.grpc.service.form.trial_balance_drillable.TrialBalanceDrillable;

public class Activator implements BundleActivator {
	private Server server;
	private static final Logger logger = Logger.getLogger(Activator.class.getName());

	@Override
	public void start(BundleContext context) throws Exception {
		// 
		ServerBuilder<?> serverBuilder = ServerBuilder.forPort(50051);
		serverBuilder.addService(new BankStatementMatch());
		logger.info("Service " + BankStatementMatch.class.getName());
		// Business Logic
		serverBuilder.addService(new BusinessData());
		logger.info("Service " + BusinessData.class.getName());
		// Business Partner Info
		serverBuilder.addService(new BusinessPartnerInfo());
		logger.info("Service " + BusinessPartnerInfo.class.getName());
		// Core Implementation
		serverBuilder.addService(new CoreFunctionality());
		logger.info("Service " + CoreFunctionality.class.getName());
		// Dashboarding
		serverBuilder.addService(new Dashboarding());
		logger.info("Service " + Dashboarding.class.getName());
		// Dictionary
		serverBuilder.addService(new Dictionary());
		logger.info("Service " + Dictionary.class.getName());
		// Display Definition
// 		serverBuilder.addService(new DisplayDefinition ());// TODO may issue of missing class
// 		logger.info("Service " + DisplayDefinition.class.getName() );
		// Enrollment
		serverBuilder.addService(new Enrollment());
		logger.info("Service " + Enrollment.class.getName());
		// Express Movement
		serverBuilder.addService(new ExpressMovement());
		logger.info("Service " + ExpressMovement.class.getName());
		// Express Receipt
		serverBuilder.addService(new ExpressReceipt());
		logger.info("Service " + ExpressReceipt.class.getName());
		// Express Shipment
		serverBuilder.addService(new ExpressShipment());
		logger.info("Service " + ExpressShipment.class.getName());
		// Field Management
		serverBuilder.addService(new FieldManagementService());
		logger.info("Service " + FieldManagementService.class.getName());
		// File Management
		serverBuilder.addService(new FileManagement());
		logger.info("Service " + FileManagement.class.getName());
		// General Ledger
		serverBuilder.addService(new GeneralLedger());
		logger.info("Service " + GeneralLedger.class.getName());
		// Import File Loader
		serverBuilder.addService(new ImportFileLoader());
		logger.info("Service " + ImportFileLoader.class.getName());
		// In-Out
		serverBuilder.addService(new InOutInfoService());
		logger.info("Service " + InOutInfoService.class.getName());
		// Invoice Field
		serverBuilder.addService(new InvoiceInfoService());
		logger.info("Service " + InvoiceInfoService.class.getName());
		// Issue Management
		serverBuilder.addService(new IssueManagement());
		logger.info("Service " + IssueManagement.class.getName());
		// Location Address
		serverBuilder.addService(new LocationAddress());
		logger.info("Service " + LocationAddress.class.getName());
		// Log
		serverBuilder.addService(new LogsInfo());
		logger.info("Service " + LogsInfo.class.getName());
		// Match PO-Receipt-Invocie
		serverBuilder.addService(new MatchPOReceiptInvoice());
		logger.info("Service " + MatchPOReceiptInvoice.class.getName());
		// Material Management
		serverBuilder.addService(new MaterialManagement());
		logger.info("Service " + MaterialManagement.class.getName());
		// Notice Management
		serverBuilder.addService(new NoticeManagement());
		logger.info("Service " + NoticeManagement.class.getName());
		// Order Field
		serverBuilder.addService(new OrderInfoService());
		logger.info("Service " + OrderInfoService.class.getName());
		// Out Bound Order
		serverBuilder.addService(new OutBoundOrderService());
		logger.info("Service " + OutBoundOrderService.class.getName());
		// Payment
		serverBuilder.addService(new PaymentInfoService());
		logger.info("Service " + PaymentInfoService.class.getName());
		// Payment Allocation
		serverBuilder.addService(new PaymentAllocation());
		logger.info("Service " + PaymentAllocation.class.getName());
		// Payment Print/Export
		serverBuilder.addService(new PaymentPrintExport());
		logger.info("Service " + PaymentPrintExport.class.getName());
		// Payroll Action Notice
		serverBuilder.addService(new PayrollActionNotice());
		logger.info("Service " + PayrollActionNotice.class.getName());
		// Preference Managment
		serverBuilder.addService(new PreferenceManagement());
		logger.info("Service " + PreferenceManagement.class.getName());
		// Product
		serverBuilder.addService(new ProductInfo());
		logger.info("Service " + ProductInfo.class.getName());
		// POS
		serverBuilder.addService(new PointOfSalesForm());
		logger.info("Service " + PointOfSalesForm.class.getName());
		// Record Management
		serverBuilder.addService(new RecordManagement());
		logger.info("Service " + RecordManagement.class.getName());
		// Report Management
		serverBuilder.addService(new ReportManagement());
		logger.info("Service " + ReportManagement.class.getName());
		// Security
		serverBuilder.addService(new Security());
		logger.info("Service " + Security.class.getName());
		// Send Notifications
		serverBuilder.addService(new SendNotifications());
		logger.info("Service " + SendNotifications.class.getName());
		// Task Management
		serverBuilder.addService(new TaskManagement());
		logger.info("Service " + TaskManagement.class.getName());
		// Time Control
		serverBuilder.addService(new TimeControl());
		logger.info("Service " + TimeControl.class.getName());
		// Time Record
		serverBuilder.addService(new TimeRecord());
		logger.info("Service " + TimeRecord.class.getName());
		// Trial Balance Drillable Report
		serverBuilder.addService(new TrialBalanceDrillable());
		logger.info("Service " + TrialBalanceDrillable.class.getName());
		// Update Center
		serverBuilder.addService(new UpdateManagement());
		logger.info("Service " + UpdateManagement.class.getName());
		// User Customization
		serverBuilder.addService(new UserCustomization());
		logger.info("Service " + UserCustomization.class.getName());
		// User Interface
		serverBuilder.addService(new UserInterface());
		logger.info("Service " + UserInterface.class.getName());
		// Web Store
		serverBuilder.addService(new WebStore());
		logger.info("Service " + WebStore.class.getName());
		// Workflow
		serverBuilder.addService(new Workflow());
		logger.info("Service " + Workflow.class.getName());

		server = serverBuilder.build().start();
		System.out.println("gRPC Server started on port 50051");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (server != null) {
			server.shutdown();
			System.out.println("gRPC Server stop on port 50051");
		}
	}
}
