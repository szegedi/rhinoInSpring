package org.szegedi.spring.web.jsflow.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.continuations.Continuation;
import org.mozilla.javascript.serialize.ScriptableInputStream;
import org.mozilla.javascript.serialize.ScriptableOutputStream;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.szegedi.spring.web.jsflow.HostObject;
import org.szegedi.spring.web.jsflow.ScriptStorage;

/**
 * A class able to serialize and deserialize a continuation within a specified
 * application context. 
 * @author Attila Szegedi
 * @version $Id: $
 */
public class FlowStateSerializer implements ApplicationContextAware, InitializingBean
{
    private ScriptStorage scriptStorage;
    private PersistenceSupport persistenceSupport;
    private ApplicationContext applicationContext;
    private Map beansToStubs = Collections.EMPTY_MAP;

    public void setScriptStorage(ScriptStorage scriptStorage)
    {
        this.scriptStorage = scriptStorage;
        if(scriptStorage != null)
        {
            persistenceSupport = scriptStorage.getPersistenceSupport();
        }
    }
    
    public ScriptStorage getScriptStorage()
    {
        return scriptStorage;
    }
    
    public void setApplicationContext(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }
    
    public void afterPropertiesSet() throws Exception
    {
        createStubInfo();
    }
    
    private void createStubInfo()
    {
        String[] names = BeanFactoryUtils.beanNamesIncludingAncestors(applicationContext);
        Map beansToStubs = new IdentityHashMap();
        for (int i = 0; i < names.length; i++)
        {
            String name = names[i];
            beansToStubs.put(applicationContext.getBean(name), 
                    new ApplicationContextBeanStub(name));
        }
        beansToStubs.put(".", applicationContext);
        this.beansToStubs = beansToStubs;
    }
    
    /**
     * Serializes a continuation. All script function object references as well
     * as all references to objects defined in the application context will be
     * replaced by named stubs. Additionally, a digital fingerprint of the 
     * internal JS bytecode representation of all JS functions on the 
     * continuation's stack is written.
     * @param state the continuation to serialize
     * @return the serialized form
     * @throws Exception
     */
    protected byte[] serializeContinuation(Continuation state) throws Exception
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ContinuationOutputStream(bout, 
                state);
        out.writeObject(FunctionFingerprintManager.getFingerprints(state));
        out.writeObject(state);
        out.close();
        byte[] b = bout.toByteArray();
        return b;
    }
    
    /**
     * Deserializes a continuation. All stubs written during serialization are
     * resolved to appropriate objects within this application context. 
     * Additionally, digital fingerprints of the functions on the 
     * continuation's stack are matched to the fingerprints of the same 
     * functions currently in the memory. If they don't match (i.e. the script
     * was modified since the continuation was serialized), an exception is
     * thrown to prevent undefined behaviour as the continuation's stack might
     * now contain invalid return addresses.
     * @param b the serialized continuation
     * @return the deserialized continuation
     * @throws Exception
     */
    protected Continuation deserializeContinuation(byte[] b) throws Exception
    {
        ObjectInputStream in = new ContinuationInputStream(
                new ByteArrayInputStream(b));
        Object fingerprints = in.readObject();
        Continuation cont = (Continuation)in.readObject();
        FunctionFingerprintManager.checkFingerprints(cont, fingerprints);
        return cont;
    }
    
    private class ContinuationInputStream extends ScriptableInputStream
    {

        public ContinuationInputStream(InputStream in) throws IOException
        {
            super(in, persistenceSupport.getLibrary());
        }

        protected Object resolveObject(Object obj)
        throws 
            IOException
        {
            if(obj instanceof ApplicationContextBeanStub)
            {
                ApplicationContextBeanStub stub = 
                    (ApplicationContextBeanStub)obj;
                Object robj = applicationContext.getBean(stub.beanName);
                if(robj != null)
                {
                    return robj;
                }
                else
                {
                    throw new InvalidObjectException("No bean with name [" + 
                            stub.beanName + "] found");
                }
            }
            else
            {
                Object robj = persistenceSupport.resolveFunctionStub(obj);
                if(robj != null)
                {
                    return robj;
                }
            }
            return super.resolveObject(obj);
        }
    }
    
    private class ContinuationOutputStream extends ScriptableOutputStream
    {
        public ContinuationOutputStream(OutputStream out, Continuation cont) throws IOException
        {
            super(out, ScriptableObject.getTopLevelScope(cont).getPrototype());
            addExcludedName(HostObject.CLASS_NAME);
            addExcludedName(HostObject.CLASS_NAME + ".prototype");
        }
        
        protected Object replaceObject(Object obj) throws IOException
        {
            Object stub = beansToStubs.get(obj);
            if(stub != null)
            {
                return stub;
            }
            stub = persistenceSupport.getFunctionStub(obj);
            if(stub != null)
            {
                return stub;
            }
            return super.replaceObject(obj);
        }
    }
    
    private static class ApplicationContextBeanStub implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private final String beanName;
        
        ApplicationContextBeanStub(String beanName)
        {
            this.beanName = beanName;
        }
        
        public boolean equals(Object obj)
        {
            if(obj instanceof ApplicationContextBeanStub)
            {
                return ((ApplicationContextBeanStub)obj).beanName.equals(beanName);
            }
            return false;
        }
        
        public int hashCode()
        {
            return beanName.hashCode();
        }
        
        public String toString()
        {
            return "stub:" + beanName;
        }
    }
}