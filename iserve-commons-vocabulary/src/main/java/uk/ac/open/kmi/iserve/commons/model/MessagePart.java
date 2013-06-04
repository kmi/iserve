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
 * Message Parts
 *
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 20/05/2013
 * Time: 17:43
 */
public class MessagePart extends AnnotableResource {

   private List<MessagePart> mandatoryParts;
    private List<MessagePart> optionalParts;

    public MessagePart(URI uri) {
        super(uri);
        this.mandatoryParts = new ArrayList<MessagePart>();
        this.optionalParts = new ArrayList<MessagePart>();
    }

    public List<MessagePart> getMandatoryParts() {
        return mandatoryParts;
    }

    public void setMandatoryParts(List<MessagePart> mandatoryParts) {
        this.mandatoryParts = mandatoryParts;
    }

    public List<MessagePart> getOptionalParts() {
        return optionalParts;
    }

    public void setOptionalParts(List<MessagePart> optionalParts) {
        this.optionalParts = optionalParts;
    }


    public boolean addOptionalPart(MessagePart part) {
        if (part != null) {
            return this.optionalParts.add(part);
        }
        return false;
    }

    public boolean removeOptionalPart(MessagePart part) {
        if (part != null) {
            return this.optionalParts.remove(part);
        }
        return false;
    }

    public boolean addMandatoryPart(MessagePart part) {
        if (part != null) {
            return this.mandatoryParts.add(part);
        }
        return false;
    }

    public boolean removeMandatoryPart(MessagePart part) {
        if (part != null) {
            return this.mandatoryParts.remove(part);
        }
        return false;
    }
}
