package not.a.valid.path;

import com.miotech.kun.workflow.core.execution.Operator;

public class TestOperator3 extends Operator {
    public boolean run() {
        throw new IllegalStateException("Failure");
    }
}
