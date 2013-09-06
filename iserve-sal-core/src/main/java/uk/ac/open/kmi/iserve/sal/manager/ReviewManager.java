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
package uk.ac.open.kmi.iserve.sal.manager;

import java.net.URI;
import java.util.List;

/**
 * Interface defining the CRUD methods that any Review Manager should provide
 * Review Manager at the moment take care of Reviews, Comments, and Tags
 * Eventually we may want to take Tagging away from this interface.
 * <p/>
 * TODO: Add methods for listing all the items by a user
 */
public interface ReviewManager extends iServeComponent {

    // Create
    public abstract boolean addRating(URI agentUri, URI resourceUri, String rating);

    public abstract boolean addComment(URI agentUri, URI resourceUri, String comment);

    public abstract boolean addTag(URI agentUri, URI resourceUri, String tag);

    public abstract boolean addTags(URI agentUri, URI resourceUri, List<String> tags);

    // Read
    public abstract List<String> getRatings(URI resourceUri);

    public abstract List<String> getComments(URI resourceUri);

    public abstract List<String> getTags(URI resourceUri);

    //Update (only seems to make sense for rating)
    public abstract boolean updateRating(URI agentUri, URI resourceUri, String rating);

    // Delete
    public abstract boolean deleteRating(URI agentUri, URI resourceUri);

    public abstract boolean deleteComment(URI agentUri, URI resourceUri);

    public abstract boolean deleteTag(URI agentUri, URI resourceUri, String tag);


}