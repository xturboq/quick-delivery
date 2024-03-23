package com.zecola.cleme.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zecola.cleme.common.R;
import com.zecola.cleme.pojo.User;
import com.zecola.cleme.service.UserService;
import com.zecola.cleme.utils.MailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private MailUtils mailUtils;


    /**
     * 发送验证码
     *
     * @param user
     * @param session
     * @return
     * @throws MessagingException
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) throws MessagingException {
        String phone = user.getPhone();
        if (!phone.isEmpty()) {
            //随机生成一个验证码
            String code = MailUtils.achieveCode();
            log.info(code);
            //这里的phone其实就是邮箱，code是我们生成的验证码

            mailUtils.sendTestMail(phone, code);
            //验证码存session，方便后面拿出来比对
            session.setAttribute(phone, code);
            return R.success("验证码发送成功");
        }
        return R.error("验证码发送失败");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        log.info(map.toString());
        String phone = (String) map.get("phone");
        String code = (String) map.get("code");
        String codeInSession = (String) session.getAttribute(phone);

        //比较用户输入的密码和session中存的验证码是否一致
        if (code != null && code.equals(codeInSession)) {
            //如果验证码正确，判断用户是否已经存在
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);

            //如果用户不存在，就创建一个用户
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                userService.save(user);
                user.setName("用户" + codeInSession);
            }
            //存个session，表示登录状态
            session.setAttribute("user", user.getId());
            //并将其作为结果返回
            return R.success(user);
        }

        return R.error("验证码错误,登录失败！");
    }
}
