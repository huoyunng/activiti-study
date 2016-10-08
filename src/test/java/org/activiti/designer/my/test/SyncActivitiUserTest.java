package org.activiti.designer.my.test;

import java.util.List;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.entity.UserImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*:applicationContext-activiti.xml")
public class SyncActivitiUserTest {

	@Autowired
	private IdentityService identityService;

	@Autowired
	private RuntimeService runtimeService;
	
	@Autowired
	private HistoryService historyService;
	
	@Rule
	@Autowired
	public ActivitiRule activitiRule;
	
	@Test
	public void testSaveUser() {
		User user = new UserImpl("xuwei", "wei", "xu", "abc@123.com", "xuwei");
		saveUser(user);
	}
	
	@Test
	public void testDeleteUser(){
		String userId = "2";
		identityService.deleteUser(userId);
		System.out.println("delete successfully...");
	}

	/**
	 * 保存用户信息 并且同步用户信息到activiti的identity.User，同时设置角色
	 * 
	 * @param user
	 * @param roleIds
	 */
	public void saveUser(User user) {
		String userId = user.getId().toString();
		List<User> activitiUsers = identityService.createUserQuery().userId(userId).list();
		if (activitiUsers.size() == 1) {
			// 更新信息
			org.activiti.engine.identity.User activitiUser = activitiUsers.get(0);
			activitiUser.setFirstName(user.getFirstName());
			activitiUser.setLastName(user.getLastName());
			activitiUser.setPassword(user.getPassword());
			activitiUser.setEmail(user.getEmail());
			identityService.saveUser(activitiUser);

			// 删除用户的membership
			List<Group> activitiGroups = identityService.createGroupQuery().groupMember(userId).list();
			for (Group group : activitiGroups) {
				identityService.deleteMembership(userId, group.getId());
			}

			// 添加membership
			// for (Long roleId : roleIds) {
			// Role role = roleManager.getEntity(roleId);
			// identityService.createMembership(userId, role.getEnName());
			// }

		} else {
			org.activiti.engine.identity.User newUser = identityService.newUser(userId);
			newUser.setFirstName(user.getFirstName());
			newUser.setLastName(user.getLastName());
			newUser.setPassword(user.getPassword());
			newUser.setEmail(user.getEmail());
			identityService.saveUser(newUser);

			// 添加membership
			// for (Long roleId : roleIds) {
			// Role role = roleManager.getEntity(roleId);
			// identityService.createMembership(userId, role.getEnName());
			// }
		}
		System.out.println("save successfully...");
	}

	@Test
	public void testCreateUnFinishedProcessInstanceQuery(){
		ProcessInstanceQuery unfinishedQuery = createUnFinishedProcessInstanceQuery("testProcess");
		long count = unfinishedQuery.count();
		System.out.println(count);
	}
	/**
	 * 获取未经完成的流程实例查询对象
	 * @param userId 用户ID
	 */
	@Transactional(readOnly = true)
	public ProcessInstanceQuery createUnFinishedProcessInstanceQuery(String userId) {
		ProcessInstanceQuery unfinishedQuery = runtimeService.createProcessInstanceQuery()
				.processDefinitionKey(userId)
				.active();
		return unfinishedQuery;
	}
	
	/**
	  获取已经完成的流程实例查询对象
	 @param userId    用户ID
	 从表ACT_HI_PROCINST中查询数据
	 */
	@Transactional(readOnly = true)
	public HistoricProcessInstanceQuery createFinishedProcessInstanceQuery(String userId) {

	    HistoricProcessInstanceQuery finishedQuery = historyService.createHistoricProcessInstanceQuery()
	    												.processDefinitionKey(userId).finished();

	    return finishedQuery;
	}
	
