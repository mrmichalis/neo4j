/**
 * Copyright (c) 2002-2014 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.internal.compiler.v2_1.ast.rewriters

import org.neo4j.cypher.internal.compiler.v2_1._
import ast._

object TheDefaultMatchPredicateNormalization
  extends MatchPredicateNormalization(MatchPredicateNormalizerChain(PropertyPredicateNormalizer, LabelPredicateNormalizer)) {
}

class MatchPredicateNormalization(normalizer: MatchPredicateNormalizer) extends Rewriter {

  def apply(that: AnyRef): Option[AnyRef] = instance.apply(that)

  private val instance: Rewriter = Rewriter.lift {

   case m@Match(_, pattern, _, where) =>
      val predicates = pattern.fold(Vector.empty[Expression]) {
        case pattern: AnyRef if normalizer.extract.isDefinedAt(pattern) => acc => acc ++ normalizer.extract(pattern)
        case _                                                          => identity
      }

      if (predicates.isEmpty)
        m
      else {
        val rewrittenPredicates = predicates ++ where.map(_.expression)
        val rewrittenPredicate = rewrittenPredicates reduceLeftOption { (lhs, rhs) => And(lhs, rhs)(m.position) }
        m.copy(
          pattern = pattern.rewrite(topDown(Rewriter.lift(normalizer.replace))).asInstanceOf[Pattern],
          where = rewrittenPredicate.map(Where(_)(where.map(_.position).getOrElse(m.position)))
        )(m.position)
      }
  }
}