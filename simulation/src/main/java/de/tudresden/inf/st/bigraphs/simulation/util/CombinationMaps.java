package de.tudresden.inf.st.bigraphs.simulation.util;

import java.util.*;

/**
 * @author Dominik Grzelak
 * @see <a href="https://stackoverflow.com/a/8217761">https://stackoverflow.com/a/8217761</a>
 */
public class CombinationMaps {

    public static <K, V> void combinations(Map<K, List<V>> map, List<Map<K, V>> list) {
        recurse(map, new LinkedList<>(map.keySet()).listIterator(), new HashMap<>(), list);
    }

    // helper method to do the recursion
    private static <K, V> void recurse(Map<K, List<V>> map, ListIterator<K> iter, Map<K, V> cur, List<Map<K, V>> list) {
        // we're at a leaf node in the recursion tree, add solution to list
        if (!iter.hasNext()) {
            Map<K, V> entry = new HashMap<K, V>();

            for (K key : cur.keySet()) {
                entry.put(key, cur.get(key));
            }

            list.add(entry);
        } else {
            K key = iter.next();
            List<V> set = map.get(key);

            for (V value : set) {
                cur.put(key, value);
                recurse(map, iter, cur, list);
                cur.remove(key);
            }

            iter.previous();
        }
    }

    public static <K, V> void Combine(int index, Map<K, V> current,
                                      Map<K, Set<V>> map,
                                      List<Map<K, V>> list) {

        if (index == map.size()) { // if we have gone through all keys in the map
            Map<K, V> newMap = new HashMap<K, V>();
            System.out.println(current);
            for (K key : current.keySet()) {          // copy contents to new map.
                newMap.put(key, current.get((K) key));
            }
            list.add(newMap); // add to result.
        } else {
            Object currentKey = map.keySet().toArray()[index]; // take the current key
            for (V value : map.get(currentKey)) {
                current.put((K) currentKey, value); // put each value into the temporary map
                Combine(index + 1, current, map, list); // recursive call
                current.remove(currentKey); // discard and try a new value
            }
        }
    }
}
