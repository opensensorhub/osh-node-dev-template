package com.sample.impl.process.processname;

import org.sensorhub.impl.processing.AbstractProcessProvider;

public class ProcessDescriptors extends AbstractProcessProvider {

    public ProcessDescriptors() {
        addImpl(MyProcess.INFO);
    }

}