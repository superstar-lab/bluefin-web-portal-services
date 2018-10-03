package com.mcmcg.ico.bluefin.util;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

public class TestUtilClass {

    /**
     * 
     * Work around to remove proxies from the classes that we need to mock.
     * 
     * Note: this is a bug present in version 4.2.7 and fixed in 4.3.1
     * 
     * @see <a href=
     *      "http://kim.saabye-pedersen.org/2012/12/mockito-and-spring-proxies.html">
     *      http://kim.saabye-pedersen.org/2012/12/mockito-and-spring-proxies.
     *      html</a>
     * @see <a href="https://jira.spring.io/browse/SPR-14050">https://jira.
     *      spring.io/browse/SPR-14050</a>
     * @see <a href=
     *      "http://forum.springsource.org/showthread.php?60216-Need-to-unwrap-a-proxy-to-get-the-object-being-proxied">
     *      http://forum.springsource.org/showthread.php?60216-Need-to-unwrap-a-
     *      proxy-to-get-the-object-being-proxied</a>
     * @param bean
     * @return
     * @throws Exception
     */
    public static final Object unwrapProxy(Object bean) throws Exception {
        /*
         * If the given object is a proxy, set the return value as the object
         * being proxied, otherwise return the given object.
         */
        if (AopUtils.isAopProxy(bean) && bean instanceof Advised) {
            Advised advised = (Advised) bean;
            bean = advised.getTargetSource().getTarget();
        }

        return bean;
    }
}
