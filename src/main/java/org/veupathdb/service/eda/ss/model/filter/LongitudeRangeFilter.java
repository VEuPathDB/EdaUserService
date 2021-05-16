package org.veupathdb.service.eda.ss.model.filter;

import org.veupathdb.service.eda.ss.model.Entity;

import static org.gusdb.fgputil.FormatUtil.NL;
import static org.veupathdb.service.eda.ss.model.RdbmsColumnNames.NUMBER_VALUE_COL_NAME;

public class LongitudeRangeFilter extends Filter {
    private Number left;
    private Number right;

    public LongitudeRangeFilter(Entity entity, String variableId, Number left, Number right) {
        super(entity, variableId);
        this.left = left;
        this.right = right;
    }

    @Override
    public String getAndClausesSql() {
        float left_f = left.floatValue();
        float right_f = right.floatValue();

        // if left and right are the same, or very close to it, then apply nop filter
        if (Math.abs(left_f - right_f) < .00000001)
            return " AND 1 = 1" + NL;

        // if  passing thru intl date line, then use OR, else use AND
        //  -50  ================== 0 ==================  50
        //   50  ============== 180/-180 =============== -50
        String bool = left_f < right_f? " AND " : " OR ";
        return "  AND (" + NUMBER_VALUE_COL_NAME + " >= " + left + bool + NUMBER_VALUE_COL_NAME + " <= " + right + ")" + NL;
    }
}
