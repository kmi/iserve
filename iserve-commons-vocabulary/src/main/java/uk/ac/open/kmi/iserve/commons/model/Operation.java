package uk.ac.open.kmi.iserve.commons.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Operations
 *
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 20/05/2013
 * Time: 17:35
 */
public class Operation extends InvocableEntity {

    private Collection<MessageContent> inputs;
    private Collection<MessageContent> outputs;
    private Collection<MessageContent> inputFaults;
    private Collection<MessageContent> outputFaults;

    public Operation(URI uri) {
        super(uri);
        this.inputs = new ArrayList<MessageContent>();
        this.outputs = new ArrayList<MessageContent>();
        this.inputFaults = new ArrayList<MessageContent>();
        this.outputFaults = new ArrayList<MessageContent>();
    }

    public Collection<MessageContent> getInputs() {
        return inputs;
    }

    public void setInputs(Collection<MessageContent> inputs) {
        this.inputs = inputs;
    }

    public Collection<MessageContent> getOutputs() {
        return outputs;
    }

    public void setOutputs(Collection<MessageContent> outputs) {
        this.outputs = outputs;
    }

    public Collection<MessageContent> getInputFaults() {
        return inputFaults;
    }

    public void setInputFaults(Collection<MessageContent> inputFaults) {
        this.inputFaults = inputFaults;
    }

    public Collection<MessageContent> getOutputFaults() {
        return outputFaults;
    }

    public void setOutputFaults(Collection<MessageContent> outputFaults) {
        this.outputFaults = outputFaults;
    }

    public boolean addInput(MessageContent mc) {
        if (mc != null) {
            return this.inputs.add(mc);
        }
        return false;
    }

    public boolean removeInput(MessageContent mc) {
        if (mc != null) {
            return this.inputs.remove(mc);
        }
        return false;
    }

    public boolean addOutput(MessageContent mc) {
        if (mc != null) {
            return this.outputs.add(mc);
        }
        return false;
    }

    public boolean removeOutput(MessageContent mc) {
        if (mc != null) {
            return this.outputs.remove(mc);
        }
        return false;
    }

    public boolean addInputFault (MessageContent mc) {
        if (mc != null) {
            return this.inputFaults.add(mc);
        }
        return false;
    }

    public boolean removeInputFault(MessageContent mc) {
        if (mc != null) {
            return this.inputFaults.remove(mc);
        }
        return false;
    }

    public boolean addOutputFault (MessageContent mc) {
        if (mc != null) {
            return this.outputFaults.add(mc);
        }
        return false;
    }

    public boolean removeOutputFault(MessageContent mc) {
        if (mc != null) {
            return this.outputFaults.remove(mc);
        }
        return false;
    }

}
