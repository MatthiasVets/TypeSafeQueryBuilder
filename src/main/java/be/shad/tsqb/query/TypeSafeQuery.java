package be.shad.tsqb.query;

import java.util.Collection;
import java.util.Date;

import be.shad.tsqb.grouping.OnGoingGroupBy;
import be.shad.tsqb.helper.TypeSafeQueryHelper;
import be.shad.tsqb.hql.HqlQuery;
import be.shad.tsqb.joins.TypeSafeQueryJoin;
import be.shad.tsqb.ordering.OnGoingOrderBy;
import be.shad.tsqb.restrictions.RestrictionChainable;
import be.shad.tsqb.restrictions.RestrictionsGroup;
import be.shad.tsqb.restrictions.WhereRestrictions;
import be.shad.tsqb.values.TypeSafeValue;
import be.shad.tsqb.values.TypeSafeValueFunctions;

/**
 * TypeSafeQuery aims to be a type safe alternative to build hql queries.
 * <p>
 * TypeSafeQuery uses proxies of the entities being queried to provide
 * the developer a way to create queries without coding property paths
 * in strings. 
 * <p>
 * When methods on the entities are refactored, the queries will either
 * automatically be refactored too, or they will show compile errors.
 * This should help with the maintanability of the created queries.
 * <p>
 * A type safe query is created in two ways.
 * <ul>
 * <li> By using {@link TypeSafeQueryHelper#createQuery()}: This will create a root query, which is always the 
 *          starting point to create a query to return data
 * <li> By using {@link #subquery(Class)}: This can be used to create a subquery, 
 *          which will select a value of the Class type.
 * </ul>
 * After a TypeSafeQuery instance was obtained, a proxy of the entity being queried is obtained by calling 
 * the {@link #from(Class)} method with the entity class.
 * <p>
 * This proxies' getters can be used to join entities, build the where clause, order and group by.
 * <p>
 * For an example, see {@link TypeSafeRootQuery}.
 */
public interface TypeSafeQuery extends WhereRestrictions {
    
    /**
     * Creates a proxy for the given fromClass.
     * <p>
     * Multiple calls are allowed to create from clauses with multiple entities.
     * This may be useful when the queries have no direct relation in hibernate,
     * but the relation can be expressed in the restrictions afterwards.
     */
    <T> T from(Class<T> fromClass);

    /**
     * Delegates to {@link #join(Collection, JoinType)} with {@link JoinType#Inner}
     */
    <T> T join(Collection<T> anyCollection);
    
    /**
     * Delegates to {@link #join(Object, JoinType)} with {@link JoinType#Inner}
     */
    <T> T join(T anyObject);
    
    /**
     * Joins an entity collection, returns a proxy of the joined entity type.
     * The method calls of the proxy will be captured to assist with the query building.
     * <p>
     * The type is fetched from hibernate.
     */
    <T> T join(Collection<T> anyCollection, JoinType joinType);
    
    /**
     * Joins an entity, returns a proxy of the joined entity type.
     * The method calls of the proxy will be captured to assist with the query building.
     * <p>
     * The type is fetched from hibernate.
     */
    <T> T join(T anyObject, JoinType joinType);
    
    /**
     * The object must be a TypeSafeQueryProxy, this will be validated at runtime.
     * 
     * @return TypeSafeQueryJoin which can be configured further for join specific configuration
     * @throws IllegalArgumentException if the object is not an entity proxy.
     */
    <T> TypeSafeQueryJoin<T> getJoin(T obj);

    /**
     * Creates a subgroup for this query. This group is not added to the query
     * where until it is added using the {@link RestrictionChainable#and(be.shad.tsqb.restrictions.Restriction) and(restriction)} 
     * or the {@link RestrictionChainable#or(be.shad.tsqb.restrictions.Restriction) or(restriction)}.
     * <p>
     * The group will not be added to the existing restrictions automatically.
     * This must be done separately.
     */
    RestrictionsGroup whereGroup();

    /**
     * Get the orderBy, allowing to add descending and ascending order bys.
     */
    OnGoingOrderBy orderBy();

    /**
     * Converts to a TypeSafeValue and delegates to {@link #groupBy(TypeSafeValue)}.
     */
    OnGoingGroupBy groupBy(Number val);

    /**
     * Converts to a TypeSafeValue and delegates to {@link #groupBy(TypeSafeValue)}.
     */
    OnGoingGroupBy groupBy(String val);

    /**
     * Converts to a TypeSafeValue and delegates to {@link #groupBy(TypeSafeValue)}.
     */
    OnGoingGroupBy groupBy(Enum<?> val);

    /**
     * Converts to a TypeSafeValue and delegates to {@link #groupBy(TypeSafeValue)}.
     */
    OnGoingGroupBy groupBy(Boolean val);

    /**
     * Converts to a TypeSafeValue and delegates to {@link #groupBy(TypeSafeValue)}.
     */
    OnGoingGroupBy groupBy(Date val);

    /**
     * Adds the value to the list of values to group by.
     */
    OnGoingGroupBy groupBy(TypeSafeValue<?> val);
    
    /**
     * Creates a subquery which will select a value of the <code>resultClass</code>.
     */
    <T> TypeSafeSubQuery<T> subquery(Class<T> resultClass);

    /**
     * Build a value using a function. Use this method to create TypeSafeValue objects fluently.
     */
    TypeSafeValueFunctions function();
    
    /**
     * Converts this query to an hqlQuery. 
     * <p>
     * The hqlQuery can be used to get the hql and the 
     * params to create a hibernate query object.
     */
    HqlQuery toHqlQuery();
    
}
