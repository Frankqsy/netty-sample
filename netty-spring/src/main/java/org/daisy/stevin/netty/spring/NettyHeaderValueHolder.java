/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.daisy.stevin.netty.spring;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;

class NettyHeaderValueHolder {

    private final List<Object> values = new LinkedList<Object>();


    public void setValue(Object value) {
        this.values.clear();
        this.values.add(value);
    }

    public void addValue(Object value) {
        this.values.add(value);
    }

    public void addValues(Collection<?> values) {
        this.values.addAll(values);
    }

    public void addValueArray(Object values) {
        CollectionUtils.mergeArrayIntoCollection(values, this.values);
    }

    public List<Object> getValues() {
        return Collections.unmodifiableList(this.values);
    }

    public List<String> getStringValues() {
        List<String> stringList = new ArrayList<String>(this.values.size());
        for (Object value : this.values) {
            stringList.add(value.toString());
        }
        return Collections.unmodifiableList(stringList);
    }

    public Object getValue() {
        return (!this.values.isEmpty() ? this.values.get(0) : null);
    }

    public String getStringValue() {
        return (!this.values.isEmpty() ? this.values.get(0).toString() : null);
    }


    public static NettyHeaderValueHolder getByName(Map<String, NettyHeaderValueHolder> headers, String name) {
        Assert.notNull(name, "Header name must not be null");
        for (String headerName : headers.keySet()) {
            if (headerName.equalsIgnoreCase(name)) {
                return headers.get(headerName);
            }
        }
        return null;
    }

}
