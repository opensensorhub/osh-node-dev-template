# [NAME]

## Configuration

Configuring the process requires:
Select ```Processing``` from the left hand accordion control and right click for context sensitive menu in accordion control.
Select the ```SensorML Stream Process``` Module
- **Module Name:** A name for the instance of the processing module
- **Description:** A description of the process chain
- **SensorML File:** The path to a process description file, which can be a XML or JSON SensorML process description.
- **Auto Start:** Check the box to start this module when OSH node is launched

# Overview of OSH Processing

Processing in OpenSensorHub is a crucial component to many sensor systems. Processes in OpenSensorHub are at the core of connecting sensors to actuators, automating tasks, and chaining sensor drivers, processes, and control streams.


## Key Components

Inputs, Outputs, and Parameters are the core components of an atomic process. When designing a process, think of how these data components will interact with each other and what you want from the process.

- **Inputs** - Data linked to the process from a source module. This source module can be either a sensor driver or another process

- **Outputs** - Outputs from the process itself. These outputs can be linked to the inputs of other processes or even a control stream of a sensor driver.

- **Parameters** - Configuration settings that are defined in the process description. These will be set in the SensorML description itself and used to configure the process module.


## Creating a Process

SensorML processes require a process implementation in Java as well as a process description in SensorML to define data sources, output structures, and parameters.


# Java Implementation

## Required Files

The OSH module containing the processing implementation should include at least **3** files. These files are:

- **Activator** - OSGi bundle activator, this is the same as any OSH module

- **ProcessDescriptor** - Descriptor to add your process implementation to OSH’s process manager

- (Process Implementation) - Process implementation as an extension of **ExecutableProcessImpl**

Typical OSH Module Files

- org.sensorhub.api.processing.IProcessProvider - Provider to allow OSH to find the executable process

- build.gradle 


## Activator.java

    public class Activator extends OshBundleActivator implements BundleActivator
    {}


## ProcessDescriptor.java

    public class ProcessDescriptors extends AbstractProcessProvider
    {

         public ProcessDescriptors()
       {
     // Adds the process implementation to the process manager using the ProcessInfo defined in the implementation
           addImpl(MyProcess.INFO);
       }

    }


## org.sensorhub.api.processing.IProcessProvider

    path.to.ProcessDescriptor


##

## MyProcess.java

    public class MyProcess extends ExecutableProcessImpl {

    // Required process information 
    public static final OSHProcessInfo INFO = new OSHProcessInfo("myprocessname", "An example process", null, MyProcess.class);

    Count input1;
    Count output1;
    Count parameter1;

    public MyProcess() {
        super(INFO);

        SWEHelper fac = new SWEHelper();

    // Initialize inputs, outputs, params
     inputData.add("input1", input1 = fac.createCount().build());
     outputData.add("output1", output1 = fac.createCount().build());
     paramData.add("param1", parameter1 = fac.createCount().build());
    }

    @Override
    public void execute() throws ProcessException {
        // Use inputs and params to update outputs
     int paramValue = parameter1.getData().getIntValue();
     int inputValue = input1.getData().getIntValue();

     int equation = inputValue * paramValue;

     output1.getData().setIntValue(equation);
    }
    }


##

## Executing the Process

Ensure that your process module is included in the project’s \`build.gradle\`. In order to execute the created process, a SensorML process description must be provided to pass to the SensorML process module in OSH.


## SensorML Process Chain Description

A SensorML process description is an aggregate process composed of outputs, components, and connections.

This required SensorML process chain description has a few requirements as listed below.

**Requirements**

- ID - Unique identifier of the process chain

- Outputs - These will be the top-level outputs available from the datastream created by the aggregate process.

- Components - These are chained together to create your final process. Components can be any of the following

  - Datastream - Datasource stream to read outputs and connect to other parts of the aggregate process.

  - Command stream - Can be used as a destination for process or datastream outputs. Commands will be sent at the rate that the linked output is updated.

  - Processes - Process inputs, outputs, and parameters can be chained together once defined as a component.

- Connections - These are explicitly defined connections to link inputs to outputs or vice-versa. Connections can only be between 2 data records that have the same record structure.


##

## Example SensorML Description

Example SensorML descriptions in JSON and XML can be found in this project directory at `/src/test/resources`
