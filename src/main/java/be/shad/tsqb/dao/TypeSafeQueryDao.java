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
package be.shad.tsqb.dao;

import java.util.List;

import be.shad.tsqb.query.TypeSafeRootQuery;

public interface TypeSafeQueryDao {

    /**
     * Transforms the query to a HqlQuery, creates a hibernate query object for
     * the current session and sets the start/max results.
     * <p>
     * The values are transformed using the transformer which was created
     * when the query was transformed.
     */
    <T> List<T> doQuery(TypeSafeRootQuery query);
    
}
