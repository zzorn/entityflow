package org.entityflow2;

import net.openhft.koloboke.collect.set.IntSet;
import org.entityflow2.component.TestComponent;
import org.flowutils.random.RandomSequence;
import org.flowutils.random.XorShift;
import org.flowutils.time.ManualTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 */
public class EntityManagerTest {


    private ManualTime time;
    private ConcurrentEntityManager entityManager;
    private TestComponent testComponent;

    private static final int LOTS = ConcurrentEntityManager.EXPECTED_ENTITY_COUNT * 10;

    @Before
    public void setUp() throws Exception {
        time = new ManualTime();
        entityManager = new ConcurrentEntityManager();
        testComponent = entityManager.addComponentType(new TestComponent());
        entityManager.init();
    }

    @After
    public void tearDown() throws Exception {
        entityManager.shutdown();
    }

    @Test
    public void testAddRemoveComponent() throws Exception {
        assertEquals(0, testComponent.getComponentCount());

        final int entityId1 = createAndInitComponent(1);
        final int entityId2 = createAndInitComponent(2);
        final int entityId3 = createAndInitComponent(3);
        final int entityId4 = createAndInitComponent(4);
        final int entityId5 = createAndInitComponent(5);

        update();

        assertEquals(5, testComponent.getComponentCount());

        assertEquals(1, testComponent.number.get(entityId1));
        assertEquals(2, testComponent.number.get(entityId2));
        assertEquals(3, testComponent.number.get(entityId3));
        assertEquals(4, testComponent.number.get(entityId4));
        assertEquals(5, testComponent.number.get(entityId5));

        update();

        removeComponentAndTest(entityId3);

        assertEquals(4, testComponent.getComponentCount());

        final int entityId6 = createAndInitComponent(6);

        assertEquals(5, testComponent.getComponentCount());

        assertEquals(1, testComponent.number.get(entityId1));
        assertEquals(2, testComponent.number.get(entityId2));
        update();
        assertEquals(4, testComponent.number.get(entityId4));
        assertEquals(5, testComponent.number.get(entityId5));
        update();
        assertEquals(6, testComponent.number.get(entityId6));

        removeComponentAndTest(entityId1);
        removeComponentAndTest(entityId4);
        update();
        removeComponentAndTest(entityId5);
        removeComponentAndTest(entityId6);

        assertEquals(2, testComponent.number.get(entityId2));

        update();
        assertEquals(1, testComponent.getComponentCount());

        final int entityId7 = createAndInitComponent(7);
        final int entityId8 = createAndInitComponent(8);
        final int entityId9 = createAndInitComponent(9);
        final int entityId10 = createAndInitComponent(10);
        final int entityId11 = createAndInitComponent(11);

        assertEquals(2, testComponent.number.get(entityId2));
        assertEquals(7, testComponent.number.get(entityId7));
        assertEquals(8, testComponent.number.get(entityId8));
        update();
        assertEquals(9, testComponent.number.get(entityId9));
        assertEquals(10, testComponent.number.get(entityId10));
        assertEquals(11, testComponent.number.get(entityId11));

        assertEquals(6, testComponent.getComponentCount());
    }

    @Test
    public void testAddRemoveLots1() throws Exception {
        for (int i = 1; i < LOTS; i++) {
            createAndInitComponent(i);
        }

        for (int i = 1; i < LOTS; i++) {
            removeComponentAndTest(i);
        }

        update();
    }

    @Test
    public void testAddRemoveLots2() throws Exception {
        for (int i = 1; i < LOTS; i++) {
            createAndInitComponent(i);
        }

        update();

        for (int i = 1; i < LOTS; i++) {
            removeComponentAndTest(i);
        }

        update();
    }

    @Test
    public void testAddRemoveLots3() throws Exception {
        for (int i = 1; i < LOTS; i++) {
            createAndInitComponent(i);
            update();
        }


        for (int i = 1; i < LOTS; i++) {
            removeComponentAndTest(i);
            update();
        }
    }

    @Test
    public void testAddRemoveLots4() throws Exception {
        RandomSequence randomSequence = new XorShift();
        for (int j = 0; j < 1000; j++) {
            for (int i = 1; i < randomSequence.nextInt(LOTS); i++) {
                createAndInitComponent(i);
            }

            for (int i = 1; i < randomSequence.nextInt(LOTS/2); i++) {
                final IntSet entityIds = entityManager.getEntityIds();
                if (!entityIds.isEmpty()) removeComponentAndTest(entityIds.cursor().elem());
            }

            update();
        }
    }

    @Test
    public void testAddRemoveLots5() throws Exception {
        RandomSequence randomSequence = new XorShift();
        for (int j = 0; j < 1000; j++) {
            for (int i = 1; i < randomSequence.nextInt(LOTS); i++) {
                createAndInitComponent(i);
                update();
            }

            for (int i = 1; i < randomSequence.nextInt(LOTS/2); i++) {
                final IntSet entityIds = entityManager.getEntityIds();
                if (!entityIds.isEmpty()) removeComponentAndTest(entityIds.cursor().elem());
                update();
            }
        }
    }

    private void removeComponentAndTest(int entityId) {
        assertEquals(entityId, testComponent.number.get(entityId));
        assertEquals("Number " + entityId, testComponent.name.get(entityId));

        testComponent.removeFromEntity(entityId);

        try {
            testComponent.number.get(entityId);
            fail("Should throw exception when trying to access removed component");
        }
        catch (Exception e) {
            // Ok
        }
    }

    private int createAndInitComponent(final int number) {
        final int entityId = entityManager.createEntity(testComponent);
        testComponent.number.set(entityId, number);
        testComponent.name.set(entityId, "Number " + number);
        assertEquals(number, testComponent.number.get(entityId));
        assertEquals("Number " + number, testComponent.name.get(entityId));
        return entityId;
    }

    private void update() {
        time.advanceTimeSeconds(0.01);
        entityManager.update(time);
    }

}