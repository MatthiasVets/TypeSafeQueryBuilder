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
package be.shad.tsqb.restrictions;

import be.shad.tsqb.restrictions.named.SingleNamedParameterBinder;
import be.shad.tsqb.values.TypeSafeValue;

/**
 * Exposes String related restrictions in addition to the basic restrictions.
 */
public interface OnGoingTextRestriction extends OnGoingRestriction<String, ContinuedOnGoingTextRestriction, OnGoingTextRestriction> {

    /**
     * @see #endsWith(String)
     * @return binder with a method to set an alias for the parameter
     */
    SingleNamedParameterBinder<String, ContinuedOnGoingTextRestriction, OnGoingTextRestriction> endsWith();

    /**
     * @see #startsWith(String)
     * @return binder with a method to set an alias for the parameter
     */
    SingleNamedParameterBinder<String, ContinuedOnGoingTextRestriction, OnGoingTextRestriction> startsWith();

    /**
     * @see #contains(String)
     * @return binder with a method to set an alias for the parameter
     */
    SingleNamedParameterBinder<String, ContinuedOnGoingTextRestriction, OnGoingTextRestriction> contains();
    
    /**
     * Generates: left like ? with (? = '%value')
     */
    ContinuedOnGoingTextRestriction endsWith(String value);

    /**
     * Generates: left like ? with (? = 'value%')
     */
    ContinuedOnGoingTextRestriction startsWith(String value);

    /**
     * Generates: left like ? with (? = '%value%')
     */
    ContinuedOnGoingTextRestriction contains(String value);

    /**
     * Generates: left like ? with (? = 'value') (will require value to contain wildcards to be useful)
     */
    ContinuedOnGoingTextRestriction like(String value);
    
    /**
     * Generates: left like stringRepresentative
     */
    ContinuedOnGoingTextRestriction like(TypeSafeValue<String> value);

}
