package not.a.valid.path;

import com.miotech.kun.workflow.core.execution.Operator;
import com.miotech.kun.workflow.core.execution.logging.Logger;

public class TestOperator2 extends Operator {
    private Logger logger;

    public void init() {
        this.logger = this.getContext().getLogger();
    }

    public boolean run() {
        this.logger.info("Execution Failed");
        return false;
    }
}
