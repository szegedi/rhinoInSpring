/*
   Copyright 2006, 2007 Attila Szegedi

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.szegedi.spring.web.jsflow.support;

import java.util.Random;
import org.mozilla.javascript.NativeContinuation;

/**
 * Default implementation of flow state id generator that uses a random number
 * generator.
 * @author Attila Szegedi
 * @version $Id: $
 */
public class RandomFlowStateIdGenerator implements FlowStateIdGenerator
{
    private Random random;

    public void setRandom(final Random random)
    {
        this.random = random;
    }

    public Long generateStateId(final NativeContinuation c)
    {
        return new Long(random.nextLong() & Long.MAX_VALUE);
    }

    public boolean dependsOnContinuation()
    {
        return false;
    }
}