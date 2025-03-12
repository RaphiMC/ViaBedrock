/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
 *
 * This program is free software: you can redistribute it and/or modify
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
package net.raphimc.viabedrock.api.util;

import team.unnamed.mocha.parser.MolangParser;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.runtime.ExpressionInterpreter;
import team.unnamed.mocha.runtime.Scope;
import team.unnamed.mocha.runtime.value.MutableObjectBinding;
import team.unnamed.mocha.runtime.value.NumberValue;
import team.unnamed.mocha.runtime.value.Value;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class MoLangEngine {

    public static Value eval(final Scope scope, final String expression) throws IOException {
        return eval(scope, parse(expression));
    }

    public static Value eval(final Scope scope, final List<Expression> expressions) {
        final Scope localScope = scope.copy();
        final MutableObjectBinding tempBinding = new MutableObjectBinding();
        localScope.set("temp", tempBinding);
        localScope.set("t", tempBinding);
        localScope.readOnly(true);

        final ExpressionInterpreter<Void> evaluator = new ExpressionInterpreter<>(null, localScope);
        evaluator.warnOnReflectiveFunctionUsage(false);

        Value lastResult = NumberValue.zero();
        for (Expression expression : expressions) {
            lastResult = expression.visit(evaluator);
            Value returnValue = evaluator.popReturnValue();
            if (returnValue != null) {
                lastResult = returnValue;
                break;
            }
        }

        return lastResult;
    }

    public static List<Expression> parse(final String expression) throws IOException {
        try (final StringReader reader = new StringReader(expression)) {
            return parse(reader);
        }
    }

    public static List<Expression> parse(final Reader reader) throws IOException {
        return MolangParser.parser(reader).parseAll();
    }

}
