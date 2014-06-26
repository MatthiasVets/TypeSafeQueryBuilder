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

import static be.shad.tsqb.restrictions.RestrictionNodeType.And;
import static be.shad.tsqb.restrictions.RestrictionNodeType.Or;
import static be.shad.tsqb.restrictions.RestrictionOperator.EQUAL;
import static be.shad.tsqb.restrictions.RestrictionOperator.IN;
import static be.shad.tsqb.restrictions.RestrictionOperator.IS_NOT_NULL;
import static be.shad.tsqb.restrictions.RestrictionOperator.IS_NULL;
import static be.shad.tsqb.restrictions.RestrictionOperator.NOT_EQUAL;
import static be.shad.tsqb.restrictions.RestrictionOperator.NOT_IN;

import java.util.Collection;

import be.shad.tsqb.query.TypeSafeQueryInternal;
import be.shad.tsqb.restrictions.named.CollectionNamedParameterBinder;
import be.shad.tsqb.restrictions.named.NamedParameterBinderImpl;
import be.shad.tsqb.restrictions.named.SingleNamedParameterBinder;
import be.shad.tsqb.values.CollectionTypeSafeValue;
import be.shad.tsqb.values.DirectTypeSafeValue;
import be.shad.tsqb.values.TypeSafeValue;

/**
 * Takes a partially built restriction and provides methods complete it.
 * Type specific methods are added in subclasses of this class.
 * 
 * @see RestrictionImpl
 */