	/**
	 * 面试流程测试
	 */
	@Test
	public void processTests(){
	    // 加载配置文件
	    ProcessEngine processEngine = ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("activiti.cfg.xml").buildProcessEngine();
	    RepositoryService repositoryService = processEngine.getRepositoryService();
	    RuntimeService runtimeService = processEngine.getRuntimeService();
	    repositoryService.createDeployment().addClasspathResource("diagrams/mytest/Interview.bpmn").deploy();
	    String processId = runtimeService.startProcessInstanceByKey("Interview").getId();

	    TaskService taskService = processEngine.getTaskService();
	    
	    // 加载配置文件
	    /*RepositoryService repositoryService = activitiRule.getRepositoryService();
	    RuntimeService runtimeService = processEngine.getRuntimeService();
	    repositoryService.createDeployment().addClasspathResource("diagrams/mytest/Interview.bpmn").deploy();
	    String processId = runtimeService.startProcessInstanceByKey("Interview").getId();

	    TaskService taskService = processEngine.getTaskService();*/
	    
	    //得到笔试的流程
	    System.out.println("\n***************笔试流程开始***************");

	    List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("人力资源部").list();
	    for (Task task : tasks) {
	        System.out.println("人力资源部的任务：name:"+task.getName()+",id:"+task.getId());
	        taskService.claim(task.getId(), "张三");
	    }

	    System.out.println("张三的任务数量："+taskService.createTaskQuery().taskAssignee("张三").count());
	    tasks = taskService.createTaskQuery().taskAssignee("张三").list();
	    for (Task task : tasks) {
	        System.out.println("张三的任务：name:"+task.getName()+",id:"+task.getId());
	        taskService.complete(task.getId());
	    }

	    System.out.println("张三的任务数量："+taskService.createTaskQuery().taskAssignee("张三").count());
	    System.out.println("***************笔试流程结束***************");

	    System.out.println("\n***************一面流程开始***************");
	    tasks = taskService.createTaskQuery().taskCandidateGroup("技术部").list();
	    for (Task task : tasks) {
	        System.out.println("技术部的任务：name:"+task.getName()+",id:"+task.getId());
	        taskService.claim(task.getId(), "李四");
	    }

	    System.out.println("李四的任务数量："+taskService.createTaskQuery().taskAssignee("李四").count());
	    for (Task task : tasks) {
	        System.out.println("李四的任务：name:"+task.getName()+",id:"+task.getId());
	        taskService.complete(task.getId());
	    }

	    System.out.println("李四的任务数量："+taskService.createTaskQuery().taskAssignee("李四").count());
	    System.out.println("***************一面流程结束***************");

	    System.out.println("\n***************二面流程开始***************");
	    tasks = taskService.createTaskQuery().taskCandidateGroup("技术部").list();
	    for (Task task : tasks) {
	        System.out.println("技术部的任务：name:"+task.getName()+",id:"+task.getId());
	        taskService.claim(task.getId(), "李四");
	    }

	    System.out.println("李四的任务数量："+taskService.createTaskQuery().taskAssignee("李四").count());
	    for (Task task : tasks) {
	        System.out.println("李四的任务：name:"+task.getName()+",id:"+task.getId());
	        taskService.complete(task.getId());
	    }

	    System.out.println("李四的任务数量："+taskService.createTaskQuery().taskAssignee("李四").count());
	    System.out.println("***************二面流程结束***************");

	    System.out.println("***************HR面流程开始***************");
	    tasks = taskService.createTaskQuery().taskCandidateGroup("人力资源部").list();
	    for (Task task : tasks) {
	        System.out.println("技术部的任务：name:"+task.getName()+",id:"+task.getId());
	        taskService.claim(task.getId(), "李四");
	    }

	    System.out.println("李四的任务数量："+taskService.createTaskQuery().taskAssignee("李四").count());
	    for (Task task : tasks) {
	        System.out.println("李四的任务：name:"+task.getName()+",id:"+task.getId());
	        taskService.complete(task.getId());
	    }

	    System.out.println("李四的任务数量："+taskService.createTaskQuery().taskAssignee("李四").count());
	    System.out.println("***************HR面流程结束***************");

	    System.out.println("\n***************录用流程开始***************");
	    tasks = taskService.createTaskQuery().taskCandidateGroup("人力资源部").list();
	    for (Task task : tasks) {
	        System.out.println("技术部的任务：name:"+task.getName()+",id:"+task.getId());
	        taskService.claim(task.getId(), "李四");
	    }

	    System.out.println("李四的任务数量："+taskService.createTaskQuery().taskAssignee("李四").count());
	    for (Task task : tasks) {
	        System.out.println("李四的任务：name:"+task.getName()+",id:"+task.getId());
	        taskService.complete(task.getId());
	    }

	    System.out.println("李四的任务数量："+taskService.createTaskQuery().taskAssignee("李四").count());
	    System.out.println("***************录用流程结束***************");

	    HistoryService historyService = processEngine.getHistoryService();
	    HistoricProcessInstance historicProcessInstance = historyService
	            .createHistoricProcessInstanceQuery()
	            .processInstanceId(processId).singleResult();
	    System.out.println("\n流程结束时间："+historicProcessInstance.getEndTime());
	}
}
