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
 * Invocable Entity Class. These entities are those that can
 * be attached logical axioms to. In general Operations and Services.
 *
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 22/05/2013
 * Time: 13:26
 */
public class InvocableEntity extends AnnotableResource {

    private List<Condition> conditions;
    private List<Effect> effects;

    public InvocableEntity(URI uri) {
        super(uri);
        conditions = new ArrayList<Condition>();
        effects = new ArrayList<Effect>();
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public List<Effect> getEffects() {
        return effects;
    }

    public void setEffects(List<Effect> effects) {
        this.effects = effects;
    }

    public boolean addCondition(Condition condition) {
        return conditions.add(condition);
    }

    public boolean removeCondition(Condition condition) {
        return conditions.remove(condition);
    }

    public boolean addEffect(Effect effect) {
        return effects.add(effect);
    }

    public boolean removeEffect(Effect effect) {
        return effects.remove(effect);
    }
}
