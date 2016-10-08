package org.activiti.designer.test;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*:applicationContext-activiti.xml")
public class ActivitiTest {
    
    @Autowired
    private RuntimeService runtimeService;
    
    @Rule
    @Autowired
    public ActivitiRule activitiRule;

    int flag = 0;
    
    @Test
    public void test() {
        runtimeService.startProcessInstanceByKey("testProcess");
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                for (int i = 0; i < 1; i++) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread().getName());
                    System.out.println("trigger event test01");
                    runtimeService.signalEventReceived("test01");
//                    runtimeService.signalEventReceivedAsync("test01");
                    flag += 1;
                }
                
            }
        }).start();
        
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                for (int i = 0; i < 1; i++) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread().getName());
                    System.out.println("trigger event test02");
                    runtimeService.signalEventReceived("test02");
//                    runtimeService.signalEventReceivedAsync("test02");
                }
            }
        }).start();
        
        boolean loop = true;
        while (loop) {
            try {
                Thread.sleep(1000);
                System.out.println("sleep");
                if (flag > 0){
                	loop = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
