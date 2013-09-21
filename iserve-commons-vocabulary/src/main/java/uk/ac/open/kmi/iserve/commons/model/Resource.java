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

/**
 * Resources
 * Resources can be attached a number of metadata properties such as
 * label, comment, creator, seeAlso and source
 * <p/>
 * Resources can also be attached a WSDL grounding for now although this
 * may better be handled differently
 * <p/>
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 20/05/2013
 * Time: 17:12
 */
public class Resource {

    private URI uri;

    private String label;

    private String comment;

    private URI creator;

    private URI seeAlso;

    private URI source;

    // this is specific to a kind of grounding, may need moving around
    private URI wsdlGrounding;

    public Resource(URI uri) {
        this.uri = uri;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public URI getCreator() {
        return creator;
    }

    public void setCreator(URI creator) {
        this.creator = creator;
    }

    public URI getSeeAlso() {
        return seeAlso;
    }

    public void setSeeAlso(URI seeAlso) {
        this.seeAlso = seeAlso;
    }

    public URI getSource() {
        return source;
    }

    public void setSource(URI source) {
        this.source = source;
    }

    public URI getWsdlGrounding() {
        return wsdlGrounding;
    }

    public void setWsdlGrounding(URI wsdlGrounding) {
        this.wsdlGrounding = wsdlGrounding;
    }

}
