package com.miotech.kun.workflow.operator;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class SparkOperatorTest {

    OperatorContextImpl context;
    SparkOperator operator;

    @Before
    public void setUp(){
        context = new OperatorContextImpl();
        operator = new SparkOperator();
        operator.init();
    }


    @Test
    public void run() {
        operator.run();
    }

    @Test
    public void onAbort() {
    }

    @Test
    public void lineangeAnalysis(){
        // test es
        String esApplicationId = "local-1590805766018";
        operator.lineangeAnalysis(context, esApplicationId);

        //test mongo
        String mongoApplicationId = "local-1590806862049";
        operator.lineangeAnalysis(context, mongoApplicationId);

    }
}