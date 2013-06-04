/*
 * Copyright (c) 2013. Knowledge Media Institute - The Open University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.open.kmi.iserve.commons.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Operations
 *
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 20/05/2013
 * Time: 17:35
 */
public class Operation extends InvocableEntity {

    private List<MessageContent> inputs;
    private List<MessageContent> outputs;
    private List<MessageContent> inputFaults;
    private List<MessageContent> outputFaults;

    public Operation(URI uri) {
        super(uri);
        this.inputs = new ArrayList<MessageContent>();
        this.outputs = new ArrayList<MessageContent>();
        this.inputFaults = new ArrayList<MessageContent>();
        this.outputFaults = new ArrayList<MessageContent>();
    }

    public List<MessageContent> getInputs() {
        return inputs;
    }

    public void setInputs(List<MessageContent> inputs) {
        this.inputs = inputs;
    }

    public List<MessageContent> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<MessageContent> outputs) {
        this.outputs = outputs;
    }

    public List<MessageContent> getInputFaults() {
        return inputFaults;
    }

    public void setInputFaults(List<MessageContent> inputFaults) {
        this.inputFaults = inputFaults;
    }

    public List<MessageContent> getOutputFaults() {
        return outputFaults;
    }

    public void setOutputFaults(List<MessageContent> outputFaults) {
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
