import com.miotech.kun.workflow.core.execution.logging.Logger;
import com.miotech.kun.workflow.core.execution.Operator;

public class TestOperator1 extends Operator {
    private Logger logger;

    public void init() {
        this.logger = this.getContext().getLogger();
    }

    public boolean run() {
        final String name = "world";
        this.logger.info("Hello, {}!", new Object[] { name });
        return true;
    }
}
