package com.sample.impl.process.processname;

import net.opengis.swe.v20.Count;
import org.sensorhub.api.processing.OSHProcessInfo;
import org.vast.process.ExecutableProcessImpl;
import org.vast.process.ProcessException;
import org.vast.swe.SWEHelper;

public class MyProcess extends ExecutableProcessImpl {

    public static final OSHProcessInfo INFO = new OSHProcessInfo("myprocessname", "Process Label", "Description of my process goes here", MyProcess.class);

    Count input1;
    Count output1;
    Count parameter1;

    /**
     * Typically, you will initialize your input, output, and parameter data structures in the constructor
     */
    protected MyProcess() {
        super(INFO);

        SWEHelper fac = new SWEHelper();

        // Create process inputs, outputs, and parameters
        this.inputData.add("input1", input1 = fac.createCount().build());
        this.outputData.add("output1", output1 = fac.createCount().build());
        this.paramData.add("parameter1", parameter1 = fac.createCount().build()); // Optional
    }

    /**
     * Process execution method. This is what gets called when your process runs
     */
    @Override
    public void execute() {
        int paramValue = parameter1.getData().getIntValue();
        int inputValue = input1.getData().getIntValue();

        // Do whatever computations/processing with your input and parameter data,
        // and use it to populate the output data blocks
        int equation = inputValue * paramValue;

        output1.getData().setIntValue(equation);
    }
}
