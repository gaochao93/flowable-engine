/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.cmmn.test.runtime;

import org.flowable.cmmn.api.runtime.CaseInstance;
import org.flowable.cmmn.api.runtime.MilestoneInstance;
import org.flowable.cmmn.api.runtime.PlanItemDefinitionType;
import org.flowable.cmmn.api.runtime.PlanItemInstance;
import org.flowable.cmmn.api.runtime.PlanItemInstanceState;
import org.flowable.cmmn.engine.test.CmmnDeployment;
import org.flowable.cmmn.engine.test.FlowableCmmnTestCase;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Dennis Federico
 */
public class MilestoneQueryTest extends FlowableCmmnTestCase {

    @Test
    @CmmnDeployment
    public void testSimpleMilestoneInstanceQuery() {
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder().caseDefinitionKey("testSimpleMilestoneInstanceQuery").start();
        //Check setup
        assertCaseInstanceNotEnded(caseInstance);
        List<PlanItemInstance> planItemInstances = cmmnRuntimeService.createPlanItemInstanceQuery().list();
        assertEquals(7, planItemInstances.size());
        assertEquals(3, planItemInstances.stream().filter(p -> PlanItemDefinitionType.MILESTONE.equals(p.getPlanItemDefinitionType()))
                .filter(p -> PlanItemInstanceState.AVAILABLE.equals(p.getState())).count());
        assertEquals(4, planItemInstances.stream().filter(p -> PlanItemDefinitionType.USER_EVENT_LISTENER.equals(p.getPlanItemDefinitionType()))
                .filter(p -> PlanItemInstanceState.AVAILABLE.equals(p.getState())).count());

        List<MilestoneInstance> milestoneInstances = cmmnRuntimeService.createMilestoneInstanceQuery().list();
        assertNotNull(milestoneInstances);
        assertTrue(milestoneInstances.isEmpty());

        //event triggering
        setClockTo(new Date(System.currentTimeMillis() + 60_000L));
        Date beforeFirstTrigger = cmmnEngineConfiguration.getClock().getCurrentTime();
        PlanItemInstance event = cmmnRuntimeService.createPlanItemInstanceQuery().planItemDefinitionId("event1").singleResult();
        cmmnRuntimeService.triggerPlanItemInstance(event.getId());
        Date afterFirstTrigger = cmmnEngineConfiguration.getClock().getCurrentTime();

        setClockTo(new Date(afterFirstTrigger.getTime() + 60_000L));
        Date beforeSecondTrigger = cmmnEngineConfiguration.getClock().getCurrentTime();
        event = cmmnRuntimeService.createPlanItemInstanceQuery().planItemDefinitionId("event2").singleResult();
        cmmnRuntimeService.triggerPlanItemInstance(event.getId());
        Date afterSecondTrigger = cmmnEngineConfiguration.getClock().getCurrentTime();


        setClockTo(new Date(afterSecondTrigger.getTime() + 60_000L));
        Date beforeThirdTrigger = cmmnEngineConfiguration.getClock().getCurrentTime();
        event = cmmnRuntimeService.createPlanItemInstanceQuery().planItemDefinitionId("event3").singleResult();
        cmmnRuntimeService.triggerPlanItemInstance(event.getId());
        Date afterThirdTrigger = cmmnEngineConfiguration.getClock().getCurrentTime();

        assertEquals(3, cmmnRuntimeService.createMilestoneInstanceQuery().milestoneInstanceCaseInstanceId(caseInstance.getId()).count());

        //There are two named milestones
        MilestoneInstance abcMilestone = cmmnRuntimeService.createMilestoneInstanceQuery().milestoneInstanceName("abcMilestone").singleResult();
        assertNotNull(abcMilestone);
        assertEquals("abcMilestone", abcMilestone.getName());

        MilestoneInstance xyzMilestone = cmmnRuntimeService.createMilestoneInstanceQuery().milestoneInstanceName("xyzMilestone").singleResult();
        assertNotNull(xyzMilestone);
        assertEquals("xyzMilestone", xyzMilestone.getName());

        MilestoneInstance one = cmmnRuntimeService.createMilestoneInstanceQuery().milestoneInstanceName("1").singleResult();
        assertNotNull(one);
        assertEquals("1", one.getName());

        //One Milestone has no name
        List<MilestoneInstance> list = cmmnRuntimeService.createMilestoneInstanceQuery().orderByMilestoneName().asc().list();
        assertEquals(3, list.size());
        //assertNull(list.get(0).getName());
        assertEquals("1", list.get(0).getName());
        assertEquals("abcMilestone", list.get(1).getName());
        assertEquals("xyzMilestone", list.get(2).getName());

        //Query timestamps
        MilestoneInstance milestone1 = cmmnRuntimeService.createMilestoneInstanceQuery()
                .milestoneInstanceReachedAfter(beforeFirstTrigger)
                .milestoneInstanceReachedBefore(afterFirstTrigger)
                .singleResult();
        assertNotNull(milestone1);
        assertEquals("milestonePlanItem1", milestone1.getElementId());
        MilestoneInstance milestone2 = cmmnRuntimeService.createMilestoneInstanceQuery()
                .milestoneInstanceReachedAfter(beforeSecondTrigger)
                .milestoneInstanceReachedBefore(afterSecondTrigger)
                .singleResult();
        assertNotNull(milestone2);
        assertEquals("milestonePlanItem2", milestone2.getElementId());
        MilestoneInstance milestone3 = cmmnRuntimeService.createMilestoneInstanceQuery()
                .milestoneInstanceReachedAfter(beforeThirdTrigger)
                .milestoneInstanceReachedBefore(afterThirdTrigger)
                .singleResult();
        assertNotNull(milestone3);
        assertEquals("milestonePlanItem3", milestone3.getElementId());

        list = cmmnRuntimeService.createMilestoneInstanceQuery().orderByTimeStamp().desc().list();
        assertEquals("milestonePlanItem3", list.get(0).getElementId());
        assertEquals("milestonePlanItem2", list.get(1).getElementId());
        assertEquals("milestonePlanItem1", list.get(2).getElementId());

        //Finish Case by triggering the last event
        event = cmmnRuntimeService.createPlanItemInstanceQuery().planItemDefinitionId("event4").singleResult();
        cmmnRuntimeService.triggerPlanItemInstance(event.getId());
        assertCaseInstanceEnded(caseInstance);
    }
}