public abstract class OnGoingRestrictionImpl<VAL, CONTINUED extends ContinuedOnGoingRestriction<VAL, CONTINUED, ORIGINAL>, 
            ORIGINAL extends OnGoingRestriction<VAL, CONTINUED, ORIGINAL>> extends RestrictionChainableDelegatingImpl
        implements OnGoingRestriction<VAL, CONTINUED, ORIGINAL>, ContinuedOnGoingRestriction<VAL, CONTINUED, ORIGINAL> {
    
    private final RestrictionNodeType restrictionNodeType;
    protected final TypeSafeValue<VAL> startValue;
    
    /*
     * NOTE: The restriction is kept around so that it is possible to 'save' the restriction to alter the query
     * without having to rebuild it completely.
     */
    private RestrictionImpl<VAL> restriction;

    public OnGoingRestrictionImpl(RestrictionsGroupInternal group, RestrictionNodeType restrictionNodeType, VAL argument) {
        super(group);
        this.restrictionNodeType = restrictionNodeType;
        this.startValue = toValue(argument);
    }

    public OnGoingRestrictionImpl(RestrictionsGroupInternal group, RestrictionNodeType restrictionNodeType, TypeSafeValue<VAL> argument) {
        super(group);
        this.restrictionNodeType = restrictionNodeType;
        this.startValue = argument;
    }
    
    /**
     * The required value class, which will be checked in the type safe values later.
     */
    protected abstract Class<VAL> getSupportedValueClass();
    
    /**
     * Delegates to subclass to create the correct type.
     */
    protected abstract CONTINUED createContinuedOnGoingRestriction(
            RestrictionNodeType restrictionNodeType, 
            TypeSafeValue<VAL> previousValue);

    /**
     * Delegates to subclass to create the correct type.
     */
    protected abstract ORIGINAL createOriginalOnGoingRestriction(
            RestrictionNodeType restrictionNodeType, 
            TypeSafeValue<VAL> previousValue);

    protected NamedParameterBinderImpl<VAL, CONTINUED, ORIGINAL> createNamedParameterBinder(
            TypeSafeValue<VAL> value, CONTINUED next) {
        return new NamedParameterBinderImpl<VAL, CONTINUED, ORIGINAL>(
                group.getQuery(), value, next);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CONTINUED isNull() {
        return addRestrictionAndContinue(startValue, IS_NULL, null);
    }

    /**
     * The go-to method for to add the restriction to the restrictions group.
     */
    @SuppressWarnings("unchecked")
    protected <L extends VAL, R extends VAL> CONTINUED addRestrictionAndContinue(
            TypeSafeValue<L> left, RestrictionOperator operator, TypeSafeValue<R> right) {
        TypeSafeValue<VAL> leftVal = (TypeSafeValue<VAL>) left;
        TypeSafeValue<VAL> rightVal = (TypeSafeValue<VAL>) right;
        if (restriction == null) {
            restriction = new RestrictionImpl<>(group, leftVal, operator, rightVal);
            if (restrictionNodeType == And) {
                group.and(restriction);
            } else {
                group.or(restriction);
            }
        } else {
            restriction.setLeft(leftVal);
            restriction.setOperator(operator);
            restriction.setRight(rightVal);
        }
        // continue with the next one assuming And, if or() is called the instance 
        // is simply discarded and one with Or is returned instead. 
        return createContinuedOnGoingRestriction(And, leftVal == null ? rightVal: leftVal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CONTINUED isNotNull() {
        return addRestrictionAndContinue(startValue, IS_NOT_NULL, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends VAL> CONTINUED in(TypeSafeValue<T> value) {
        return addRestrictionAndContinue(startValue, IN, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T extends VAL> CONTINUED in(Collection<T> values) {
        // suppressing warnings because we know T is a kind of VAL, and we won't be changing the collection internally
        return in(new CollectionTypeSafeValue<>(group.getQuery(), getSupportedValueClass(), (Collection) values));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends VAL> CONTINUED notIn(TypeSafeValue<T> value) {
        return addRestrictionAndContinue(startValue, NOT_IN, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T extends VAL> CONTINUED notIn(Collection<T> values) {
        // suppressing warnings because we know T is a kind of VAL, and we won't be changing the collection internally
        return notIn(new CollectionTypeSafeValue<>(group.getQuery(), getSupportedValueClass(), (Collection) values));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CONTINUED eq(TypeSafeValue<VAL> value) {
        return addRestrictionAndContinue(startValue, EQUAL, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CONTINUED eq(VAL value) {
        return eq(toValue(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CONTINUED notEq(TypeSafeValue<VAL> value) {
        return addRestrictionAndContinue(startValue, NOT_EQUAL, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CONTINUED not(VAL value) {
        return notEq(toValue(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SingleNamedParameterBinder<VAL, CONTINUED, ORIGINAL> eq() {
        DirectTypeSafeValue<VAL> value = createDirectValue();
        return createNamedParameterBinder(value, eq(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SingleNamedParameterBinder<VAL, CONTINUED, ORIGINAL> notEq() {
        DirectTypeSafeValue<VAL> value = createDirectValue();
        return createNamedParameterBinder(value, notEq(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CollectionNamedParameterBinder<VAL, CONTINUED, ORIGINAL> in() {
        CollectionTypeSafeValue<VAL> value = createCollectionValue();
        return createNamedParameterBinder(value, in(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CollectionNamedParameterBinder<VAL, CONTINUED, ORIGINAL> notIn() {
        CollectionTypeSafeValue<VAL> value = createCollectionValue();
        return createNamedParameterBinder(value, notIn(value));
    }
    
    /**
     * Create a new unbound single value.
     */
    protected DirectTypeSafeValue<VAL> createDirectValue() {
        return new DirectTypeSafeValue<>(group.getQuery(), getSupportedValueClass());
    }

    /**
     * Create a new unbound collection value.
     */
    protected CollectionTypeSafeValue<VAL> createCollectionValue() {
        return new CollectionTypeSafeValue<>(group.getQuery(), getSupportedValueClass());
    }

    /**
     * Delegates to {@link TypeSafeQueryInternal#toValue(Object)}
     */
    protected TypeSafeValue<VAL> toValue(VAL value) {
        return group.getQuery().toValue(value);
    }

    @Override
    public ORIGINAL and() {
        return createOriginalOnGoingRestriction(And, startValue);
    }

    @Override
    public ORIGINAL or() {
        return createOriginalOnGoingRestriction(Or, startValue);
    }
    
}
