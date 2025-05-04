package ve.net.alge.grpc.util;

import java.util.Arrays;
import java.util.Comparator;

import org.compiere.model.MColumn;
import org.compiere.model.MTable;
import org.compiere.model.PO;
import org.compiere.util.Util;

public class POUtils {

	/**
	 * Get Label value from po
	 * 
	 * @return
	 */
	public static String getDisplayValue(PO po) {
		StringBuffer identifier = new StringBuffer();
		MTable table = MTable.get(po.getCtx(), po.get_Table_ID());
		Arrays.asList(table.getColumns(true)).stream().sorted(Comparator.comparing(MColumn::getSeqNo))
				.filter(entry -> entry.isIdentifier() && po.get_Value(entry.getColumnName()) != null
						&& !Util.isEmpty(po.get_DisplayValue(entry.getColumnName(), true)))
				.forEach(column -> {
					// Validate value
					String displayColumn = po.get_DisplayValue(column.getColumnName(), true);
					// Add separator
					if (identifier.length() > 0) {
						identifier.append("_");
					}
					// Add Value
					identifier.append(displayColumn);
				});
		// Add default
		if (identifier.length() == 0) {
			identifier.append("<").append(po.get_ID()).append(">");
		}
		return identifier.toString();
	}

}
