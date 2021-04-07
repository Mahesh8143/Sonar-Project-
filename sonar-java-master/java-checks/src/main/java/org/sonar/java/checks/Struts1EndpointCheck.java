/*
 * SonarQube Java
 * Copyright (C) 2012-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.java.checks;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.semantic.MethodMatchers;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;

@Rule(key = "S4530")
public class Struts1EndpointCheck extends IssuableSubscriptionVisitor {

  private static final MethodMatchers STRUTS_METHOD = MethodMatchers.create()
    .ofAnyType()
    .names("perform", "execute")
    .addParametersMatcher(
      "org.apache.struts.action.ActionMapping",
      "org.apache.struts.action.ActionForm",
      "javax.servlet.http.HttpServletRequest",
      "javax.servlet.http.HttpServletResponse")
    .addParametersMatcher(
      "org.apache.struts.action.ActionMapping",
      "org.apache.struts.action.ActionForm",
      "javax.servlet.ServletRequest",
      "javax.servlet.ServletResponse")
    .build();

  @Override
  public List<Tree.Kind> nodesToVisit() {
    return Collections.singletonList(Tree.Kind.METHOD);
  }

  @Override
  public void visitNode(Tree tree) {
    if(!hasSemantic()) {
      return;
    }
    MethodTree methodTree = (MethodTree) tree;
    if (methodTree.symbol().owner().type().isSubtypeOf("org.apache.struts.action.Action") && STRUTS_METHOD.matches(methodTree)) {
      List<IdentifierTree> actionMappingUsages = methodTree.parameters().get(1).symbol().usages();
      if (!actionMappingUsages.isEmpty()) {
        reportIssue(methodTree.simpleName(), "Make sure that the ActionForm is used safely here.",
          actionMappingUsages.stream().map(idTree -> new JavaFileScannerContext.Location("", idTree)).collect(Collectors.toList()), null);
      }
    }
  }
}
