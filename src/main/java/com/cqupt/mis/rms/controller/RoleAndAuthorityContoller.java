package com.cqupt.mis.rms.controller;

import com.alibaba.fastjson.JSONObject;
import com.cqupt.mis.rms.dao.CQUPTRoleDao;
import com.cqupt.mis.rms.model.CQUPTRole;
import com.cqupt.mis.rms.model.UserAndRole;
import com.cqupt.mis.rms.service.CQUPTRoleService;
import com.cqupt.mis.rms.service.GrantService;
import com.cqupt.mis.rms.service.UserManagerService;
import com.cqupt.mis.rms.utils.JSONUtils;
import com.cqupt.mis.rms.utils.RequestConstant;
import com.cqupt.mis.rms.vo.ResultInfo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

/**
 * 角色权限相关的控制器
 * Created by Bern on 2016/5/4.
 */
@Controller
@RequestMapping("/roleandauth")
public class RoleAndAuthorityContoller {
    @Resource
    CQUPTRoleService cquptRoleServiceImpl;

    @Resource
    CQUPTRoleDao cquptRoleDao;

    @Resource
    GrantService grantServiceImpl;

    @Resource
    UserManagerService userManagerServiceImpl;

    /**
     * 获取所有的角色信息列表
     * @param response HttpServletResponse
     */
    @RequestMapping("/getrole")
    public void getRoleList(HttpServletResponse response) {
        List<CQUPTRole> roleList = cquptRoleServiceImpl.findAll();
        JSONUtils.toJSON(roleList, response);
    }

    /**
     * 添加角色信息
     * @param role 待添加的角色
     * @return 如果成功重定向到角色权限管理页面；否则定向到结果页面
     */
    @RequestMapping("/addrole")
    public ModelAndView addRole(CQUPTRole role) {
        ResultInfo<Object> result = cquptRoleServiceImpl.add(role);
        if(result.isResult()) {
            return new ModelAndView("redirect:/pages/system/roleandauthority.html");
        }else {
            return new ModelAndView("result.jsp", RequestConstant.RESULT, result);
        }
    }

    /**
     * 角色名字重名检测
     * @param roleName 待检测的角色名字
     * @param response HttpServletResponse response
     */
    @RequestMapping("/checkrolename")
    public void checkRoleName(@RequestParam(value="roleName", required=true)String roleName, HttpServletResponse response) {
        CQUPTRole role = cquptRoleDao.findByName(roleName);
        if(role == null) {
            JSONUtils.toJSON(true, response);
        }else {
            JSONUtils.toJSON(false, response);
        }
    }

    /**
     * 删除角色信息
     * @param roleId    角色id
     * @return 如果成功重定向到角色权限管理页面， 如果失败定向到结果页面
     */
    @RequestMapping("/deleterole/{roleId}")
    public ModelAndView deleteRole(@PathVariable("roleId")int roleId) {
        ResultInfo<Object> result = cquptRoleServiceImpl.deleteByRoleId(roleId);
        if(result.isResult()) {
            return new ModelAndView("redirect:/pages/system/roleandauthority.html");
        }else {
            return new ModelAndView("result.jsp", RequestConstant.RESULT, result);
        }
    }

    /**
     * 修改角色信息
     * @param role 角色信息
     * @return 如果成功重定向到角色权限管理页面，如果失败定向到结果页面
     */
    @RequestMapping("/modifyrole")
    public ModelAndView modifyRole(CQUPTRole role) {
        ResultInfo<Object> result = cquptRoleServiceImpl.modifyRole(role);
        if(result.isResult()) {
            return new ModelAndView("redirect:/pages/system/roleandauthority.html");
        }else {
            return new ModelAndView("result.jsp", RequestConstant.RESULT, result);
        }
    }

    /**
     * 获取全部资源的列表 以及 角色权限列表
     * @param roleId 待查询的角色Id
     * @param response HttpServletResponse
     */
    @RequestMapping("/authority/{roleId}")
    public void getAuthority(@PathVariable("roleId")int roleId, HttpServletResponse response) {
        Map resultMap = grantServiceImpl.getAuthority(roleId);
        JSONUtils.toJSON(resultMap, response);
    }

    /**
     * 查询待授权的角色信息 并 定向到授权页面
     * @param roleId 待授权的角色id
     * @return 如果成功定向到授权页面，如果失败定向到结果页面
     */
    @RequestMapping("/togrant/{roleId}")
    public ModelAndView toGrant(@PathVariable("roleId")int roleId) {
        CQUPTRole role = cquptRoleDao.selectByPrimaryKey(roleId);
        if(role == null) {
            return new ModelAndView("result.jsp", RequestConstant.RESULT, new ResultInfo<Object>(false, "查询角色信息失败！"));
        }else {
            return new ModelAndView("pages/system/grant.jsp", RequestConstant.ROLE, role);
        }
    }

    /**
     * 改变某个角色的权限信息
     * @param changedAuthJson 改变权限的json
     * @return 定向到结果页面
     */
    @RequestMapping(value="/grant", method=RequestMethod.POST)
    public ModelAndView grant(@RequestParam("json")String changedAuthJson) {
        JSONObject jsonObject = JSONUtils.parseObject(changedAuthJson);
        ResultInfo<Object> result = grantServiceImpl.grant(jsonObject);
        //TODO：权限修改成功更新权限检查列表
        return new ModelAndView("result.jsp", RequestConstant.RESULT, result);
    }

    /**
     * 查找所有的用户及其角色信息
     * @param response @see HttpServletResponse
     */
    @RequestMapping("/finduserrole")
    public void findAllUserRoleInfo(HttpServletResponse response) {
        List<UserAndRole> userAndRoleList = cquptRoleServiceImpl.findAllUserAndRole();
        JSONUtils.toJSON(userAndRoleList, response);
    }

    /**
     * 为用户分配角色
     * @param roleIdArray 角色id数组
     * @param userId 用户id
     * @return 重定向到用户角色管理界面
     */
    @RequestMapping("/assignrole")
    public ModelAndView assignRole(@RequestParam(value="roleId", required=false)int[] roleIdArray, @RequestParam("userId")String userId) {
        if(roleIdArray == null) {
            roleIdArray = new int[0];
        }
        grantServiceImpl.assignRole(userId, roleIdArray);
        return new ModelAndView("redirect:/pages/system/manageuserrole.html");
    }

    /**
     * 重置密码
     * @param userId 用户Id
     * @return 定向到结果页面
     */
    @RequestMapping("/resetpw/{uId}")
    public ModelAndView resetPW(@PathVariable("uId")String userId) {
        userManagerServiceImpl.resetPW(userId);
        return new ModelAndView("result.jsp", RequestConstant.RESULT, new ResultInfo<Object>(null, true));
    }
}
