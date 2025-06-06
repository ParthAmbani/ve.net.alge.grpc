package ve.net.alge.grpc.util;

import java.util.Arrays;
import java.util.List;

import org.compiere.model.I_M_Product;
import org.compiere.util.Util;

public class SearchInfoFields {

	public List<String> customSearhIngo = Arrays.asList(
		I_M_Product.Table_Name
	);

	public boolean isCustomSeachInfo(String tableName) {
		if (Util.isEmpty(tableName, true)) {
			return false;
		}
		if (customSearhIngo.contains(tableName)) {
			return true;
		}
		return false;
	}

}
