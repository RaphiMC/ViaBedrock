/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
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

import java.util.*;

public class CombinationUtil {

    public static <K, V> List<Map<K, V>> generateCombinations(final Map<K, Set<V>> map) {
        final List<Map<K, V>> output = new ArrayList<>();
        generateCombinationsRecursively(map, new LinkedList<>(map.keySet()).listIterator(), new LinkedHashMap<>(), output);
        return output;
    }

    private static <K, V> void generateCombinationsRecursively(final Map<K, Set<V>> map, final ListIterator<K> keys, final Map<K, V> cur, final List<Map<K, V>> output) {
        if (!keys.hasNext()) {
            final Map<K, V> entry = new LinkedHashMap<>();

            for (K key : cur.keySet()) {
                entry.put(key, cur.get(key));
            }

            output.add(entry);
        } else {
            final K key = keys.next();
            final Set<V> set = map.get(key);

            for (V value : set) {
                cur.put(key, value);
                generateCombinationsRecursively(map, keys, cur, output);
                cur.remove(key);
            }

            keys.previous();
        }
    }

}
