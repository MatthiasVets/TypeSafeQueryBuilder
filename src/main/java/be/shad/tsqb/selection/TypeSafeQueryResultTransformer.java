/*
 * Copyright Gert Wijns gert.wijns@gmail.com
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
package be.shad.tsqb.selection;

import static be.shad.tsqb.selection.SelectionTree.getField;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;

import org.hibernate.transform.BasicTransformerAdapter;

import be.shad.tsqb.data.TypeSafeQuerySelectionProxyData;
import be.shad.tsqb.selection.group.SelectionTreeGroup;
import be.shad.tsqb.selection.group.TypeSafeQuerySelectionGroup;
import be.shad.tsqb.selection.parallel.SelectionMerger;

/**
 * Implementation to set values on nested select dtos.
 * Seems to be faster than the alias to bean result transformer too.
 */
public class TypeSafeQueryResultTransformer extends BasicTransformerAdapter {
    private static final long serialVersionUID = 4686800769621139636L;
    
    private final Field[] setters;
    private final SelectionTree[] values;
    private final SelectionTreeGroup[] groups;
    
    // reusing a result array to reduce object creation during transformation
    // the tuple transformation may be called thousands of times or more in
    // queries with big result sets
    private final Object[] resultArray; 
    
    @SuppressWarnings("rawtypes")
    private final SelectionValueTransformer[] transformers;
    
    public TypeSafeQueryResultTransformer(
            List<TypeSafeQuerySelectionProxyData> selectionDatas, 
            List<SelectionValueTransformer<?, ?>> transformers) {
        try {
            this.transformers = transformers.toArray(new SelectionValueTransformer[transformers.size()]);
            this.setters = new Field[selectionDatas.size()];
            this.values = new SelectionTree[selectionDatas.size()];
            LinkedHashMap<TypeSafeQuerySelectionGroup, SelectionTreeGroup> groups = new LinkedHashMap<>();
            int a = 0;
            for(TypeSafeQuerySelectionProxyData selectionData: selectionDatas) {
                String propertyPath = selectionData.getEffectivePropertyPath();
                TypeSafeQuerySelectionGroup group = selectionData.getGroup();
                SelectionTreeGroup tree = groups.get(group);
                if (tree == null) {
                    tree = new SelectionTreeGroup(group.getResultClass(), group);
                    groups.put(group, tree);
                }
                String[] alias = propertyPath.split("\\.");
                SelectionTree subtree = tree;
                for(int i=0; i < alias.length-1; i++) {
                    subtree = subtree.getSubtree(alias[i]);
                }
                values[a] = subtree;
                setters[a++] = getField(subtree.getResultType(), alias[alias.length-1]);
            }
            
            // Create groups array, having the result group as first group:
            a = 1;
            this.groups = new SelectionTreeGroup[groups.size()];
            for(SelectionTreeGroup group: groups.values()) {
                if (group.getGroup().isResultGroup()) {
                    this.groups[0] = group;
                } else {
                    this.groups[a++] = group;
                }
            }
            this.resultArray = new Object[this.groups.length];
            AccessibleObject.setAccessible(setters, true);
        } catch (NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object transformTuple(Object[] tuple, String[] aliases) {
        try {
            int i=0;
            for(SelectionTree group: groups) {
                resultArray[i] = group.getResultType().newInstance();
                group.populate(resultArray[i++]);
            }
            for(i=0; i < aliases.length; i++) {
                Object value = tuple[i];
                if (transformers[i] != null) {
                    value = transformers[i].convert(value);
                }
                setters[i].set(values[i].getValue(), value);
            }
            
            for(i=1; i < groups.length; i++) {
                @SuppressWarnings("rawtypes")
                SelectionMerger merger = groups[i].getGroup().getParallelSelectionMerger();
                if (merger != null) {
                    merger.mergeIntoResult(resultArray[0], resultArray[i]);
                }
            }
            return resultArray[0];
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public List transformList(List list) {
        for(int i=0; i < groups.length; i++) {
            resultArray[i] = null;
        }
        return list;
    }

}
