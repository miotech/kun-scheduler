package com.miotech.kun.workflow.operator;

import org.junit.Before;
import org.junit.Test;

public class SparkOperatorTest {

    OperatorContextImpl context;
    SparkOperator operator;

    @Before
    public void setUp(){
        context = new OperatorContextImpl();
        operator = new SparkOperator();
    }

    @Test
    public void init() {
    }

    @Test
    public void run() {
//        OperatorContextImpl context = new OperatorContextImpl();
//        Operator operator = new SparkOperator();
        operator.init(context);
        operator.run(context);
    }

    @Test
    public void onAbort() {
    }

    @Test
    public void lineangeAnalysis(){
        // test es
//        String esApplicationId = "local-1590805766018";
//        operator.lineangeAnalysis(context, esApplicationId);

        //test mongo
        String mongoApplicationId = "local-1590806862049";
        operator.lineangeAnalysis(context, mongoApplicationId);

    }
}