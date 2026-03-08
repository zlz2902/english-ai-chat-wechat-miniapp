package com.horzits.framework.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.horzits.common.constant.CacheConstants;
import com.horzits.common.constant.Constants;
import com.horzits.common.constant.UserConstants;
import com.horzits.common.core.domain.entity.SysUser;
import com.horzits.common.core.domain.entity.SysRole;
import com.horzits.common.core.domain.model.RegisterBody;
import com.horzits.common.core.redis.RedisCache;
import com.horzits.common.exception.user.CaptchaException;
import com.horzits.common.exception.user.CaptchaExpireException;
import com.horzits.common.utils.MessageUtils;
import com.horzits.common.utils.SecurityUtils;
import com.horzits.common.utils.StringUtils;
import com.horzits.framework.manager.AsyncManager;
import com.horzits.framework.manager.factory.AsyncFactory;
import com.horzits.system.service.ISysConfigService;
import com.horzits.system.service.ISysUserService;
import com.horzits.system.service.ISysRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 注册校验方法
 * 
 * @author ruoyi
 */
@Component
public class SysRegisterService {
    private static final Logger log = LoggerFactory.getLogger(SysRegisterService.class);

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ISysRoleService roleService;

    /**
     * 注册
     */
    public String register(RegisterBody registerBody) {
        String msg = "", username = registerBody.getUsername(), password = registerBody.getPassword();
        String idCard = registerBody.getIdCard();
        log.info("Starting registration for user: {}, roleKey: {}", username, registerBody.getRoleKey());

        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);

        // 验证码开关
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled) {
            try {
                validateCaptcha(username, registerBody.getCode(), registerBody.getUuid());
            } catch (Exception e) {
                log.error("Captcha validation failed for user: {}", username, e);
                throw e;
            }
        }

        if (StringUtils.isEmpty(username)) {
            msg = "用户名不能为空";
        } else if (StringUtils.isEmpty(password)) {
            msg = "用户密码不能为空";
        } else if (StringUtils.isEmpty(idCard)) {
            msg = "身份证号码不能为空";
        } else if (!isValidIdCard(idCard)) {
            msg = "身份证号码格式不正确";
        } else if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            msg = "账户长度必须在2到20个字符之间";
        } else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            msg = "密码长度必须在5到20个字符之间";
        } else if (!userService.checkUserNameUnique(sysUser)) {
            msg = "保存用户'" + username + "'失败，注册账号已存在";
        } else {
            sysUser.setNickName(username);
            sysUser.setPassword(SecurityUtils.encryptPassword(password));
            String masked = maskIdCard(idCard);
            String hash = sha256Hex(idCard.trim().toUpperCase());
            sysUser.setIdCardMasked(masked);
            sysUser.setIdCardHash(hash);
            boolean regFlag = userService.registerUser(sysUser);
            if (!regFlag) {
                msg = "注册失败,请联系系统管理人员";
                log.error("userService.registerUser failed for user: {}", username);
            } else {
                log.info("User {} registered successfully. UserId: {}", username, sysUser.getUserId());
                String roleKey = registerBody.getRoleKey();
                if (StringUtils.isNotEmpty(roleKey)) {
                    Long roleId = null;
                    for (SysRole r : roleService.selectRoleAll()) {
                        if (roleKey.equals(r.getRoleKey())) {
                            roleId = r.getRoleId();
                            break;
                        }
                    }
                    if (roleId != null) {
                        log.info("Assigning role {} (ID: {}) to user {}", roleKey, roleId, username);
                        if (sysUser.getUserId() != null) {
                            userService.insertUserAuth(sysUser.getUserId(), new Long[] { roleId });
                        } else {
                            log.error("UserId is null after registration for user: {}", username);
                        }
                    } else {
                        log.warn("Role key {} not found for user {}", roleKey, username);
                    }
                }
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.REGISTER,
                        MessageUtils.message("user.register.success")));
            }
        }
        if (StringUtils.isNotEmpty(msg)) {
            log.warn("Registration failed for user {}: {}", username, msg);
        }
        return msg;
    }

    private boolean isValidIdCard(String idCard) {
        if (StringUtils.isEmpty(idCard))
            return false;
        String v = idCard.trim().toUpperCase();
        if (v.length() == 18) {
            if (!v.matches("^[0-9]{17}[0-9X]$"))
                return false;
            // 校验码计算
            int[] weights = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };
            char[] validate = { '1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2' };
            int sum = 0;
            for (int i = 0; i < 17; i++) {
                sum += (v.charAt(i) - '0') * weights[i];
            }
            char code = validate[sum % 11];
            return v.charAt(17) == code;
        } else if (v.length() == 15) {
            return v.matches("^[0-9]{15}$");
        }
        return false;
    }

    private String maskIdCard(String id) {
        if (StringUtils.isEmpty(id))
            return "";
        String v = id.trim().toUpperCase();
        int len = v.length();
        if (len <= 4)
            return v;
        String last4 = v.substring(len - 4);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len - 4; i++)
            sb.append('*');
        sb.append(last4);
        return sb.toString();
    }

    private String sha256Hex(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1)
                    hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 not available", e);
            return "";
        }
    }

    /**
     * 校验验证码
     * 
     * @param username 用户名
     * @param code     验证码
     * @param uuid     唯一标识
     * @return 结果
     */
    public void validateCaptcha(String username, String code, String uuid) {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        if (captcha == null) {
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha)) {
            throw new CaptchaException();
        }
    }
}
