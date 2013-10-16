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

package uk.ac.open.kmi.iserve.discovery.util;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.BoundType;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * MatchResultPredicates provides useful predicates to use when filtering collections of Match Results
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 16/10/2013
 */
public class MatchResultPredicates {

    /**
     * Generates a Predicate that only accepts the Match Results that have a Match Type greater than matchType
     *
     * @param matchType the matchType that defines the boundary
     * @param <T>       a subclass of MatchResult
     * @param <S>       a subclass of MatchType
     * @return the Predicate
     */
    public static <T extends MatchResult, S extends MatchType> Predicate<T> greaterThan(S matchType) {
        return new GreaterThanPredicate<T, S>(matchType);
    }

    /**
     * Generates a Predicate that only accepts the Match Results that have a Match Type lower than matchType
     *
     * @param matchType the matchType that defines the boundary
     * @param <T>       a subclass of MatchResult
     * @param <S>       a subclass of MatchType
     * @return the Predicate
     */
    public static <T extends MatchResult, S extends MatchType> Predicate<T> lowerThan(S matchType) {
        return new LowerThanPredicate<T, S>(matchType);
    }

    /**
     * Generates a Predicate that only accepts the Match Results that have a Match Type equal to matchType
     *
     * @param matchType the matchType
     * @param <T>       a subclass of MatchResult
     * @param <S>       a subclass of MatchType
     * @return the Predicate
     */
    public static <T extends MatchResult, S extends MatchType> Predicate<T> equalTo(S matchType) {
        return new EqualToPredicate<T, S>(matchType);
    }

    /**
     * Generates a Predicate that only accepts the Match Results that have a Match Type greater or equal to matchType
     *
     * @param matchType the matchType that defines the boundary
     * @param <T>       a subclass of MatchResult
     * @param <S>       a subclass of MatchType
     * @return the Predicate
     */
    public static <T extends MatchResult, S extends MatchType> Predicate<T> greaterOrEqualTo(S matchType) {
        return Predicates.or(greaterThan(matchType), equalTo(matchType));
    }

    /**
     * Generates a Predicate that only accepts the Match Results that have a Match Type lower or equal to matchType
     *
     * @param matchType the matchType that defines the boundary
     * @param <T>       a subclass of MatchResult
     * @param <S>       a subclass of MatchType
     * @return the Predicate
     */
    public static <T extends MatchResult, S extends MatchType> Predicate<T> lowerOrEqualTo(S matchType) {
        return Predicates.or(lowerThan(matchType), equalTo(matchType));
    }

    /**
     * Generates a Predicate that only accepts the Match Results within a range. The range may be closed or open at each
     * of the boundaries. {@code BoundType.CLOSED} means that the boundary should also be accepted. {@code BoundType.OPEN}
     * on the other indicates that the boundary itself should not be accepted.
     *
     * @param minMatchType the matchType that defines the lower boundary
     * @param minBound     the lower {@code BoundType}
     * @param maxMatchType the matchType that defines the upper boundary
     * @param maxBound     the upper {@code BoundType}
     * @param <T>          a subclass of MatchResult
     * @param <S>          a subclass of MatchType
     * @return the Predicate
     */
    public static <T extends MatchResult, S extends MatchType> Predicate<T> withinRange(S minMatchType, BoundType minBound, S maxMatchType, BoundType maxBound) {

        Predicate<T> lowerPredicate;
        Predicate<T> upperPredicate;

        if (minBound.equals(BoundType.CLOSED)) {
            lowerPredicate = greaterOrEqualTo(minMatchType);
        } else {
            lowerPredicate = greaterThan(minMatchType);
        }

        if (maxBound.equals(BoundType.CLOSED)) {
            upperPredicate = lowerOrEqualTo(maxMatchType);
        } else {
            upperPredicate = lowerThan(maxMatchType);
        }

        return Predicates.and(lowerPredicate, upperPredicate);
    }


    private static class GreaterThanPredicate<T extends MatchResult, S extends MatchType> implements Predicate<T> {

        private final S matchType;

        private GreaterThanPredicate(S matchType) {
            this.matchType = checkNotNull(matchType);
        }

        @Override
        public boolean apply(@Nullable T matchResult) {
            return (matchResult.getMatchType().compareTo(matchType) > 0) ? true : false;
        }
    }

    private static class LowerThanPredicate<T extends MatchResult, S extends MatchType> implements Predicate<T> {

        private final S matchType;

        private LowerThanPredicate(S matchType) {
            this.matchType = checkNotNull(matchType);
        }

        @Override
        public boolean apply(@Nullable T matchResult) {
            return (matchResult.getMatchType().compareTo(matchType) < 0) ? true : false;
        }
    }

    private static class EqualToPredicate<T extends MatchResult, S extends MatchType> implements Predicate<T> {

        private final S matchType;

        private EqualToPredicate(S matchType) {
            this.matchType = checkNotNull(matchType);
        }

        @Override
        public boolean apply(@Nullable T matchResult) {
            return (matchResult.getMatchType().compareTo(matchType) == 0) ? true : false;
        }
    }
}
