/*
   Copyright 2006 Attila Szegedi

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
package org.szegedi.spring.web.jsflow;

import org.springframework.core.NestedRuntimeException;

/**
 * Thrown when
 * {@link org.szegedi.spring.web.jsflow.support.AbstractFlowStateStorage}
 * persistence fails.
 * 
 * @author Attila Szegedi
 * @version $Id$
 */
public class FlowStateStorageException extends NestedRuntimeException {
    private static final long serialVersionUID = 1L;

    public FlowStateStorageException(final String msg) {
        super(msg);
    }

    public FlowStateStorageException(final String msg, final Throwable ex) {
        super(msg, ex);
    }
}
