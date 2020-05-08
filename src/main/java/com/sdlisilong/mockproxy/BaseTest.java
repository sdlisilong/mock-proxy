package com.sdlisilong.mockproxy;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * @author sdlisilong
 * @date 2020/4/30
 */
public class BaseTest {

    @Rule
    public TestName testName= new TestName();

    @Before
    public void setup() {
        MockProxyContextUtil.getInstance().setMethodName(testName.getMethodName());
    }

    @After
    public void end() {
        MockProxyContextUtil.getInstance().destroy();
    }
}
