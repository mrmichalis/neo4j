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
package org.neo4j.cypher.internal.compiler.v2_1.planner.logical.plans

import org.neo4j.graphdb.Direction
import org.neo4j.cypher.internal.compiler.v2_1.ast.RelTypeName

case class Expand(left: LogicalPlan,
                  from: IdName,
                  dir: Direction,
                  types: Seq[RelTypeName],
                  to: IdName,
                  relName: IdName,
                  length: PatternLength)
                 (pattern: PatternRelationship) extends LogicalPlan {
  val lhs = Some(left)
  def rhs = None

  def availableSymbols: Set[IdName] = left.availableSymbols + relName + to
}

object ExpandPlan {
  def apply(left: QueryPlan,
            from: IdName,
            dir: Direction,
            types: Seq[RelTypeName],
            to: IdName,
            relName: IdName,
            length: PatternLength,
            pattern: PatternRelationship) =
    QueryPlan( Expand(left.plan, from, dir, types, to, relName, length)(pattern), left.solved.addPatternRel(pattern) )
}
