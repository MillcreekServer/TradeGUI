package io.github.wysohn.tradegui.util;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NumBuilderTest {

    private NumBuilder numBuilder;

    @Before
    public void init() {
        numBuilder = new NumBuilder();
    }

    @Test
    public void append() {
        numBuilder.append(0);
        numBuilder.append(0);
        numBuilder.append(0);
        numBuilder.append(1);
        numBuilder.append(2);
        numBuilder.append(0);
        numBuilder.append(3);

        numBuilder.toggleDot();
        numBuilder.append(4);
        numBuilder.append(3);

        assertEquals(1203.43, numBuilder.getNum(), 0.001);

        numBuilder.toggleDot();
        numBuilder.append(1);
        numBuilder.append(2);
        numBuilder.append(5);

        assertEquals(1203125.43, numBuilder.getNum(), 0.001);

        numBuilder.toggleDot();
        numBuilder.append(2);
        numBuilder.append(0);
        numBuilder.append(8); // dot is automatically toggled at this point

        assertEquals(12031258.2, numBuilder.getNum(), 0.001);

        for (int i = 0; i < 400; i++) // should hit ceiling
            numBuilder.append(9);

        assertEquals(NumBuilder.CEILING, numBuilder.getNum(), 0.001);

        numBuilder.toggleDot();
        numBuilder.append(2);
        numBuilder.append(0);
        numBuilder.append(8); // dot is automatically toggled at this point

        assertEquals(NumBuilder.CEILING, numBuilder.getNum(), 0.001);
    }

    @Test
    public void clear() {
        numBuilder.append(1);
        numBuilder.append(2);
        numBuilder.append(5);
        numBuilder.append(3);

        numBuilder.toggleDot();
        numBuilder.append(4);
        numBuilder.append(3);

        assertEquals(1253.43, numBuilder.getNum(), 0.001);

        numBuilder.clear();

        assertEquals(0.0, numBuilder.getNum(), 0.001);
    }
}