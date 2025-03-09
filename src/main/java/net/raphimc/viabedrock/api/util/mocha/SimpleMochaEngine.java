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
package net.raphimc.viabedrock.api.util.mocha;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mocha.parser.MolangParser;
import team.unnamed.mocha.parser.ParseException;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.runtime.ExpressionInterpreter;
import team.unnamed.mocha.runtime.Scope;
import team.unnamed.mocha.runtime.value.NumberValue;
import team.unnamed.mocha.runtime.value.Value;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.List;

// Take a look at MochaEngineImpl, this is a simpler implementation and allow string output!
public final class SimpleMochaEngine<T> {
    private Scope scope;

    public SimpleMochaEngine(Scope scope) {
        this.scope = scope;
    }

    public String eval(final List<String> variables, String source) {
        // This is due to mocha parsing, you have to do (value > 3) ? 'value1' : 'value2' instead of value > 3 ? 'value1' : 'value2'.
        // Hacky workaround for this.
        if (!source.toLowerCase().startsWith("array.")) {
            String[] split = source.split("\\?");
            if (split.length == 2) {
                source = "(" + split[0].replace(" ", "") + ") ? " + split[1];
            }
        } else {
            String[] splitArray = source.toLowerCase().split("\\[");
            if (splitArray.length != 2) {
                return "0";
            }

            String newSource = splitArray[1].replace("]", "").replace("[", "");
            String[] split = newSource.split("\\?");
            if (split.length == 2) {
                newSource = "(" + split[0].replace(" ", "") + ") ? " + split[1];
            }

            source = splitArray[0] + "[" + newSource + "]";
        }
        source = source.replace(" ", "");

        final StringBuilder builder = new StringBuilder();
        variables.forEach(var -> builder.append(var).append(";"));
        builder.append(source);

        return eval(builder.toString());
    }

    public String eval(final @NotNull String source) {
        final List<Expression> parsed;
        try {
            parsed = parse(new StringReader(source));
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read from given reader", e);
        }
        return eval(parsed);
    }

    @SuppressWarnings("UnstableApiUsage")
    public String eval(final @NotNull List<Expression> expressions) {
        // create bindings that just apply for this evaluation
        final Scope local = this.scope.copy();
        local.readOnly(true);
        final ExpressionInterpreter<T> evaluator = new ExpressionInterpreter<>(null, local);
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

        // ensure returned value is a number
        return lastResult == null ? "0" : lastResult.getAsString();
    }

    public @NotNull List<Expression> parse(final @NotNull Reader reader) throws IOException {
        return MolangParser.parser(reader).parseAll();
    }
}