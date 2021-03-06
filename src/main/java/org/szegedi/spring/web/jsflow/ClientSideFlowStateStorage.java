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

import javax.servlet.http.HttpServletRequest;
import org.springframework.util.Base64Utils;
import org.szegedi.spring.web.jsflow.support.AbstractFlowStateStorage;

/**
 * An implementation of the flowstate storage that returns the BASE64-encoded
 * serialized state as the ID. This way, the ID encapsulates the whole state,
 * and the client will store it as part of the rendered HTML page, with no state
 * whatsoever remaining on the server, allowing for truly massive scalability.
 * Note however that in order to prevent the users from tampering with the
 * flowstates while they are on the client side, or forging false flowstates,
 * you should install at least a digital signature providing
 * {@link org.szegedi.spring.web.jsflow.codec.IntegrityCodec} into this storage
 * instance, although you can go for a full codec stack including compression,
 * encryption, and digital signature by using a
 * {@link org.szegedi.spring.web.jsflow.codec.CompositeCodec} (that you can
 * further enhance with {@link org.szegedi.spring.web.jsflow.codec.PooledCodec
 * pooling}).
 *
 * @author Attila Szegedi
 * @version $Id$
 */
public class ClientSideFlowStateStorage extends AbstractFlowStateStorage {
    @Override
    protected byte[] getSerializedState(final HttpServletRequest request, final String id) {
        return Base64Utils.decodeFromString(id);
    }

    @Override
    protected String storeSerializedState(final HttpServletRequest request, final byte[] state) {
        return Base64Utils.encodeToString(state);
    }
}
