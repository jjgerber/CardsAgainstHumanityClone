package org.j3y.cards.util;

import java.util.Collection;
import java.util.Random;

public class CollectionUtil {

    public static <T> T getRandomItemFromCollection(Collection<T> objects) {
        Random random = new Random();
        int randomIdx = random.nextInt(objects.size());

        int i = 0;
        for (T object : objects) {
            if (i == randomIdx) {
                return object;
            }
            i++;
        }

        return null; // Should never hit this.
    }

}
