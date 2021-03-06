package com.fyp.layim.web.biz;

import com.fyp.layim.common.event.ApplyEvent;
import com.fyp.layim.domain.result.JsonResult;
import com.fyp.layim.im.LayimWebsocketStarter;
import com.fyp.layim.im.common.util.SpringUtil;
import com.fyp.layim.im.packet.ContextUser;
import com.fyp.layim.service.ApplyService;
import com.fyp.layim.service.GroupService;
import com.fyp.layim.service.UserService;
import com.fyp.layim.service.auth.ShiroUtil;
import com.fyp.layim.web.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author fyp
 * @crate 2017/11/2 22:50
 * @project SpringBootLayIM
 */
@RestController
@RequestMapping("/layim")
public class UserController extends BaseController {

    /**
     * 用于事件发布
     * applicationContext.publishEvent(event)
     * */
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ApplyService applyService;

    /**
     * layim基础初始化数据
     * 好友信息，群组信息，个人信息
     * */
    @GetMapping(value = "/base")
    public JsonResult getBaseData(){
        return userService.getBaseList();
    }

    /**
     * 根据群ID获取群内的所有人
     * */
    @GetMapping(value="/members")
    public JsonResult getMembers( long id){
        return groupService.getGroupMembers(id);
    }

    /**
     * 获取用户token，调用api会用到token
     * */
    @GetMapping(value = "/token")
    public JsonResult getToken() throws Exception{
        return userService.getUserToken();
    }

    /**
     * 好友申请
     * */
    @PostMapping(value = "/apply-friend")
    public JsonResult apply(@RequestParam("toid") Long toId,@RequestParam("remark") String remark){
        boolean isFriend = groupService.isFriend(getUserId(),toId);
        if(isFriend){
            return JsonResult.fail("已经是好友了");
        }
        JsonResult result = applyService.saveFriendApply(toId, remark);
        //申请成功，发布申请事件，通知 toId处理消息，如果不在线，不会进行处理
        if(result.isSuccess()){
            applicationContext.publishEvent(new ApplyEvent("apply",toId));
        }
        return result;
    }

    /**
     * 用户收到的通知分页
     * */
    @GetMapping(value = "/notice/{pageIndex}")
    public JsonResult apply(@PathVariable("pageIndex") int pageIndex){
        return applyService.getSystemNotices(pageIndex,20,getUserId());
    }
}
