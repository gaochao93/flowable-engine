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

package org.activiti5.engine.impl.bpmn.behavior;

import org.activiti5.engine.impl.context.Context;
import org.activiti5.engine.impl.delegate.JavaDelegateInvocation;
import org.activiti5.engine.impl.pvm.delegate.ActivityExecution;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.delegate.ActivityBehavior;


/**
 * @author Tom Baeyens
 */
public class ServiceTaskJavaDelegateActivityBehavior extends TaskActivityBehavior implements ActivityBehavior, ExecutionListener {
  
  protected JavaDelegate javaDelegate;
  
  protected ServiceTaskJavaDelegateActivityBehavior() {
  }

  public ServiceTaskJavaDelegateActivityBehavior(JavaDelegate javaDelegate) {
    this.javaDelegate = javaDelegate;
  }

  public void execute(DelegateExecution execution) {
    Context.getProcessEngineConfiguration()
      .getDelegateInterceptor()
      .handleInvocation(new JavaDelegateInvocation(javaDelegate, execution));
    leave((ActivityExecution) execution);
  }
  
  public void notify(DelegateExecution execution) {
    execute(execution);
  }
}